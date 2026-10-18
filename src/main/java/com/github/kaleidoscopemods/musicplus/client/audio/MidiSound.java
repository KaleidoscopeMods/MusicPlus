package com.github.kaleidoscopemods.musicplus.client.audio;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.init.ModSound;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MidiSound extends AbstractTickableSoundInstance {
    private final URL midiUrl;
    private volatile MidiAudioStream midiStream;
    private long lastParticleSequence;

    public MidiSound(URL midiUrl, BlockPos pos) {
        super(ModSound.MIDI.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.midiUrl = midiUrl;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
    }

    @Override
    public void tick() {
        MidiAudioStream stream = this.midiStream;
        if (stream == null) {
            return;
        }

        MidiAudioStream.NoteState note = stream.getCurrentNote();
        if (note == null || note.sequence() <= this.lastParticleSequence) {
            return;
        }
        this.lastParticleSequence = note.sequence();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        double normalizedPitch = Mth.clamp(note.note() / 127.0, 0.0, 1.0);
        double normalizedVelocity = Mth.clamp(note.velocity() / 127.0, 0.0, 1.0);
        double particleZ = this.z + (0.5 - normalizedPitch) * 8.0;
        double particleColor = 2.0 / 3.0 - normalizedVelocity / 3.0;
        minecraft.level.addParticle(ParticleTypes.NOTE, this.x, this.y + 1.5, particleZ,
                particleColor, 0.0, 0.0);
    }

    public void forceStop() {
        this.stop();
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MidiAudioStream stream = new MidiAudioStream(this.midiUrl);
                this.midiStream = stream;
                return stream;
            } catch (Exception e) {
                MusicPlus.LOGGER.error("Failed to create audio stream for URL: {}", midiUrl, e);
                Minecraft.getInstance().submit(this::stop);
                throw new CompletionException(e);
            }
        }, Util.backgroundExecutor());
    }
}
