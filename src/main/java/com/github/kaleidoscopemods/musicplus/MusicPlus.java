package com.github.kaleidoscopemods.musicplus;

import com.github.kaleidoscopemods.musicplus.init.ModSound;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MusicPlus.MOD_ID)
public class MusicPlus {
    public static final String MOD_ID = "musicplus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public MusicPlus() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSound.SOUND_EVENTS.register(eventBus);
    }
}
