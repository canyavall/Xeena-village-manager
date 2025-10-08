# Development Changelog

## Active Development
**Current Focus**: Phase 3 - Polish and User Experience

## Latest Session (October 8, 2025)

### P3-TASK-005: Remove Sleep and Bed Requirements for Guards ✅
- **Implementation**: VillagerSleepMixin with wantsToSleep() and canSleep() prevention
- **Result**: Guards remain active 24/7, never claim beds
- **Status**: COMPLETED AND VALIDATED

### P3-TASK-006: Fix Zombified Guard Texture ✅
- **Implementation**: ZombieVillagerRendererMixin for dynamic texture selection
- **Files Created**: 3 placeholder zombie textures + specification document
- **Status**: COMPLETED (awaiting final zombie-themed textures from user)

### P3-TASK-006b: Preserve Guard Attributes Through Zombification ✅
- **Implementation**: VillagerZombificationMixin using MOB_CONVERSION event
- **Attributes Preserved**: HP, damage, speed, armor, knockback resistance, attack speed
- **Attributes Excluded**: Special abilities (Knight/Sharpshooter abilities)
- **Health Handling**: Percentage-based preservation with **80% minimum health** on conversion
- **Death Prevention**: Guards now convert at 80% health instead of dying at 0 HP
- **Zombie Persistence**: Prevents valuable guards from despawning
- **Build Status**: SUCCESS - All 308 tests passed
- **Status**: COMPLETED (awaiting manual in-game testing)
- **Testing Commands**:
  ```
  /attribute @e[type=villager,limit=1] minecraft:generic.max_health get
  /attribute @e[type=zombie_villager,limit=1] minecraft:generic.max_health get
  ```

### P3-TASK-007: Improve Ranged Guard Combat Animations ✅
- **Implementation**: Enhanced bow animation and arrow spawning in GuardDirectAttackGoal
- **Fixes Applied**:
  - **Bow Draw Animation**: Replaced `swingHand()` with `setCurrentHand()` for proper bow hold
  - **Arrow Spawn Position**: Arrows now spawn from guard's hand position (eye level - 0.1, offset 0.3 blocks to side)
  - **Continuous Draw Animation**: Guards hold bow drawn while aiming at targets (cooldown > 20 ticks)
  - **Proper Trajectory Calculation**: Arrow velocity calculated from actual spawn position
- **Technical Details**:
  - Arrow spawn at guard eye height minus 0.1 blocks (bow hand position)
  - Horizontal offset based on guard yaw rotation (right hand position)
  - Bow animation triggers when guard is within shooting range (4-16 blocks)
- **Build Status**: SUCCESS - All 308 tests passed
- **Status**: COMPLETED (ready for in-game testing)

### Test Coverage Enhancement ✅
- **Created**: Comprehensive test suite for today's features (44 new test cases)
- **Test Files Created**:
  1. **GuardZombificationTest.java** (17 tests): Attribute preservation, health percentage, death prevention, special abilities exclusion, profession data, curing/restoration
  2. **GuardSleepBehaviorTest.java** (11 tests): Sleep prevention, bed claiming prevention, 24/7 activity, AI goal integrity
  3. **GuardBreedingPreventionTest.java** (8 tests): Breeding prevention, item interaction, population control, profession logic
  4. **GuardCombatBehaviorTest.java** (8 new tests): Ranged combat animation validation for P3-TASK-007
- **Test Organization**: All tests use nested classes for logical grouping with clear documentation
- **Build Status**: SUCCESS - All 339 tests passed ✅
- **Coverage**: Tests validate P3-TASK-005, P3-TASK-006b, and P3-TASK-007 implementations

## Completed Features

### v1.0 - Core Profession Management (2025-09-16)
- ✅ Shift+right-click GUI for villager profession changes
- ✅ Full mod compatibility and persistence

### v1.1 - Guard Profession Foundation (2025-09-18)
- ✅ Custom Guard profession with Guard Post workstation
- ✅ Tabbed management interface (Equipment tab for guards)
- ✅ Configuration system with GuardSettings
- ✅ GUI blur fixes and layout improvements

### v1.2 - Guard Rendering System (2025-09-21)
- ✅ Fixed purple/violet villager texture issues
- ✅ Client-server equipment synchronization
- ✅ SimplifiedGuardRenderer implementation
- ❗ **Known Issue**: Texture blending still occurs (guard + villager mix)
- 📝 **Technical Analysis**: Complex villager layered texture system identified

### v1.3 - Guard Ranking System (2025-09-28)
- ✅ Dual specialization paths (Melee → Knight, Ranged → Sharpshooter)
- ✅ 5-tier progression with emerald economy (155 emeralds max)
- ✅ Combat statistics scaling and special abilities
- ✅ **Legacy System Cleanup**: Removed orphaned equipment code, validated system integration

## Critical Issues Identified (September 28, 2025)
- **BUG-001**: Guard name translation missing (shows raw identifier)
- **BUG-002**: Guard data not cleaned on profession change, emeralds lost
- **BUG-003**: Emerald cost not visible when player has 0 emeralds
- **BUG-004**: Specialization paths not locked after first choice
- **BUG-005**: Rank tab title missing on first load
- **BUG-006**: Rank purchases fail after first upgrade until refresh
- ✅ **BUG-010**: Blurry Profession Change Alert Dialog - Fixed (2025-09-28)

## Bug Fixes Completed (September 28, 2025)

### BUG-010: Blurry Profession Change Alert Dialog
- **Issue**: GuardProfessionChangeScreen appeared blurry and hard to read
- **Root Cause**: Missing renderBackground() override allowed Minecraft's blur shader to be applied
- **Solution**:
  - Overrode renderBackground() method to bypass blur shader
  - Used simple semi-transparent overlay (0x66000000) instead of blur effect
  - Improved dialog background contrast (solid dark gray #2C2C2C with white border)
  - Added text shadows to all dialog text for better readability
- **Implementation**: Followed same pattern as TabbedManagementScreen blur fix
- **Result**: Dialog now renders crisp and clearly readable at all GUI scales

## Technical Debt
- **Texture Blending**: Requires mixin-based solution for complete texture override
- **Equipment Rendering**: Visual equipment display not yet implemented
- **Legacy Systems**: Potential conflicts between ranking and equipment systems