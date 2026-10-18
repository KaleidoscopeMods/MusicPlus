package com.github.kaleidoscopemods.musicplus.init;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface ModSound {
    DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MusicPlus.MOD_ID);

    RegistryObject<SoundEvent> MIDI = SOUND_EVENTS.register("midi", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MusicPlus.MOD_ID, "midi")));
}
