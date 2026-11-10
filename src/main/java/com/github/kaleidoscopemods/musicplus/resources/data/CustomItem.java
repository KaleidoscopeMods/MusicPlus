package com.github.kaleidoscopemods.musicplus.resources.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public record CustomItem(
        @SerializedName("format") int format,
        @SerializedName("midi_instrument") MidiInstrument instrument,
        @SerializedName("visual") ResourceLocation visual
) {
}
