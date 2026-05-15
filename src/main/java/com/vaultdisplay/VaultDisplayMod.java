package com.vaultdisplay;

import com.vaultdisplay.registry.ModBlocks;
import com.vaultdisplay.registry.ModBlockEntities;
import com.vaultdisplay.registry.ModCreativeTabs;
import com.vaultdisplay.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(VaultDisplayMod.MOD_ID)
public class VaultDisplayMod {

    public static final String MOD_ID = "vaultdisplay";

    public VaultDisplayMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}
