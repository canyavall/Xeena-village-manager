# Guard Profession Feature Test Cases

## Overview
This document contains comprehensive test cases for the Guard profession feature in Xeenaa Villager Manager mod v1.0.0. The Guard profession includes a custom villager profession, Guard Post workstation block, and configuration system.

## Feature Components
- **Guard Profession**: Custom villager profession (`xeenaa_villager_manager:guard`)
- **Guard Post Block**: Workstation block for Guards (`xeenaa_villager_manager:guard_post`)
- **Configuration**: JSON config with guard_settings section
- **GUI Integration**: Guard profession appears in profession selection screen

---

## 1. Guard Profession Registration Tests

### Test Case 1.1: Guard Profession Registry Validation
**Objective**: Verify Guard profession is properly registered in Minecraft's villager profession registry

**Prerequisites**:
- Mod loaded successfully
- No registration errors in logs

**Test Steps**:
1. Start Minecraft client with mod loaded
2. Check logs for profession registration messages
3. Use `/data get entity @e[type=villager,limit=1] VillagerData.profession` on Guard villager
4. Verify profession shows as `xeenaa_villager_manager:guard`

**Expected Results**:
- Log shows: "Successfully registered profession: xeenaa_villager_manager:guard with workstation POI and work sound"
- Command returns correct profession identifier
- No registration errors or warnings

**Priority**: Critical

### Test Case 1.2: Guard Profession POI Association
**Objective**: Verify Guard profession correctly associates with Guard Post POI type

**Test Steps**:
1. Place Guard Post block in world
2. Spawn unemployed villager near Guard Post
3. Wait for villager to pathfind to Guard Post
4. Verify villager adopts Guard profession

**Expected Results**:
- Villager pathfinds to Guard Post within reasonable time (~30 seconds)
- Villager profession changes to Guard
- Villager skin/appearance updates to Guard appearance
- Villager work sound plays when interacting with Guard Post

**Priority**: Critical

### Test Case 1.3: Guard Profession Work Sound
**Objective**: Verify Guard profession plays correct work sound (anvil use sound)

**Test Steps**:
1. Create Guard villager using Guard Post
2. Interact with Guard Post during work hours
3. Listen for anvil use sound (`BLOCK_ANVIL_USE`)

**Expected Results**:
- Anvil use sound plays when Guard works at Guard Post
- Sound volume and pitch are appropriate
- No audio errors or missing sounds

**Priority**: Medium

---

## 2. Guard Post Block Functionality Tests

### Test Case 2.1: Guard Post Block Placement
**Objective**: Verify Guard Post can be placed and exists in world

**Test Steps**:
1. Obtain Guard Post item from creative inventory or crafting
2. Place Guard Post on valid surface
3. Verify block appears with correct model/texture
4. Break block and verify it drops as item

**Expected Results**:
- Guard Post places successfully on solid surfaces
- Block has correct visual appearance and hitbox
- Block drops Guard Post item when broken
- No placement restrictions beyond normal block rules

**Priority**: High

### Test Case 2.2: Guard Post POI Registration
**Objective**: Verify Guard Post creates Point of Interest for villagers

**Test Steps**:
1. Place Guard Post in world
2. Use F3+B to show POI information (if available)
3. Check that POI type `xeenaa_villager_manager:guard_post_poi` exists
4. Verify POI has correct ticket count and search distance

**Expected Results**:
- POI type registered with identifier `xeenaa_villager_manager:guard_post_poi`
- POI has ticket count of 1 (allows 1 villager)
- POI has search distance of 1 block
- All Guard Post block states create valid POI

**Priority**: High

### Test Case 2.3: Guard Post Block States
**Objective**: Verify all Guard Post block states function correctly as POI

**Test Steps**:
1. Place Guard Post in different orientations (if applicable)
2. Test with different block states (if any)
3. Verify each state creates valid POI for villagers

**Expected Results**:
- All block states of Guard Post register as valid POI
- Villagers can pathfind to Guard Post regardless of orientation
- No block state causes POI registration failures

**Priority**: Medium

### Test Case 2.4: Guard Post Block Models and Textures
**Objective**: Verify Guard Post has correct visual appearance

**Test Steps**:
1. Place Guard Post in world
2. View from different angles and distances
3. Check block model, textures, and item model
4. Verify appearance matches intended design

**Expected Results**:
- Block model renders correctly in world
- Textures load without errors or missing texture indicators
- Item model matches block appearance
- No visual glitches or rendering errors

**Priority**: Medium

---

## 3. Configuration System Tests

