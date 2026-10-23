package com.github.kaleidoscopemods.musicplus.client.resources;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.InputStream;

public class ModelReloadListener implements ResourceManagerReloadListener {
    public static Model DFAULT_HANDHELD_INSTRUMENT_MODEL;
    public static ResourceLocation DFAULT_HANDHELD_INSTRUMENT_TEXTURE = new ResourceLocation(MusicPlus.MOD_ID, "custom_instrument/textures/ce_guitar.png");

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        if (DFAULT_HANDHELD_INSTRUMENT_MODEL == null) {
            readDefaultModel(manager);
        }
    }

    private static void readDefaultModel(ResourceManager manager) {
        manager.getResource(new ResourceLocation(MusicPlus.MOD_ID, "custom_instrument/models/ce_guitar.json")).ifPresent(res -> {
            try (InputStream stream = res.open()) {
                DFAULT_HANDHELD_INSTRUMENT_MODEL = new BedrockModel(stream);
            } catch (Exception e) {
                MusicPlus.LOGGER.error("Failed to load default custom instrument model", e);
            }
        });
    }
}
