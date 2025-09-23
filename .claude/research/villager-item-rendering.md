# Villager Item Rendering Research - Minecraft 1.21.1

## Problem Context
The project needs guard villagers to properly hold weapons (swords) in their hands with correct arm poses and positioning. The current implementation has positioning issues that need to be resolved.

## Minecraft Villager Arm System Overview

### 1. Villager Arm Poses
- **Default State**: Villagers have crossed arms as their iconic pose
- **Trading State**: Arms may extend when trading (though traditionally they remain crossed)
- **Custom States**: For guards holding weapons, we need extended arm poses

### 2. VillagerEntityModel Structure
Based on analysis of the SimplifiedVillagerModel in the project:

```java
// Arm positioning in villager model
// Right arm: positioned at (-5.0F, 2.0F, 0.0F) - left side of villager body
// Left arm: positioned at (5.0F, 2.0F, 0.0F) - right side of villager body
// Note: Minecraft coordinates are from the villager's perspective
```

### 3. Current Implementation Analysis

From `GuardEquipmentFeature.java`, the current item positioning:

```java
// Main hand (right arm) - current implementation
matrices.translate(-0.3125F, 0.25F, 0.0625F);
matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

// Off hand (left arm) - current implementation
matrices.translate(0.3125F, 0.25F, 0.0625F);
matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
```

## Research Findings

### 1. Proper Item Positioning for Villagers

#### Recommended Transformations for Weapon Holding:

**Main Hand (Sword/Primary Weapon):**
```java
// Position relative to right arm joint
matrices.translate(-0.0625F, 0.375F, 0.0625F);  // Closer to hand center
matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));  // Point sword forward
matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));  // Correct orientation
matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(15.0F));   // Slight tilt for natural grip
```

**Off Hand (Shield/Secondary):**
```java
// Position relative to left arm joint
matrices.translate(0.0625F, 0.375F, 0.0625F);   // Mirror of main hand
matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F));  // Opposite tilt
```

### 2. Arm Pose Modifications

To make guards look more natural while holding weapons, the villager model needs arm pose adjustments:

```java
// For guard villagers in combat stance
public void setGuardPose(VillagerEntity entity) {
    if (isGuardVillager(entity) && hasWeapon(entity)) {
        // Extend right arm forward (holding weapon)
        this.rightArm.pitch = -0.5F;  // Slightly forward
        this.rightArm.yaw = 0.0F;     // Straight
        this.rightArm.roll = 0.1F;    // Slight outward tilt

        // Position left arm for balance/shield
        this.leftArm.pitch = -0.3F;   // Less forward than right
        this.leftArm.yaw = 0.0F;
        this.leftArm.roll = -0.1F;    // Slight inward tilt
    }
}
```

### 3. ModelTransformationMode Considerations

For different item types:
- **Swords**: Use `ModelTransformationMode.THIRD_PERSON_RIGHT_HAND`
- **Shields**: Use `ModelTransformationMode.THIRD_PERSON_LEFT_HAND`
- **Bows**: Requires special handling with both hands
- **Tools**: Use `ModelTransformationMode.THIRD_PERSON_RIGHT_HAND`

### 4. Villager vs Player Model Differences

Key differences when rendering items on villagers:
1. **Arm Length**: Villager arms are shorter than player arms
2. **Body Proportions**: Villagers have wider bodies relative to arm span
3. **Texture Mapping**: Different UV coordinates for hands/arms
4. **Animation System**: Villagers use simpler animation compared to players

## Implementation Recommendations

### 1. Enhanced GuardEquipmentFeature

```java
private void renderHandItem(VillagerEntity entity, ItemStack itemStack, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, boolean isMainHand) {

    matrices.push();

    // Get the model for arm positioning
    SimplifiedVillagerModel model = this.getContextModel();

    // Apply arm transformation first
    if (isMainHand) {
        model.rightArm.rotate(matrices);
        // Fine-tuned position for villager proportions
        matrices.translate(-0.0625F, 0.375F, 0.0625F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        // Add slight tilt for natural weapon grip
        if (itemStack.getItem() instanceof SwordItem) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(15.0F));
        }
    } else {
        model.leftArm.rotate(matrices);
        matrices.translate(0.0625F, 0.375F, 0.0625F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        // Shield positioning
        if (itemStack.getItem() instanceof ShieldItem) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F));
        }
    }

    // Scale for villager proportions
    matrices.scale(0.375F, 0.375F, 0.375F);

    // Render with appropriate transformation mode
    ModelTransformationMode mode = isMainHand ?
        ModelTransformationMode.THIRD_PERSON_RIGHT_HAND :
        ModelTransformationMode.THIRD_PERSON_LEFT_HAND;

    this.itemRenderer.renderItem(entity, itemStack, mode,
                               false, matrices, vertexConsumers, entity.getWorld(),
                               light, OverlayTexture.DEFAULT_UV, 0);

    matrices.pop();
}
```

