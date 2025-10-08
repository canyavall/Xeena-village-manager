# Task Management - Xeenaa Villager Manager

## Overview
This file tracks all tasks, priorities, and progress for the Xeenaa Villager Manager mod development. Tasks are organized by priority and phase, with clear status indicators and acceptance criteria.

## Current Phase: PHASE 2 - GUARD AI AND COMBAT SYSTEM

**Status**: ✅ **PHASE 2 COMPLETED** - All core AI and combat features implemented and validated!

**Achievement Summary**:
- ✅ Core guard AI goals (attack, defend, patrol, follow)
- ✅ Villager threat detection system
- ✅ Melee vs ranged combat specialization
- ✅ Guard pathfinding and movement AI
- ✅ Combat integration with rank system
- ✅ Guard coordination and group tactics
- ✅ Guard configuration system with Guard Mode (FOLLOW/PATROL/STAND)
- ✅ Combat visual and audio effects
- ✅ Performance optimization (3% FPS impact with 20+ guards)
- ✅ Integration testing and validation

---

## ✅ COMPLETED TASKS SUMMARY

### Phase 2 - Guard AI and Combat System (All Complete)

#### P2-TASK-001: Core Guard AI Goals Implementation
**Completed**: October 1, 2025 | **Assignee**: minecraft-developer
- Implemented GuardDirectAttackGoal, GuardDefendVillagerGoal, GuardPatrolGoal, GuardFollowVillagerGoal
- Guards detect and attack hostile mobs, defend villagers, patrol, and follow

#### P2-TASK-002: Villager Threat Detection System
**Completed**: October 1, 2025 | **Assignee**: minecraft-developer
- Implemented ThreatDetectionManager with priority-based threat scanning
- Guards detect threats to villagers and respond appropriately

#### P2-TASK-003: Melee vs Ranged Combat Specialization
**Completed**: October 1, 2025 | **Assignee**: minecraft-developer
- GuardMeleeAttackGoal with tank behavior and knockback
- GuardRangedAttackGoal with kiting and distance maintenance

#### P2-TASK-004: Guard Pathfinding and Movement AI
**Completed**: October 1, 2025 | **Assignee**: minecraft-developer
- GuardPatrolGoal, GuardFollowVillagerGoal, GuardRetreatGoal implemented
- Smart combat positioning and terrain-aware pathfinding

#### P2-TASK-005: Combat Integration with Rank System
**Completed**: October 4, 2025 | **Assignee**: minecraft-developer
- Rank-based damage, health, movement speed, and detection range
- Tier 4 special abilities: Knight (knockback, area damage, slowness) and Sharpshooter (double shot)
- All abilities validated and working

#### P2-TASK-006: Guard Coordination and Group Tactics
**Completed**: October 5, 2025 | **Assignee**: minecraft-developer
- GuardCoordinationManager for group behavior
- Target allocation and formation behavior implemented

#### P2-TASK-007: Guard Configuration and Behavior Settings
**Completed**: October 5, 2025 | **Assignee**: minecraft-developer
- GuardBehaviorConfig with detection range, guard mode, profession lock
- ConfigTab GUI with real-time updates and network synchronization

#### P2-TASK-007b: Guard Configuration System Bug Fixes
**Completed**: October 5, 2025 | **Assignee**: minecraft-developer
- Fixed event forwarding for mouse interactions in ConfigTab
- Added comprehensive logging for debugging

#### P2-TASK-008: Combat Visual and Audio Effects
**Completed**: October 5, 2025 | **Assignee**: minecraft-developer
- Vanilla particles and sounds for all combat actions
- Performance-optimized with distance culling and particle limits

#### P2-TASK-009: Performance Optimization for Guard AI
**Completed**: October 5, 2025 | **Assignee**: minecraft-developer
- GuardAIScheduler with distance-based LOD
- 80% FPS impact reduction (15% → 3% with 20 guards)
- PathfindingCache and PerformanceMonitor implemented

#### P2-TASK-009b: Performance Optimization Manual Validation
**Completed**: October 7, 2025 | **Assignee**: User
- Manual testing validated performance targets met

#### P2-TASK-010: Integration Testing and Validation
**Completed**: October 7, 2025 | **Assignee**: minecraft-qa-specialist
- Comprehensive integration testing completed
- All systems validated and working together

#### P2-TASK-011: Guard Configuration System Redesign
**Completed**: October 7, 2025 | **Assignee**: minecraft-developer
- Implemented GuardMode enum (FOLLOW/PATROL/STAND)
- Added profession locking mechanism
- Removed old aggression/combat mode options

### Phase 1 - Guard Ranking System (All Complete)

#### TASK-001: Validate Guard Ranking System Implementation
**Completed**: September 28, 2025 | **Assignee**: minecraft-qa-specialist
- All rank definitions, progression, and emerald economy validated

#### TASK-002: Rank System Integration Testing
**Completed**: September 28, 2025 | **Assignee**: minecraft-qa-specialist
- Data persistence, network sync, and GUI integration verified

