# Workflow Guide - Xeenaa Villager Manager

## Quick Reference

### Slash Commands

| Command | Purpose | Example |
|---------|---------|---------|
| `/next` | Execute next task from tasks.md | `/next` |
| `/new_feat` | Add new feature to project | `/new_feat "Add villager trading system"` |
| `/fix` | Fix a bug | `/fix "Guards not attacking zombies"` |
| `/serve_client` | Launch Minecraft client with mod | `/serve_client` |

## Agent Roles

### project-scope-manager
- **Manages**: `.claude/project.md` ONLY
- **Does**: Define features, phases, game mechanics design
- **Called by**: `/new_feat` command
- **Outputs**: Updated project.md, game design docs in `.claude/game-design/`

### tech-lead-coordinator
- **Manages**: `.claude/tasks.md` ONLY
- **Does**: Convert features to tasks, orchestrate agents, track progress
- **Called by**: `/next` and `/new_feat` commands
- **Outputs**: Updated tasks.md, agent coordination

### minecraft-developer
- **Manages**: All code files
- **Does**: Write ALL code, architecture, UI/UX implementation
- **Called by**: tech-lead-coordinator via tasks, `/fix` command
- **Outputs**: Code implementation, requests user validation

### minecraft-qa-specialist
- **Manages**: Test files in `.claude/temp/`
- **Does**: Write automated tests AFTER user validates features
- **Called by**: tech-lead-coordinator via tasks
- **Outputs**: Test plans, automated test suites

### minecraft-researcher
- **Manages**: Research docs in `.claude/research/`
- **Does**: Investigate unknown problems, research Minecraft systems
- **Called by**: tech-lead-coordinator via tasks
- **Outputs**: Research findings, technical recommendations

## Standard Workflows

### Adding a New Feature

```bash
1. User: /new_feat "description of feature"
2. project-scope-manager:
   - Updates project.md with feature
   - Designs game mechanics if needed
   - Saves to .claude/game-design/ if complex
3. tech-lead-coordinator:
   - Reads updated project.md
   - Creates tasks in tasks.md
   - ASKS USER for prioritization
   - Updates tasks.md after approval
4. User: /next (to start first task)
5. tech-lead-coordinator:
   - Reads next task
   - Invokes assigned agent
   - Waits for completion
   - Updates task status
   - STOPS for user confirmation
6. Repeat /next until feature complete
```

### Executing Tasks

```bash
1. User: /next
2. tech-lead-coordinator:
   - Finds next TODO task
   - Gathers context files
   - Invokes assigned agent with full context
3. Agent (e.g., minecraft-developer):
   - Completes the task
   - Reports completion
4. tech-lead-coordinator:
   - Updates task status (TODO → COMPLETED)
   - Reports to user
   - STOPS - waits for user confirmation
5. User validates the work
6. User: /next (for next task)
```

### Fixing a Bug

```bash
1. User: /fix "bug description"
2. minecraft-developer:
   - Investigates the bug
   - Fixes the code
   - Logs in .claude/bugs.md
   - Requests user validation
3. User tests the fix
4. User confirms fix works
5. minecraft-developer updates bug status to "Verified"
```

### Running the Mod

```bash
1. User: /serve_client
2. System:
   - Checks if mod is built
   - Builds if needed (./gradlew build)
   - Launches client (./gradlew runClient)
```

## File Structure

```
.claude/
├── CLAUDE.md                    # Project overview and workflow
├── project.md                   # Features and phases (project-scope-manager)
├── tasks.md                     # Task breakdown (tech-lead-coordinator)
├── bugs.md                      # Bug tracking (minecraft-developer)
├── changelog.md                 # Version history
├── WORKFLOW_GUIDE.md           # This file
│
├── guidelines/
│   ├── standards.md            # Code conventions
│   └── dev-setup.md            # Development environment
│
├── temp/                        # Temporary files (validation, tests)
├── game-design/                 # Game mechanics design docs
├── research/                    # Research findings
├── ui/                          # UI designs and specs
│
├── agents/                      # Agent definitions
│   ├── project-scope-manager.md
│   ├── tech-lead-coordinator.md
│   ├── minecraft-developer.md
│   ├── minecraft-qa-specialist.md
│   └── minecraft-researcher.md
│
└── commands/                    # Slash commands
    ├── next.mts
    ├── new_feat.mts
    ├── fix.mts
    └── serve_client.mts
```

## Key Principles

### Phase-Based Development
- Project organized into phases
- Each phase delivers user value
- Features grouped by what players get

### Task-Based Execution
- Features broken into tasks
- Each task has: description, goal, requirements, assigned agent
- Tasks execute one at a time with user confirmation

### Agent Specialization
- **project-scope-manager**: WHAT to build (features)
- **tech-lead-coordinator**: HOW and WHEN (tasks)
- **minecraft-developer**: Implementation (code)
- **minecraft-qa-specialist**: Quality (tests after user validation)
- **minecraft-researcher**: Investigation (unknowns)

### Stop-and-Confirm Workflow
- After each task: agent completes → tech-lead updates → STOP
- User validates work before next task
- No autonomous multi-task execution

### Document Handovers
- Tasks specify handover documents
- Agents pass artifacts to next agent
- Example: game-design doc → minecraft-developer → test plan → minecraft-qa-specialist

## Best Practices

1. **Use /next frequently**: Work one task at a time
2. **Validate after each task**: Don't skip user testing
3. **Clear feature descriptions**: Help project-scope-manager with detailed /new_feat descriptions
4. **Bug tracking**: Use /fix to log all bugs in bugs.md
5. **Phase completion**: Complete full phases for user value delivery

## Migration Notes

### What Changed (October 2025)
- **game-mechanics-designer** merged into **project-scope-manager**
- **tech-lead-coordinator** now manages tasks.md (was project-scope-manager)
- **project-scope-manager** now ONLY manages project.md
- New slash commands for workflow automation
- Stop-and-confirm workflow enforced
- bugs.md file for bug tracking

### What Stayed the Same
- minecraft-developer still ONLY agent writing code
- minecraft-qa-specialist still tests AFTER user validation
- minecraft-researcher still investigates unknowns
- Code standards in .claude/guidelines/standards.md
