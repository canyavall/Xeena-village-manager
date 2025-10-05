# Guard Combat System - Automated Test Suite

## Overview

This test suite provides comprehensive automated testing for the guard combat system in Xeenaa Villager Manager. The tests validate that the system works correctly after manual testing has confirmed the features function as expected.

## Test Files

### 1. GuardRankStatsTest.java
**Purpose**: Validates rank statistics including HP, damage, movement speed, and path IDs.

**Tests**:
- HP values for all tiers (10, 14, 18, 22, 26)
- Base damage values (0.5, 1.5, 2.5, 3.0-3.5, 4.0-4.5)
- Movement speed ranges (melee: 0.4-0.6, ranged: 0.45-0.65)
- Path IDs are correct ("melee" and "ranged", NOT "marksman")
- Tier level assignments (0-4)
- Movement speed scaling with rank progression
- Ranged guards are faster than melee at same tier

**Critical Validation**:
- ✓ Path ID "ranged" not "marksman" (bug fix validation)
- ✓ All path queries return correct path enum

### 2. GuardCombatMechanicsTest.java
**Purpose**: Tests combat mechanics including attack cooldowns, damage calculation, area damage, and retreat.

**Tests**:
- Attack cooldowns (melee: 20 ticks, ranged: 30 ticks)
- Damage calculation (base + weapon damage)
- Tier 4 melee area damage (30% of base, 1.5 block radius)
- Retreat behavior (starts at 20% HP, stops at 50%)
- Knockback scaling by tier
- Regeneration (Tier 3+: 0.5-1.0 HP/sec)
- Status effects (Tier 4 Resistance I, Knight Slowness II)
- Ranged distance management (8-15 blocks preferred)
- Arrow Slowness effect (Slowness I for 3 seconds)

**Coverage**:
- ✓ All attack timing mechanics
- ✓ Damage formulas
- ✓ Health thresholds
- ✓ Status effect durations

### 3. GuardSpecialAbilitiesTest.java
**Purpose**: Tests special abilities for Tier 4 ranks (Knockback and Double Shot).

**Tests**:
- Knight has Knockback Strike ability (20% trigger chance, 1.5 strength)
- Sharpshooter has Double Shot ability (100% trigger chance, fires at 2 targets)
- Abilities only available at Tier 4
- Double Shot detection range (15 blocks)
- Double Shot arrow properties (Slowness I for 3 seconds, accuracy 8.0, velocity 1.6)
- Ability cooldowns (Double Shot: 180 ticks / 9 seconds)
- Path ID validation (CRITICAL: checks for "ranged" not "marksman")
- Target validation (different, alive, line of sight)
- Ability logging and descriptions

**Critical Validation**:
- ✓ Path validation uses "ranged" not "marksman"
- ✓ GuardSpecialAbilities.java line 179 checks pathId.equals("ranged")
- ✓ VillagerAIMixin.java line 135 checks pathId.equals("ranged")
- ✓ Double Shot fires at 2 different targets
- ✓ Knockback triggers on 20% of attacks

### 4. GuardPathSpecializationTest.java
**Purpose**: Tests path selection, progression, and validation.

**Tests**:
- Recruit can choose melee or ranged (2 options)
- Once chosen, progression stays in path
- Cannot switch between paths
- Cannot go back to recruit
- Path progression sequences (I → II → III → Tier 4)
- Path IDs and display names
- Rank purchase validation (cannot skip ranks)
- Emerald costs (0, 15, 20, 45, 75)
- Previous rank tracking
- Path descriptions

**Coverage**:
- ✓ All path transition rules
- ✓ Rank progression chains
- ✓ Cost escalation
- ✓ Path locking mechanism

### 5. GuardAIGoalPriorityTest.java
**Purpose**: Tests AI goal system including priorities, initialization, and management.