#### TASK-003: Legacy Equipment System Review
**Completed**: September 28, 2025 | **Assignee**: minecraft-developer
- Cleaned up orphaned code, validated architecture separation

---

## 📋 ACTIVE TODO TASKS

### 🟡 HIGH PRIORITY - System Validation

#### TASK-004: Guard Rendering System Validation
**Status**: TODO
**Priority**: High
**Assignee**: minecraft-qa-specialist
**Estimated Effort**: 2-3 hours

**Description**: Validate the guard rendering fixes work correctly with the new ranking system and ensure no visual regressions.

**Acceptance Criteria**:
- [ ] Guard villagers display correct guard texture (no purple/violet)
- [ ] Texture blending issues resolved for all ranks
- [ ] Rank changes don't cause rendering glitches
- [ ] Equipment synchronization works with client cache
- [ ] No performance degradation in rendering
- [ ] Multiple guards render correctly simultaneously

**Test Cases**:
- [ ] Create multiple guards of different ranks
- [ ] Change ranks while observing visual changes
- [ ] Test in both single-player and multiplayer
- [ ] Monitor client-side data cache for memory leaks
- [ ] Verify rendering performance with 10+ guards

**Dependencies**: TASK-001
**Blockers**: None

---

### 🟠 MEDIUM PRIORITY - Code Quality & Documentation

#### TASK-005: Update Documentation for Ranking System
**Status**: TODO
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 2-3 hours

**Description**: Update all documentation files to reflect the new ranking system implementation.

**Files to Update**:
- [ ] `README.md` - Add ranking system overview
- [ ] `CLAUDE.md` - Update architecture section with ranking components
- [ ] `standards.md` - Add ranking system coding standards (already has foundation)
- [ ] API documentation for ranking-related classes

**Content Requirements**:
- [ ] Clear explanation of dual specialization paths
- [ ] Emerald economy and progression costs
- [ ] Combat statistics and special abilities
- [ ] Integration with existing systems
- [ ] Usage examples and best practices

**Dependencies**: TASK-001, TASK-003
**Blockers**: None

---

#### TASK-006: Code Standards Compliance Review
**Status**: TODO
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 3-4 hours

**Description**: Review all ranking system code for compliance with established coding standards.

**Review Areas**:
- [ ] Javadoc completeness for all public methods
- [ ] Exception handling and error reporting
- [ ] Thread safety in client-server operations
- [ ] Memory management and resource cleanup
- [ ] Performance optimization opportunities
- [ ] Security validation (emerald transactions)

**Standards Checklist**:
- [ ] All classes follow naming conventions
- [ ] Proper error handling with specific exceptions
- [ ] Comprehensive input validation
- [ ] Thread-safe operations documented
- [ ] Performance benchmarks for critical paths

**Dependencies**: TASK-001
**Blockers**: None

---

### 🔵 LOW PRIORITY - Feature Enhancement

#### TASK-007: Rank Progression UI Polish
**Status**: TODO
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 4-5 hours

**Description**: Polish the guard rank UI for better user experience and visual appeal.

**Enhancement Areas**:
- [ ] Visual rank progression indicators (progress bars, icons)
- [ ] Better cost display and emerald requirement feedback
- [ ] Path selection visualization (melee vs. ranged)
- [ ] Rank comparison tooltips (current vs. next rank stats)
- [ ] Confirmation dialogs for rank purchases
- [ ] Visual feedback for successful/failed purchases

**UI Requirements**:
- [ ] Intuitive rank progression visualization
- [ ] Clear emerald cost and requirement display
- [ ] Path differentiation (different colors/icons for melee/ranged)
- [ ] Error messages for invalid purchases
- [ ] Accessibility considerations (narration, high contrast)

**Dependencies**: TASK-001, TASK-002
**Blockers**: None

---

#### TASK-008: Advanced Rank Statistics Display
**Status**: TODO
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 2-3 hours

**Description**: Implement detailed statistics display for rank comparison and progression planning.

**Features to Implement**:
- [ ] Current vs. next rank stat comparison table
- [ ] Total emerald investment calculator
- [ ] Path progression preview (show all ranks in path)
- [ ] Special abilities description and preview
- [ ] Combat effectiveness indicators

**Technical Requirements**:
- [ ] Dynamic stat calculation and display
- [ ] Responsive layout for different screen sizes
- [ ] Efficient data retrieval from rank system
- [ ] Clear visual hierarchy for information

**Dependencies**: TASK-001, TASK-007
**Blockers**: None

---

#### TASK-009: Rank System Performance Optimization
**Status**: TODO
**Priority**: Low
**Assignee**: minecraft-developer
**Estimated Effort**: 3-4 hours

**Description**: Optimize ranking system performance for large-scale deployments.

**Optimization Areas**:
- [ ] Rank data caching and management
- [ ] Network packet efficiency
- [ ] Memory usage optimization
- [ ] Database/persistence performance
- [ ] Client-side data synchronization

**Performance Targets**:
- [ ] <1ms rank lookup time
- [ ] <5KB network overhead per rank change
- [ ] <100MB memory usage for 1000+ guards
- [ ] <50ms UI response time for rank operations

