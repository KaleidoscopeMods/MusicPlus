package com.github.kaleidoscopemods.musicplus.client.event;

import com.github.kaleidoscopemods.musicplus.client.render.layer.HandheldInstrumentLayer;
import com.github.kaleidoscopemods.musicplus.client.resources.ItemReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.ModelReloadListener;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetupEvent {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent evt) {
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new HandheldInstrumentLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ModelReloadListener());
        event.registerReloadListener(new ItemReloadListener());
    }
}
