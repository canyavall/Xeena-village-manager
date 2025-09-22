package com.xeenaa.villagermanager.client.render;

import com.xeenaa.villagermanager.client.model.SimplifiedVillagerModel;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates a conditional renderer for villagers.
 * Guards use the custom renderer, all other villagers use vanilla renderer.
 */
@Environment(EnvType.CLIENT)
public class VillagerRendererFactory implements EntityRendererFactory<VillagerEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerRendererFactory");

    @Override
    public EntityRenderer<VillagerEntity> create(Context ctx) {
        LOGGER.info("Creating conditional villager renderer factory");

        return new EntityRenderer<VillagerEntity>(ctx) {
            private final VillagerEntityRenderer defaultRenderer = new VillagerEntityRenderer(ctx);
            private final GuardVillagerRenderer guardRenderer = new GuardVillagerRenderer(ctx,
                new SimplifiedVillagerModel(ctx.getPart(VillagerRendererFactory.getGuardModelLayer())));

            @Override
            public Identifier getTexture(VillagerEntity entity) {
                if (isGuard(entity)) {
                    return guardRenderer.getTexture(entity);
                }
                return defaultRenderer.getTexture(entity);
            }

            @Override
            public void render(VillagerEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light) {

                if (isGuard(entity)) {
                    LOGGER.trace("Using guard renderer for villager {}", entity.getUuid());
                    guardRenderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
                } else {
                    LOGGER.trace("Using default renderer for villager {}", entity.getUuid());
                    defaultRenderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
                }
            }

            private boolean isGuard(VillagerEntity entity) {
                return entity.getVillagerData() != null &&
                       entity.getVillagerData().getProfession() == ModProfessions.GUARD;
            }
        };
    }

    /**
     * Gets the model layer identifier for guard villagers.
     */
    public static net.minecraft.client.render.entity.model.EntityModelLayer getGuardModelLayer() {
        return new net.minecraft.client.render.entity.model.EntityModelLayer(
            Identifier.of("xeenaa_villager_manager", "guard_villager"), "main");
    }
}