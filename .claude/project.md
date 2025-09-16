# Xeenaa Village Picker

## Project Overview

**Xeenaa Village Picker** is a Minecraft Fabric mod for version 1.21.1 that provides an intuitive GUI for managing villager professions.

### Core Features
- **Right-click Interaction**: Players can right-click on any villager to open the profession selection GUI
- **Complete Profession List**: Displays all available professions including vanilla and modded professions
- **Profession Assignment**: Allows players to assign any available profession to the selected villager
- **Persistence**: Assigned professions persist through game sessions and work as intended by the game mechanics
- **Mod Compatibility**: Automatically detects and includes professions from other installed mods
- **Performance Optimized**: Built with efficiency in mind to minimize performance impact

### Technical Requirements
- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API
- **Java Version**: 21 (required for Minecraft 1.21.1)

## Architecture Overview

### Package Structure
```
com.xeenaa.villagepicker/
├── XeenaaVillagePicker.java           # Main mod entry point
├── XeenaaVillagePickerClient.java     # Client-side initializer  
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
- `xeenaa_village_picker.mixins.json` - Mixin configurations  
- `xeenaa_village_picker.client.mixins.json` - Client-side mixin configs

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

The project is currently in Phase 6 of development (Networking & Profession Persistence), with Phases 1-5 completed:

- ✅ **Phase 1-3**: Project setup, core foundation, and entity interaction system
- ✅ **Phase 4**: Profession registry system with automatic mod detection
- ✅ **Phase 5**: Complete GUI implementation with 3-column grid layout
- ⚠️ **Phase 6**: Networking system implemented, persistence solution in testing
- 🔄 **Phases 7-11**: Pending - profession assignment, edge cases, polish, and release

The mod currently provides a fully functional GUI for profession selection with comprehensive profession detection, but profession persistence after assignment is being refined.