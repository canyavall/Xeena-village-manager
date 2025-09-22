# Research Directory

This directory is managed by the **minecraft-mod-researcher** agent and contains research materials for the Xeenaa Villager Manager mod development.

## Structure

- `mods/` - Downloaded and analyzed Minecraft mods for reference
- `documentation/` - Technical documentation, API references, and guides
- `code-samples/` - Code snippets and examples from other mods
- `ui-patterns/` - UI/UX patterns and designs from other Minecraft mods

## Current Research Projects

### Guard Villager Visual Implementation Research (September 2025)

**Objective**: Research how existing mods implement guard villager visual systems to solve the "purple villager" issue in the Xeenaa Villager Manager mod.

**Key Findings**:
- **Guard Villagers mod** (65M+ downloads) uses custom entity approach rather than villager profession modification
- **Custom entity strategy** avoids conflicts with villager profession texture system
- **Equipment rendering systems** provide dynamic armor and weapon visualization
- **Multiple implementation approaches** available for Minecraft 1.21.1 Fabric

**Research Locations**:
- `mods/guard-villagers/` - Analysis of the popular Guard Villagers mod
- `documentation/villager-visual-systems/` - Comprehensive implementation guides
  - `equipment-rendering.md` - Equipment and visual customization systems
  - `texture-implementation-approaches.md` - Three different texture implementation strategies
  - `fabric-1-21-specific-implementation.md` - Specific code examples for Minecraft 1.21.1 Fabric

**Recommendations**:
1. **Immediate Fix**: Implement profession texture solution to resolve purple villager issue
2. **Long-term Solution**: Migrate to custom guard entity system for full equipment support
3. **Architecture**: Follow Guard Villagers mod patterns for professional-grade implementation

## Usage

The minecraft-mod-researcher agent will:
1. Download relevant mods for analysis
2. Document findings and patterns
3. Store code examples and best practices
4. Maintain organized research for future reference

## Note

All content in this directory is for research and reference purposes only. Always respect licenses and attribution requirements when learning from other mods.