### Test Case 3.1: Default Guard Settings
**Objective**: Verify default guard_settings configuration is created properly

**Test Steps**:
1. Start game with fresh config (delete existing config file)
2. Check generated config file at `config/xeenaa-villager-manager.json`
3. Verify guard_settings section exists with default values

**Expected Results**:
```json
{
  "guard_settings": {
    "enabled": true,
    "max_guards_per_village": 10,
    "guard_range": 16,
    "patrol_radius": 48,
    "experience_gain_rate": 1.0,
    "allowed_equipment": [
      "minecraft:iron_sword",
      "minecraft:diamond_sword",
      "minecraft:shield",
      "minecraft:iron_helmet",
      "minecraft:iron_chestplate",
      "minecraft:iron_leggings",
      "minecraft:iron_boots"
    ]
  }
}
```

**Priority**: High

### Test Case 3.2: Guard Profession Enable/Disable
**Objective**: Verify guard_settings.enabled flag controls Guard profession availability

**Test Steps**:
1. Set `guard_settings.enabled = false` in config
2. Restart game/reload mod
3. Try to access Guard profession in GUI
4. Set `guard_settings.enabled = true`
5. Restart and verify Guard profession is available

**Expected Results**:
- When disabled: Guard profession not visible in GUI
- When enabled: Guard profession appears in profession selection
- Configuration changes take effect after restart
- No errors when toggling enabled state

**Priority**: High

### Test Case 3.3: Configuration Validation
**Objective**: Verify configuration system handles invalid values gracefully

**Test Steps**:
1. Set invalid values in guard_settings:
   - `max_guards_per_village: -1`
   - `guard_range: -5`
   - `experience_gain_rate: -2.0`
2. Start game and check behavior
3. Verify config validation or default fallbacks

**Expected Results**:
- Invalid values either use defaults or are validated
- No crashes or errors from invalid config values
- Warning messages logged for invalid configurations
- Game remains stable with invalid config

**Priority**: Medium

### Test Case 3.4: Configuration Persistence
**Objective**: Verify configuration changes persist between game sessions

**Test Steps**:
1. Modify guard_settings values in config file
2. Start game and verify settings are loaded
3. Make runtime changes (if supported)
4. Restart game and verify persistence

**Expected Results**:
- Config file changes persist between sessions
- Modified values load correctly on game start
- No data loss or corruption in config file
- Consistent behavior across restarts

**Priority**: Medium

---

## 4. GUI Integration Tests

### Test Case 4.1: Guard Profession in Selection Screen
**Objective**: Verify Guard profession appears in profession selection GUI

**Test Steps**:
1. Right-click on any villager to open GUI
2. Navigate to profession selection screen
3. Locate Guard profession in available professions list
4. Verify Guard profession has proper display name and icon

**Expected Results**:
- Guard profession appears in profession list
- Display name is "Guard" (from lang file)
- Icon/texture displays correctly
- Profession is selectable (not grayed out)

**Priority**: Critical

### Test Case 4.2: Guard Profession Assignment via GUI
**Objective**: Verify villagers can be assigned Guard profession through GUI

**Test Steps**:
1. Open profession selection GUI on unemployed villager
2. Select Guard profession from list
3. Confirm selection
4. Verify villager profession changes to Guard

**Expected Results**:
- Guard profession button responds to clicks
- Profession change packet sent to server
- Villager profession updates to Guard
- GUI reflects new profession status
- Villager appearance updates appropriately

**Priority**: Critical

### Test Case 4.3: Guard Profession with Existing Professions
**Objective**: Verify Guard profession coexists properly with vanilla and other modded professions

**Test Steps**:
1. Open profession GUI and verify all professions present
2. Assign Guard profession to villager
3. Change same villager to different profession
4. Change different villager back to Guard profession
5. Test with multiple profession changes

**Expected Results**:
- Guard profession appears alongside all other professions
- No conflicts with vanilla professions
- Profession changes work in both directions (to/from Guard)
- No duplicate entries or missing professions
- Consistent behavior across multiple changes

**Priority**: High

### Test Case 4.4: GUI Error Handling
**Objective**: Verify GUI handles Guard profession assignment errors gracefully

**Test Steps**:
1. Try assigning Guard profession with no Guard Post nearby
2. Test with disabled Guard profession (config disabled)
3. Test with villager that cannot change profession
4. Test network errors during profession assignment

**Expected Results**:
- Appropriate error messages or feedback
- No GUI crashes or unresponsive states
- Clear indication of why assignment failed
- Graceful recovery from error conditions