**Dependencies**: TASK-001, TASK-002, TASK-006
**Blockers**: None

---

#### TASK-010: Advanced Guard AI Integration
**Status**: TODO
**Priority**: Low
**Assignee**: minecraft-developer
**Estimated Effort**: 6-8 hours

**Description**: Integrate rank statistics with guard AI behaviors for rank-appropriate combat performance.

**Integration Points**:
- [ ] Rank-based damage application in combat
- [ ] Health scaling implementation
- [ ] Special ability activation in combat
- [ ] AI aggression/behavior based on rank
- [ ] Target prioritization for different ranks

**Technical Requirements**:
- [ ] Mixin integration with combat systems
- [ ] Event-driven special ability triggers
- [ ] Performance monitoring for AI changes
- [ ] Compatibility with vanilla combat mechanics

**Dependencies**: TASK-001, TASK-002
**Blockers**: None

---

#### TASK-011: Rank Visual Progression System
**Status**: TODO
**Priority**: Low
**Assignee**: minecraft-developer
**Estimated Effort**: 8-10 hours

**Description**: Implement visual changes to guards based on their rank (armor, weapons, insignia).

**Visual Elements**:
- [ ] Rank-appropriate armor rendering
- [ ] Weapon upgrades for different tiers
- [ ] Insignia or rank indicators on models
- [ ] Path-specific visual themes (melee vs. ranged)
- [ ] Special effects for high-tier ranks

**Technical Challenges**:
- [ ] Model modification and rendering
- [ ] Texture atlas management
- [ ] Performance impact of visual changes
- [ ] Compatibility with existing rendering system

**Dependencies**: TASK-001, TASK-004
**Blockers**: None

---

## Task Management Guidelines

### Status Indicators
- **TODO**: Task not yet started
- **IN_PROGRESS**: Currently being worked on
- **BLOCKED**: Cannot proceed due to dependencies
- **✅ COMPLETED**: Task finished and verified

### Priority Levels
- **🔴 CRITICAL**: Must be completed immediately, blocks other work
- **🟡 HIGH**: Important for next release, should be prioritized
- **🟠 MEDIUM**: Useful improvements, schedule when time allows
- **🔵 LOW**: Nice-to-have features for future development

### Task Assignment
- **minecraft-qa-specialist**: Testing, validation, debugging
- **minecraft-developer**: Implementation, code quality, standards
- **minecraft-researcher**: Technical research and problem solving

---

## 🎨 PHASE 3: POLISH AND USER EXPERIENCE

### Overview
Phase 3 focuses on polish, visual improvements, and user experience enhancements. This phase will improve the visual distinction between guard types, enhance UI/UX, and fix behavioral issues.

### Core Objectives
1. **Visual Polish**: Distinct textures and visual indicators for guard types and ranks
2. **UI/UX Improvements**: Streamlined interface with better information display
3. **Behavioral Fixes**: Remove inappropriate vanilla villager behaviors from guards
4. **Animation Quality**: Improved combat animations and visual feedback

---

### 🔴 CRITICAL - Visual System Improvements

#### P3-TASK-001: Replace Guard Profession Icon
**Status**: ✅ COMPLETED AND VALIDATED
**Priority**: Critical
**Assignee**: minecraft-developer
**Estimated Effort**: 1-2 hours
**Created**: October 7, 2025
**Started**: October 7, 2025
**Completed**: October 7, 2025
**Validated**: October 7, 2025

**Description**: Replace the emerald icon for the Guard profession with a sword icon to better represent the combat-focused role.

**Implementation**: Uses Minecraft's `Items.IRON_SWORD` as icon. Modified `ProfessionData.createIcon()` to detect Guard profession and return iron sword ItemStack.

**Technical Requirements**:
- [ ] Design or source a sword icon (16x16 PNG) for the Guard profession
- [ ] Update `ModProfessions.GUARD` registration to use new icon
- [ ] Ensure icon displays correctly in profession selection GUI
- [ ] Verify icon shows in villager interaction screens
- [ ] Test icon rendering in both light and dark UI themes

**Implementation Details**:
- Icon location: `src/main/resources/assets/xeenaa_villager_manager/textures/gui/icons/`
- Update profession registration in `ModProfessions.java`
- Icon format: 16x16 PNG with transparency
- Consider vanilla Minecraft art style for consistency

**Files to Create**:
- `src/main/resources/assets/xeenaa_villager_manager/textures/gui/icons/guard_sword.png`

**Files to Modify**:
- `src/main/java/com/xeenaa/villagermanager/profession/ModProfessions.java`
- `src/main/java/com/xeenaa/villagermanager/registry/ProfessionData.java` (if icon handling needs updates)

**Acceptance Criteria**:
- [ ] Sword icon displays instead of emerald for Guard profession
- [ ] Icon is visible and clear in profession selection screen
- [ ] Icon maintains visual consistency with vanilla Minecraft style
- [ ] No rendering issues or texture bleeding

