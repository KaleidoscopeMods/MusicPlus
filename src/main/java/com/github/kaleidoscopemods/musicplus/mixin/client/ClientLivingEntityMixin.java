package com.github.kaleidoscopemods.musicplus.mixin.client;

import com.github.kaleidoscopemods.musicplus.api.mixin.ILivingEntityMidiData;
import com.github.kaleidoscopemods.musicplus.client.audio.NoteState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public class ClientLivingEntityMixin implements ILivingEntityMidiData {
    @Unique
    private @Nullable NoteState noteState = null;


    @Override
    public @Nullable NoteState getNoteState() {
        return this.noteState;
    }

    @Override
    public void setNoteState(NoteState noteState) {
        this.noteState = noteState;
    }
}