**Priority**: Medium

---

## 5. Profession Assignment and Persistence Tests

### Test Case 5.1: Guard Profession Assignment Persistence
**Objective**: Verify Guard profession assignment persists through game sessions

**Test Steps**:
1. Assign Guard profession to villager
2. Save and quit world
3. Reload world
4. Verify villager still has Guard profession

**Expected Results**:
- Guard profession data saves to world file
- Villager retains Guard profession after reload
- All Guard-specific attributes persist
- No data corruption or loss

**Priority**: Critical

### Test Case 5.2: Guard Villager Behavior
**Objective**: Verify Guard villagers behave correctly with their workstation

**Test Steps**:
1. Create Guard villager with nearby Guard Post
2. Observe villager work schedule (work hours vs. free time)
3. Verify villager pathfinds to Guard Post for work
4. Test villager behavior during different time periods

**Expected Results**:
- Guard villager follows standard work schedule
- Pathfinds to Guard Post during work hours
- Performs work animations at Guard Post
- Follows normal villager AI patterns when not working

**Priority**: Medium

### Test Case 5.3: Guard Post Destruction Impact
**Objective**: Verify behavior when Guard Post is destroyed

**Test Steps**:
1. Create Guard villager with Guard Post
2. Destroy the Guard Post block
3. Observe villager behavior
4. Place new Guard Post and verify re-assignment

**Expected Results**:
- Villager becomes unemployed when Guard Post destroyed
- No crashes or errors from missing workstation
- Villager can claim new Guard Post when available
- Profession data handled correctly during transitions

**Priority**: Medium

### Test Case 5.4: Multiple Guard Villagers
**Objective**: Verify multiple Guard villagers can coexist

**Test Steps**:
1. Create multiple Guard Posts in area
2. Assign Guard profession to multiple villagers
3. Verify each villager claims appropriate Guard Post
4. Test villager interactions and pathfinding

**Expected Results**:
- Multiple Guards can exist simultaneously
- Each Guard claims separate Guard Post
- No conflicts between multiple Guard villagers
- Pathfinding works correctly with multiple POIs

**Priority**: Medium

---

## 6. Edge Cases and Error Handling Tests

### Test Case 6.1: Invalid Profession Data Handling
**Objective**: Verify system handles corrupted or invalid Guard profession data

**Test Steps**:
1. Manually corrupt villager NBT data for Guard profession
2. Load world with corrupted data
3. Verify graceful error handling
4. Test recovery mechanisms

**Expected Results**:
- No crashes from corrupted profession data
- Error logging for invalid data
- Fallback to safe state (unemployed villager)
- System remains stable despite data issues

**Priority**: Low

### Test Case 6.2: Guard Post in Unloaded Chunks
**Objective**: Verify Guard villager behavior when Guard Post is in unloaded chunk

**Test Steps**:
1. Create Guard villager with Guard Post
2. Move villager far from Guard Post (unload chunk)
3. Observe villager behavior
4. Return to Guard Post area and verify reconnection

**Expected Results**:
- Villager doesn't crash when workstation chunk unloaded
- Appropriate behavior when workstation inaccessible
- Villager reconnects to Guard Post when chunk reloads
- No data loss during chunk loading/unloading

**Priority**: Low

### Test Case 6.3: Mod Compatibility
**Objective**: Verify Guard profession works with other villager-related mods

**Test Steps**:
1. Install compatible villager mods (if any)
2. Test Guard profession alongside other modded professions
3. Verify no conflicts or interference
4. Test with villager enhancement mods

**Expected Results**:
- Guard profession appears alongside other modded professions
- No conflicts with other profession systems
- Compatible with villager enhancement mods
- Stable performance with multiple villager mods

**Priority**: Low

### Test Case 6.4: Resource Pack Compatibility
**Objective**: Verify Guard Post textures work with custom resource packs

**Test Steps**:
1. Apply various resource packs
2. Check Guard Post texture rendering
3. Verify fallback behavior for missing textures
4. Test with resource packs that modify villager textures

**Expected Results**:
- Guard Post renders correctly with different resource packs
- Graceful fallback for missing Guard Post textures
- No conflicts with villager appearance modifications
- Consistent behavior across different resource pack configurations

**Priority**: Low

---

## 7. Regression Tests

### Test Case 7.1: Existing Profession Functionality
**Objective**: Verify Guard profession addition doesn't break existing profession features

