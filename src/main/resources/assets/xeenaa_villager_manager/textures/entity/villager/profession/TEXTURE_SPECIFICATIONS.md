# Guard Villager Texture Specifications

This directory requires three texture files to visually distinguish guard types. These textures are dynamically selected based on the guard's specialization path.

## Required Texture Files

### 1. guard_recruit.png
**File**: `guard_recruit.png`
**Size**: 64x64 pixels (standard villager texture dimensions)
**Format**: PNG with transparency support
**Purpose**: Used for Recruit guards (base rank, no specialization)

**Visual Design**:
- **Base Template**: Use vanilla villager body as template
- **Color Palette**: Brown/tan tones (neutral, basic guard appearance)
- **Style**: Simple guard uniform, minimal armor
- **Details**:
  - Basic tunic or simple clothing
  - No specialized armor pieces
  - Neutral, unadorned appearance
  - Leather-like texturing
  - Simple belt or basic guard insignia
- **Theme**: Basic guard trainee, unspecialized

**Reference Colors**:
- Primary: Brown (#8B4513, #A0522D)
- Secondary: Tan (#D2B48C, #DEB887)
- Accents: Dark brown (#654321)

---

### 2. guard_marksman.png
**File**: `guard_marksman.png`
**Size**: 64x64 pixels (standard villager texture dimensions)
**Format**: PNG with transparency support
**Purpose**: Used for Marksman path guards (ranged specialization)

**Visual Design**:
- **Base Template**: Use vanilla villager body as template
- **Color Palette**: Green/brown ranger theme (forest colors)
- **Style**: Agile, mobile, ranger-themed appearance
- **Details**:
  - Lighter armor (leather/hide)
  - Quiver visible on back (with arrows)
  - Hood or ranger cap
  - Forest camouflage colors
  - Archer's arm guards
  - Nimble, light protection aesthetic
- **Theme**: Skilled ranger/archer, ranged combat specialist

**Reference Colors**:
- Primary: Forest green (#228B22, #2E8B57)
- Secondary: Brown (#8B4513, #A0522D)
- Accents: Dark green (#013220), leather brown (#654321)
- Arrow details: Brown shafts, gray/white fletching

---

### 3. guard_arms.png
**File**: `guard_arms.png`
**Size**: 64x64 pixels (standard villager texture dimensions)
**Format**: PNG with transparency support
**Purpose**: Used for Man-at-Arms path guards (melee specialization)

**Visual Design**:
- **Base Template**: Use vanilla villager body as template
- **Color Palette**: Gray/silver knight theme (metal armor)
- **Style**: Heavy, armored, knight-themed appearance
- **Details**:
  - Heavy armor plates
  - Chainmail visible at joints
  - Shoulder guards/pauldrons
  - Chest plate
  - Metal plating texture
  - Tank-like, heavily armored aesthetic
  - Battle-worn appearance
- **Theme**: Armored knight, melee combat tank

**Reference Colors**:
- Primary: Steel gray (#708090, #778899)
- Secondary: Silver (#C0C0C0, #A8A8A8)
- Accents: Dark metal (#2F4F4F), iron (#36454F)
- Highlights: Light silver (#DCDCDC) for metal shine

---

## Technical Requirements

### Texture Format
- **Dimensions**: 64x64 pixels (standard Minecraft villager texture)
- **File Format**: PNG with alpha channel (transparency support)
- **Color Mode**: RGBA (8-bit per channel)
- **Compression**: PNG compression (lossless)

### Villager UV Mapping
The texture must follow Minecraft's villager UV layout:
- **Head**: Top-left area (villager head cube)
- **Body**: Center area (villager torso)
- **Arms**: Side areas (left and right arms)
- **Legs**: Bottom areas (left and right legs)

**Important**: Use the vanilla villager texture as a base template to ensure proper UV mapping alignment.

### Compatibility
- Must work with Minecraft 1.21.1 villager model
- Compatible with SimplifiedVillagerModel used by GuardVillagerRenderer
- Support both normal and zombified states
- Work correctly in all biomes and lighting conditions

---

## Implementation Details

### Texture Selection Logic
The `GuardVillagerRenderer` automatically selects the appropriate texture based on the guard's current rank:

```java
// Recruit rank → guard_recruit.png
RECRUIT -> GUARD_RECRUIT_TEXTURE

// Man-at-Arms I-IV, Knight → guard_arms.png
MELEE -> GUARD_MELEE_TEXTURE

// Marksman I-IV, Sharpshooter → guard_marksman.png
RANGED -> GUARD_RANGED_TEXTURE
```

### Dynamic Switching
Textures are dynamically loaded and switched when:
- A guard is first converted from villager
- A guard upgrades from Recruit to a specialization path
- The game client renders the guard entity
- No manual texture reloading required

---

## Creating the Textures

### Recommended Tools
- **GIMP**: Free, open-source image editor
- **Aseprite**: Pixel art editor (paid)
- **Paint.NET**: Free Windows image editor
- **Photoshop**: Professional image editor

### Steps
1. Open the vanilla Minecraft villager texture as a base template
2. Locate the texture in: `.minecraft/versions/1.21.1/assets/minecraft/textures/entity/villager/`
3. Copy the villager body template (not the profession overlay)
4. Apply the color scheme and details according to specifications above
5. Save as PNG with transparency
6. Place files in this directory

### Testing
1. Place the texture files in this directory
2. Run `./gradlew build` to rebuild the mod
3. Launch the game with `./gradlew runClient`
4. Create guard villagers and test each path
5. Verify textures display correctly and switch when ranks change

---

## Current Status

**Created**: ❌ None
**Required**: ✅ All three textures

To complete Phase 3 Task 2, create all three texture files following the specifications above.

---

## Notes
- The old `guard_complete.png` texture is no longer used
- Each texture should be visually distinct at a glance
- Maintain Minecraft's art style (blocky, pixelated)
- Avoid excessive detail that won't be visible at normal viewing distances
- Test in-game before finalizing designs
