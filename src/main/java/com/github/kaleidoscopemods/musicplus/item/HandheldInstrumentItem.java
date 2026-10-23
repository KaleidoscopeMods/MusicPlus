package com.github.kaleidoscopemods.musicplus.item;

import com.github.kaleidoscopemods.musicplus.client.render.item.HandheldInstrumentItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class HandheldInstrumentItem extends Item {
    public HandheldInstrumentItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HandheldInstrumentItemRenderer render = null;

            @Override
            public HandheldInstrumentItemRenderer getCustomRenderer() {
                Minecraft mc = Minecraft.getInstance();
                if (render == null) {
                    render = new HandheldInstrumentItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return render;
            }
        });
    }
}
