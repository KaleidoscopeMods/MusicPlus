package com.github.kaleidoscopemods.musicplus.datagen.model;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MusicPlus.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }
}
