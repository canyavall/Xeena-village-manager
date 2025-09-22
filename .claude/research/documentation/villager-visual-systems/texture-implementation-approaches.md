# Texture Implementation Approaches for Guard Villagers - Minecraft 1.21.1 Fabric

## Overview

This document analyzes different approaches to implementing guard villager textures and models in Minecraft 1.21.1 Fabric, based on research of successful guard villager mods and their visual implementations.

## Core Texture Strategies

### 1. Custom Entity Approach (Recommended)

**Description**: Create a completely separate guard entity with its own textures and models.

**Implementation**:
```
assets/mod_id/textures/entity/guard/
├── guard_base.png              # Base guard texture
├── guard_variants/
│   ├── guard_iron.png         # Iron-equipped variant
│   ├── guard_leather.png      # Leather-equipped variant
│   └── guard_archer.png       # Archer specialist
└── equipment/
    ├── armor_overlays/        # Armor rendering overlays
    └── weapon_overlays/       # Weapon attachment points
```

**Benefits**:
- Complete control over appearance
- No profession system conflicts
- Full equipment support
- Professional mod compatibility

**Drawbacks**:
- More complex implementation
- Requires new entity registration
- Additional memory usage

### 2. Profession Texture Override

**Description**: Create proper texture assets for the guard profession within the villager system.

**Implementation**:
```
assets/mod_id/textures/entity/villager/profession/
├── guard.png                  # Main guard profession texture
├── guard_level_1.png         # Novice guard
├── guard_level_2.png         # Apprentice guard
├── guard_level_3.png         # Journeyman guard
├── guard_level_4.png         # Expert guard
└── guard_level_5.png         # Master guard
```

**Benefits**:
- Simple implementation
- Works within existing villager system
- Automatic trade level progression
- Lower memory footprint

**Drawbacks**:
- Limited equipment customization
- Potential conflicts with other profession mods
- Less flexible visual system

### 3. Hybrid Approach

**Description**: Use profession textures as base with equipment overlay system.

**Implementation**:
```
assets/mod_id/textures/entity/villager/
├── profession/
│   └── guard.png             # Base guard profession
└── equipment_overlays/
    ├── helmet_overlay.png    # Equipment overlays
    ├── chestplate_overlay.png
    ├── sword_overlay.png
    └── crossbow_overlay.png
```

## Texture Resolution and Standards

### Standard Minecraft Texture Specifications

**Villager Texture Dimensions**:
- **Resolution**: 64x64 pixels
- **Body Parts**: Head, hat, body, arms, legs
- **Format**: PNG with transparency support
- **Color Depth**: 32-bit RGBA

**Equipment Overlay Specifications**:
- **Armor Overlays**: Match body part dimensions
- **Weapon Attachments**: Variable size based on weapon
- **Transparency**: Essential for proper layering

### Texture Mapping Layout

**Standard Villager UV Mapping**:
```
Villager Texture Layout (64x64):
┌─────────────────────────────────────────────────────────────────┐
│ Head (front) │ Head (right) │ Head (back)  │ Head (left)  │ Hat │
│ 8x8 at 8,8   │ 8x8 at 0,8   │ 8x8 at 24,8  │ 8x8 at 16,8  │ ... │
├─────────────────────────────────────────────────────────────────┤
│ Body (front) │ Body (right) │ Body (back)  │ Body (left)  │     │
│ 8x12 at 20,20│ 4x12 at 16,20│ 8x12 at 32,20│ 4x12 at 28,20│     │
├─────────────────────────────────────────────────────────────────┤
│ Arms and Legs following similar pattern...                      │
└─────────────────────────────────────────────────────────────────┘
```

## Model Implementation Approaches

### 1. Custom Entity Model (GuardModel.java Pattern)

**Based on Guard Villagers mod implementation**:
```java
public class GuardModel extends HumanoidModel<GuardEntity> {
    private final ModelPart nose;           // Villager-like nose
    private final ModelPart quiver;         // Equipment: arrow quiver
    private final ModelPart shoulderPads;   // Armor indication
    private final ModelPart cape;           // Rank/status indicator

    @Override
    public void setupAnim(GuardEntity entity, float limbSwing, ...) {
        super.setupAnim(entity, limbSwing, ...);

        // Equipment-specific animations
        if (entity.isHoldingCrossbow()) {
            setupCrossbowAnim();
            quiver.visible = true;
        } else if (entity.isHoldingSword()) {
            setupSwordAnim();
            quiver.visible = false;
        }

        // Armor-based modifications
        shoulderPads.visible = entity.hasChestArmor();
    }
}
```

