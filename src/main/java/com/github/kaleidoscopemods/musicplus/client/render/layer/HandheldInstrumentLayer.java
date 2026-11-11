package com.github.kaleidoscopemods.musicplus.client.render.layer;

import com.github.kaleidoscopemods.musicplus.client.bedrock.BedrockModel;
import com.github.kaleidoscopemods.musicplus.client.resources.ItemReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.ModelReloadListener;
import com.github.kaleidoscopemods.musicplus.client.resources.data.CustomClientItem;
import com.github.kaleidoscopemods.musicplus.item.HandheldInstrumentItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class HandheldInstrumentLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public HandheldInstrumentLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    public static boolean replacesHandItem(LivingEntity entity, HumanoidArm arm) {
        return entity instanceof AbstractClientPlayer && arm == entity.getMainArm()
               && entity.getMainHandItem().getItem() instanceof HandheldInstrumentItem;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!replacesHandItem(player, player.getMainArm()) || player.isInvisible() || player.isSpectator()) {
            return;
        }

        ItemStack handItem = player.getMainHandItem();
        ResourceLocation id = HandheldInstrumentItem.getInstrumentId(handItem);
        CustomClientItem clientItem = ItemReloadListener.INSTANCES.getOrDefault(id, CustomClientItem.DEFAULT);
        BedrockModel model = ModelReloadListener.INSTANCES.getOrDefault(clientItem.model(), ModelReloadListener.DFAULT_HANDHELD_INSTRUMENT_MODEL);

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        if (clientItem.transform() != null) {
            clientItem.transform().thirdPersonRightHand.apply(false, poseStack);
        }
        RenderType renderType = RenderType.entityCutoutNoCull(clientItem.texture());
        model.renderToBuffer(poseStack, buffer.getBuffer(renderType), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
    }
}
