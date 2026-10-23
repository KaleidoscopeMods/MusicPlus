package com.github.kaleidoscopemods.musicplus.init;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.item.HandheldInstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface ModItem {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicPlus.MOD_ID);

    RegistryObject<Item> HANDHELD_INSTRUMENT = ITEMS.register("handheld_instrument", HandheldInstrumentItem::new);
}
