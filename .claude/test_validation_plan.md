# Test Validation Plan - Profession Management System

## Overview
This document outlines comprehensive tests to validate all profession management features and ensure no regressions occur in future changes.

## Test Categories

### 1. Profession Selection & Filtering Tests

#### Test Case 1.1: Current Profession Exclusion
- **Scenario**: Open profession tab for villager with existing profession
- **Expected**: Current profession should NOT appear in selection list
- **Test Data**: All profession types (guard, farmer, toolsmith, etc.)
- **Validation**: Count available professions = total professions - 1 (current)

#### Test Case 1.2: Profession List Refresh
- **Scenario**: Change profession, then reopen GUI
- **Expected**: New current profession excluded, previous profession now available
- **Test Steps**:
  1. Open GUI for farmer villager
  2. Verify farmer not in list, guard IS in list
  3. Change to guard profession
  4. Reopen GUI
  5. Verify guard not in list, farmer IS in list

### 2. Guard Profession Workflow Tests

#### Test Case 2.1: Guard Assignment Flow
- **Scenario**: Assign guard profession to non-guard villager
- **Expected**:
  - Window refreshes to show 2 tabs (profession + rank)
  - Automatically switches to rank tab
  - Guard data created and synced
- **Validation**: Log contains "Guard profession selected - refreshing tabs"

#### Test Case 2.2: Guard to Other Profession Flow
- **Scenario**: Change guard villager to different profession
- **Expected**:
  - Emerald loss warning dialog appears
  - After confirmation, window closes completely
  - Guard data cleaned up
- **Validation**: No GUI logs after profession change completion

#### Test Case 2.3: Emerald Loss System
- **Scenario**: Change guard with purchased ranks to another profession
- **Expected**:
  - Correct emerald count displayed in warning
  - Emeralds actually deducted from player
  - Guard data removed
- **Test Data**: Guards with 0, 10, 35, 155 emerald investments

### 3. Tab Navigation Tests

#### Test Case 3.1: Tab Visibility After Guard Assignment
- **Scenario**: Assign guard profession
- **Expected**:
  - Both "Profession" and "Rank" tab titles visible
  - Can switch between tabs seamlessly
  - Tab buttons properly recreated
- **Validation**: GUI logs show "2 tabs" and successful tab switches

#### Test Case 3.2: Tab Hiding After Guard Removal
- **Scenario**: Change guard to non-guard profession
- **Expected**:
  - Window closes (no longer shows rank tab)
  - Next GUI opening shows only 1 tab (profession)
- **Validation**: Subsequent GUI opening logs show "1 tabs"

### 4. Translation & Display Tests

#### Test Case 4.1: Guard Name Translation
- **Scenario**: Check guard profession display in various contexts
- **Expected**: Shows "Guard" not "entity.minecraft.villager.guard"
- **Test Contexts**:
  - Profession selection buttons
  - Tab titles
  - Warning dialogs
  - Entity hover names

#### Test Case 4.2: Rank Name Display
- **Scenario**: Guards with different ranks
- **Expected**: Proper rank names displayed (Recruit, Man-at-Arms I, etc.)
- **Test Data**: All 9 rank levels across both paths

### 5. Data Persistence Tests

#### Test Case 5.1: Guard Data Survival
- **Scenario**: Create guard, close/reopen world
- **Expected**: Guard data persists, ranks maintained
- **Validation**: Server logs show guard data loaded on world start

#### Test Case 5.2: Guard Data Cleanup
- **Scenario**: Change guard to non-guard profession
- **Expected**: No orphaned guard data remains
- **Validation**: Server logs confirm "guard data cleaned up"

### 6. Regression Prevention Tests

#### Test Case 6.1: Window Closing Behavior
- **Test Matrix**:
  | From Profession | To Profession | Expected Behavior |
  |----------------|---------------|-------------------|
  | Farmer | Guard | Window refreshes, shows rank tab |
  | Guard | Farmer | Warning dialog, then window closes |
  | Farmer | Toolsmith | Window closes immediately |
  | Guard | Guard | Not possible (filtered out) |

#### Test Case 6.2: Tab Recreation After Refresh
- **Scenario**: Multiple guard assignment/removal cycles
- **Expected**: Tab buttons always properly recreated
- **Validation**: No missing tab titles or broken navigation

### 7. Error Handling Tests

#### Test Case 7.1: Invalid Profession Selection
- **Scenario**: Attempt to select blacklisted profession
- **Expected**: Profession not available in list
- **Test Data**: Configured blacklisted professions

#### Test Case 7.2: Insufficient Emeralds
- **Scenario**: Guard with ranks but player has no emeralds
- **Expected**: Warning shows "0 emeralds will be lost"
- **Validation**: No emerald deduction occurs

## Automated Test Implementation

### Log Pattern Validation
```
SUCCESS_PATTERNS = {
    "guard_assignment": "Guard profession selected - refreshing tabs",
    "tab_creation": "Initialized VillagerManagementScreen with 2 tabs",
    "rank_switch": "Successfully switched to RankTab",
    "guard_removal": "guard data cleaned up",
    "window_close": "Successfully processed guard profession change"
}
```

### Test Execution Framework
1. **Pre-test Setup**: Clean world state, known villager professions
2. **Test Execution**: Automated UI interactions via test framework
3. **Log Validation**: Parse game logs for expected patterns
4. **Post-test Cleanup**: Reset test environment

### Continuous Integration
- Run full test suite on every build
- Generate test reports with pass/fail status
- Alert on any regression detection
- Block merges if critical tests fail

## Test Data Requirements

### Villager Test Set
- 1x Farmer villager (for guard assignment tests)
- 1x Guard villager with no ranks
- 1x Guard villager with multiple ranks purchased
- 1x Toolsmith villager (for standard profession changes)

### Player Test State
- Sufficient emeralds for rank testing (200+)
- Creative mode for reliable test execution
- Known starting position near test villagers

## Success Criteria

### All tests must pass:
- ✅ Profession filtering works correctly
- ✅ Guard workflow maintains integrity
- ✅ Tab navigation functions properly
- ✅ Window closing behavior consistent
- ✅ Data persistence and cleanup working
- ✅ Translation system functional
- ✅ No regressions in existing features

### Performance Requirements
- Full test suite completes in <5 minutes
- No memory leaks during repeated testing
- UI remains responsive throughout test execution

## Maintenance
- Update tests when adding new features
- Review test coverage quarterly
- Add new test cases for reported bugs
- Keep test data synchronized with game updates