package com.github.kaleidoscopemods.musicplus.client.audio;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.api.mixin.ILivingEntityMidiData;
import com.github.kaleidoscopemods.musicplus.init.ModSound;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MidiSound extends AbstractTickableSoundInstance {
    private final URL midiUrl;
    private final MidiInstrument instrument;
    private final LivingEntity performer;

    private volatile MidiAudioStream midiStream;

    public MidiSound(URL midiUrl, LivingEntity performer, MidiInstrument instrument) {
        super(ModSound.MIDI.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());

        this.midiUrl = midiUrl;
        this.instrument = instrument;
        this.performer = performer;

        this.x = performer.getX();
        this.y = performer.getY() + performer.getEyeHeight();
        this.z = performer.getZ();
    }

    @Override
    public void tick() {
        if (!this.performer.isAlive()) {
            this.forceStop();
            return;
        }
        this.x = performer.getX();
        this.y = performer.getY() + performer.getEyeHeight();
        this.z = performer.getZ();

        MidiAudioStream stream = this.midiStream;
        if (stream == null) {
            return;
        }
        @Nullable NoteState note = stream.getCurrentNote();
        if (this.performer instanceof ILivingEntityMidiData data) {
            data.setNoteState(note);
        }
    }

    public void forceStop() {
        this.stop();
        if (this.performer instanceof ILivingEntityMidiData data) {
            data.setNoteState(null);
        }
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MidiAudioStream stream = new MidiAudioStream(this.midiUrl, this.instrument);
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
