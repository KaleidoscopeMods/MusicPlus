package com.github.kaleidoscopemods.musicplus.item;

import com.github.kaleidoscopemods.musicplus.MusicPlus;
import com.github.kaleidoscopemods.musicplus.client.animation.HandheldInstrumentArmPose;
import com.github.kaleidoscopemods.musicplus.client.render.item.HandheldInstrumentItemRenderer;
import com.github.kaleidoscopemods.musicplus.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HandheldInstrumentItem extends Item {
    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(MusicPlus.MOD_ID, "ce_guitar");
    public static final String NBT_ID = "instrument_id";

    public HandheldInstrumentItem() {
        super(new Properties().stacksTo(1));
    }

    public static ResourceLocation getInstrumentId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(NBT_ID, CompoundTag.TAG_STRING)) {
            return new ResourceLocation(tag.getString(NBT_ID));
        }
        return DEFAULT_ID;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide()) {
            ModNetwork.sendPlayMidi(player);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            // 放这里，提前触发加载
            private final HumanoidModel.ArmPose pose = HandheldInstrumentArmPose.HANDHELD_INSTRUMENT_POSE;
            private HandheldInstrumentItemRenderer render = null;

            @Override
            public HandheldInstrumentItemRenderer getCustomRenderer() {
                Minecraft mc = Minecraft.getInstance();
                if (render == null) {
                    render = new HandheldInstrumentItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return render;
            }

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack stack) {
                if (hand == InteractionHand.MAIN_HAND && !stack.isEmpty()) {
                    return pose;
                }
                return HumanoidModel.ArmPose.EMPTY;
            }
        });
    }
}
