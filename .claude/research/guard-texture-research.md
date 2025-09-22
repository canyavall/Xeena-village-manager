# Guard Villager Texture Research

## Research Summary
Date: 2025-09-21
Objective: Extract guard villager texture from existing mods to solve purple villager issue in our guard profession

## Identified Source Mods

### Guard Villagers (Fabric/Quilt) by MrSterner
- **GitHub Repository**: https://github.com/mrsterner/GuardVillagers
- **License**: CC0-1.0 (Public Domain - allows reuse)
- **Downloads**: 14.2M+ on CurseForge
- **Latest Version**: 2.1.2-1.21 (released June 22, 2024)
- **Compatibility**: Minecraft 1.21, Fabric & Quilt

### Key Findings
1. **Licensing**: CC0-1.0 license allows free reuse of textures
2. **Popularity**: Well-established mod with millions of downloads
3. **Active Development**: Recent updates for Minecraft 1.21
4. **Repository Access**: Public GitHub repository available

## Technical Challenges Encountered
1. **Direct Download**: CurseForge and Modrinth require browser interaction for downloads
2. **API Access**: Modrinth API returned 404 for the mod ID
3. **GitHub Assets**: Texture assets not directly visible through web interface

## Alternative Solutions

### Solution 1: Manual Texture Creation
Create a guard texture based on standard Minecraft villager profession patterns:
- **Base Template**: Use existing Minecraft villager texture as template
- **Guard Elements**: Add armor, helmet, weapon accessories
- **Color Scheme**: Military/guard colors (browns, grays, metallics)
- **Format**: 64x64 PNG texture following Minecraft villager texture layout

### Solution 2: Texture Pack Resources
Several texture packs include guard villager variations:
- "All Villager Professions" packs on Planet Minecraft
- Custom villager profession mods with guard variants

## Villager Texture Structure
- **Format**: 64x64 PNG
- **Layout**: Standard Minecraft entity texture layout
- **Profession Areas**: Specific areas of texture define profession-specific elements
- **Base Template**: Available from Minecraft assets or modding resources

## Recommended Approach
Given the research findings, recommend creating a custom guard texture using:
1. Base villager texture template
2. Guard-specific visual elements (armor, helmet, colors)
3. Following Minecraft's standard villager profession texture conventions

## License Considerations
- Guard Villagers mod uses CC0-1.0 license (public domain)
- Safe to use and modify textures from this source
- Attribution not required but recommended
- Compatible with our project's licensing