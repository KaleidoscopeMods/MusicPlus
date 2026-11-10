package com.github.kaleidoscopemods.musicplus.client.resources;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModel;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ModelReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, BedrockModel>> {
    public static final String MODEL_FOLDER = MusicPlus.FOLDER_NAME + "/models";
    public static final Map<ResourceLocation, BedrockModel> INSTANCES = Maps.newHashMap();

    public static BedrockModel DFAULT_HANDHELD_INSTRUMENT_MODEL = new BedrockModel();
    public static ResourceLocation DFAULT_HANDHELD_INSTRUMENT_TEXTURE = new ResourceLocation(MusicPlus.MOD_ID, "custom_instrument/textures/ce_guitar.png");

    @Override
    protected Map<ResourceLocation, BedrockModel> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, BedrockModel> models = Maps.newHashMap();
        manager.listResources(MODEL_FOLDER, path -> path.getPath().endsWith(".json"))
                .forEach((path, resource) -> {
                    try (InputStream stream = resource.open()) {
                        models.put(path, new BedrockModel(stream));
                    } catch (IOException | JsonParseException | InvalidVersionSpecificationException e) {
                        MusicPlus.LOGGER.warn("Failed to load model resource {}", path, e);
                    }
                });
        return models;
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModel> map, ResourceManager manager, ProfilerFiller profiler) {
        INSTANCES.clear();
        INSTANCES.putAll(map);

        DFAULT_HANDHELD_INSTRUMENT_MODEL = INSTANCES.getOrDefault(
                new ResourceLocation(MusicPlus.MOD_ID, MODEL_FOLDER + "/ce_guitar.json"),
                new BedrockModel()
        );
    }
}
