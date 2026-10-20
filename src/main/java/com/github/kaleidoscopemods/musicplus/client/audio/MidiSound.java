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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

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
            this.stop();
            return;
        }
        this.x = performer.getX();
        this.y = performer.getY() + performer.getEyeHeight();
        this.z = performer.getZ();

        MidiAudioStream stream = this.midiStream;
        if (stream == null) {
            return;
        }
        MidiAudioStream.NoteState note = stream.getCurrentNote();
        if (note == null) {
            return;
        }

        // TODO 依据当前音符，给 performer 设置一些可用的值，从而方便做出动画
    }

    public void forceStop() {
        this.stop();
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