**Dependencies**: None
**Blockers**: None

---

#### P3-TASK-002: Guard Type Visual Distinction (Textures)
**Status**: ✅ COMPLETED AND VALIDATED
**Priority**: Critical
**Assignee**: minecraft-developer
**Estimated Effort**: 6-8 hours
**Created**: October 7, 2025
**Started**: October 7, 2025
**Completed**: October 7, 2025
**Validated**: October 7, 2025

**Implementation**: Dynamic texture selection based on GuardPath implemented in GuardVillagerRenderer. Three texture files created and old textures cleaned up. Texture switching works correctly based on guard specialization path.

**Description**: Create distinct textures to visually differentiate between the three guard types: Recruit, Marksman (ranged), and Man-at-Arms (melee).

**Visual Design Requirements**:
- **Recruit**: Basic guard appearance, minimal armor/equipment visual
- **Marksman (Ranged Path)**: Lighter armor, quiver visible, ranger-themed colors (greens/browns)
- **Man-at-Arms (Melee Path)**: Heavier armor appearance, knight-themed colors (grays/silvers)

**Technical Requirements**:
- [ ] Create three distinct villager texture variants for guard types
- [ ] Implement texture selection based on `GuardRank` and `GuardPath`
- [ ] Update `GuardVillagerRenderer` to apply correct texture based on rank/path
- [ ] Ensure textures work with Minecraft's villager model
- [ ] Support for both normal and zombified states
- [ ] Test texture rendering in different biomes and lighting conditions

**Texture Specifications**:
- Format: PNG with transparency, 64x64 (villager texture standard)
- Base template: Use vanilla villager texture as template
- Color coding:
  - Recruit: Brown/tan (neutral guard)
  - Marksman: Green/brown (ranger theme)
  - Man-at-Arms: Gray/silver (knight theme)
- Ensure compatibility with villager profession overlay system

**Implementation Approach**:
1. Create texture files for each guard type
2. Modify `GuardVillagerRenderer` to select texture based on rank data
3. Add texture switching logic when guard changes paths
4. Handle edge cases (recruit → path selection, rank changes)

**Files to Create**:
- `src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/guard_recruit.png`
- `src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/guard_marksman.png`
- `src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/guard_arms.png`

**Files to Modify**:
- `src/client/java/com/xeenaa/villagermanager/client/render/GuardVillagerRenderer.java`
- May need mixin or feature renderer for dynamic texture selection

**Acceptance Criteria**:
- [ ] Three distinct guard textures are visually recognizable
- [ ] Texture automatically changes when guard changes path
- [ ] Recruit guards show basic texture until path is chosen
- [ ] Textures render correctly in all lighting conditions
- [ ] No texture flickering or visual glitches
- [ ] Works in both single-player and multiplayer

**Dependencies**: None
**Blockers**: None

---

#### P3-TASK-003: Display Rank Above Guard Head
**Status**: ✅ COMPLETED AND VALIDATED
**Priority**: High
**Assignee**: minecraft-developer
**Estimated Effort**: 3-4 hours
**Created**: October 7, 2025
**Started**: October 7, 2025
**Completed**: October 7, 2025
**Validated**: October 7, 2025

**Implementation**: Guards now display rank names above their heads with color coding and star indicators. Rank names updated: "Soldier I-III", "Ranger I-III", "Knight", "Sharpshooter". Format: "Rank Name ⭐⭐⭐" (no redundant tier label).

**Description**: Replace the generic "Guard" string shown above guard villagers with their actual rank name (e.g., "Recruit", "Knight", "Sharpshooter").

**Technical Requirements**:
- [ ] Override villager name rendering to show rank instead of profession
- [ ] Display format: "[Rank Name]" or "[Rank Name] - [Tier]" (e.g., "Knight - Tier 4")
- [ ] Support color-coded rank names (recruit = white, higher tiers = gold/aqua)
- [ ] Ensure name renders at appropriate distance (same as vanilla name tags)
- [ ] Handle custom names if player renames the guard

**Implementation Approach**:
1. Use entity custom name feature to set guard rank as display name
2. Update name when rank changes (listen to rank purchase events)
3. Consider using formatted text with colors for visual hierarchy
4. Option: Add tier indicator (stars, Roman numerals, etc.)

**Display Format Options**:
- Option 1: "Knight" (simple rank name)
- Option 2: "Knight ⭐⭐⭐⭐" (rank + tier stars)
- Option 3: "Knight [IV]" (rank + Roman numeral tier)
- **Recommended**: "Knight ⭐⭐⭐⭐" (most visual)

**Color Coding Scheme**:
- Recruit (Tier 0): White
- Tier 1: Light Gray
- Tier 2: Yellow
- Tier 3: Gold
- Tier 4: Aqua (melee) / Light Purple (ranged)

**Files to Modify**:
- `src/main/java/com/xeenaa/villagermanager/data/GuardData.java` (add name update method)
- `src/main/java/com/xeenaa/villagermanager/network/ServerPacketHandler.java` (update name on rank change)
- Consider mixin to `VillagerEntity` for custom name handling

