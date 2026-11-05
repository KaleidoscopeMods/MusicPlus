package com.github.kaleidoscopemods.musicplus.api.mixin;

import com.github.kaleidoscopemods.musicplus.client.audio.NoteState;
import org.jetbrains.annotations.Nullable;

public interface ILivingEntityMidiData {
    @Nullable
    NoteState getNoteState();

    void setNoteState(@Nullable NoteState noteState);
}
