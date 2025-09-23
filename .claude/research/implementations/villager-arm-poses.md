# Villager Arm Pose Implementation Guide

## Overview
This document provides specific implementation details for creating proper arm poses for guard villagers holding weapons in Minecraft 1.21.1.

## Core Problem
Villagers by default have crossed arms, but guards need extended arms to properly hold weapons. This requires:
1. Dynamic arm pose detection
2. Proper item positioning relative to extended arms
3. Smooth transitions between poses

## Implementation Strategy

### 1. Enhanced SimplifiedVillagerModel

#### Add Pose State Management
```java
public class SimplifiedVillagerModel extends EntityModel<VillagerEntity> {
    // ... existing fields ...

    private boolean isInCombatPose = false;
    private float combatPoseTransition = 0.0F;

    // Pose transition speeds
    private static final float POSE_TRANSITION_SPEED = 0.1F;

    public void setAngles(VillagerEntity entity, float limbAngle, float limbDistance,
                         float animationProgress, float headYaw, float headPitch) {

        // Update head rotation
        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;

        // Determine if villager should be in combat pose
        boolean shouldBeInCombatPose = shouldUseCombatPose(entity);

        // Smooth transition between poses
        if (shouldBeInCombatPose && !isInCombatPose) {
            combatPoseTransition = Math.min(1.0F, combatPoseTransition + POSE_TRANSITION_SPEED);
            if (combatPoseTransition >= 1.0F) {
                isInCombatPose = true;
            }
        } else if (!shouldBeInCombatPose && isInCombatPose) {
            combatPoseTransition = Math.max(0.0F, combatPoseTransition - POSE_TRANSITION_SPEED);
            if (combatPoseTransition <= 0.0F) {
                isInCombatPose = false;
            }
        }

        // Apply pose based on transition state
        if (combatPoseTransition > 0.0F) {
            applyCombatPose(entity, limbAngle, limbDistance, combatPoseTransition);
        } else {
            applyDefaultPose(entity, limbAngle, limbDistance);
        }

        // Always apply leg animations
        applyLegAnimations(limbAngle, limbDistance);
    }

    private boolean shouldUseCombatPose(VillagerEntity entity) {
        // Check if this villager is a guard with equipment
        ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
        GuardData guardData = cache.getGuardData(entity);

        if (guardData == null) {
            return false;
        }

        // Check if guard has a weapon equipped
        ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
        return !weapon.isEmpty();
    }

    private void applyCombatPose(VillagerEntity entity, float limbAngle, float limbDistance, float transition) {
        // Target pose values for combat stance
        float rightArmPitchTarget = -0.4F;  // Slightly forward
        float leftArmPitchTarget = -0.2F;   // Less forward
        float rightArmRollTarget = 0.15F;   // Slight outward tilt
        float leftArmRollTarget = -0.15F;   // Slight inward tilt

        // Walking motion adjustments
        float rightArmWalkMotion = (float)(Math.cos(limbAngle * 0.6662F + Math.PI) * 0.3F * limbDistance);
        float leftArmWalkMotion = (float)(Math.cos(limbAngle * 0.6662F) * 0.3F * limbDistance);

        // Interpolate between default and combat pose
        this.rightArm.pitch = MathHelper.lerp(transition,
            (float)(Math.cos(limbAngle * 0.6662F + Math.PI) * 2.0F * limbDistance * 0.5F), // Default pose
            rightArmPitchTarget + rightArmWalkMotion); // Combat pose

        this.leftArm.pitch = MathHelper.lerp(transition,
            (float)(Math.cos(limbAngle * 0.6662F) * 2.0F * limbDistance * 0.5F), // Default pose
            leftArmPitchTarget + leftArmWalkMotion); // Combat pose

        // Set arm roll (only in combat pose)
        this.rightArm.roll = transition * rightArmRollTarget;
        this.leftArm.roll = transition * leftArmRollTarget;

        // Keep arms straight (no yaw)
        this.rightArm.yaw = 0.0F;
        this.leftArm.yaw = 0.0F;
    }

    private void applyDefaultPose(VillagerEntity entity, float limbAngle, float limbDistance) {
        // Standard villager arm animations (crossed arms with walking motion)
        this.rightArm.pitch = (float)(Math.cos(limbAngle * 0.6662F + Math.PI) * 2.0F * limbDistance * 0.5F);
        this.leftArm.pitch = (float)(Math.cos(limbAngle * 0.6662F) * 2.0F * limbDistance * 0.5F);
        this.rightArm.yaw = 0.0F;
        this.leftArm.yaw = 0.0F;
        this.rightArm.roll = 0.0F;
        this.leftArm.roll = 0.0F;
    }

    private void applyLegAnimations(float limbAngle, float limbDistance) {
        // Standard walking animation for legs (unchanged)
        this.rightLeg.pitch = (float)(Math.cos(limbAngle * 0.6662F) * 1.4F * limbDistance);
        this.leftLeg.pitch = (float)(Math.cos(limbAngle * 0.6662F + Math.PI) * 1.4F * limbDistance);
    }

    // Getter for equipment feature to know current pose state
    public boolean isInCombatPose() {
        return combatPoseTransition > 0.5F;
    }

    public float getCombatPoseTransition() {
        return combatPoseTransition;
    }
}
```

### 2. Updated GuardEquipmentFeature

