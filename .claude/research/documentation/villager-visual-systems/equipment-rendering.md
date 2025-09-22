# Equipment and Visual Customization Systems for Guard Villagers

## Overview

This document analyzes how successful guard villager mods handle equipment rendering and visual customization, providing insights for implementing proper guard appearance in Minecraft 1.21.1 Fabric.

## Core Problem: Purple Villager Issue

### The Problem
When assigning a guard profession to a villager, the model turns violet/purple instead of displaying proper guard appearance. This indicates:
1. Missing or incorrect profession texture mapping
2. Fallback to default "missing texture" state
3. Conflict between profession system and visual rendering

### Root Cause Analysis
- Minecraft's villager profession system expects specific texture files
- Custom professions without proper texture assets default to placeholder colors
- The profession texture system is separate from equipment rendering

## Successful Implementation Approaches

### 1. Custom Entity Approach (Guard Villagers Mod)

**Strategy**: Create separate guard entity instead of modifying villager professions

**Benefits**:
- Complete control over appearance and behavior
- No conflicts with villager profession system
- Full equipment and animation support
- Maintains villager-like appearance while being distinct

**Implementation Details**:
```java
// Custom entity extending standard humanoid model
public class GuardModel extends HumanoidModel<Guard> {
    // Custom model parts
    private final ModelPart nose;
    private final ModelPart quiver;
    private final ModelPart shoulderPads;

    // Equipment-aware animation system
    public void setupAnim(Guard entity, float limbSwing, ...) {
        // Handle different weapon poses
        // Manage equipment visibility
        // Apply contextual animations
    }
}
```

### 2. Equipment Rendering System

**Multi-Layer Rendering**:
1. **Base Entity Model**: Core villager-like appearance
2. **Armor Overlays**: Separate rendering for each armor piece
3. **Weapon Positioning**: Dynamic weapon and shield placement
4. **Contextual Elements**: Quivers, pouches, and accessories

**Equipment Slots Supported**:
- Main Hand: Weapons (sword, crossbow, bow, modded weapons)
- Off Hand: Shields, food items, secondary weapons
- Armor Slots: Helmet, chestplate, leggings, boots
- Cosmetic Slots: Capes, badges, rank indicators

### 3. Animation and Pose System

**Dynamic Animations**:
- **Idle Poses**: Different stances based on equipment
- **Combat Ready**: Alert posture with weapon drawn
- **Patrol State**: Walking animation with equipment
- **Interaction**: Eating, drinking, equipment handling

**Equipment-Specific Animations**:
```java
// Example animation logic
if (isHoldingCrossbow()) {
    // Crossbow holding pose
    rightArm.xRot = -0.97079635F;
    leftArm.xRot = -0.97079635F;
    showQuiver = true;
} else if (isHoldingSword()) {
    // Sword ready pose
    rightArm.xRot = -1.2F;
    showQuiver = false;
}
```

## Visual Customization Features

### 1. Equipment Management Interface

**Player Interaction**:
- Right-click guard to open equipment GUI
- Drag-and-drop armor and weapons
- Visual preview of equipment changes
- Equipment stats and effects display

**Equipment Categories**:
1. **Weapons**: Swords, crossbows, bows, modded weapons
2. **Armor**: Full set support with visual rendering
3. **Accessories**: Shields, food, utility items
4. **Cosmetics**: Banners, badges, rank indicators

### 2. Texture and Model Compatibility

**Resource Pack Support**:
- Entity Texture Features (ETF) compatibility
- Entity Model Features (EMF) support
- Custom model variations (human guards, armored guards)
- Texture pack override capabilities

**Model Variations**:
```
guard_variants/
├── default_guard.json      # Standard villager-like guard
├── human_guard.json        # Human appearance
├── armored_guard.json      # Heavily armored variant
└── archer_guard.json       # Specialized archer appearance
```

## Technical Implementation Considerations

### 1. Fabric 1.21.1 Specific Requirements

**Entity Registration**:
```java
// Register custom guard entity
EntityType<GuardEntity> GUARD = FabricEntityTypeBuilder
    .create(SpawnGroup.CREATURE, GuardEntity::new)
    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
    .build();
```

**Model Registration**:
```java
// Client-side model registration
EntityModelLayerRegistry.registerModelLayer(GUARD_LAYER, GuardModel::getTexturedModelData);
EntityRendererRegistry.register(GUARD, GuardRenderer::new);
```

### 2. Equipment Rendering Architecture

**Layer-Based Rendering**:
1. **Base Layer**: Entity model and texture
2. **Armor Layers**: Individual armor piece rendering
3. **Held Item Layer**: Weapon and tool rendering
4. **Effect Layer**: Enchantment glints, status effects

**Performance Optimization**:
- LOD (Level of Detail) system for distant guards
- Equipment culling when not visible
- Batch rendering for multiple guards
- Texture atlas optimization

### 3. Mod Compatibility Systems

**Equipment Integration**:
- Automatic detection of modded weapons and armor
- Dynamic texture loading for custom equipment
- API for other mods to register guard equipment
- Compatibility with popular equipment mods

**Resource Pack Integration**:
```java
// Example texture override system
public ResourceLocation getGuardTexture(GuardEntity guard) {
    // Check for resource pack overrides
    // Fall back to mod defaults
    // Support texture variations
}
```

## Recommendations for Implementation

### 1. Immediate Solution for Purple Villager Issue

**Option A: Custom Guard Entity** (Recommended)
- Create separate guard entity type
- Implement custom model and renderer
- Full equipment support from start
- Future-proof architecture

**Option B: Profession Texture Fix** (Quick Fix)
- Add proper texture assets for guard profession
- Implement basic equipment overlay
- Limited customization options
- May conflict with other profession mods

### 2. Equipment System Implementation

**Phase 1: Basic Equipment**
- Sword and crossbow support
- Basic armor rendering
- Simple animation system

**Phase 2: Advanced Features**
- Full equipment GUI
- Multiple weapon types
- Complex animations
- Resource pack support

**Phase 3: Customization**
- Multiple guard types
- Rank and specialty systems
- Advanced visual effects
- Mod integration APIs

### 3. Performance and Compatibility

**Optimization Strategies**:
- Use existing Minecraft rendering pipelines
- Implement equipment caching
- Minimize texture switching
- Optimize for large villages

**Compatibility Measures**:
- Test with popular villager mods
- Ensure resource pack compatibility
- Provide migration tools for existing worlds
- Document mod interaction guidelines

This comprehensive equipment and visual customization system provides the foundation for creating professional-grade guard villagers that solve the purple villager issue while offering extensive customization options.