package com.vaultdisplay.registry;

import com.vaultdisplay.VaultDisplayMod;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(VaultDisplayMod.MOD_ID);

    public static final DeferredItem<BlockItem> STORAGE_PANEL =
            ITEMS.registerSimpleBlockItem("storage_panel", ModBlocks.STORAGE_PANEL);

    public static final DeferredItem<BlockItem> FLUID_PANEL =
            ITEMS.registerSimpleBlockItem("fluid_panel", ModBlocks.FLUID_PANEL);
}
