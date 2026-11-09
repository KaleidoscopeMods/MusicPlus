package com.github.kaleidoscopemods.musicplus.resources.data;

import com.github.kaleidoscopemods.musicplus.client.audio.MidiInstrument;
import net.minecraft.resources.ResourceLocation;

public record CustomItem(int format, MidiInstrument instrument, ResourceLocation visual) {
}