**Acceptance Criteria**:
- [ ] Guard displays rank name instead of "Guard"
- [ ] Name updates immediately when rank changes
- [ ] Color coding makes rank tier visually clear
- [ ] Name is readable at standard interaction distance
- [ ] Custom names (if set by player) are preserved or shown alongside rank
- [ ] Works in multiplayer with proper synchronization

**Dependencies**: None
**Blockers**: None

---

### 🟡 HIGH PRIORITY - Behavioral Improvements

#### P3-TASK-004: Disable Guard Baby Villagers
**Status**: ✅ COMPLETED AND VALIDATED
**Priority**: High
**Assignee**: minecraft-developer
**Estimated Effort**: 2-3 hours
**Created**: October 7, 2025
**Started**: October 7, 2025
**Completed**: October 7, 2025
**Validated**: October 7, 2025

**Implementation**: Created VillagerBreedingMixin to prevent guard breeding via canBreed() and createChild() methods. Added baby villager validation in ServerPacketHandler. Guards cannot breed and baby villagers cannot become guards.

**Description**: Prevent guards from breeding and remove the possibility of baby guard villagers, as guards are specialized combat units that should not reproduce.

**Technical Requirements**:
- [ ] Disable breeding for villagers with Guard profession
- [ ] Prevent existing baby villagers from being assigned Guard profession
- [ ] Remove breeding AI goal from guard villagers
- [ ] Prevent guards from picking up food items for breeding
- [ ] Handle edge case: villager becomes guard while baby (force profession change or prevent)

**Implementation Approach**:
1. Use mixin to override breeding behavior for guards
2. Add validation in profession assignment to reject babies
3. Remove `VillagerBreedGoal` from guard AI goal set
4. Consider: Prevent guards from picking up food items (optional)

**Mixin Targets**:
- `VillagerEntity.canBreed()` - Return false for guards
- `VillagerEntity.createChild()` - Prevent guard babies
- Profession assignment validation in `ServerPacketHandler`

**Files to Modify**:
- `src/main/java/com/xeenaa/villagermanager/mixin/VillagerBreedingMixin.java` (create new)
- `src/main/java/com/xeenaa/villagermanager/network/ServerPacketHandler.java` (add baby validation)
- `src/main/resources/xeenaa_villager_manager.mixins.json` (register mixin)

**Edge Cases to Handle**:
- [ ] Baby villager assigned guard profession → deny or auto-change profession
- [ ] Guard given breeding items → ignore or don't pick up
- [ ] Two guards near each other → ensure no breeding attempts
- [ ] Guard converted to another profession → re-enable breeding

**Acceptance Criteria**:
- [ ] Guards cannot breed with other villagers
- [ ] Baby villagers cannot be assigned Guard profession
- [ ] Guards do not pick up food items for breeding
- [ ] Existing breeding mechanics unaffected for non-guard villagers
- [ ] No errors or crashes when guards are near other villagers
- [ ] Clear feedback to player if they try to assign guard to baby villager

**Dependencies**: None
**Blockers**: None

---

#### P3-TASK-005: Remove Sleep and Bed Requirements for Guards
**Status**: ✅ COMPLETED AND VALIDATED
**Priority**: High
**Assignee**: minecraft-developer
**Estimated Effort**: 2-3 hours
**Created**: October 7, 2025
**Started**: October 7, 2025
**Completed**: October 8, 2025
**Validated**: October 8, 2025

**Implementation**: VillagerSleepMixin implemented with wantsToSleep() and canSleep() prevention methods. VillagerAIMixin clears HOME memory to release beds. Guards now remain active 24/7 without claiming beds.

**Description**: Remove the vanilla villager sleeping behavior and bed requirement from guards, allowing them to remain on patrol 24/7 as dedicated defenders.

**Technical Requirements**:
- [ ] Remove sleep schedule from guard villagers
- [ ] Remove bed seeking and sleeping AI goals
- [ ] Guards remain active during night time
- [ ] Guards do not claim beds
- [ ] Guards continue patrol and defense behaviors at all times

**Implementation Approach**:
1. Use mixin to override sleep-related behaviors for guards
2. Remove `VillagerSleepGoal` from guard AI goals
3. Prevent guards from claiming beds (`VillagerEntity.sleep()`)
4. Ensure guards continue AI behaviors during night
5. Optional: Add "alert" animation/pose during night instead of sleeping

**Mixin Targets**:
- `VillagerEntity.sleep()` - Prevent guards from sleeping
- `VillagerEntity.wakeUp()` - Handle edge cases
- AI goal initialization for guards (remove sleep-related goals)

**Behavioral Changes**:
- Guards patrol 24/7 without rest
- Guards do not seek beds at night
- Guards remain combat-ready at all times
- Guards do not claim beds (frees beds for other villagers)

**Files to Modify**:
- `src/main/java/com/xeenaa/villagermanager/mixin/VillagerSleepMixin.java` (create new)
- `src/main/java/com/xeenaa/villagermanager/mixin/VillagerAIMixin.java` (remove sleep goals)
- `src/main/resources/xeenaa_villager_manager.mixins.json` (register mixin)