### 2. Enhanced Villager Model

**Extending existing villager model**:
```java
public class EnhancedVillagerModel extends VillagerModel<VillagerEntity> {
    private final ModelPart equipmentLayer;
    private final Map<EquipmentSlot, ModelPart> equipmentParts;

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, ...) {
        super.renderToBuffer(poseStack, buffer, ...);

        // Render equipment overlays
        if (isGuardProfession()) {
            renderEquipmentLayer(poseStack, buffer, ...);
        }
    }
}
```

## Resource Pack Compatibility

### Entity Texture Features (ETF) Integration

**Configuration for Resource Pack Support**:
```json
{
  "name": "Guard Villager Textures",
  "description": "Custom textures for guard villagers",
  "model": "minecraft:villager",
  "texture": "guard_villagers:textures/entity/guard/guard_base.png",
  "shadowSize": 0.5,
  "rules": [
    {
      "index": 0,
      "basePath": "optifine/random/entity/guard/",
      "textures": [
        "guard1.png",
        "guard2.png",
        "guard3.png"
      ]
    }
  ]
}
```

### Entity Model Features (EMF) Support

**Model Variations**:
```json
{
  "textureSize": [64, 64],
  "models": [
    {
      "id": "guard_base",
      "part": "body",
      "boxes": [
        {
          "coordinates": [-4, 0, -2, 8, 12, 4],
          "textureOffset": [16, 20]
        }
      ]
    }
  ]
}
```

## Implementation Recommendations for Minecraft 1.21.1 Fabric

### Phase 1: Basic Guard Appearance

**Immediate Solution for Purple Villager Issue**:
1. Create basic guard profession texture
2. Register texture in villager profession system
3. Implement basic equipment overlay

**Files Required**:
```
src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/
└── guard.png
```

**Registration Code**:
```java
// In client initialization
VillagerProfessionTextureRegistry.register(
    CustomProfessions.GUARD,
    new Identifier("xeenaa_villager_manager", "textures/entity/villager/profession/guard.png")
);
```

### Phase 2: Equipment System

**Enhanced Visual System**:
1. Implement equipment overlay rendering
2. Add dynamic texture switching
3. Support for multiple equipment types

**Architecture**:
```java
public class GuardEquipmentRenderer {
    private static final Map<Item, ResourceLocation> WEAPON_OVERLAYS = Map.of(
        Items.IRON_SWORD, new ResourceLocation("...", "sword_overlay.png"),
        Items.CROSSBOW, new ResourceLocation("...", "crossbow_overlay.png")
    );

    public void renderEquipment(GuardEntity guard, PoseStack pose, ...) {
        ItemStack mainHand = guard.getMainHandItem();
        if (!mainHand.isEmpty()) {
            ResourceLocation overlay = WEAPON_OVERLAYS.get(mainHand.getItem());
            if (overlay != null) {
                renderOverlay(overlay, pose, ...);
            }
        }
    }
}
```

### Phase 3: Advanced Customization

**Professional-Grade Features**:
1. Multiple guard variants
2. Rank and specialization systems
3. Dynamic texture generation
4. Mod compatibility APIs

## Performance Considerations

### Texture Optimization

**Memory Management**:
- Use texture atlasing for equipment overlays
- Implement texture LOD for distant entities
- Cache commonly used texture combinations
- Unload unused variant textures

**Rendering Optimization**:
```java
public class OptimizedGuardRenderer extends EntityRenderer<GuardEntity> {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    @Override
    public ResourceLocation getTextureLocation(GuardEntity entity) {
        String textureKey = generateTextureKey(entity);
        return TEXTURE_CACHE.computeIfAbsent(textureKey, this::loadTexture);
    }
}
```

### Batching and Culling

**Rendering Efficiency**:
- Batch render multiple guards with same equipment
- Implement frustum culling for equipment details
- Use distance-based detail reduction
- Optimize texture binding and switching

## Testing and Validation

### Compatibility Testing

**Required Test Cases**:
1. Resource pack compatibility (ETF/EMF)
2. Other villager mod interactions
3. Equipment mod integration
4. Performance with large villages
5. Memory usage monitoring

**Visual Validation**:
- Texture alignment verification
- Animation smoothness testing
- Equipment positioning accuracy
- LOD transition quality

This comprehensive texture implementation guide provides the foundation for creating professional-quality guard villager visuals that solve the purple villager issue while maintaining high performance and mod compatibility.