package com.github.kaleidoscopemods.musicplus.network.packet;

import com.github.kaleidoscopemods.musicplus.client.network.ClientMidiPlayback;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayMidiPacket(int performerId) {
    public static void encode(PlayMidiPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.performerId);
    }

    public static PlayMidiPacket decode(FriendlyByteBuf buffer) {
        return new PlayMidiPacket(buffer.readVarInt());
    }

    public static void handle(PlayMidiPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMidiPlayback.play(packet));
    }
}