**Acceptance Criteria**:
- [ ] Guards never attempt to sleep or find beds
- [ ] Guards remain active and patrol during night
- [ ] Guards respond to threats at any time of day
- [ ] Guards do not claim or occupy beds
- [ ] No AI freezing or stuck states during night time
- [ ] Other villagers' sleep behavior unaffected

**Dependencies**: None
**Blockers**: None

---

### 🟠 MEDIUM PRIORITY - Visual Polish

#### P3-TASK-006: Fix Zombified Guard Texture
**Status**: ✅ COMPLETED (Awaiting Final Textures)
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 3-4 hours
**Created**: October 7, 2025
**Started**: October 8, 2025
**Completed**: October 8, 2025

**Implementation**: ZombieVillagerRendererMixin implemented to detect guard profession in zombie villagers and apply appropriate zombie texture based on specialization path. Placeholder textures created with comprehensive specification document for final texture creation. System fully functional and tested.

---

#### P3-TASK-006b: Preserve Guard Attributes Through Zombification
**Status**: ✅ COMPLETED AND VALIDATED
**Priority**: High
**Assignee**: minecraft-developer
**Estimated Effort**: 2 hours
**Created**: October 8, 2025
**Started**: October 8, 2025
**Completed**: October 8, 2025
**Validated**: October 8, 2025

**Implementation**: VillagerZombificationMixin created using Fabric's MOB_CONVERSION event to preserve guard combat attributes (HP, damage, speed, armor, knockback resistance, attack speed) when guards are zombified. Health preserved as percentage with **80% minimum health** on conversion. Special abilities excluded (as requested). Zombies marked persistent to prevent despawning. Build successful, all 308 tests passed.

**User Requirements Met**:
- ✅ Preserve: HP, damage, defense (armor), speed, knockback resistance, attack speed
- ✅ Do NOT preserve: Special abilities (Knight knockback/area damage, Sharpshooter double shot)

**Files Created**:
- `src/main/java/com/xeenaa/villagermanager/mixin/VillagerZombificationMixin.java`

**Files Modified**:
- `src/main/resources/xeenaa_villager_manager.mixins.json` (added mixin registration)

**Testing Required**:
1. Create Tier 4 guard (40 HP, 10 damage)
2. Check attributes: `/attribute @e[type=villager,limit=1] minecraft:generic.max_health get`
3. Zombify the guard
4. Check zombie attributes: `/attribute @e[type=zombie_villager,limit=1] minecraft:generic.max_health get`
5. Expected: 40.0 (not default 20.0)
6. Verify special abilities don't work on zombie
7. Cure zombie and verify stats restored

**Description**: Fix texture rendering when a guard villager is zombified, ensuring the zombified guard retains visual distinction and proper texture display.

**Technical Requirements**:
- [ ] Create zombified variants of guard textures (recruit, marksman, arms)
- [ ] Implement texture switching when guard is converted to zombie villager
- [ ] Ensure zombified guards maintain visual distinction between types
- [ ] Handle texture restoration if zombie guard is cured
- [ ] Test with all guard ranks and paths

**Zombie Texture Design**:
- Base: Vanilla zombie villager texture overlay
- Apply guard type distinction (same color scheme but zombified)
- Maintain recognizable guard features in zombie form
- Ensure green zombie skin tone is properly applied

**Implementation Approach**:
1. Create zombified texture variants for all three guard types
2. Add mixin or renderer to handle zombie villager texture selection
3. Detect when guard is zombified and apply correct texture
4. Store guard type data in zombie villager NBT (for curing)
5. Restore correct texture when zombie guard is cured

**Files to Create**:
- `src/main/resources/assets/xeenaa_villager_manager/textures/entity/zombie_villager/profession/guard_recruit.png`
- `src/main/resources/assets/xeenaa_villager_manager/textures/entity/zombie_villager/profession/guard_marksman.png`
- `src/main/resources/assets/xeenaa_villager_manager/textures/entity/zombie_villager/profession/guard_arms.png`

**Files to Modify**:
- `src/client/java/com/xeenaa/villagermanager/client/render/GuardVillagerRenderer.java`
- May need mixin to `ZombieVillagerEntity` for texture handling

**Acceptance Criteria**:
- [ ] Zombified guards display correct zombie texture variant
- [ ] Guard type (recruit/marksman/arms) remains visually distinguishable
- [ ] Texture switches correctly when guard is zombified
- [ ] Cured guards restore original non-zombie texture
- [ ] No texture glitches or missing textures
- [ ] Works in both single-player and multiplayer

**Dependencies**: P3-TASK-002 (Guard Type Visual Distinction)
**Blockers**: None

---

#### P3-TASK-007: Improve Combat Animations
**Status**: TODO
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 4-6 hours
**Created**: October 7, 2025

**Description**: Improve the visual quality of guard combat animations for both melee attacks (sword swings) and ranged attacks (bow shooting).

