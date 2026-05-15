package com.vaultdisplay.blockentity;

import com.vaultdisplay.block.FluidPanelBlock;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FluidPanelBlockEntity extends BlockEntity {

    private static final ResourceLocation FLUID_TANK_ID =
            ResourceLocation.fromNamespaceAndPath("create", "fluid_tank");

    private int currentFluid = 0;
    private int maxFluid     = 0;
    private String fluidName = "";
    private boolean vaultFound = false;

    private static final int TICK_INTERVAL = 20;
    private int tickCounter = 0;

    public FluidPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PANEL.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level level,
                                   BlockPos pos,
                                   BlockState state,
                                   FluidPanelBlockEntity be) {
        be.tickCounter++;
        if (be.tickCounter >= TICK_INTERVAL) {
            be.tickCounter = 0;
            be.scanForTank();
        }
    }

    private void scanForTank() {
        if (level == null) return;

        int found    = 0;
        int max      = 0;
        String name  = "";
        boolean connected = false;

        // Считываем со стороны противоположной лицу панели
        BlockState panelState = level.getBlockState(worldPosition);
        Direction facing  = panelState.getValue(com.vaultdisplay.block.FluidPanelBlock.FACING);
        net.minecraft.world.level.block.state.properties.AttachFace face =
                panelState.getValue(com.vaultdisplay.block.FluidPanelBlock.FACE);

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

        if (FLUID_TANK_ID.equals(blockId)) {
            IFluidHandler handler = level.getCapability(
                    Capabilities.FluidHandler.BLOCK, checkPos, facing);
            if (handler != null) {
                int tanks = handler.getTanks();
                for (int i = 0; i < tanks; i++) {
                    FluidStack stack = handler.getFluidInTank(i);
                    found += stack.getAmount();
                    max   += handler.getTankCapacity(i);
                    if (name.isEmpty() && !stack.isEmpty()) {
                        name = stack.getHoverName().getString();
                    }
                }
                connected = true;
            }
        }

        boolean changed = (found != currentFluid)
                || (max != maxFluid)
                || !name.equals(fluidName)
                || (connected != vaultFound);

        currentFluid = found;
        maxFluid     = max;
        fluidName    = name;
        vaultFound   = connected;

        if (changed) {
            setChanged();
            if (!level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // ── Геттеры для рендерера ──────────────────────────────────────────────

    public int getCurrentFluid()  { return currentFluid; }
    public int getMaxFluid()      { return maxFluid; }
    public String getFluidName()  { return fluidName; }
    public boolean isVaultFound() { return vaultFound; }

    public float getFillPercent() {
        if (maxFluid <= 0) return 0f;
        return (float) currentFluid / maxFluid;
    }

    // ── Синхронизация с клиентом ──────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CurrentFluid", currentFluid);
        tag.putInt("MaxFluid", maxFluid);
        tag.putString("FluidName", fluidName);
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
            currentFluid = tag.getInt("CurrentFluid");
            maxFluid     = tag.getInt("MaxFluid");
            fluidName    = tag.getString("FluidName");
            vaultFound   = tag.getBoolean("VaultFound");
        }
    }

    // ── NBT сохранение/загрузка ───────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CurrentFluid", currentFluid);
        tag.putInt("MaxFluid", maxFluid);
        tag.putString("FluidName", fluidName);
        tag.putBoolean("VaultFound", vaultFound);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        currentFluid = tag.getInt("CurrentFluid");
        maxFluid     = tag.getInt("MaxFluid");
        fluidName    = tag.getString("FluidName");
        vaultFound   = tag.getBoolean("VaultFound");
    }
}
