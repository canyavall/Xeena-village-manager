package com.xeenaa.villagermanager.client.render;

import com.xeenaa.villagermanager.client.model.SimplifiedVillagerModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom renderer for guard villagers that bypasses the overlay system completely.
 * Uses SimplifiedVillagerModel which renders only a single texture without blending.
 * Extends LivingEntityRenderer to properly support feature layers for equipment.
 */
@Environment(EnvType.CLIENT)
public class GuardVillagerRenderer extends LivingEntityRenderer<VillagerEntity, SimplifiedVillagerModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger("GuardVillagerRenderer");

    // Pure guard texture - no overlays will be applied
    private static final Identifier GUARD_TEXTURE =
        Identifier.of("xeenaa_villager_manager", "textures/entity/villager/guard_complete.png");

    public GuardVillagerRenderer(EntityRendererFactory.Context ctx, SimplifiedVillagerModel model) {
        super(ctx, model, 0.5F);

        // Equipment feature removed - ranking system will handle visual elements

        LOGGER.info("GuardVillagerRenderer initialized with simplified model");
    }

    @Override
    public Identifier getTexture(VillagerEntity entity) {
        // Return the ONE AND ONLY texture that will be used - no overlays
        return GUARD_TEXTURE;
    }

}