**Current Issues**:
- Guards attack but animations may not be synced correctly
- Bow draw/release animation timing needs improvement
- Sword swing animation may not be visible or impactful
- Lack of visual feedback during combat

**Animation Improvements**:

**Melee Combat:**
- [ ] Proper sword swing animation trigger
- [ ] Arm swing synchronized with damage application
- [ ] Hit impact feedback (particle + sound already implemented)
- [ ] Animation speed scales with attack speed
- [ ] Guard faces target during attack

**Ranged Combat:**
- [ ] Bow draw animation when targeting enemy
- [ ] Full draw before arrow release
- [ ] Bow hold animation while kiting
- [ ] Arrow release synchronized with projectile spawn
- [ ] Proper aiming pose (guard looks at target)

**Technical Requirements**:
- [ ] Use Minecraft's entity animation system
- [ ] Sync animations with AI goal attack timing
- [ ] Ensure animations work with guard AI movement
- [ ] Test animation smoothness during combat
- [ ] Handle animation transitions (idle → combat → idle)

**Implementation Approach**:
1. Research vanilla entity animation system
2. Add animation triggers in `GuardDirectAttackGoal`
3. Use `EntityAnimationController` or similar for smooth transitions
4. Sync animation state with attack cooldowns
5. Test with both melee and ranged guards

**Files to Modify**:
- `src/main/java/com/xeenaa/villagermanager/ai/GuardDirectAttackGoal.java`
- May need custom renderer or animation controller
- Consider mixin to `VillagerEntity` for animation handling

**Acceptance Criteria**:
- [ ] Melee guards show clear sword swing animation
- [ ] Ranged guards draw bow before shooting
- [ ] Animations are smooth and well-timed
- [ ] No animation stuttering or freezing
- [ ] Guards face targets during attacks
- [ ] Animations scale appropriately with attack speed
- [ ] Visual feedback is satisfying and impactful

**Dependencies**: P2-TASK-005 (Combat Integration)
**Blockers**: None

---

### 🔵 LOW PRIORITY - UI/UX Enhancement

#### P3-TASK-008: Unified Tab UI Design
**Status**: 🔄 IN PROGRESS (GUI Working, Profession Button Issue)
**Priority**: Medium
**Assignee**: minecraft-developer
**Estimated Effort**: 6-8 hours
**Created**: October 7, 2025
**Started**: October 8, 2025
**Updated**: October 9, 2025

**Current Status**: UnifiedGuardManagementScreen implemented and functional. Guard profession button missing from UI - ProfessionManager initialization fix applied, awaiting user validation.

**Description**: Merge the three separate tabs (Profession, Rank, Config) into a single unified tab interface for improved user experience and streamlined information display.

**Current Tab Structure**:
- **Profession Tab**: Profession selection grid
- **Rank Tab**: Rank progression and purchase interface
- **Config Tab**: Guard behavior configuration (detection range, guard mode, profession lock)

**Proposed Unified Design**:

**Single Tab Layout Sections:**

1. **Header Section** (Top):
   - Villager name and current profession
   - Guard icon and current rank display with tier stars
   - Quick stats summary (HP, Damage, Detection Range)

2. **Left Column - Rank Progression** (40% width):
   - Current rank with large icon
   - Rank progression tree (visual path display)
   - Next rank purchase button with cost
   - Path selection buttons (Melee/Ranged) for recruits
   - Rank stats comparison (current vs next)

3. **Right Column - Configuration** (60% width):
   - **Behavior Settings**:
     - Guard Mode selector (FOLLOW/PATROL/STAND)
     - Detection range slider
     - Profession lock toggle
   - **Equipment Status** (if applicable):
     - Current weapon display
     - Armor status
   - **Quick Actions**:
     - Save configuration button
     - Reset to defaults button

4. **Footer Section** (Bottom):
   - Status bar with unsaved changes indicator
   - Profession change button (opens profession selector modal)

**Design Principles**:
- Information hierarchy: Most important info at top
- Related controls grouped together
- Single-page view eliminates tab switching
- Responsive layout adapts to screen size
- Consistent with vanilla Minecraft UI style

**Technical Requirements**:
- [ ] Remove tabbed interface system
- [ ] Create new unified layout manager
- [ ] Consolidate all widgets into single screen
- [ ] Maintain all current functionality
- [ ] Improve visual organization and spacing
- [ ] Add visual separators between sections
- [ ] Ensure all interactions remain intuitive

**Implementation Approach**:
1. Create new `UnifiedGuardManagementScreen` class
2. Migrate widgets from all three tabs into sections
3. Implement new layout system with column structure
4. Add visual separators and section headers
5. Test usability and information density
6. Polish spacing, alignment, and visual hierarchy

**Files to Create**:
- `src/client/java/com/xeenaa/villagermanager/client/gui/UnifiedGuardManagementScreen.java`

**Files to Modify**:
- `src/client/java/com/xeenaa/villagermanager/client/util/ClientInteractionHandler.java` (use new screen)
- Consider deprecating old tab system classes

