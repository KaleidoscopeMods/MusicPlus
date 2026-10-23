package com.github.kaleidoscopemods.musicplus.datagen.model;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockModelGenerator extends BlockModelProvider {
    public BlockModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MusicPlus.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }
}
