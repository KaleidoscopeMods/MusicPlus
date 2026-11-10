package com.github.kaleidoscopemods.musicplus.client.resources.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;

public record CustomClientItem(
        @SerializedName("model") ResourceLocation model,
        @SerializedName("texture") ResourceLocation texture,
        @SerializedName("animation") ResourceLocation animation,
        @SerializedName("transform") ItemTransforms transform
) {
}
