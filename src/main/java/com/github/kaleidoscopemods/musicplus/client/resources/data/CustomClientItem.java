package com.github.kaleidoscopemods.musicplus.client.resources.data;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record CustomClientItem(
        @SerializedName("model") ResourceLocation model,
        @SerializedName("texture") ResourceLocation texture,
        @SerializedName("gui") ResourceLocation gui,
        @SerializedName("animation") ResourceLocation animation,
        @Nullable @SerializedName("transforms") ItemTransforms transforms
) {
    public static final CustomClientItem DEFAULT = new CustomClientItem(
            new ResourceLocation(MusicPlus.MOD_ID, "custom_instrument/models/ce_guitar.json"),
            new ResourceLocation(MusicPlus.MOD_ID, "custom_instrument/textures/ce_guitar.png"),
            new ResourceLocation(MusicPlus.MOD_ID, "custom_instrument/textures/gui/ce_guitar.png"),
            new ResourceLocation(MusicPlus.MOD_ID, "ce_guitar"),
            null
    );
}
