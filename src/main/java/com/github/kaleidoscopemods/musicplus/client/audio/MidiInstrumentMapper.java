package com.github.kaleidoscopemods.musicplus.client.audio;

import com.github.kaleidoscopemods.musicplus.resources.data.MidiInstrument;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 仅在客户端将乐器映射数据应用到 MIDI 序列。
 */
public final class MidiInstrumentMapper {
    public static final int PERCUSSION_CHANNEL = 9;

    public static Sequence apply(Sequence source, MidiInstrument instrument) throws InvalidMidiDataException {
        if (instrument.playbackMode() == MidiInstrument.PlaybackMode.ORIGINAL) {
            return source;
        }

        List<MidiEvent> events = new ArrayList<>();
        for (Track track : source.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                events.add(track.get(i));
            }
        }
        // 稳定排序，保留同一时刻事件的轨道顺序及轨道内消息顺序。
        events.sort(Comparator.comparingLong(MidiEvent::getTick));

        Sequence result = new Sequence(source.getDivisionType(), source.getResolution());
        Track target = result.createTrack();
        int[] channelPrograms = new int[16];
        for (int channel = 0; channel < 16; channel++) {
            if (acceptsChannel(instrument.channelMode(), channel)) {
                ShortMessage message = new ShortMessage(
                        ShortMessage.PROGRAM_CHANGE,
                        channel,
                        instrument.targetProgram(),
                        0
                );
                target.add(new MidiEvent(message, 0));
            }
        }

        for (MidiEvent event : events) {
            MidiMessage message = event.getMessage();
            if (message instanceof SysexMessage) {
                // 屏蔽系统专用消息，避免 GM/GS/XG 重置或音色库切换覆盖固定音色。
                continue;
            }
            if (message instanceof ShortMessage shortMessage && shortMessage.getStatus() < 0xf0) {
                int channel = shortMessage.getChannel();
                if (!acceptsChannel(instrument.channelMode(), channel)) {
                    continue;
                }
                int command = shortMessage.getCommand();
                if (command == ShortMessage.PROGRAM_CHANGE) {
                    channelPrograms[channel] = shortMessage.getData1();
                    continue;
                }
                if (command == ShortMessage.CONTROL_CHANGE
                    && (shortMessage.getData1() == 0 || shortMessage.getData1() == 32)) {
                    continue;
                }
                if (command == ShortMessage.NOTE_ON && shortMessage.getData2() > 0
                    && !instrument.sourcePrograms().contains(channelPrograms[channel])) {
                    continue;
                }
            }
            target.add(new MidiEvent((MidiMessage) message.clone(), event.getTick()));
        }
        return result;
    }

    private static boolean acceptsChannel(MidiInstrument.ChannelMode mode, int channel) {
        return switch (mode) {
            case MELODIC -> channel != PERCUSSION_CHANNEL;
            case PERCUSSION -> channel == PERCUSSION_CHANNEL;
            case ALL -> true;
        };
    }
}
