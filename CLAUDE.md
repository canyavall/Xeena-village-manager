# CLAUDE.md

## Project Overview
**Xeenaa Villager Manager** - Minecraft 1.21.1 Fabric mod providing villager management tools through GUI interface.

### Core Features
- Right-click villager interaction → management GUI
- Profession assignment (vanilla + modded)
- Guard profession with ranking system
- Equipment management for guards
- Full persistence and mod compatibility

### Technical Stack
- **Minecraft**: 1.21.1 | **Mod Loader**: Fabric | **Java**: 21
- **Dependencies**: Fabric API

## Development Commands
- `./gradlew build` - Build mod
- `./gradlew runClient` - Test in game client
- `./gradlew runServer` - Test dedicated server

## Architecture
```
com.xeenaa.villagermanager/
├── XeenaaVillagerManager.java          # Main entry point
├── client/gui/                         # GUI screens and widgets
├── network/                            # Client-server packets
└── registry/                           # Profession management
```

**Key Patterns**: Client-server networking, event-driven interaction, registry singleton pattern

## Workflow

### File Management
- **Tasks**: `.claude/tasks.md` - Current priorities and status
- **History**: `.claude/changelog.md` - Completed features (version-based)
- **Standards**: `.claude/standards.md` - Code conventions
- **Project**: `.claude/project.md` - Current specifications

### Agent Specialization and Workflow

**Specialized Agents** (trigger automatically based on content):
- **game-mechanics-designer**: ONLY for game mechanics design (rank systems, progression, economy, social systems). Triggered when discussing game mechanics like ranking, leveling, achievements, trading systems, etc.
- **minecraft-developer**: All code implementation, architecture decisions, UI/UX work. ONLY agent that writes code. Handles system design merged from architect role.
- **minecraft-qa-specialist**: Testing and debugging AFTER user manual validation. Writes automated tests post-implementation.
- **project-scope-manager**: ONLY handles project scope, tasks, and project management files.
- **minecraft-researcher**: Unknown problem investigation and research.

**Workflow Rules**:
1. **Game Mechanics Discussion** → `game-mechanics-designer` auto-triggers for rank/progression/economy design
2. **Code Implementation** → `minecraft-developer` handles ALL coding, architecture, UI/UX
3. **User Manual Testing Complete** → `minecraft-qa-specialist` writes automated tests
4. **Project Management** → `project-scope-manager` updates tasks/scope only

**Process**:
1. Game mechanics design → game-mechanics-designer agent
2. Code implementation → minecraft-developer agent (includes architecture + UI/UX)
3. User validates manually → minecraft-qa-specialist writes tests
4. Throughout → project-scope-manager handles tasks

## Current Agent Status
- ✅ **game-mechanics-designer** - Handles rank systems, progression, economy design
- ✅ **minecraft-developer** - ONLY agent that writes code (includes architecture + UI/UX)
- ✅ **minecraft-qa-specialist** - Writes tests AFTER user manual validation
- ✅ **project-scope-manager** - ONLY handles project/task management
- ✅ **minecraft-researcher** - Investigates unknown problems
- ❌ **minecraft-architect** - REMOVED (duties merged into minecraft-developer)
- ❌ **minecraft-ui-ux-designer** - REMOVED (duties merged into minecraft-developer)