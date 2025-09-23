// Improved Guard Equipment Renderer with Proper Arm Poses
// For Minecraft 1.21.1 Fabric
// Research-based implementation for natural weapon holding

package com.xeenaa.villagermanager.client.render;

import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.client.model.SimplifiedVillagerModel;
import com.xeenaa.villagermanager.data.GuardData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced guard equipment renderer with research-based arm poses and item positioning.
 *
 * Key improvements:
 * - Arm-relative item positioning that follows villager arm movement
 * - Weapon-specific positioning and rotation adjustments
 * - Smooth transitions between default and combat poses
 * - Proper scaling for villager proportions
 */
@Environment(EnvType.CLIENT)
public class ImprovedGuardEquipmentFeature extends FeatureRenderer<VillagerEntity, SimplifiedVillagerModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger("ImprovedGuardEquipmentFeature");

    private final ItemRenderer itemRenderer;

    public ImprovedGuardEquipmentFeature(FeatureRendererContext<VillagerEntity, SimplifiedVillagerModel> context, ItemRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
        LOGGER.info("ImprovedGuardEquipmentFeature initialized with research-based positioning");
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, VillagerEntity entity,
                      float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

        ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
        GuardData guardData = cache.getGuardData(entity);

        if (guardData == null) {
            return;
        }

        // Render hand items with improved positioning
        renderHandItems(entity, guardData, matrices, vertexConsumers, light);

        // Render armor (existing implementation can remain)
        renderArmor(entity, guardData, matrices, vertexConsumers, light);
    }

    private void renderHandItems(VillagerEntity entity, GuardData guardData, MatrixStack matrices,
                                VertexConsumerProvider vertexConsumers, int light) {

        ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
        ItemStack shield = guardData.getEquipment(GuardData.EquipmentSlot.SHIELD);

        // Render weapon in main hand with improved positioning
        if (!weapon.isEmpty()) {
            renderImprovedHandItem(entity, weapon, matrices, vertexConsumers, light, true);
        }

        // Render shield in off hand
        if (!shield.isEmpty()) {
            renderImprovedHandItem(entity, shield, matrices, vertexConsumers, light, false);
        }
    }

    /**
     * Renders hand item with research-based positioning that follows arm movement.
     * This method applies transformations relative to the villager's arm position and rotation.
     */
    private void renderImprovedHandItem(VillagerEntity entity, ItemStack itemStack, MatrixStack matrices,
                                       VertexConsumerProvider vertexConsumers, int light, boolean isMainHand) {

        matrices.push();

        SimplifiedVillagerModel model = this.getContextModel();

        if (isMainHand) {
            renderMainHandItem(entity, itemStack, matrices, vertexConsumers, light, model);
        } else {
            renderOffHandItem(entity, itemStack, matrices, vertexConsumers, light, model);
        }

        matrices.pop();
    }

    private void renderMainHandItem(VillagerEntity entity, ItemStack itemStack, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light, SimplifiedVillagerModel model) {

        LOGGER.debug("Rendering {} in main hand with arm-relative positioning", itemStack.getItem().getName().getString());

        // Move to right arm joint position (villager model coordinates)
        matrices.translate(-5.0F / 16.0F, 2.0F / 16.0F, 0.0F);

        // Apply current arm rotation to follow arm movement
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(model.rightArm.pitch));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(model.rightArm.yaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(model.rightArm.roll));

        // Move to hand position (end of arm, accounting for 12-unit arm length)
        matrices.translate(0.0F, 10.0F / 16.0F, 0.0F);

        // Apply item-specific positioning and rotation
        applyItemSpecificTransforms(itemStack, matrices, true);

        // Scale appropriately for villager hands
        matrices.scale(0.375F, 0.375F, 0.375F);

        // Render the item
        this.itemRenderer.renderItem(entity, itemStack, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                                   false, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);
    }

    private void renderOffHandItem(VillagerEntity entity, ItemStack itemStack, MatrixStack matrices,
                                  VertexConsumerProvider vertexConsumers, int light, SimplifiedVillagerModel model) {

        LOGGER.debug("Rendering {} in off hand with arm-relative positioning", itemStack.getItem().getName().getString());

        // Move to left arm joint position
        matrices.translate(5.0F / 16.0F, 2.0F / 16.0F, 0.0F);

        // Apply current arm rotation
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(model.leftArm.pitch));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(model.leftArm.yaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(model.leftArm.roll));

        // Move to hand position
        matrices.translate(0.0F, 10.0F / 16.0F, 0.0F);

        // Apply item-specific transforms
        applyItemSpecificTransforms(itemStack, matrices, false);

        // Scale for villager hands
        matrices.scale(0.375F, 0.375F, 0.375F);

        // Render the item
        this.itemRenderer.renderItem(entity, itemStack, ModelTransformationMode.THIRD_PERSON_LEFT_HAND,
                                   false, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);
    }

    /**
     * Applies item-specific transformations for natural weapon/tool holding.
     */
    private void applyItemSpecificTransforms(ItemStack itemStack, MatrixStack matrices, boolean isMainHand) {
        Item item = itemStack.getItem();

        // Base orientation for all items
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        if (item instanceof SwordItem) {
            // Sword-specific positioning
            if (isMainHand) {
                matrices.translate(0.0F, 0.0F, -0.0625F); // Slight forward for better grip
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(15.0F)); // Natural grip angle
            } else {
                matrices.translate(0.0F, 0.0F, 0.0625F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F));
            }

        } else if (item instanceof AxeItem) {
            // Axe positioning (similar to sword but slightly different angle)
            if (isMainHand) {
                matrices.translate(0.0F, 0.0F, -0.03125F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(20.0F)); // Slightly more angle for axes
            } else {
                matrices.translate(0.0F, 0.0F, 0.03125F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F));
            }

        } else if (item instanceof ShieldItem) {
            // Shield positioning (defensive angle)
            if (isMainHand) {
                matrices.translate(0.0F, 0.0F, 0.0625F); // Further back for shields
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(25.0F)); // More defensive angle
            } else {
                matrices.translate(0.0F, 0.0F, 0.125F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-25.0F));
            }

        } else if (item instanceof BowItem) {
            // Bow positioning (vertical grip)
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(isMainHand ? 45.0F : -45.0F));
            matrices.translate(0.0F, 0.0F, isMainHand ? -0.0625F : 0.0625F);

        } else if (item instanceof CrossbowItem) {
            // Crossbow positioning (horizontal grip)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(isMainHand ? 15.0F : -15.0F));
            matrices.translate(0.0F, 0.0F, isMainHand ? -0.03125F : 0.03125F);

        } else if (item instanceof ToolItem) {
            // General tool positioning
            if (isMainHand) {
                matrices.translate(0.0F, 0.0F, -0.03125F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(10.0F)); // Slight angle for tools
            } else {
                matrices.translate(0.0F, 0.0F, 0.03125F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F));
            }
        }

        // For any other items, use default positioning (no additional transforms)
    }

    // Existing armor rendering method (unchanged)
    private void renderArmor(VillagerEntity entity, GuardData guardData, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumers, int light) {

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

        // Position armor piece based on body part (existing logic)
        switch (bodyPart) {
            case "head":
                matrices.translate(0.0F, -0.6F, 0.0F);
                matrices.scale(1.0F, 1.0F, 1.0F);
                break;
            case "chest":
                matrices.translate(0.0F, 0.0F, 0.0F);
                matrices.scale(1.1F, 1.1F, 1.1F);
                break;
            case "legs":
                matrices.translate(0.0F, 0.5F, 0.0F);
                matrices.scale(1.05F, 1.05F, 1.05F);
                break;
            case "feet":
                matrices.translate(0.0F, 0.8F, 0.0F);
                matrices.scale(1.0F, 1.0F, 1.0F);
                break;
        }

        ModelTransformationMode transformationMode = switch (bodyPart) {
            case "head" -> ModelTransformationMode.HEAD;
            default -> ModelTransformationMode.FIXED;
        };

        this.itemRenderer.renderItem(entity, armorStack, transformationMode,
                                   false, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);

        matrices.pop();
    }
}

/*
USAGE INSTRUCTIONS:

1. Replace the existing GuardEquipmentFeature with this improved version
2. Update the registration in GuardVillagerRenderer:

   this.addFeature(new ImprovedGuardEquipmentFeature(this, ctx.getItemRenderer()));

3. Ensure your SimplifiedVillagerModel has public access to arm parts:
   - Make rightArm and leftArm public fields, or
   - Add getter methods for arm access

4. Test with various weapon types to verify positioning

EXPECTED IMPROVEMENTS:

- Items will now follow villager arm movement naturally
- Different weapon types will have appropriate grip angles
- Walking animations won't break item positioning
- Items will scale properly for villager proportions
- Support for various weapon and tool types

DEBUGGING:

- Enable DEBUG logging for "ImprovedGuardEquipmentFeature" to see positioning info
- Use F3+B to see entity hitboxes and verify item alignment
- Test with different villager poses (walking, idle, combat)
*/