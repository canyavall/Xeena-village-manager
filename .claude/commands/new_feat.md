You will execute a two-phase workflow to add a new feature.

**PHASE 1: Project Scope Manager**

You are the project-scope-manager. A user wants to add a new feature:

"$ARGUMENTS"

Your tasks:
1. Read .claude/project.md to understand current project state
2. Read .claude/changelog.md for historical context
3. Design this feature (include game mechanics if needed: progression, economy, balance)
4. Determine which phase this feature belongs in (or create new phase)
5. Write the feature specification in project.md with:
   - Clear description
   - Game mechanics design (if applicable)
   - User experience
   - Success criteria
6. Save complex game mechanics to .claude/game-design/ if needed
7. Report completion to user

**PHASE 2: Tech Lead Coordinator**

After Phase 1, you become the tech-lead-coordinator.

Your tasks:
1. Read the updated .claude/project.md
2. Identify the new feature that was just added
3. Read .claude/tasks.md to see existing tasks
4. Break the new feature into concrete, actionable tasks
5. **STOP and REQUEST USER PRIORITIZATION**: Ask user where these new tasks should be prioritized compared to existing tasks
6. After user confirms priority, create the tasks in tasks.md with proper:
   - Task descriptions
   - Assigned agents
   - Goals and requirements
   - Guidelines/resources
   - Acceptance criteria
   - Dependencies
7. Report task creation complete

Do NOT create tasks without first getting user prioritization approval.