**Test Steps**:
1. Test all vanilla villager professions still work
2. Verify profession blacklist functionality unchanged
3. Test existing GUI functionality with non-Guard professions
4. Verify other mod features remain functional

**Expected Results**:
- All vanilla professions work as before
- Profession blacklist system unchanged
- GUI works normally for all existing professions
- No regression in existing functionality

**Priority**: Critical

### Test Case 7.2: Network Packet Compatibility
**Objective**: Verify profession selection packets work correctly with Guard profession

**Test Steps**:
1. Test Guard profession selection in multiplayer
2. Verify client-server synchronization
3. Test with multiple players selecting professions simultaneously
4. Verify no packet corruption or loss

**Expected Results**:
- Guard profession packets transmit correctly
- Client-server state remains synchronized
- No network errors with multiple players
- Consistent behavior in multiplayer environment

**Priority**: High

### Test Case 7.3: Performance Impact Assessment
**Objective**: Verify Guard profession addition doesn't negatively impact performance

**Test Steps**:
1. Measure FPS with and without Guard professions in world
2. Test memory usage with many Guard villagers
3. Monitor CPU usage during profession changes
4. Test world loading times with Guard profession data

**Expected Results**:
- No significant FPS impact from Guard profession
- Memory usage remains within acceptable bounds
- CPU usage for profession changes remains efficient
- World loading times not significantly increased

**Priority**: Medium

### Test Case 7.4: Save Data Integrity
**Objective**: Verify world save data remains stable with Guard profession

**Test Steps**:
1. Create world with various villagers including Guards
2. Save and load multiple times
3. Verify save file size and integrity
4. Test save data migration (if applicable)

**Expected Results**:
- Save files remain stable and loadable
- No save data corruption with Guard profession
- Save file size increases are reasonable
- Backward compatibility maintained where possible

**Priority**: Medium

---

## 8. Automated Test Scenarios

### Test Case 8.1: Guard Profession Registration Test
**Automation Level**: Full
**Test Script Location**: `src/test/java/com/xeenaa/villagermanager/profession/GuardProfessionRegistrationTest.java`

**Test Coverage**:
- Verify ModProfessions.GUARD is not null after registration
- Verify profession exists in Villager Profession registry
- Verify profession has correct identifier
- Verify profession associates with correct POI type

### Test Case 8.2: Guard Post Block Test
**Automation Level**: Full
**Test Script Location**: `src/test/java/com/xeenaa/villagermanager/block/GuardPostBlockTest.java`

**Test Coverage**:
- Verify Guard Post block registration
- Verify Guard Post item registration
- Verify Guard Post POI type registration
- Verify block state functionality

### Test Case 8.3: Configuration System Test
**Automation Level**: Full
**Test Script Location**: `src/test/java/com/xeenaa/villagermanager/config/GuardConfigTest.java`

**Test Coverage**:
- Verify default configuration generation
- Verify configuration loading and saving
- Verify guard_settings validation
- Verify configuration persistence

---

## Test Execution Guidelines

### Manual Testing Priority Order
1. **Critical Tests First**: Registration, GUI integration, profession assignment
2. **High Priority**: Configuration, block functionality, persistence
3. **Medium Priority**: Error handling, edge cases, compatibility
4. **Low Priority**: Advanced edge cases, resource pack compatibility

### Test Environment Setup
1. Clean Minecraft 1.21.1 installation
2. Fresh Fabric loader installation
3. Only Xeenaa Villager Manager mod loaded (unless testing compatibility)
4. Clean world for each major test category
5. Developer tools enabled for debugging

### Test Data Collection
- Log files for each test execution
- Screenshots of GUI states and visual elements
- Performance metrics for regression tests
- Error messages and stack traces for failure cases

### Pass/Fail Criteria
- **Pass**: All expected results achieved, no critical errors
- **Fail**: Any expected result not achieved, critical errors present
- **Partial**: Some expected results achieved, minor issues present
- **Blocked**: Test cannot be executed due to prerequisites

### Bug Reporting
For any test failures, include:
- Test case number and description
- Steps to reproduce
- Expected vs. actual results
- Log files and error messages
- Screenshots or video if applicable
- Minecraft version, mod version, and environment details

---

## Conclusion

These test cases provide comprehensive coverage of the Guard profession feature, ensuring:
- Proper registration and functionality of all components
- Configuration system reliability and flexibility
- GUI integration without regressions
- Data persistence and stability
- Error handling and edge case management
- Performance and compatibility considerations

Regular execution of these tests during development and before releases will help maintain the quality and stability of the Guard profession feature within the Xeenaa Villager Manager mod.