package com.xeenaa.villagermanager.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;

/**
 * Simplified villager model without overlay/blending logic.
 * This model renders a single texture without biome/level overlays.
 * Implements ModelWithArms to support held item rendering.
 */
@Environment(EnvType.CLIENT)
public class SimplifiedVillagerModel extends EntityModel<VillagerEntity> implements ModelWithArms {
    public final ModelPart root;
    public final ModelPart head;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public SimplifiedVillagerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    /**
     * Creates the model data for a simplified villager model.
     * Based on VillagerEntityModel but without overlay support.
     */
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Head - proper villager head UV mapping
        modelPartData.addChild("head", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F)
                .uv(32, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), // Hat layer
                ModelTransform.NONE);

        // Body - villager robe with multiple layers to prevent transparency
        modelPartData.addChild("body", ModelPartBuilder.create()
                .uv(16, 20).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)    // Base body
                .uv(0, 38).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F)     // Robe layer (longer)
                .uv(16, 32).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)    // Chest layer 1
                .uv(32, 32).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)    // Chest layer 2 (fill stomach)
                .uv(0, 20).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F),    // Extra layer to prevent transparency
                ModelTransform.NONE);

        // Right arm
        modelPartData.addChild("right_arm", ModelPartBuilder.create()
                .uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .uv(40, 32).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), // Sleeve
                ModelTransform.of(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        // Left arm
        modelPartData.addChild("left_arm", ModelPartBuilder.create()
                .uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), // Sleeve
                ModelTransform.of(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        // Right leg
        modelPartData.addChild("right_leg", ModelPartBuilder.create()
                .uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .uv(0, 32).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), // Pants
                ModelTransform.of(-1.9F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        // Left leg
        modelPartData.addChild("left_leg", ModelPartBuilder.create()
                .uv(16, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .uv(0, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), // Pants
                ModelTransform.of(1.9F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        // Return model data for a single 64x64 texture (no overlays)
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(VillagerEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Basic villager animations
        this.head.yaw = headYaw * 0.017453292F; // Convert degrees to radians
        this.head.pitch = headPitch * 0.017453292F;

        // Simple walking animation for legs
        this.rightLeg.pitch = (float) (Math.cos(limbAngle * 0.6662F) * 1.4F * limbDistance);
        this.leftLeg.pitch = (float) (Math.cos(limbAngle * 0.6662F + Math.PI) * 1.4F * limbDistance);

        // Arm animations - base walking animation
        this.rightArm.pitch = (float) (Math.cos(limbAngle * 0.6662F + Math.PI) * 2.0F * limbDistance * 0.5F);
        this.leftArm.pitch = (float) (Math.cos(limbAngle * 0.6662F) * 2.0F * limbDistance * 0.5F);

        // Attack animation - swing the right arm when attacking
        if (entity.handSwingProgress > 0.0F) {
            // Calculate swing progress (0.0 to 1.0)
            float swingProgress = entity.getHandSwingProgress(animationProgress);

            // Determine which arm is swinging
            net.minecraft.util.Arm arm = entity.preferredHand == net.minecraft.util.Hand.MAIN_HAND
                ? entity.getMainArm()
                : entity.getMainArm().getOpposite();

            ModelPart swingingArm = arm == net.minecraft.util.Arm.RIGHT ? this.rightArm : this.leftArm;

            // Animate the swing - raise arm up and swing down
            float swingAngle = net.minecraft.util.math.MathHelper.sin(swingProgress * (float) Math.PI);
            float swingRotation = net.minecraft.util.math.MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float) Math.PI);

            swingingArm.roll = 0.0F;
            swingingArm.yaw = -(0.1F - swingAngle * 0.6F);
            swingingArm.pitch = -1.5707964F; // -90 degrees - raise arm up
            swingingArm.pitch -= swingAngle * 1.2F - swingRotation * 0.4F; // Swing motion
        } else {
            // Reset arm rotations when not attacking
            this.rightArm.roll = 0.0F;
            this.rightArm.yaw = 0.0F;
            this.leftArm.roll = 0.0F;
            this.leftArm.yaw = 0.0F;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // Render all model parts individually to ensure proper geometry
        this.head.render(matrices, vertices, light, overlay);
        this.body.render(matrices, vertices, light, overlay);
        this.rightArm.render(matrices, vertices, light, overlay);
        this.leftArm.render(matrices, vertices, light, overlay);
        this.rightLeg.render(matrices, vertices, light, overlay);
        this.leftLeg.render(matrices, vertices, light, overlay);
    }

    /**
     * Copies the pose from another SimplifiedVillagerModel to this model.
     */
    public void copyStateTo(SimplifiedVillagerModel target) {
        target.head.copyTransform(this.head);
        target.body.copyTransform(this.body);
        target.rightArm.copyTransform(this.rightArm);
        target.leftArm.copyTransform(this.leftArm);
        target.rightLeg.copyTransform(this.rightLeg);
        target.leftLeg.copyTransform(this.leftLeg);
    }

    /**
     * Required by ModelWithArms interface - sets the angle of both arms.
     * This is used by HeldItemFeatureRenderer to position held items correctly.
     */
    @Override
    public void setArmAngle(net.minecraft.util.Arm arm, MatrixStack matrices) {
        ModelPart modelPart = this.getArm(arm);
        modelPart.rotate(matrices);
    }

    /**
     * Helper method to get the arm model part based on which arm (left or right).
     */
    private ModelPart getArm(net.minecraft.util.Arm arm) {
        return arm == net.minecraft.util.Arm.LEFT ? this.leftArm : this.rightArm;
    }
}