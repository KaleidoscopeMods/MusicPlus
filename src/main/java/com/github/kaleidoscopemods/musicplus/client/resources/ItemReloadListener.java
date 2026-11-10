package com.github.kaleidoscopemods.musicplus.client.resources;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.resources.data.CustomClientItem;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import static com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModelUtil.GSON;

public class ItemReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, CustomClientItem>> {
    public static final String ITEM_FOLDER = MusicPlus.FOLDER_NAME + "/items";
    public static final Map<ResourceLocation, CustomClientItem> INSTANCES = Maps.newHashMap();

    @Override
    protected Map<ResourceLocation, CustomClientItem> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, CustomClientItem> items = Maps.newHashMap();
        manager.listResources(ITEM_FOLDER, path -> path.getPath().endsWith(".json"))
                .forEach((path, resource) -> {
                    try (Reader reader = resource.openAsReader()) {
                        items.put(path, GSON.fromJson(reader, CustomClientItem.class));
                    } catch (IOException | JsonParseException | IllegalStateException e) {
                        MusicPlus.LOGGER.warn("Failed to load client item resource {}", path, e);
                    }
                });
        return items;
    }

    @Override
    protected void apply(Map<ResourceLocation, CustomClientItem> data, ResourceManager manager, ProfilerFiller profiler) {
        INSTANCES.clear();
        INSTANCES.putAll(data);
    }
}
