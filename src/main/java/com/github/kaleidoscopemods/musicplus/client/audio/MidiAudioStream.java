package com.github.kaleidoscopemods.musicplus.client.audio;

import com.google.common.collect.Maps;
import com.sun.sound.extern.MidiAudioInputStream;
import com.sun.sound.extern.MidiPlayer;
import com.sun.sound.extern.SF2SoundbankReader;
import com.sun.sound.extern.SoftSynthesizer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.sounds.AudioStream;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.BufferUtils;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MidiAudioStream implements AudioStream {
    private static final int GM_PERCUSSION_CHANNEL = 9;
    private static final double MELODY_ONSET_WINDOW_SECONDS = 0.06;
    private static final int PRELOAD_BUFFER_COUNT = 4;
    private static final int REFILL_BUFFER_COUNT = 2;
    private static final ExecutorService AUDIO_STREAM_EXECUTOR = new ThreadPoolExecutor(
            4,
            64,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            runnable -> {
                Thread thread = new Thread(runnable, "MusicPlus-Midi-Decoder");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private static volatile Soundbank CACHED_SOUNDBANK;

    private final AudioInputStream stream;
    private final SoftSynthesizer synth;
    private final MidiPlayer player;
    private final Sequencer sequencer;

    private final List<MidiEvent> observedEvents;
    private final Int2ObjectMap<NoteState> activeNotes = new Int2ObjectOpenHashMap<>();
    private final ConcurrentLinkedQueue<ByteBuffer> audioDataQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong queuedBytes = new AtomicLong();
    private final AtomicBoolean loading = new AtomicBoolean();
    private final AtomicBoolean playbackStarted = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();

    private final int frameSize;
    private final int streamingBufferSize;
    private final byte[] decodeBuffer;

    private int observedEventIndex;
    private long lastObservedTick = -1;
    private long noteOnSequence;

    private volatile boolean endOfStream;
    private volatile IOException failed;

    public MidiAudioStream(URL url, MidiInstrument instrument) throws IOException, InvalidMidiDataException, MidiUnavailableException {
        Sequence sequence = instrument.applyTo(MidiSystem.getSequence(url));
        Sequencer sequencer = MidiSystem.getSequencer(false);
        SoftSynthesizer synth = new SoftSynthesizer();
        MidiPlayer player = new MidiPlayer();

        AudioInputStream rawStream = null;
        AudioInputStream midiStream = null;

        try {
            Map<String, Object> options = Maps.newHashMap();
            options.put("load default soundbank", false);
            rawStream = synth.openStream(null, options);

            // TODO 需要修改成配置文件管控的
            URL soundbankUrl = FMLPaths.CONFIGDIR.get().resolve("universal_soundfont.sf2").toUri().toURL();
            Soundbank soundbank = getSoundbank(soundbankUrl);
            if (!synth.loadAllInstruments(soundbank)) {
                throw new MidiUnavailableException("Unable to load soundbank: " + soundbankUrl);
            }

            AudioFormat format = rawStream.getFormat();
            int frameSize = format.getFrameSize();
            if (frameSize <= 0) {
                throw new IOException("Invalid MIDI audio frame size: " + frameSize);
            }

            player.setSynthesizer(synth);
            player.open();
            player.setSequence(sequence);
            player.getTransmitter().setReceiver(synth.getReceiver());
            midiStream = new MidiAudioInputStream(rawStream, player);
            rawStream = null;

            sequencer.open();
            sequencer.setSequence(sequence);

            this.stream = midiStream;
            this.synth = synth;
            this.player = player;
            this.sequencer = sequencer;
            this.frameSize = frameSize;

            this.observedEvents = collectNoteEvents(sequence);
            this.streamingBufferSize = alignToFrame(calculateBufferSize(format), frameSize);

            int decodeBufferSize = Math.max(frameSize, (int) (format.getFrameRate() * frameSize / 50));
            this.decodeBuffer = new byte[alignToFrame(decodeBufferSize, frameSize)];

            pumpBuffers(PRELOAD_BUFFER_COUNT);
        } catch (IOException | InvalidMidiDataException | MidiUnavailableException | RuntimeException exception) {
            if (rawStream != null) {
                rawStream.close();
            }
            if (midiStream != null) {
                midiStream.close();
            } else {
                player.close();
                synth.close();
            }
            sequencer.close();
            throw exception;
        }
    }

    private static Soundbank getSoundbank(URL url) throws IOException, InvalidMidiDataException {
        Soundbank soundbank = CACHED_SOUNDBANK;
        if (soundbank != null) {
            return soundbank;
        }

        synchronized (MidiAudioStream.class) {
            soundbank = CACHED_SOUNDBANK;
            if (soundbank == null) {
                soundbank = new SF2SoundbankReader().getSoundbank(url);
                if (soundbank == null) {
                    throw new InvalidMidiDataException("Unable to read soundbank: " + url);
                }
                CACHED_SOUNDBANK = soundbank;
            }
        }
        return soundbank;
    }

    private static int calculateBufferSize(AudioFormat format) {
        return (int) (format.getFrameRate() * format.getFrameSize());
    }

    private static int alignToFrame(int size, int frameSize) {
        return Math.max(frameSize, size - size % frameSize);
    }

    private void pumpBuffers(int readCount) throws IOException {
        for (int i = 0; i < readCount && !endOfStream && !closed.get(); i++) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(streamingBufferSize);
            while (buffer.hasRemaining()) {
                int requested = Math.min(decodeBuffer.length, buffer.remaining());
                int count = stream.read(decodeBuffer, 0, requested);
                if (count < 0) {
                    endOfStream = true;
                    break;
                }
                if (count == 0) {
                    Thread.yield();
                    continue;
                }
                buffer.put(decodeBuffer, 0, count);
            }

            if (buffer.position() > 0) {
                buffer.flip();
                audioDataQueue.offer(buffer);
                queuedBytes.addAndGet(buffer.remaining());
            }
        }
    }

    private void loadAudioData() {
        if (!closed.get() && !endOfStream && failed == null
            && queuedBytes.get() < (long) streamingBufferSize * PRELOAD_BUFFER_COUNT
            && loading.compareAndSet(false, true)) {
            AUDIO_STREAM_EXECUTOR.execute(() -> {
                try {
                    pumpBuffers(REFILL_BUFFER_COUNT);
                } catch (IOException exception) {
                    if (!closed.get()) {
                        failed = exception;
                    }
                } finally {
                    loading.set(false);
                }
            });
        }
    }

    public synchronized NoteState getCurrentNote() {
        if (observedEvents.isEmpty() || !sequencer.isOpen()) {
            return null;
        }

        long tick = sequencer.getTickPosition();
        if (tick < lastObservedTick) {
            observedEventIndex = 0;
            activeNotes.clear();
            noteOnSequence = 0;
        }

        long onsetWindowTicks = getOnsetWindowTicks();
        long onsetWindowStart = Math.max(0, tick - onsetWindowTicks);
        NoteState bestRecentOnset = null;

        while (observedEventIndex < observedEvents.size()) {
            MidiEvent event = observedEvents.get(observedEventIndex);
            if (event.getTick() > tick) {
                break;
            }

            NoteState note = updateActiveNotes(event.getMessage());
            if (note != null && event.getTick() >= onsetWindowStart
                && isBetterMelodyCandidate(note, bestRecentOnset)) {
                bestRecentOnset = note;
            }
            observedEventIndex++;
        }
        lastObservedTick = tick;

        if (bestRecentOnset != null) {
            return copyNoteState(bestRecentOnset);
        }

        NoteState current = null;
        for (NoteState note : activeNotes.values()) {
            if (isBetterMelodyCandidate(note, current)) {
                current = note;
            }
        }
        return current == null ? null : copyNoteState(current);
    }

    private long getOnsetWindowTicks() {
        double tempoBpm = sequencer.getTempoInBPM();
        if (tempoBpm <= 0.0) {
            return 1;
        }
        double ticks = sequencer.getSequence().getResolution()
                       * tempoBpm / 60.0 * MELODY_ONSET_WINDOW_SECONDS;
        return Math.max(1L, Math.round(ticks));
    }

    private static boolean isBetterMelodyCandidate(NoteState candidate, NoteState current) {
        return current == null
               || candidate.note() > current.note()
               || (candidate.note() == current.note() && candidate.velocity() > current.velocity())
               || (candidate.note() == current.note()
                   && candidate.velocity() == current.velocity()
                   && candidate.sequence() > current.sequence());
    }

    private static NoteState copyNoteState(NoteState note) {
        return new NoteState(note.note(), note.velocity(), note.sequence());
    }

    private NoteState updateActiveNotes(MidiMessage message) {
        if (!(message instanceof ShortMessage shortMessage)) {
            return null;
        }

        if (shortMessage.getChannel() == GM_PERCUSSION_CHANNEL) {
            return null;
        }

        int command = shortMessage.getCommand();
        int note = shortMessage.getData1();
        int key = shortMessage.getChannel() * 128 + note;
        if (command == ShortMessage.NOTE_ON && shortMessage.getData2() > 0) {
            NoteState state = new NoteState(note, shortMessage.getData2(), ++noteOnSequence);
            activeNotes.put(key, state);
            return state;
        } else if (command == ShortMessage.NOTE_ON && shortMessage.getData2() == 0) {
            activeNotes.remove(key);
        } else if (command == ShortMessage.NOTE_OFF) {
            activeNotes.remove(key);
        }
        return null;
    }

    private static List<MidiEvent> collectNoteEvents(Sequence sequence) {
        List<MidiEvent> events = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage shortMessage) {
                    int command = shortMessage.getCommand();
                    if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF) {
                        events.add(event);
                    }
                }
            }
        }
        events.sort(Comparator.comparingLong(MidiEvent::getTick));
        return List.copyOf(events);
    }

    @Override
    public AudioFormat getFormat() {
        return stream.getFormat();
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        if (size <= 0) {
            return BufferUtils.createByteBuffer(0);
        }

        IOException failure = failed;
        if (failure != null) {
            throw failure;
        }

        int bytesToRead = size - size % frameSize;
        if (bytesToRead == 0) {
            return BufferUtils.createByteBuffer(0);
        }

        loadAudioData();
        long available = queuedBytes.get();
        if (available < bytesToRead) {
            if (!endOfStream || available == 0) {
                return null;
            }
            bytesToRead = (int) available;
            bytesToRead -= bytesToRead % frameSize;
        }

        if (playbackStarted.compareAndSet(false, true)) {
            sequencer.start();
        }

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(bytesToRead);
        while (byteBuffer.hasRemaining()) {
            ByteBuffer queuedBuffer = audioDataQueue.peek();
            if (queuedBuffer == null) {
                break;
            }

            int count = Math.min(queuedBuffer.remaining(), byteBuffer.remaining());
            if (count == queuedBuffer.remaining()) {
                byteBuffer.put(queuedBuffer);
                audioDataQueue.poll();
            } else {
                int oldLimit = queuedBuffer.limit();
                queuedBuffer.limit(queuedBuffer.position() + count);
                byteBuffer.put(queuedBuffer);
                queuedBuffer.limit(oldLimit);
            }
            queuedBytes.addAndGet(-count);
        }
        byteBuffer.flip();
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        IOException failure = null;

        try {
            sequencer.stop();
        } catch (IllegalStateException ignored) {
        }
        sequencer.close();

        try {
            stream.close();
        } catch (IOException exception) {
            failure = exception;
        }

        player.close();
        synth.close();
        audioDataQueue.clear();
        queuedBytes.set(0);

        if (failure != null) {
            throw failure;
        }
    }

    public record NoteState(int note, int velocity, long sequence) {
    }
}
