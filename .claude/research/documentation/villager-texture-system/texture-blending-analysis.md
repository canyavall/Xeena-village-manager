# Villager Texture Blending Issue Analysis

## Problem Summary
When assigning a guard profession to villagers in our Minecraft 1.21.1 Fabric mod, the texture appears as a "mix" or "blend" between the villager base texture and our guard texture, instead of showing the pure guard texture.

## Research Findings

### 1. Minecraft Villager Texture System Architecture

Based on research, Minecraft 1.14+ changed the villager texture system by introducing **separate overlay layers** for:

- **Base Texture**: Never seen in-game, serves as foundation
- **Biome Overlays**: desert, jungle, plains, savanna, snow, swamp, taiga
- **Profession Overlays**: armorer, butcher, cartographer, cleric, farmer, fisherman, fletcher, leatherworker, librarian, mason, nitwit, shepherd, toolsmith, weaponsmith
- **Level Overlays**: stone, iron, gold, diamond, emerald (profession levels)

### 2. Texture File Structure

**Official Villager Profession Textures Location:**
```
/assets/minecraft/textures/entity/villager/profession/
```

**Zombie Villager Profession Textures:**
```
/assets/minecraft/textures/entity/zombie_villager/profession/
```

### 3. Root Cause of Blending Issue

The blending behavior is likely caused by **multiple texture layers being composited** rather than our custom texture completely replacing the base villager appearance. The villager rendering system applies:

1. Base villager texture
2. Biome overlay (based on villager's biome type)
3. Profession overlay (our guard texture)
4. Level overlay (based on profession level)

**Critical Issue**: Our guard texture may be applied as an **overlay** rather than a **complete replacement**, causing it to blend with the underlying base and biome textures.

### 4. Known Solutions from Community

**Entity Texture Features (ETF) Mod Approach:**
- ETF mod provides advanced texture customization for entities
- Supports OptiFine-format texture properties
- Handles custom & random entity textures with enhanced properties
- May provide proper texture replacement rather than overlay blending

**Model Gap Fix Approach:**
- Addresses texture blending issues by disabling "texture scaling"
- Specifically designed to fix visual blending problems in Minecraft textures

### 5. Technical Implementation Gaps

**Missing Components Identified:**
1. **Texture Atlas Registration**: Custom profession textures may need proper registration in the texture atlas
2. **Resource Pack Integration**: May require resource pack format compliance
3. **Layer Renderer Control**: Need to control which texture layers are applied
4. **Base Texture Override**: May need to override the base villager texture, not just profession overlay

## Recommended Solutions

### Solution 1: Complete Texture Replacement Strategy
Instead of intercepting `getTexture()` alone, implement a complete texture replacement that:
- Overrides the base villager texture
- Disables biome overlay application
- Provides a complete guard appearance

### Solution 2: Resource Pack Integration
Structure guard textures as a proper resource pack:
```
assets/
  xeenaa_villager_manager/
    textures/
      entity/
        villager/
          profession/
            guard.png
          type/
            guard_base.png  # Custom base texture
```

### Solution 3: ETF-Compatible Implementation
Implement texture replacement using Entity Texture Features format for maximum compatibility.

### Solution 4: Layer Renderer Modification
Instead of texture replacement, create a custom layer renderer that:
- Renders the complete guard appearance
- Bypasses the default overlay system
- Provides pixel-perfect texture control

## Next Steps for Implementation

1. **Analyze ETF mod source code** for proper texture replacement techniques
2. **Test resource pack approach** with complete texture replacement
3. **Investigate layer renderer alternatives** for custom villager appearances
4. **Implement texture atlas registration** if required for custom professions

## Technical References

- **VillagerConfig Mod**: Behavioral customization approach
- **Entity Texture Features**: Advanced texture replacement capabilities
- **Villager-Models Repository**: Complete model and texture reference
- **OptiFine Issue #2484**: Discusses villager overlay system limitations