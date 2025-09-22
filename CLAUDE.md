# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Xeenaa Villager Manager** is a Minecraft Fabric mod for version 1.21.1 that provides comprehensive villager management tools through an intuitive GUI interface.

### Core Features
- **Right-click Interaction**: Players can right-click on any villager to open the management GUI
- **Profession Management**: Assign any available profession including vanilla and modded professions
- **Trade Management**: View and manage villager trades and trading levels
- **Villager Utilities**: Additional tools for villager management and optimization
- **Persistence**: All changes persist through game sessions and work as intended by the game mechanics
- **Mod Compatibility**: Automatically detects and includes content from other installed mods
- **Performance Optimized**: Built with efficiency in mind to minimize performance impact

### Technical Requirements
- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API
- **Java Version**: 21 (required for Minecraft 1.21.1)

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

## Working Methodology

This project uses specialized agents to manage different aspects of development:

### Agent Responsibilities

- **project-scope-manager**: Manages `.claude/project.md` (project description) and `.claude/tasks.md` (task tracking)
- **minecraft-architect**: Makes high-level technical decisions, designs systems, and resolves architectural challenges. NEVER writes code - only provides patterns and solutions
- **minecraft-developer**: Implements features following architecture guidance and standards. Focuses on clean, maintainable code. Manages `standards.md` (coding standards) and `changelog.md` (decisions and failed solutions)
- **minecraft-qa-specialist**: Specializes in testing, debugging, and log analysis. Finds and diagnoses issues in implemented code
- **minecraft-ui-ux-designer**: Analyzes, improves, and designs Minecraft mod UI/UX elements. Reviews GUI implementations and proposes enhancements. UI concepts and designs are stored in `.claude/ui/`
- **minecraft-researcher**: Resolves "unknown unknowns" - investigates how Minecraft systems work, analyzes other mods, and finds solutions to novel problems. Stores findings in `.claude/research/`

### Workflow Process

**Before any work:**
1. Read `.claude/changelog.md` to avoid repeating past issues
2. Check `.claude/tasks.md` to identify the next task and review previous task status
3. Review `standards.md` for current code conventions
4. Consult `.claude/project.md` for project specifications and context

**During work:**
1. Follow all code standards defined in `standards.md`
2. Reference the current task from `.claude/tasks.md`
3. Use specialized agents for their respective domains:
   - Use **minecraft-architect** when designing new features or resolving architectural challenges
   - Use **minecraft-researcher** when facing unknown problems or investigating how systems work
   - Use **minecraft-ui-ux-designer** after implementing GUI code or when planning new UI features

**After completing work:**
1. Use **project-scope-manager** to update tasks and project scope
2. Use **minecraft-developer** to update changelog and standards
3. For GUI-related changes, use **minecraft-ui-ux-designer** to review and suggest improvements
4. Update CLAUDE.md only if agent responsibilities change

This methodology persists across all sessions - always maintain and reference these files when working on the project.