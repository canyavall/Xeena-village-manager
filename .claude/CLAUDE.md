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
- **Project**: `.claude/project.md` - Features and phases (managed by project-scope-manager)
- **Tasks**: `.claude/tasks.md` - Task breakdown and status (managed by tech-lead-coordinator)
- **Bugs**: `.claude/bugs.md` - Bug tracking and fixes
- **History**: `.claude/changelog.md` - Completed features (version-based)
- **Standards**: `.claude/guidelines/standards.md` - Code conventions
- **Dev Setup**: `.claude/guidelines/dev-setup.md` - Development environment

### Agent Specialization

**Specialized Agents**:
- **project-scope-manager**: Defines features and phases in project.md. Includes game mechanics design.
- **tech-lead-coordinator**: Converts project.md into tasks, orchestrates agents, tracks progress
- **minecraft-developer**: ONLY agent that writes code. All implementation, architecture, UI/UX
- **minecraft-qa-specialist**: Writes automated tests AFTER user manual validation
- **minecraft-researcher**: Investigates unknown problems and Minecraft system research

### Workflow Process

**Standard Feature Flow**:
1. User requests feature via `/new_feat` command
2. **project-scope-manager** defines feature in `.claude/project.md` with game mechanics
3. **tech-lead-coordinator** breaks feature into tasks in `.claude/tasks.md`
4. User uses `/next` to execute next task
5. **tech-lead-coordinator** invokes assigned agent (minecraft-developer, etc.)
6. Agent completes work, tech-lead updates task status
7. User confirms completion before next task
8. Repeat until feature complete

**Bug Fix Flow**:
1. User reports bug via `/fix` command
2. **minecraft-developer** investigates and fixes
3. Bug logged in `.claude/bugs.md`
4. User validates fix

**Task Execution**:
- Each task completes and STOPS for user confirmation
- Tasks include: description, goal, requirements, assigned agent, handover docs
- Phases deliver user value incrementally

## Current Agent Status
- ✅ **project-scope-manager** - Manages project.md, designs game mechanics
- ✅ **tech-lead-coordinator** - Creates tasks, orchestrates workflow
- ✅ **minecraft-developer** - ONLY agent that writes code (implementation + architecture + UI/UX)
- ✅ **minecraft-qa-specialist** - Writes automated tests AFTER user validation
- ✅ **minecraft-researcher** - Investigates unknown problems
- ❌ **game-mechanics-designer** - REMOVED (merged into project-scope-manager)
- ❌ **minecraft-architect** - REMOVED (merged into minecraft-developer)
- ❌ **minecraft-ui-ux-designer** - REMOVED (merged into minecraft-developer)