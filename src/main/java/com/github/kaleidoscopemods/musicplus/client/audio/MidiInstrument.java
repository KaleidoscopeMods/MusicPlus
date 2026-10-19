package com.github.kaleidoscopemods.musicplus.client.audio;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GM 音色编号从 0 开始；通道 9（第 10 通道）专用于打击乐。
 */
public enum MidiInstrument {
    /**
     * 全乐器：保留原曲的全部声部和音色。
     */
    ALL(0, 0, 127),
    /**
     * 钢琴类：使用原声大钢琴音色。
     */
    PIANO(0, 0, 7),
    /**
     * 钢琴类：使用羽管键琴音色。
     */
    HARPSICHORD(6, 0, 7),
    /**
     * 钢琴类：使用电钢琴音色。
     */
    ELECTRIC_PIANO(4, 0, 7),
    /**
     * 半音阶打击乐类：使用八音盒音色。
     */
    MUSIC_BOX(10, 8, 15),
    /**
     * 半音阶打击乐类：使用颤音琴音色。
     */
    VIBRAPHONE(11, 8, 15),
    /**
     * 风琴类：使用教堂管风琴音色。
     */
    ORGAN(19, 16, 23),
    /**
     * 风琴类：使用手风琴音色。
     */
    ACCORDION(21, 16, 23),
    /**
     * 吉他类：使用尼龙弦原声吉他音色。
     */
    GUITAR(24, 24, 31),
    /**
     * 吉他类：使用清音电吉他音色。
     */
    ELECTRIC_GUITAR(27, 24, 31),
    /**
     * 贝斯类：使用指弹电贝斯音色。
     */
    BASS(33, 32, 39),
    /**
     * 弦乐与合奏类：使用小提琴音色。
     */
    VIOLIN(40, 40, 55),
    /**
     * 弦乐与合奏类：使用竖琴音色。
     */
    HARP(46, 40, 55),
    /**
     * 铜管类：使用小号音色。
     */
    TRUMPET(56, 56, 63),
    /**
     * 簧片管乐类：使用中音萨克斯音色。
     */
    SAXOPHONE(65, 64, 71),
    /**
     * 吹管乐类：使用长笛音色。
     */
    FLUTE(73, 72, 79),
    /**
     * 合成主音类：使用方波主音音色。
     */
    SYNTH_LEAD(80, 80, 87),
    /**
     * 合成铺底类：使用新时代氛围铺底音色。
     */
    SYNTH_PAD(88, 88, 95),
    /**
     * 民族乐器类：使用西塔琴音色。
     */
    ETHNIC(104, 104, 111),
    /**
     * 打击音色类：使用叮当铃音色，按旋律通道播放。
     */
    PERCUSSIVE(112, 112, 119),
    /**
     * 鼓组类：仅播放第 10 通道，使用标准鼓组音色。
     */
    DRUM_KIT(0, 0, 127);

    private static final int PERCUSSION_CHANNEL = 9;

    private final int program;
    private final int firstProgram;
    private final int lastProgram;

    MidiInstrument(int program, int firstProgram, int lastProgram) {
        this.program = program;
        this.firstProgram = firstProgram;
        this.lastProgram = lastProgram;
    }

    /**
     * 全乐器模式保留原曲；其他模式按 GM 乐器类别筛选声部，并使用固定音色。
     * 音色编号 0 和未支持的效果音色使用所选默认音色，与参考播放器的回退规则一致。
     */
    public Sequence applyTo(Sequence source) throws InvalidMidiDataException {
        if (this == ALL) {
            return source;
        }

        List<MidiEvent> events = new ArrayList<>();
        for (Track track : source.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                events.add(track.get(i));
            }
        }
        // 稳定排序保留同一时刻事件的轨道顺序及轨道内消息顺序。
        events.sort(Comparator.comparingLong(MidiEvent::getTick));

        Sequence result = new Sequence(source.getDivisionType(), source.getResolution());
        Track target = result.createTrack();
        int[] channelPrograms = new int[16];
        for (int channel = 0; channel < 16; channel++) {
            if ((channel == PERCUSSION_CHANNEL) == (this == DRUM_KIT)) {
                target.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0), 0));
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
                if ((channel == PERCUSSION_CHANNEL) != (this == DRUM_KIT)) {
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
                    && this != DRUM_KIT && !acceptsProgram(channelPrograms[channel])) {
                    continue;
                }
            }
            target.add(new MidiEvent((MidiMessage) message.clone(), event.getTick()));
        }
        return result;
    }

    private boolean acceptsProgram(int sourceProgram) {
        return sourceProgram == 0
               || (sourceProgram >= 96 && sourceProgram <= 103)
               || sourceProgram >= 120
               || (sourceProgram >= firstProgram && sourceProgram <= lastProgram);
    }
}
