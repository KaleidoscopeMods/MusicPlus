package com.github.kaleidoscopemods.musicplus.client.network;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.audio.MidiSound;
import com.github.kaleidoscopemods.musicplus.network.packet.PlayMidiPacket;
import com.github.kaleidoscopemods.musicplus.resources.data.MidiInstrument;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.loading.FMLPaths;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientMidiPlayback {
    private static final String TEST_MIDI_FILE = "钟.mid";
    private static final Int2ObjectMap<MidiSound> ACTIVE_SOUNDS = new Int2ObjectOpenHashMap<>();

    public static void play(PlayMidiPacket packet) {
        int performerId = packet.performerId();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(performerId);
        if (!(entity instanceof LivingEntity performer)) {
            return;
        }

        Path midiPath = FMLPaths.CONFIGDIR.get().resolve(TEST_MIDI_FILE);
        if (!Files.isRegularFile(midiPath)) {
            return;
        }

        try {
            MidiSound sound = new MidiSound(midiPath.toUri().toURL(), performer, MidiInstrument.BASS);
            MidiSound previous = ACTIVE_SOUNDS.put(performerId, sound);
            if (previous != null) {
                previous.forceStop();
            }
            minecraft.getSoundManager().play(sound);
        } catch (MalformedURLException exception) {
            MusicPlus.LOGGER.error("Invalid MIDI file path: {}", midiPath, exception);
        }
    }
}
