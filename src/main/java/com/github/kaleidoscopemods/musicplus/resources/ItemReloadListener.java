package com.github.kaleidoscopemods.musicplus.resources;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.resources.data.CustomItem;
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

public class ItemReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, CustomItem>> {
    public static final String ITEM_FOLDER = MusicPlus.FOLDER_NAME + "/items";
    public static final Map<ResourceLocation, CustomItem> INSTANCES = Maps.newHashMap();

    @Override
    protected Map<ResourceLocation, CustomItem> prepare(ResourceManager manager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, CustomItem> maps = Maps.newHashMap();
        manager.listResources(ITEM_FOLDER, p -> p.getPath().endsWith(".json"))
                .forEach((path, res) -> {
                    try (Reader reader = res.openAsReader()) {
                        CustomItem customItem = GSON.fromJson(reader, CustomItem.class);
                        maps.put(path, customItem);
                    } catch (IOException | JsonParseException e) {
                        MusicPlus.LOGGER.warn("Failed to load resource {}", path, e);
                    }
                });
        return maps;
    }

    @Override
    protected void apply(Map<ResourceLocation, CustomItem> data, ResourceManager manager, ProfilerFiller profilerFiller) {
        INSTANCES.clear();
        INSTANCES.putAll(data);
    }
}
