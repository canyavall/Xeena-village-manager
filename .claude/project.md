# Xeenaa Villager Manager - Project Overview

## Project Description

**Xeenaa Villager Manager** is a comprehensive Minecraft Fabric mod for version 1.21.1 that provides advanced villager management tools through an intuitive GUI interface. The mod focuses on profession management and guard villager ranking systems.

## Core Features Implemented

### ✅ Profession Management System (Completed)
- **Right-click Interaction**: Players can shift+right-click on any villager to open the management GUI
- **Profession Selection**: Assign any available profession including vanilla and modded professions
- **Trade Integration**: Works seamlessly with Minecraft's trading system
- **Mod Compatibility**: Automatically detects and includes content from other installed mods
- **Persistence**: All changes persist through game sessions

### ✅ Guard Profession Foundation (Completed)
- **Custom Guard Profession**: Complete guard profession with workstation (Guard Post block)
- **Configuration Support**: Comprehensive config system for guard settings
- **Tabbed GUI Interface**: Professional tabbed management interface
- **Rendering System**: Fixed texture blending and visual improvements

### ✅ Guard Ranking System (Recently Implemented)
- **Dual Specialization Paths**: Melee (Man-at-Arms) and Ranged (Marksman) progression
- **5-Tier Progression**: Recruit → 4 specialized ranks per path
- **Emerald Economy**: Purchase-based rank progression using emeralds
- **Combat Statistics**: Rank-based health and damage progression
- **Special Abilities**: Unique abilities for high-tier guards
- **Visual Progression**: Rank-based cosmetic equipment and insignia

## Technical Architecture

### Core Components
- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API
- **Java Version**: 21

### Key Systems
1. **Profession Management**: `ProfessionManager` singleton with caching
2. **Guard Data**: `GuardData` and `GuardDataManager` for persistence
3. **Ranking System**: `GuardRank`, `GuardRankData`, and `GuardPath` enums
4. **Networking**: Client-server synchronization for all features
5. **GUI Framework**: Tabbed interface with role and rank management

### Package Structure
```
com.xeenaa.villagermanager/
├── XeenaaVillagerManager.java           # Main mod entry point
├── XeenaaVillagerManagerClient.java     # Client-side initializer
├── client/
│   ├── gui/                           # GUI screens and widgets
│   │   ├── VillagerManagementScreen     # Main tabbed interface
│   │   ├── GuardRankScreen             # Guard ranking interface
│   │   ├── ProfessionTab               # Profession selection
│   │   └── TabbedManagementScreen      # Base tabbed GUI
│   ├── render/                        # Client-side rendering
│   │   └── GuardVillagerRenderer       # Guard texture fixes
│   └── util/ClientInteractionHandler  # Client-side interaction logic
├── network/                           # Client-server communication
│   ├── SelectProfessionPacket         # C2S profession selection
│   ├── PurchaseRankPacket             # C2S rank purchase
│   └── ServerPacketHandler            # Server packet processing
├── data/                              # Data management
│   ├── GuardData                      # Guard entity data
│   ├── GuardDataManager               # Persistence manager
│   └── rank/                          # Ranking system
│       ├── GuardRank                  # Rank definitions
│       ├── GuardRankData              # Per-guard rank data
│       └── GuardPath                  # Specialization paths
├── profession/                        # Custom professions
│   └── ModProfessions                 # Guard profession registration
└── registry/                          # Profession data management
    ├── ProfessionManager              # Singleton profession registry
    └── ProfessionData                 # Profession data model
```

## Current System Status

### Guard Ranking System Details

**Specialization Paths:**
- **Melee Path (Man-at-Arms)**: Tank-focused progression with high health
- **Ranged Path (Marksman)**: Glass cannon with high ranged damage

**Rank Progression:**
1. **Recruit** (Starting rank) - 0 emeralds
2. **Tier 1** (Guard/Marksman) - 15 emeralds
3. **Tier 2** (Sergeant) - 20 emeralds
4. **Tier 3** (Lieutenant) - 45 emeralds
5. **Tier 4** (Captain/Master) - 75 emeralds

**Combat Statistics:**
- **Melee Path**: 20→40 HP progression, 2→10 damage
- **Ranged Path**: 20→26 HP progression, 3→9 ranged damage
- **Special Abilities**: High-tier ranks gain unique combat abilities

**Economy Integration:**
- Purchase-based progression using emeralds
- Sequential rank requirements (cannot skip ranks)
- Total investment: 155 emeralds for max rank

### Recent Implementation Status

Based on the latest commit "new rank system" (3b1b1f0), the following components have been implemented:

**✅ Completed Features:**
- Complete rank data structures (GuardRank, GuardPath, GuardRankData)
- Networking packets for rank purchases (PurchaseRankPacket)
- Guard rank GUI (GuardRankScreen)
- Client-server synchronization for rank data
- Integration with existing guard system

**🔄 Current State:**
- Rank system code is implemented and integrated
- Equipment system appears to still exist alongside ranking system
- Some legacy equipment files may need cleanup
- Testing and refinement likely needed

## Development Workflow

### Agent Responsibilities
- **project-scope-manager**: Manages project overview and task tracking
- **minecraft-architect**: High-level technical decisions and system design
- **minecraft-developer**: Feature implementation and coding standards
- **minecraft-qa-specialist**: Testing, debugging, and quality assurance
- **minecraft-ui-ux-designer**: GUI design and user experience
- **minecraft-researcher**: Technical research and problem solving

### Key Files to Maintain
- `.claude/project.md` - This project overview (current file)
- `.claude/tasks.md` - Current task priorities and progress
- `.claude/changelog.md` - Development history and decisions
- `standards.md` - Coding standards and conventions
- `CLAUDE.md` - Agent workflow and methodology

## Project Goals

### Primary Objectives
1. **Intuitive Villager Management**: Simple, powerful GUI for profession changes
2. **Guard System Excellence**: Comprehensive guard ranking with meaningful progression
3. **Mod Compatibility**: Seamless integration with existing Minecraft ecosystem
4. **Performance Optimization**: Efficient systems that don't impact gameplay
5. **User Experience**: Professional, polished interface design

### Success Metrics
- ✅ Profession management fully functional across all villager types
- ✅ Guard ranking system provides meaningful progression
- ✅ No conflicts with vanilla trading or other mod systems
- ✅ Stable multiplayer synchronization
- 🔄 Comprehensive testing and quality assurance

## Technical Requirements

### Performance Standards
- Minimal impact on game performance (< 5% overhead)
- Efficient caching and data management
- Thread-safe operations for multiplayer
- Memory-conscious resource usage

### Compatibility Requirements
- Full Minecraft 1.21.1 compatibility
- Fabric API integration following best practices
- Compatible with major performance mods (Sodium, Lithium)
- Graceful handling of unknown/modded professions

### Security Considerations
- Server-side validation for all profession changes
- Anti-exploit protection for rank purchases
- Distance-based interaction validation
- Currency transaction integrity

## Future Enhancement Opportunities

### Potential Features
- Advanced AI behaviors for different guard roles
- Combat integration with rank bonuses
- Visual rank progression (armor, weapons, insignia)
- Administrative tools for server management
- Integration with other villager enhancement mods

### Performance Optimizations
- Caching improvements for large villages
- Network packet optimization
- Rendering performance enhancements
- Memory usage optimization

---

**Project Status**: Active Development - Rank System Integration Phase
**Last Updated**: September 23, 2025
**Current Focus**: Testing and refining the newly implemented rank system
**Next Priorities**: Quality assurance, cleanup of legacy systems, and polish