# Villager Profession Texture System in Minecraft 1.21.1

## Key Discovery: No Client-Side Registration Required

### The Problem
Guard villagers appear as purple/violet (missing texture) when assigned the guard profession.

### Root Cause Analysis
The issue is **NOT** missing client-side registration code. In Minecraft 1.21.1, villager profession textures are loaded automatically by the resource pack system when:
1. The profession is properly registered server-side
2. The texture file exists at the correct path

### How Villager Profession Textures Work

#### Automatic Texture Loading
Minecraft's resource pack system automatically searches for profession textures using this pattern:
```
assets/{mod_namespace}/textures/entity/villager/profession/{profession_id}.png
```

When a villager is assigned a profession, the game:
1. Gets the profession's identifier (e.g., `xeenaa_villager_manager:guard`)
2. Constructs the texture path: `assets/xeenaa_villager_manager/textures/entity/villager/profession/guard.png`
3. Loads the texture automatically - no code registration needed

#### What Causes Purple/Violet Villagers
Purple/violet appearance indicates Minecraft's "missing texture" fallback. This happens when:
- The texture file doesn't exist at the expected path
- The texture file is corrupted or invalid format
- The file name doesn't match the profession ID exactly

### Solution Implementation

#### 1. Texture File Requirements
**Required Path**: `assets/xeenaa_villager_manager/textures/entity/villager/profession/guard.png`

**File Specifications**:
- Resolution: 64x64 pixels
- Format: PNG with transparency support
- Content: Villager profession outfit/appearance

#### 2. Optional Metadata File
**Path**: `assets/xeenaa_villager_manager/textures/entity/villager/profession/guard.png.mcmeta`
**Purpose**: Defines hat rendering properties

```json
{
  "villager": {
    "hat": "full"
  }
}
```

#### 3. Current Project Status
✅ Directory structure exists: `src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/`
✅ Metadata file created: `guard.png.mcmeta`
❌ **Missing**: `guard.png` texture file

### Why No Client-Side Code Is Needed

#### Historical Context
- **Old Fabric Versions**: Required `VillagerProfessionBuilder` for registration
- **Minecraft 1.21.1**: `VillagerProfessionBuilder` is deprecated
- **Current System**: Uses direct `VillagerProfession` constructor + automatic texture loading

#### Modern Fabric Implementation
Our current `ModProfessions.java` implementation is correct:
1. Creates profession with proper identifier
2. Registers with Minecraft registry
3. Texture system handles the rest automatically

### Required Action
The only remaining step is to **create the actual guard.png texture file**. This can be done by:
1. Extracting a suitable texture from Minecraft's assets
2. Downloading from Minecraft Wiki
3. Creating a custom texture

### Debugging Villager Profession Textures
To verify texture loading:
1. Check resource pack structure is correct
2. Ensure file names match profession IDs exactly
3. Verify PNG format and dimensions
4. Look for resource loading errors in game logs

### Technical Notes
- Texture loading happens during resource pack initialization
- Changes require game restart or resource pack reload (F3+T)
- Case sensitivity matters on some file systems
- No mixin or client-side registration hooks required