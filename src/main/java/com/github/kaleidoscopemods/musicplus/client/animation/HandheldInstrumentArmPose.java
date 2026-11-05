package com.github.kaleidoscopemods.musicplus.client.animation;

import com.github.kaleidoscopemods.musicplus.api.mixin.ILivingEntityMidiData;
import com.github.kaleidoscopemods.musicplus.client.audio.NoteState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class HandheldInstrumentArmPose {
    private static final int LOW_BASS_NOTE = 28;
    private static final int HIGH_BASS_NOTE = 67;

    /**
     * 玩家手持乐器弹奏时，应该随音符有不同的变化
     */
    public static final HumanoidModel.ArmPose HANDHELD_INSTRUMENT_POSE = HumanoidModel.ArmPose.create("MP_HANDHELD_INSTRUMENT", true, (model, entity, arm) -> {
        ModelPart supportingArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        ModelPart playingArm = arm == HumanoidArm.RIGHT ? model.leftArm : model.rightArm;
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;

        NoteState noteState = entity instanceof ILivingEntityMidiData midiData ? midiData.getNoteState() : null;
        float noteProgress = 0.5F;
        float pluck = 0.0F;
        if (noteState != null) {
            noteProgress = Mth.clamp((noteState.note() - LOW_BASS_NOTE)
                                     / (float) (HIGH_BASS_NOTE - LOW_BASS_NOTE), 0.0F, 1.0F);
            float velocity = Mth.clamp(noteState.velocity() / 127.0F, 0.0F, 1.0F);
            float age = entity.tickCount + Minecraft.getInstance().getFrameTime();
            float phase = age * 0.9F + noteState.sequence() % 32L * 0.65F;
            pluck = Mth.sin(phase) * (0.04F + velocity * 0.08F);
        }

        supportingArm.xRot = -1.05F;
        supportingArm.yRot = -side * 0.30F;
        supportingArm.zRot = side * 0.16F;

        float fretOffset = noteProgress - 0.5F;
        playingArm.xRot = -1.18F - fretOffset * 0.28F + pluck;
        playingArm.yRot = side * (0.58F - fretOffset * 0.18F + pluck * 0.25F);
        playingArm.zRot = -side * (0.14F + fretOffset * 0.12F);
    });
}
