package com.vaultdisplay.registry;

import com.vaultdisplay.VaultDisplayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VaultDisplayMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VAULT_DISPLAY_TAB =
            TABS.register("vault_display_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.vaultdisplay.tab"))
                    .icon(() -> ModItems.STORAGE_PANEL.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.STORAGE_PANEL.get());
                        output.accept(ModItems.FLUID_PANEL.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
