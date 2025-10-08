# Zombie Guard Textures - PLACEHOLDER STATUS

## Current Status: PLACEHOLDER TEXTURES

The three PNG files in this directory (`guard_recruit.png`, `guard_marksman.png`, `guard_arms.png`) are **PLACEHOLDER TEXTURES ONLY**.

These files are copies of the living guard textures and do NOT include zombie features like:
- Green zombie skin tone
- Tattered, decayed clothing/armor
- Zombie eyes and deterioration effects
- Rust, rot, and wear patterns

## What This Means

The zombie guard texture system is **FULLY IMPLEMENTED** and works correctly. However, the textures themselves need to be created with proper zombie theming.

**Current Behavior**:
- Zombie guards will display different textures based on their type (Recruit/Marksman/Man-at-Arms)
- Visual distinction between guard types IS preserved
- However, zombie guards will look like normal living guards (not zombie-like)

**After Proper Textures Created**:
- Zombie guards will have green skin and decay effects
- Tattered armor and weathered appearance
- Professional zombie guard aesthetic
- Visual distinction still maintained

## How to Create Final Textures

See `ZOMBIE_TEXTURE_SPECIFICATIONS.md` in this directory for comprehensive instructions on:
- Technical requirements (64x64 PNG format)
- Design guidelines for each guard type
- Color schemes and zombie overlay effects
- Recommended tools and workflow
- Validation checklist

## Quick Start

1. **Read**: `ZOMBIE_TEXTURE_SPECIFICATIONS.md`
2. **Tools**: Use Aseprite, GIMP, Paint.NET, or Blockbench
3. **Base**: Start with vanilla zombie villager texture
4. **Apply**: Add guard characteristics with zombie theme
5. **Replace**: Replace the placeholder PNG files in this directory
6. **Test**: Build and test in-game

## Files to Replace

- `guard_recruit.png` - Zombified recruit (brown/tan + zombie green)
- `guard_marksman.png` - Zombified ranger (green/brown + zombie decay)
- `guard_arms.png` - Zombified knight (gray/silver + zombie rust)

## Reference Files

**Living Guard Textures** (for design reference):
`src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/`
- guard_recruit.png
- guard_marksman.png
- guard_arms.png

**Vanilla Zombie Villager** (for zombie features):
Minecraft assets: `assets/minecraft/textures/entity/zombie_villager/zombie_villager.png`

## Testing

After creating new textures:
1. Rebuild the mod: `./gradlew build`
2. Run the game: `./gradlew runClient`
3. Create guards, zombify them, and verify appearance
4. Check that all three types are visually distinct

## Questions?

If you need help with texture creation, consider:
- Consulting Minecraft texture creation tutorials
- Using Blockbench's texture editor with live preview
- Asking the Minecraft modding community for feedback
- Reviewing other mods' zombie villager textures for inspiration

---

**Last Updated**: 2025-10-08
**Implementation Status**: Code complete, textures pending user creation
