package com.github.kaleidoscopemods.musicplus.resources.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * MIDI 乐器映射数据，不依赖客户端或 MIDI API。
 * GM 音色编号从 0 开始；通道 9（第 10 通道）专用于打击乐。
 *
 * @param sourcePrograms 接受的原曲音色编号；空集合表示不接受任何音色
 * @param targetProgram  输出音色编号，不是 SoundFont 音源库编号
 * @param channelMode    需要处理的通道类型
 * @param playbackMode   ORIGINAL 完整保留原曲并忽略其他映射字段，REMAP 筛选并替换音色
 */
@JsonAdapter(MidiInstrument.Deserializer.class)
public record MidiInstrument(
        @SerializedName("source_programs") Set<Integer> sourcePrograms,
        @SerializedName("target_program") int targetProgram,
        @SerializedName("channel_mode") ChannelMode channelMode,
        @SerializedName("playback_mode") PlaybackMode playbackMode
) {
    /**
     * 全乐器：保留原曲的全部声部和音色。
     */
    public static final MidiInstrument ALL = new MidiInstrument(programsBetween(0, 127), 0, ChannelMode.ALL, PlaybackMode.ORIGINAL);
    /**
     * 钢琴类：使用原声大钢琴音色。
     */
    public static final MidiInstrument PIANO = melodicPreset(0, 0, 7);
    /**
     * 钢琴类：使用羽管键琴音色。
     */
    public static final MidiInstrument HARPSICHORD = melodicPreset(6, 0, 7);
    /**
     * 钢琴类：使用电钢琴音色。
     */
    public static final MidiInstrument ELECTRIC_PIANO = melodicPreset(4, 0, 7);
    /**
     * 半音阶打击乐类：使用八音盒音色。
     */
    public static final MidiInstrument MUSIC_BOX = melodicPreset(10, 8, 15);
    /**
     * 半音阶打击乐类：使用颤音琴音色。
     */
    public static final MidiInstrument VIBRAPHONE = melodicPreset(11, 8, 15);
    /**
     * 风琴类：使用教堂管风琴音色。
     */
    public static final MidiInstrument ORGAN = melodicPreset(19, 16, 23);
    /**
     * 风琴类：使用手风琴音色。
     */
    public static final MidiInstrument ACCORDION = melodicPreset(21, 16, 23);
    /**
     * 吉他类：使用尼龙弦原声吉他音色。
     */
    public static final MidiInstrument GUITAR = melodicPreset(24, 24, 31);
    /**
     * 吉他类：使用清音电吉他音色。
     */
    public static final MidiInstrument ELECTRIC_GUITAR = melodicPreset(27, 24, 31);
    /**
     * 贝斯类：使用指弹电贝斯音色。
     */
    public static final MidiInstrument BASS = melodicPreset(33, 32, 39);
    /**
     * 弦乐与合奏类：使用小提琴音色。
     */
    public static final MidiInstrument VIOLIN = melodicPreset(40, 40, 55);
    /**
     * 弦乐与合奏类：使用竖琴音色。
     */
    public static final MidiInstrument HARP = melodicPreset(46, 40, 55);
    /**
     * 铜管类：使用小号音色。
     */
    public static final MidiInstrument TRUMPET = melodicPreset(56, 56, 63);
    /**
     * 簧片管乐类：使用中音萨克斯音色。
     */
    public static final MidiInstrument SAXOPHONE = melodicPreset(65, 64, 71);
    /**
     * 吹管乐类：使用长笛音色。
     */
    public static final MidiInstrument FLUTE = melodicPreset(73, 72, 79);
    /**
     * 合成主音类：使用方波主音音色。
     */
    public static final MidiInstrument SYNTH_LEAD = melodicPreset(80, 80, 87);
    /**
     * 合成铺底类：使用新时代氛围铺底音色。
     */
    public static final MidiInstrument SYNTH_PAD = melodicPreset(88, 88, 95);
    /**
     * 民族乐器类：使用西塔琴音色。
     */
    public static final MidiInstrument ETHNIC = melodicPreset(104, 104, 111);
    /**
     * 打击音色类：使用叮当铃音色，按旋律通道播放。
     */
    public static final MidiInstrument PERCUSSIVE = melodicPreset(112, 112, 119);
    /**
     * 鼓组类：仅播放第 10 通道，使用标准鼓组音色。
     */
    public static final MidiInstrument DRUM_KIT = new MidiInstrument(programsBetween(0, 127), 0, ChannelMode.PERCUSSION, PlaybackMode.REMAP);

    public MidiInstrument {
        sourcePrograms = Set.copyOf(sourcePrograms);
        sourcePrograms.forEach(MidiInstrument::validateProgram);
        validateProgram(targetProgram);
        Objects.requireNonNull(channelMode, "channelMode");
        Objects.requireNonNull(playbackMode, "playbackMode");
    }

    public enum ChannelMode {
        @SerializedName("melodic") MELODIC,
        @SerializedName("percussion") PERCUSSION,
        @SerializedName("all") ALL
    }

    public enum PlaybackMode {
        @SerializedName("original") ORIGINAL,
        @SerializedName("remap") REMAP
    }

    /**
     * 支持预设名称和自定义对象；对象中缺失或为 null 的字段使用 PIANO 的对应值。
     */
    public static final class Deserializer implements JsonDeserializer<MidiInstrument> {
        private static final Type PROGRAM_SET_TYPE = new TypeToken<Set<Integer>>() {
        }.getType();

        @Override
        public MidiInstrument deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return findPreset(json.getAsString());
            }
            if (!json.isJsonObject()) {
                throw new JsonParseException("MidiInstrument must be a preset name string or an object");
            }

            JsonObject object = json.getAsJsonObject();
            Set<Integer> sourcePrograms = read(object, "source_programs", PROGRAM_SET_TYPE, PIANO.sourcePrograms(), context);
            int targetProgram = read(object, "target_program", Integer.class, PIANO.targetProgram(), context);
            ChannelMode channelMode = read(object, "channel_mode", ChannelMode.class, PIANO.channelMode(), context);
            PlaybackMode playbackMode = read(object, "playback_mode", PlaybackMode.class, PIANO.playbackMode(), context);

            try {
                return new MidiInstrument(sourcePrograms, targetProgram, channelMode, playbackMode);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new JsonParseException("Invalid MidiInstrument configuration", e);
            }
        }

        private static <T> T read(JsonObject object, String name, Type type, T fallback, JsonDeserializationContext context) {
            JsonElement value = object.get(name);
            if (value == null || value.isJsonNull()) {
                return fallback;
            }
            T result = context.deserialize(value, type);
            if (result == null) {
                // Gson 遇到未知枚举名称时会返回 null。
                throw new JsonParseException("Invalid value for field '" + name + "': " + value);
            }
            return result;
        }

        private static MidiInstrument findPreset(String name) {
            return switch (name.trim().toUpperCase(Locale.ROOT)) {
                case "ALL" -> ALL;
                case "HARPSICHORD" -> HARPSICHORD;
                case "ELECTRIC_PIANO" -> ELECTRIC_PIANO;
                case "MUSIC_BOX" -> MUSIC_BOX;
                case "VIBRAPHONE" -> VIBRAPHONE;
                case "ORGAN" -> ORGAN;
                case "ACCORDION" -> ACCORDION;
                case "GUITAR" -> GUITAR;
                case "ELECTRIC_GUITAR" -> ELECTRIC_GUITAR;
                case "BASS" -> BASS;
                case "VIOLIN" -> VIOLIN;
                case "HARP" -> HARP;
                case "TRUMPET" -> TRUMPET;
                case "SAXOPHONE" -> SAXOPHONE;
                case "FLUTE" -> FLUTE;
                case "SYNTH_LEAD" -> SYNTH_LEAD;
                case "SYNTH_PAD" -> SYNTH_PAD;
                case "ETHNIC" -> ETHNIC;
                case "PERCUSSIVE" -> PERCUSSIVE;
                case "DRUM_KIT" -> DRUM_KIT;
                default -> PIANO;
            };
        }
    }

    private static void validateProgram(int program) {
        if (program < 0 || program > 127) {
            throw new IllegalArgumentException("MIDI program must be between 0 and 127: " + program);
        }
    }

    private static Set<Integer> programsBetween(int first, int last) {
        return IntStream.rangeClosed(first, last).boxed().collect(Collectors.toSet());
    }

    private static MidiInstrument melodicPreset(int targetProgram, int first, int last) {
        Set<Integer> programs = new HashSet<>(programsBetween(first, last));
        // 默认第 0 PC 是所有乐器都能播放的
        programs.add(0);
        return new MidiInstrument(programs, targetProgram, ChannelMode.MELODIC, PlaybackMode.REMAP);
    }
}
