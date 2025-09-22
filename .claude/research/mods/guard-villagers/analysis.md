# Guard Villagers Mod - Technical Analysis

## Visual Implementation Overview

The Guard Villagers mod solves the "purple villager" issue by implementing a completely custom entity type rather than modifying villager professions. This is a key architectural decision that avoids conflicts with Minecraft's profession system.

## Model Implementation (GuardModel.java)

### Core Architecture
- **Extends**: `HumanoidModel<Guard>` - Uses Minecraft's standard humanoid model as base
- **Custom Entity**: Creates separate `Guard` entity type instead of modifying `Villager`
- **Model Parts**: Adds custom components like "Nose", "quiver", and shoulder pads

### Key Model Features
```java
// Custom model parts added to standard humanoid model
- Nose (maintains villager-like appearance)
- Quiver (for projectile weapons)
- Shoulder pads (contextual armor indication)
```

### Animation System
The mod implements sophisticated animation handling:

1. **Equipment-Based Animations**:
   - Different poses for sword vs crossbow holding
   - Eating/drinking animations
   - Shield holding positions

2. **Contextual Visual Elements**:
   - Quiver visibility based on weapon type
   - Shoulder pad rendering when wearing chest armor
   - Dynamic equipment positioning

3. **Mod Compatibility**:
   - Special handling for modded weapons (e.g., "musketmod")
   - Extensible animation system for custom equipment

## Texture and Rendering Approach

### Base Appearance Strategy
- **NOT a villager profession**: Avoids Minecraft's profession texture system entirely
- **Custom entity textures**: Uses dedicated texture files for guard entities
- **Equipment overlay system**: Renders armor and weapons as separate layers

### Visual Customization Options
1. **Equipment Slots**:
   - Main hand: Sword, crossbow, or custom weapons
   - Off hand: Shield or food items
   - Armor slots: Full armor set support
   - Head, chest, legs, feet armor rendering

2. **Texture Variations**:
   - Base guard texture (villager-like but distinct)
   - Equipment-specific overlays
   - Compatibility with resource packs

## Key Technical Decisions

### Why This Approach Works
1. **Avoids Profession Conflicts**: By creating a separate entity, doesn't interfere with villager profession textures
2. **Full Equipment Support**: Complete armor and weapon rendering system
3. **Mod Compatibility**: Works alongside other villager-modifying mods
4. **Resource Pack Support**: Allows texture customization without breaking functionality

### Implementation Benefits
- No "purple villager" issues because it's not using villager profession system
- Full control over appearance and animations
- Maintains villager-like appearance while being distinct
- Professional-grade equipment rendering

## Resource Pack Integration

### Compatible Texture Packs
1. **Freshly Modded (F.M.R.P)**:
   - Replaces guard model with Steve model
   - Requires Entity Texture Features (ETF) and Entity Model Features (EMF)
   - Note: Has known issues with crossbow wielding

2. **Guards Humans**:
   - Provides human appearance variations
   - 7 different human models
   - Configurable through mod settings

### Technical Requirements for Resource Packs
- Entity Texture Features 7.0.1+
- Entity Model Features 3.0.1+
- OptiFine NOT supported (uses different entity system)

## Recommendations for Implementation

### For Solving Purple Villager Issue
1. **Create Custom Entity**: Don't modify villager professions directly
2. **Separate Model System**: Use custom model class extending HumanoidModel
3. **Equipment Rendering**: Implement proper armor/weapon overlay system
4. **Animation Support**: Add contextual animations for different equipment types

### Architecture Patterns to Follow
1. Custom entity registration
2. Dedicated model and renderer classes
3. Equipment-aware animation system
4. Resource pack compatibility layer
5. Mod integration support

This approach completely sidesteps the villager profession texture issues while providing a robust, customizable guard system.