package com.github.kaleidoscopemods.musicplus.client.render.item;

import com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModel;
import com.github.kaleidoscopemods.musicplus.client.resources.ItemReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.ModelReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.data.CustomClientItem;
import com.github.kaleidoscopemods.musicplus.item.HandheldInstrumentItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HandheldInstrumentItemRenderer extends BlockEntityWithoutLevelRenderer {
    public HandheldInstrumentItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // 第三人称交给 Layer
        if (transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            return;
        }

        ResourceLocation id = HandheldInstrumentItem.getInstrumentId(stack);
        CustomClientItem clientItem = ItemReloadListener.INSTANCES.getOrDefault(id, CustomClientItem.DEFAULT);
        BedrockModel model = ModelReloadListener.INSTANCES.getOrDefault(clientItem.model(), ModelReloadListener.DFAULT_HANDHELD_INSTRUMENT_MODEL);

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));

        RenderType renderType = RenderType.entityCutoutNoCull(clientItem.texture());
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        model.renderToBuffer(poseStack, buffer, combinedLight, combinedOverlay, 1, 1, 1, 1);
        poseStack.popPose();
    }
}
