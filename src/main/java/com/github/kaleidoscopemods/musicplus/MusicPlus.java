package com.github.kaleidoscopemods.musicplus;

import com.github.kaleidoscopemods.musicplus.init.ModCreativeTab;
import com.github.kaleidoscopemods.musicplus.init.ModItem;
import com.github.kaleidoscopemods.musicplus.init.ModSound;
import com.github.kaleidoscopemods.musicplus.network.ModNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MusicPlus.MOD_ID)
public class MusicPlus {
    public static final String MOD_ID = "musicplus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final String FOLDER_NAME = "custom_instrument";

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    public MusicPlus() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItem.ITEMS.register(eventBus);
        ModCreativeTab.TABS.register(eventBus);
        ModSound.SOUND_EVENTS.register(eventBus);
        ModNetwork.register();
    }
}
