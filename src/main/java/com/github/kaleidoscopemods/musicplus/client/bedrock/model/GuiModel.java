package com.github.kaleidoscopemods.musicplus.client.bedrock.model;

import com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModelUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public class GuiModel extends Model {
    private final BedrockPart bone;

    public GuiModel() {
        super(RenderType::entityCutoutNoCull);
        bone = new BedrockPart();
        bone.setPos(8.0F, 24.0F, -10.0F);
        bone.cubes.add(new BedrockCubePerFace(
                -16.0F, -16.0F, 9.5F,
                16.0F, 16.0F,
                0, 0,
                16, 16,
                BedrockModelUtil.singleSouthFace())
        );
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
