package com.github.kaleidoscopemods.musicplus.client.event;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.audio.MidiSound;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.net.MalformedURLException;
import java.net.URL;

@Mod.EventBusSubscriber(modid = MusicPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MidiTestEvent {
    private static MidiSound currentSound;

    private MidiTestEvent() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) throws MalformedURLException {
        if (!event.getLevel().isClientSide
            || event.getHand() != InteractionHand.MAIN_HAND
            || !event.getEntity().getItemInHand(event.getHand()).is(Items.STICK)
            || !event.getLevel().getBlockState(event.getPos()).is(Blocks.GRASS_BLOCK)) {
            return;
        }

        URL midiUrl = FMLPaths.CONFIGDIR.get().resolve("钟.mid").toUri().toURL();
        if (midiUrl == null) {
            MusicPlus.LOGGER.error("Could not find MIDI resource: 钟.mid");
            return;
        }

        if (currentSound != null) {
            currentSound.forceStop();
        }

        currentSound = new MidiSound(midiUrl, event.getEntity().blockPosition());
        Minecraft.getInstance().getSoundManager().play(currentSound);
    }
}
