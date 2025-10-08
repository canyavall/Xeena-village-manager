package com.xeenaa.villagermanager.client.render;

import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.client.model.SimplifiedVillagerModel;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.data.rank.GuardRank;
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

    // Guard textures for each path type
    private static final Identifier GUARD_RECRUIT_TEXTURE =
        Identifier.of("xeenaa_villager_manager", "textures/entity/villager/profession/guard_recruit.png");
    private static final Identifier GUARD_MELEE_TEXTURE =
        Identifier.of("xeenaa_villager_manager", "textures/entity/villager/profession/guard_arms.png");
    private static final Identifier GUARD_RANGED_TEXTURE =
        Identifier.of("xeenaa_villager_manager", "textures/entity/villager/profession/guard_marksman.png");

    public GuardVillagerRenderer(EntityRendererFactory.Context ctx, SimplifiedVillagerModel model) {
        super(ctx, model, 0.5F);

        // Add held item renderer so guards can visually hold weapons
        this.addFeature(new net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer<>(this, ctx.getHeldItemRenderer()));

        LOGGER.info("GuardVillagerRenderer initialized with dynamic texture selection and held item renderer");
    }

    @Override
    public Identifier getTexture(VillagerEntity entity) {
        // Get guard data from client cache to determine path
        GuardData guardData = ClientGuardDataCache.getInstance().getGuardData(entity);

        if (guardData != null) {
            GuardRank rank = guardData.getRankData().getCurrentRank();
            GuardPath path = rank.getPath();

            // Select texture based on guard path
            return switch (path) {
                case RECRUIT -> GUARD_RECRUIT_TEXTURE;
                case MELEE -> GUARD_MELEE_TEXTURE;
                case RANGED -> GUARD_RANGED_TEXTURE;
            };
        }

        // Fallback to recruit texture if no guard data is available
        LOGGER.trace("No guard data found for villager {}, using recruit texture", entity.getUuid());
        return GUARD_RECRUIT_TEXTURE;
    }

}