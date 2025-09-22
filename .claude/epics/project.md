# Xeenaa Villager Manager

## Project Overview

**Xeenaa Villager Manager** is a comprehensive Minecraft Fabric mod for version 1.21.1 that provides advanced villager management tools through an intuitive GUI interface. The mod offers a complete suite of features designed to give players full control over villager behavior, trades, and properties.

### Completed Features (Epic 1: Profession Management)
- **Right-click Interaction**: Players can shift+right-click on any villager to open the management GUI
- **Complete Profession List**: Displays all available professions including vanilla and modded professions
- **Profession Assignment**: Allows players to assign any available profession to the selected villager
- **Persistence**: Assigned professions persist through game sessions and work as intended by the game mechanics
- **Mod Compatibility**: Automatically detects and includes professions from other installed mods
- **Performance Optimized**: Built with efficiency in mind to minimize performance impact

### Planned Features (Future Epics)
The mod is designed as a comprehensive villager management platform with multiple feature sets planned for future development:

**Epic 2: Guard Profession System** (Next Major Feature - In Development)
- **Custom Guard Profession**: New profession with comprehensive equipment management capabilities
- **Tabbed GUI System**: Enhanced interface with tabs for profession and equipment management
- **AI Behavior Modes**: Three distinct behaviors - Patrol, Guard (stationary), and Follow
- **Combat System**: Equipment-based combat statistics and performance optimization
- **JSON Configuration**: Fully configurable system with external JSON config files

This epic builds directly upon the existing profession management system, extending the GUI framework and registry architecture to support custom professions with advanced capabilities.

**Future Epics**:
- **Trade Management System**: View, modify, and manage villager trades and trading levels
- **Villager Utilities**: Additional tools for villager optimization and management
- **Advanced Properties**: Control over villager attributes and behaviors
- **Batch Operations**: Tools for managing multiple villagers simultaneously
- **Integration Features**: Enhanced compatibility with other villager-focused mods

### Technical Requirements
- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API
- **Java Version**: 21 (required for Minecraft 1.21.1)

## Architecture Overview

### Package Structure
```
com.xeenaa.villagermanager/
├── XeenaaVillagerManager.java           # Main mod entry point
├── XeenaaVillagerManagerClient.java     # Client-side initializer  
├── client/
│   ├── gui/                           # GUI screens and widgets
│   └── util/ClientInteractionHandler  # Client-side interaction logic
├── network/                           # Client-server communication
│   ├── SelectProfessionPacket         # C2S profession selection
│   └── ServerPacketHandler            # Server packet processing
└── registry/                          # Profession data management
    ├── ProfessionManager              # Singleton profession registry
    └── ProfessionData                 # Profession data model
```

### Key Design Patterns

**Client-Server Architecture**: Uses Fabric's networking API for client-server communication. GUI opens client-side, profession changes sent via packets to server.

**Event-Driven Interaction**: Uses `UseEntityCallback.EVENT` for villager right-click detection rather than mixins when possible.

**Registry Pattern**: `ProfessionManager` singleton handles all profession discovery and caching, including modded professions.

**Resource Files**:
- `fabric.mod.json` - Mod metadata and entry points
- `xeenaa_villager_manager.mixins.json` - Mixin configurations
- `xeenaa_villager_manager.client.mixins.json` - Client-side mixin configs

## Common Development Commands

### Building and Testing
- `./gradlew build` - Full build of the mod
- `./gradlew build -q` - Quiet build (faster, useful for hot reload testing)
- `./gradlew runClient` - Start Minecraft client with mod loaded
- `./gradlew runServer` - Start Minecraft dedicated server with mod
- `./gradlew genSources` - Generate Minecraft source code for reference
- `./gradlew tasks --group="fabric"` - List all available Fabric development tasks

### IDE Support
- VSCode configurations available in `.vscode/` (settings.json, tasks.json)
- IntelliJ IDEA run configurations in `.idea/runConfigurations/`
- Import as Gradle project in any IDE

## Development Methodology

This project follows a structured documentation approach with key files in the `.claude` folder:

1. **project.md** (this file) - Contains project specifications and architecture overview
2. **standards.md** - Documents all project code standards and conventions
3. **tasks.md** - Tracks tasks to be completed
4. **changelog.md** - Records all changes and decisions made during development

### Workflow Process

When working on this project, ALWAYS:

**Before any work:**
1. Read changelog.md to avoid repeating past issues
2. Check tasks.md to identify the next task and review previous task status
3. Review standards.md for current code conventions
4. Consult project.md for project specifications and context

**During work:**
1. Follow all code standards defined in standards.md
2. Reference the current task from tasks.md

**After completing work:**
1. Update tasks.md - mark completed tasks and add any new tasks discovered
2. Update changelog.md with changes made and decisions taken
3. Update standards.md when new code patterns or conventions are established
4. Update project.md if project specifications change

This methodology persists across all sessions - always maintain and reference these files when working on the project. Never skip checking these files.

## Current Development Status

**Status**: Feature Expansion Phase

### Epic 1: Profession Management System ✅ COMPLETED
**Completion Date**: 2025-09-18

The profession management system is fully implemented and functional:
- Complete shift+right-click interaction system
- Comprehensive profession detection (vanilla + modded)
- Full GUI implementation with 3-column grid layout
- Client-server networking with packet validation
- Persistent profession assignment with proper game mechanics integration
- Performance optimized caching and registry management

### Next Development Phase
The mod is now positioned for expansion into a comprehensive villager management suite. The next epic will focus on implementing additional villager management features, building upon the solid foundation established by the profession management system.

**Architecture Status**: The core architecture (client-server communication, GUI framework, registry system, and event handling) is stable and ready to support additional feature development.