package com.vaultdisplay.registry;

import com.vaultdisplay.VaultDisplayMod;
import com.vaultdisplay.blockentity.FluidPanelBlockEntity;
import com.vaultdisplay.blockentity.StoragePanelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VaultDisplayMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoragePanelBlockEntity>> STORAGE_PANEL =
            BLOCK_ENTITIES.register("storage_panel", () ->
                    BlockEntityType.Builder.of(StoragePanelBlockEntity::new,
                            ModBlocks.STORAGE_PANEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPanelBlockEntity>> FLUID_PANEL =
            BLOCK_ENTITIES.register("fluid_panel", () ->
                    BlockEntityType.Builder.of(FluidPanelBlockEntity::new,
                            ModBlocks.FLUID_PANEL.get()).build(null));
}
