# Guard Villager Texture Creation Guide

## Problem
The guard profession currently shows purple/violet villagers because the required texture file is missing.

## Solution Required
Create a 64x64 PNG texture file at:
```
src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/guard.png
```

## Texture Specifications

### File Format
- **Format**: PNG with transparency support
- **Dimensions**: 64x64 pixels (standard Minecraft villager texture size)
- **Color Depth**: 32-bit RGBA

### Design Requirements
The guard texture should be based on existing Minecraft villager profession textures but modified for a guard role:

1. **Base Template**: Use weaponsmith or armorer villager texture as starting point
2. **Color Scheme**:
   - Dark leather apron or vest
   - Brown/tan clothing colors
   - Metal belt or armor accents
   - Guard-appropriate colors (browns, grays, dark blues)

3. **Visual Elements**:
   - Sturdy clothing suitable for combat
   - Leather apron or vest (like weaponsmith)
   - Belt for equipment
   - Rolled-up sleeves or protective arm guards
   - Stern/alert facial expression

### Texture Layout (Standard Minecraft Villager Format)
```
+--+--+--+--+--+--+--+--+
|  HEAD FRONT/BACK/SIDES |  (rows 0-7)
+--+--+--+--+--+--+--+--+
|    BODY FRONT/BACK     |  (rows 8-19)
+--+--+--+--+--+--+--+--+
|  ARMS FRONT/BACK/SIDES |  (rows 20-31)
+--+--+--+--+--+--+--+--+
|  LEGS FRONT/BACK/SIDES |  (rows 32-47)
+--+--+--+--+--+--+--+--+
|     HAT/ACCESSORIES    |  (rows 48-63)
+--+--+--+--+--+--+--+--+
```

## Recommended Sources for Base Texture

### Option 1: Extract from Minecraft JAR
1. Locate your Minecraft installation
2. Find the client JAR file (minecraft-1.21.1.jar)
3. Extract: `assets/minecraft/textures/entity/villager/profession/weaponsmith.png`
4. Use as base template

### Option 2: Download from Official Sources
- Minecraft Wiki may have texture files
- Official Bedrock resource pack download
- Community texture repositories

### Option 3: Reference Images
Use existing weaponsmith/armorer villager images as reference to recreate the texture:
- Weaponsmith: Black apron, eye patch, sturdy appearance
- Armorer: Metal visor, blast furnace worker appearance

## Texture Editing Tools
- **GIMP**: Free, supports PNG with transparency
- **Paint.NET**: Free Windows image editor
- **Photoshop**: Professional editing
- **Online editors**: Pixlr, Photopea

## Implementation Steps
1. Create or obtain 64x64 base villager texture
2. Modify clothing colors and details for guard appearance
3. Ensure proper transparency in unused areas
4. Save as `guard.png` in the specified directory
5. Test in-game by assigning guard profession to villager

## Testing
After creating the texture:
1. Build mod: `./gradlew build`
2. Launch game with mod
3. Assign guard profession to villager
4. Verify villager no longer appears purple/violet
5. Confirm guard appearance matches intended design

## Notes
- The texture file name must exactly match the profession ID: `guard.png`
- Texture will be automatically loaded by Minecraft's resource system
- No additional code registration required in client
- Compatible with resource packs and texture modifications