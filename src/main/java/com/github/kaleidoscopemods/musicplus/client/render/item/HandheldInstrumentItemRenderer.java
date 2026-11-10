package com.github.kaleidoscopemods.musicplus.client.render.item;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModel;
import com.github.kaleidoscopemods.musicplus.client.resources.ItemReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.ModelReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.data.CustomClientItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HandheldInstrumentItemRenderer extends BlockEntityWithoutLevelRenderer {
    public HandheldInstrumentItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        BedrockModel model = ModelReloadListener.DFAULT_HANDHELD_INSTRUMENT_MODEL;
        ResourceLocation texture = ModelReloadListener.DFAULT_HANDHELD_INSTRUMENT_TEXTURE;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
