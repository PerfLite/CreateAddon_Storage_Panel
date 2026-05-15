package com.vaultdisplay.registry;

import com.vaultdisplay.VaultDisplayMod;
import com.vaultdisplay.block.FluidPanelBlock;
import com.vaultdisplay.block.StoragePanelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(VaultDisplayMod.MOD_ID);

    public static final DeferredBlock<StoragePanelBlock> STORAGE_PANEL =
            BLOCKS.register("storage_panel", () -> new StoragePanelBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f, 6.0f)  // destroyTime=0.5 — ломается быстро
                            .noOcclusion()
            ));

    public static final DeferredBlock<FluidPanelBlock> FLUID_PANEL =
            BLOCKS.register("fluid_panel", () -> new FluidPanelBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f, 6.0f)
                            .noOcclusion()
            ));
}