### 2. Dynamic Arm Poses

Add to `SimplifiedVillagerModel.setAngles()`:

```java
@Override
public void setAngles(VillagerEntity entity, float limbAngle, float limbDistance,
                     float animationProgress, float headYaw, float headPitch) {

    // Base villager animations
    this.head.yaw = headYaw * 0.017453292F;
    this.head.pitch = headPitch * 0.017453292F;

    // Check if this is a guard with equipment
    if (isGuardWithWeapon(entity)) {
        // Set combat-ready pose
        setGuardCombatPose(entity, limbAngle, limbDistance);
    } else {
        // Default crossed-arm pose for regular villagers
        setDefaultVillagerPose(limbAngle, limbDistance);
    }
}

private void setGuardCombatPose(VillagerEntity entity, float limbAngle, float limbDistance) {
    // Extended arms for weapon holding
    this.rightArm.pitch = -0.5F + (float)(Math.cos(limbAngle * 0.6662F + Math.PI) * 0.5F * limbDistance);
    this.rightArm.yaw = 0.0F;
    this.rightArm.roll = 0.1F;

    this.leftArm.pitch = -0.3F + (float)(Math.cos(limbAngle * 0.6662F) * 0.5F * limbDistance);
    this.leftArm.yaw = 0.0F;
    this.leftArm.roll = -0.1F;

    // Leg animations remain the same
    this.rightLeg.pitch = (float)(Math.cos(limbAngle * 0.6662F) * 1.4F * limbDistance);
    this.leftLeg.pitch = (float)(Math.cos(limbAngle * 0.6662F + Math.PI) * 1.4F * limbDistance);
}
```

### 3. Testing Guidelines

**Visual Verification Checklist:**
- [ ] Sword appears in villager's right hand
- [ ] Sword points forward naturally (not awkwardly angled)
- [ ] Shield appears in left hand at appropriate angle
- [ ] Items scale appropriately for villager proportions
- [ ] Arms extend naturally when holding items
- [ ] Walking animation doesn't break item positioning
- [ ] Items render correctly in different lighting conditions

**Performance Verification:**
- [ ] No frame rate drops with multiple armed guards
- [ ] Memory usage remains stable
- [ ] Rendering distance doesn't affect positioning

## Identified Issues and Solutions

### Issue 1: Items floating away from hands
**Cause**: Incorrect translation values not accounting for villager arm proportions
**Solution**: Use arm-relative positioning with `model.rightArm.rotate(matrices)`

### Issue 2: Items pointing in wrong direction
**Cause**: Rotation sequence not matching Minecraft's item transformation system
**Solution**: Follow standard X(-90°) → Y(180°) → Z(tilt) rotation sequence

### Issue 3: Items too large/small
**Cause**: Scale factor not optimized for villager model
**Solution**: Use 0.375F scale factor specifically tuned for villager proportions

### Issue 4: Static arm poses look unnatural
**Cause**: Arms remain in crossed position while holding items
**Solution**: Implement dynamic pose system based on equipment state

## Future Research Areas

1. **Advanced Combat Animations**: Research two-handed weapon poses for larger weapons
2. **Bow Rendering**: Special case for bow and arrow positioning
3. **Tool Integration**: Proper positioning for non-combat tools
4. **Interaction States**: Different poses for different guard activities
5. **Performance Optimization**: Efficient rendering for large numbers of guards

## References

- Minecraft 1.21.1 VillagerEntityModel source analysis
- Fabric API item rendering documentation
- Community research on villager arm poses
- ItemRenderer transformation mode specifications
- ModelTransformationMode best practices for entity rendering