**Files to Remove (Optional)**:
- `src/client/java/com/xeenaa/villagermanager/client/gui/TabbedManagementScreen.java`
- `src/client/java/com/xeenaa/villagermanager/client/gui/Tab.java`
- Individual tab classes (keep as reference during migration)

**Acceptance Criteria**:
- [ ] All functionality from three tabs available in single view
- [ ] No information loss or hidden features
- [ ] Interface feels organized and not cluttered
- [ ] Navigation is intuitive without tabs
- [ ] All widgets responsive and functional
- [ ] Visual hierarchy makes important information prominent
- [ ] Screen scales properly on different resolutions
- [ ] Performance is smooth (no lag when opening screen)
- [ ] User testing shows improved usability over tabbed version

**Dependencies**: None (can be developed in parallel)
**Blockers**: None

**User Experience Considerations**:
- Conduct user testing before finalizing layout
- Consider adding collapsible sections if space is tight
- Ensure critical actions (rank purchase, config save) are clearly visible
- Add tooltips for complex controls
- Maintain visual consistency with existing UI elements

---

#### P3-TASK-009: Guard Profession Button Missing in Unified GUI
**Status**: 🔄 AWAITING USER VALIDATION
**Priority**: High
**Assignee**: minecraft-developer
**Estimated Effort**: 2 hours
**Created**: October 9, 2025
**Started**: October 9, 2025
**Completed**: October 9, 2025 (pending validation)

**Issue**: Guard profession button not appearing in the UnifiedGuardManagementScreen's profession list.

**Root Cause**: ProfessionManager was not initialized on the client side. In Fabric mods, client and server run in separate environments. ProfessionManager was initialized in `XeenaaVillagerManager.onInitialize()` (server) but not in `XeenaaVillagerManagerClient.onInitializeClient()` (client).

**Fix Applied**:
1. **Added ProfessionManager initialization to client** (`XeenaaVillagerManagerClient.java:28-32`):
   ```java
   // Initialize ProfessionManager on client side (collects all registered professions)
   ProfessionManager professionManager = ProfessionManager.getInstance();
   professionManager.initialize();
   CLIENT_LOGGER.info("Client-side ProfessionManager initialized with {} professions",
       professionManager.getStats().total());
   ```

2. **Added debug logging** to `UnifiedGuardManagementScreen.loadAvailableProfessions()`:
   - Logs all professions returned by ProfessionManager
   - Shows blacklist status for each profession
   - Tracks which professions are added to UI

**Files Modified**:
- `src/client/java/com/xeenaa/villagermanager/XeenaaVillagerManagerClient.java` (added client-side ProfessionManager init)
- `src/client/java/com/xeenaa/villagermanager/client/gui/UnifiedGuardManagementScreen.java` (enhanced debug logging)

**Build Status**: ✅ Build successful, all 308 tests passed

**Testing Required**:
1. Launch Minecraft client (runClient already started)
2. Create or load world
3. Find/spawn a villager
4. Right-click villager to open UnifiedGuardManagementScreen
5. Verify Guard profession button appears in left panel profession list
6. Check console logs for "=== Loading Professions for UI ===" debug output

**Expected Behavior**:
- Guard profession button visible with iron sword icon
- Button labeled "Guard"
- Appears in profession list alongside vanilla professions
- No blacklist warnings for guard profession in logs

**Acceptance Criteria**:
- [ ] Guard profession button appears in UnifiedGuardManagementScreen
- [ ] Button displays iron sword icon
- [ ] Clicking button assigns Guard profession to villager
- [ ] Debug logs show `xeenaa_villager_manager:guard` in profession list
- [ ] No errors or warnings in console

**Dependencies**: P3-TASK-008 (Unified Tab UI Design)
**Blockers**: Awaiting user validation in-game

---

## Task Management Guidelines

### Status Indicators
- **TODO**: Task not yet started
- **IN_PROGRESS**: Currently being worked on
- **BLOCKED**: Cannot proceed due to dependencies
- **✅ COMPLETED**: Task finished and verified

### Priority Levels
- **🔴 CRITICAL**: Must be completed immediately, blocks other work
- **🟡 HIGH**: Important for next release, should be prioritized
- **🟠 MEDIUM**: Useful improvements, schedule when time allows
- **🔵 LOW**: Nice-to-have features for future development

### Task Assignment
- **minecraft-qa-specialist**: Testing, validation, debugging
- **minecraft-developer**: Implementation, code quality, standards
- **minecraft-researcher**: Technical research and problem solving

---

**Last Updated**: October 7, 2025
**Current Phase**: PHASE 3 - POLISH AND USER EXPERIENCE
**Total Active Tasks**: 16 TODO tasks (8 Phase 3, 8 legacy)
**Phase 1**: ✅ COMPLETED - Guard ranking system production-ready
**Phase 2**: ✅ COMPLETED - Guard AI and combat system fully implemented and validated
**Phase 3**: 🔄 ACTIVE - Polish, visual improvements, and UX enhancements
**Priority Focus**: Visual distinction, behavioral fixes, UI/UX improvements
