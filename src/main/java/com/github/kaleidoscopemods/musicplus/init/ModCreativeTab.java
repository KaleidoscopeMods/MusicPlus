package com.github.kaleidoscopemods.musicplus.init;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModCreativeTab {
    DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MusicPlus.MOD_ID);

    RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.musicplus.main.name"))
            .icon(() -> ModItem.HANDHELD_INSTRUMENT.get().getDefaultInstance())
            .displayItems((par, output) -> {
                output.accept(ModItem.HANDHELD_INSTRUMENT.get());
            }).build());
}