#### Arm-Relative Item Positioning
```java
private void renderHandItem(VillagerEntity entity, ItemStack itemStack, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, boolean isMainHand) {

    matrices.push();

    SimplifiedVillagerModel model = this.getContextModel();

    // Apply the arm's current transformation to get proper positioning
    if (isMainHand) {
        // Move to right arm position and apply its rotation
        matrices.translate(-5.0F / 16.0F, 2.0F / 16.0F, 0.0F); // Arm joint position
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(model.rightArm.pitch));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(model.rightArm.yaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(model.rightArm.roll));

        // Position relative to hand (end of arm)
        matrices.translate(0.0F, 10.0F / 16.0F, 0.0F); // Hand position at end of arm

        // Item orientation
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        // Weapon-specific adjustments
        if (itemStack.getItem() instanceof SwordItem) {
            matrices.translate(0.0F, 0.0F, -0.0625F); // Slight forward adjustment
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(15.0F)); // Natural grip angle
        }

    } else {
        // Mirror logic for left arm
        matrices.translate(5.0F / 16.0F, 2.0F / 16.0F, 0.0F); // Left arm joint
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(model.leftArm.pitch));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(model.leftArm.yaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(model.leftArm.roll));

        matrices.translate(0.0F, 10.0F / 16.0F, 0.0F); // Hand position

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        // Shield-specific adjustments
        if (itemStack.getItem() instanceof ShieldItem) {
            matrices.translate(0.0F, 0.0F, 0.0625F); // Slightly back
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F)); // Defensive angle
        }
    }

    // Scale for villager hand size
    matrices.scale(0.375F, 0.375F, 0.375F);

    // Render item
    ModelTransformationMode mode = isMainHand ?
        ModelTransformationMode.THIRD_PERSON_RIGHT_HAND :
        ModelTransformationMode.THIRD_PERSON_LEFT_HAND;

    this.itemRenderer.renderItem(entity, itemStack, mode,
                               false, matrices, vertexConsumers, entity.getWorld(),
                               light, OverlayTexture.DEFAULT_UV, 0);

    matrices.pop();
}
```

### 3. Combat State Detection

#### Enhanced Guard Data Integration
```java
// Add to GuardData class
public boolean isInCombatStance() {
    // Could be based on:
    // - Has weapon equipped
    // - Is targeting enemies
    // - Recent combat activity
    // - Manual stance setting

    return !getEquipment(EquipmentSlot.WEAPON).isEmpty();
}

public boolean shouldExtendArms() {
    // More specific check for when arms should be extended
    ItemStack weapon = getEquipment(EquipmentSlot.WEAPON);
    ItemStack shield = getEquipment(EquipmentSlot.SHIELD);

    return !weapon.isEmpty() || !shield.isEmpty();
}
```

## Alternative Approach: Mixin for VillagerEntityModel

If the SimplifiedVillagerModel approach doesn't work well, you can mixin directly into the vanilla VillagerEntityModel:

```java
@Mixin(VillagerEntityModel.class)
public class VillagerModelArmPoseMixin {

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void adjustGuardArmPose(VillagerEntity villager, float f, float g, float h, float i, float j, CallbackInfo ci) {
        // Check if this is a guard villager
        if (isGuardVillager(villager)) {
            VillagerEntityModel model = (VillagerEntityModel)(Object)this;

            // Access arm parts via reflection or accessor mixins
            ModelPart rightArm = ((VillagerModelAccessor)model).getRightArm();
            ModelPart leftArm = ((VillagerModelAccessor)model).getLeftArm();

            // Apply combat pose
            rightArm.pitch = -0.4F;
            rightArm.roll = 0.15F;
            leftArm.pitch = -0.2F;
            leftArm.roll = -0.15F;
        }
    }

    private boolean isGuardVillager(VillagerEntity villager) {
        return villager.getVillagerData().getProfession() == ModProfessions.GUARD;
    }
}

// Accessor interface
@Mixin(VillagerEntityModel.class)
public interface VillagerModelAccessor {
    @Accessor("rightArm")
    ModelPart getRightArm();

    @Accessor("leftArm")
    ModelPart getLeftArm();
}
```

## Testing Strategy

### 1. Visual Testing
- Spawn guard villagers with different weapons
- Test walking animations with extended arms
- Verify item positioning at different angles
- Check for clipping issues

### 2. Performance Testing
- Monitor frame rate with multiple armed guards
- Test pose transition smoothness
- Verify memory usage stability

### 3. Integration Testing
- Ensure compatibility with existing equipment system
- Test with different item types (swords, axes, shields)
- Verify behavior in different scenarios (combat, idle, walking)

## Known Issues and Solutions

### Issue: Arms snap between poses
**Solution**: Implement gradual transition with `combatPoseTransition` float

### Issue: Items don't follow arm movement
**Solution**: Use arm-relative positioning with proper matrix transformations

### Issue: Walking animation breaks item positioning
**Solution**: Apply walking motion to combat pose target values

### Issue: Performance impact with many guards
**Solution**: Only update pose when equipment changes, cache pose state

## Future Enhancements

1. **Context-Aware Poses**: Different poses for patrolling vs combat vs idle
2. **Weapon-Specific Poses**: Special poses for bows, crossbows, two-handed weapons
3. **Animation Blending**: Smooth transitions between multiple pose states
4. **Player Interaction Poses**: Special poses when trading or being configured