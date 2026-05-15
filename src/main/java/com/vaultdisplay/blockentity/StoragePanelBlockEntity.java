package com.vaultdisplay.blockentity;

import com.vaultdisplay.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class StoragePanelBlockEntity extends BlockEntity {


    private static final ResourceLocation ITEM_VAULT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "item_vault");
    private static final ResourceLocation ITEM_SILO_ID =
            ResourceLocation.fromNamespaceAndPath("create_connected", "item_silo");

    private int currentItems = 0;
    private int maxItems     = 0;
    private boolean vaultFound = false;

    private static final int TICK_INTERVAL = 20;
    private int tickCounter = 0;

    public StoragePanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE_PANEL.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level level,
                                   BlockPos pos,
                                   BlockState state,
                                   StoragePanelBlockEntity be) {
        be.tickCounter++;
        if (be.tickCounter >= TICK_INTERVAL) {
            be.tickCounter = 0;
            be.scanForVault();
        }
    }

    private void scanForVault() {
        if (level == null) return;

        int found = 0;
        int max   = 0;
        boolean connected = false;

        // Считываем со стороны противоположной лицу панели
        BlockState panelState = level.getBlockState(worldPosition);
        com.vaultdisplay.block.StoragePanelBlock panelBlock =
                (com.vaultdisplay.block.StoragePanelBlock) panelState.getBlock();

        Direction facing  = panelState.getValue(com.vaultdisplay.block.StoragePanelBlock.FACING);
        net.minecraft.world.level.block.state.properties.AttachFace face =
                panelState.getValue(com.vaultdisplay.block.StoragePanelBlock.FACE);

        Direction backDir = switch (face) {
            case FLOOR   -> Direction.DOWN;
            case CEILING -> Direction.UP;
            default      -> facing.getOpposite();
        };

        BlockPos checkPos = worldPosition.relative(backDir);
        ResourceLocation blockId = level.getBlockState(checkPos)
                .getBlock()
                .builtInRegistryHolder()
                .key()
                .location();

        if (ITEM_VAULT_ID.equals(blockId) || ITEM_SILO_ID.equals(blockId)) {
            IItemHandler handler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, checkPos, facing);
            if (handler != null) {
                int slots = handler.getSlots();
                for (int slot = 0; slot < slots; slot++) {
                    found += handler.getStackInSlot(slot).getCount();
                }
                max = slots * 64;
                connected = true;
            }
        }

        boolean changed = (found != currentItems) || (max != maxItems) || (connected != vaultFound);
        currentItems = found;
        maxItems     = max;
        vaultFound   = connected;

        if (changed) {
            setChanged();
            // Отправляем обновление клиенту
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // ── Геттеры для рендерера ──────────────────────────────────────────────

    public int getCurrentItems()  { return currentItems; }
    public int getMaxItems()      { return maxItems; }
    public boolean isVaultFound() { return vaultFound; }

    public float getFillPercent() {
        if (maxItems <= 0) return 0f;
        return (float) currentItems / maxItems;
    }

    // ── Синхронизация с клиентом ──────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CurrentItems", currentItems);
        tag.putInt("MaxItems", maxItems);
        tag.putBoolean("VaultFound", vaultFound);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
                              HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            currentItems = tag.getInt("CurrentItems");
            maxItems     = tag.getInt("MaxItems");
            vaultFound   = tag.getBoolean("VaultFound");
        }
    }

    // ── NBT сохранение/загрузка ───────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CurrentItems", currentItems);
        tag.putInt("MaxItems", maxItems);
        tag.putBoolean("VaultFound", vaultFound);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        currentItems = tag.getInt("CurrentItems");
        maxItems     = tag.getInt("MaxItems");
        vaultFound   = tag.getBoolean("VaultFound");
    }
}