**Tests**:
- Goal priorities (0: DirectAttack, 1: DefendVillage, 2: Retreat, 3: Combat, 5: FollowVillager, 7: Patrol)
- Goal control flags (MOVE, LOOK, TARGET)
- Flee goal removal (initial and continuous)
- Brain panic clearing
- Path-specific combat goals (melee vs ranged)
- Target detection (16 blocks, every 10 ticks)
- Attack cooldown mechanics
- Auto-equipment (weapons, tier-based)
- Weapon properties (unbreakable, 0% drop chance)
- Tier-based swords (iron → diamond → netherite)
- Goal reinitialization on specialization change
- Special abilities integration
- Passive effects handling

**Coverage**:
- ✓ All AI goal priorities and ordering
- ✓ Goal control mechanisms
- ✓ Equipment management
- ✓ Initialization and cleanup

## Running the Tests

### Command Line

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests GuardRankStatsTest

# Run tests with verbose output
./gradlew test --info

# Run tests and generate report
./gradlew test
# Report located at: build/reports/tests/test/index.html
```

### IDE (IntelliJ IDEA / Eclipse)

1. Right-click on test file or test package
2. Select "Run Tests" or "Run All Tests"
3. View results in test runner window

## Test Organization

```
src/test/java/com/xeenaa/villagermanager/tests/
├── GuardRankStatsTest.java          # Rank statistics validation
├── GuardCombatMechanicsTest.java    # Combat mechanics validation
├── GuardSpecialAbilitiesTest.java   # Special abilities validation (CRITICAL)
├── GuardPathSpecializationTest.java # Path system validation
└── GuardAIGoalPriorityTest.java     # AI goal system validation
```

## Critical Bug Validations

These tests specifically validate the critical path ID bug fix:

1. **Path ID Validation** (GuardRankStatsTest)
   - Ranged path ID is "ranged" not "marksman"
   - Path enum returns correct IDs

2. **Ability Path Checks** (GuardSpecialAbilitiesTest)
   - GuardSpecialAbilities checks pathId.equals("ranged")
   - VillagerAIMixin checks pathId.equals("ranged")
   - All ranged ability validations use "ranged"

3. **Path Specialization** (GuardPathSpecializationTest)
   - Path system enforces "ranged" ID
   - Progression maintains correct path

## Test Coverage Summary

| Category | Tests | Coverage |
|----------|-------|----------|
| Rank Stats | 25 | HP, damage, speed, path IDs |
| Combat Mechanics | 35 | Cooldowns, damage, retreat, regeneration |
| Special Abilities | 40 | Knockback, Double Shot, path validation |
| Path Specialization | 30 | Selection, progression, costs |
| AI Goals | 45 | Priorities, initialization, equipment |
| **TOTAL** | **175** | **All combat systems** |

## Expected Results

All tests should **PASS** if the combat system is working correctly:

- ✅ All HP values match design spec
- ✅ All damage values match design spec
- ✅ Movement speed ranges are correct
- ✅ Path IDs are "melee" and "ranged" (NOT "marksman")
- ✅ Attack cooldowns are correct (20 ticks melee, 30 ticks ranged)
- ✅ Tier 4 abilities trigger (Knockback and Double Shot)
- ✅ Double Shot fires at 2 different targets
- ✅ Path progression is enforced
- ✅ AI goals have correct priorities
- ✅ Equipment scales with tier

## Debugging Failed Tests

If any tests fail:

1. **Check the failure message** - it will indicate exactly what value was expected vs actual
2. **Review the source file** - tests reference specific line numbers
3. **Verify manual testing** - ensure the feature works correctly in-game first
4. **Check logs** - all combat actions are logged for debugging

## Adding New Tests

When adding new features:

1. Create tests AFTER manual validation confirms the feature works
2. Follow existing test naming conventions
3. Use descriptive test names with `@DisplayName`
4. Include expected values in assertion messages
5. Group related tests together
6. Add comments for complex validations

## Notes

- These tests validate the system AS IT WORKS in manual testing
- Tests document expected behavior for regression prevention
- All logging statements are preserved in source code
- Tests are designed to catch regressions after code changes
- Path ID validation is CRITICAL - "ranged" not "marksman"
