package com.github.kaleidoscopemods.musicplus.network;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.network.packet.PlayMidiPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final double MIDI_HEARING_RANGE = 128.0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MusicPlus.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(PlayMidiPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayMidiPacket::encode)
                .decoder(PlayMidiPacket::decode)
                .consumerMainThread(PlayMidiPacket::handle)
                .add();
    }

    public static void sendPlayMidi(Entity performer) {
        CHANNEL.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(
                performer.getX(), performer.getY(), performer.getZ(), MIDI_HEARING_RANGE,
                performer.level().dimension()
        )), new PlayMidiPacket(performer.getId()));
    }
}
