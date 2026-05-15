package com.vaultdisplay;

import com.vaultdisplay.registry.ModBlockEntities;
import com.vaultdisplay.renderer.FluidPanelRenderer;
import com.vaultdisplay.renderer.StoragePanelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = VaultDisplayMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.STORAGE_PANEL.get(),
                StoragePanelRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.FLUID_PANEL.get(),
                FluidPanelRenderer::new
        );
    }
}
