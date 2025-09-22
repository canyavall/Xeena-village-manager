package com.xeenaa.villagermanager.client.render;

import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.client.model.SimplifiedVillagerModel;
import com.xeenaa.villagermanager.data.GuardData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feature renderer for guard equipment using proper Minecraft armor rendering system.
 */
@Environment(EnvType.CLIENT)
public class GuardEquipmentFeature extends FeatureRenderer<VillagerEntity, SimplifiedVillagerModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger("GuardEquipmentFeature");

    private final ItemRenderer itemRenderer;

    public GuardEquipmentFeature(FeatureRendererContext<VillagerEntity, SimplifiedVillagerModel> context, ItemRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
        LOGGER.info("GuardEquipmentFeature initialized");
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, VillagerEntity entity,
                      float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

        // Get guard data from client cache
        ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
        GuardData guardData = cache.getGuardData(entity);

        if (guardData == null) {
            return; // No guard data, no equipment to render
        }

        LOGGER.trace("Rendering equipment for guard villager {}", entity.getUuid());

        // Render hand items (weapons and shields)
        renderHandItems(entity, guardData, matrices, vertexConsumers, light);

        // Render armor pieces
        renderArmor(entity, guardData, matrices, vertexConsumers, light);
    }

    private void renderHandItems(VillagerEntity entity, GuardData guardData, MatrixStack matrices,
                                VertexConsumerProvider vertexConsumers, int light) {

        ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
        ItemStack shield = guardData.getEquipment(GuardData.EquipmentSlot.SHIELD);

        // Render weapon in main hand
        if (!weapon.isEmpty()) {
            renderHandItem(entity, weapon, matrices, vertexConsumers, light, true);
        }

        // Render shield in off hand
        if (!shield.isEmpty()) {
            renderHandItem(entity, shield, matrices, vertexConsumers, light, false);
        }
    }

    private void renderHandItem(VillagerEntity entity, ItemStack itemStack, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light, boolean isMainHand) {

        matrices.push();

        // Get the model and position relative to the appropriate arm
        SimplifiedVillagerModel model = this.getContextModel();

        if (isMainHand) {
            // Position relative to right arm - moved to actual arm position
            LOGGER.info("HAND DEBUG: Rendering {} in MAIN HAND at new position (-0.3125, 0.25, 0.0625)", itemStack.getItem().getName().getString());
            matrices.translate(-0.3125F, 0.25F, 0.0625F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        } else {
            // Position relative to left arm - moved to actual arm position
            LOGGER.info("HAND DEBUG: Rendering {} in OFF HAND at new position (0.3125, 0.25, 0.0625)", itemStack.getItem().getName().getString());
            matrices.translate(0.3125F, 0.25F, 0.0625F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        }

        matrices.scale(0.375F, 0.375F, 0.375F);

        this.itemRenderer.renderItem(entity, itemStack, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                                   false, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);

        matrices.pop();
    }

    private void renderArmor(VillagerEntity entity, GuardData guardData, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumers, int light) {

        // Render armor pieces using ItemRenderer for proper texture handling
        renderArmorAsItem(entity, guardData.getEquipment(GuardData.EquipmentSlot.HELMET),
                         matrices, vertexConsumers, light, "head");
        renderArmorAsItem(entity, guardData.getEquipment(GuardData.EquipmentSlot.CHESTPLATE),
                         matrices, vertexConsumers, light, "chest");
        renderArmorAsItem(entity, guardData.getEquipment(GuardData.EquipmentSlot.LEGGINGS),
                         matrices, vertexConsumers, light, "legs");
        renderArmorAsItem(entity, guardData.getEquipment(GuardData.EquipmentSlot.BOOTS),
                         matrices, vertexConsumers, light, "feet");
    }

    private void renderArmorAsItem(VillagerEntity entity, ItemStack armorStack, MatrixStack matrices,
                                  VertexConsumerProvider vertexConsumers, int light, String bodyPart) {

        if (armorStack.isEmpty()) {
            return;
        }

        matrices.push();

        // Position armor piece based on body part
        switch (bodyPart) {
            case "head":
                LOGGER.info("ARMOR DEBUG: Rendering {} on HEAD at new position (0.0, -0.6, 0.0) scale 1.0", armorStack.getItem().getName().getString());
                matrices.translate(0.0F, -0.6F, 0.0F);
                matrices.scale(1.0F, 1.0F, 1.0F);
                break;
            case "chest":
                LOGGER.info("ARMOR DEBUG: Rendering {} on CHEST at position (0.0, 0.0, 0.0) scale 1.1", armorStack.getItem().getName().getString());
                matrices.translate(0.0F, 0.0F, 0.0F);
                matrices.scale(1.1F, 1.1F, 1.1F);
                break;
            case "legs":
                LOGGER.info("ARMOR DEBUG: Rendering {} on LEGS at position (0.0, 0.5, 0.0) scale 1.05", armorStack.getItem().getName().getString());
                matrices.translate(0.0F, 0.5F, 0.0F);
                matrices.scale(1.05F, 1.05F, 1.05F);
                break;
            case "feet":
                LOGGER.info("ARMOR DEBUG: Rendering {} on FEET at position (0.0, 0.8, 0.0) scale 1.0", armorStack.getItem().getName().getString());
                matrices.translate(0.0F, 0.8F, 0.0F);
                matrices.scale(1.0F, 1.0F, 1.0F);
                break;
        }

        // Use ItemRenderer with proper transformation mode for armor
        ModelTransformationMode transformationMode = switch (bodyPart) {
            case "head" -> ModelTransformationMode.HEAD;
            default -> ModelTransformationMode.FIXED;
        };

        this.itemRenderer.renderItem(entity, armorStack, transformationMode,
                                   false, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);

        matrices.pop();
    }
}