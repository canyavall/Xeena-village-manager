# Current Development Tasks

## Active Epic: Guard Profession System
**Current Focus**: Equipment System Removal (Phase 3) → Guard Ranking System Implementation (Phase 4)
**Status**: 🔄 REFOCUSED - Removing Equipment System, Then Implementing Ranking System
**Epic Document**: `guard-profession-epic.md`
**Previous Epic**: Profession Management System ✅ COMPLETED (see `profession-feature-epic.md`)

**Progress Summary:**
- **Phase 1**: ✅ COMPLETED - Guard profession foundation with workstation block and configuration
- **Phase 2**: ✅ COMPLETED - Comprehensive tabbed GUI system with fully functional Equipment Tab
- **Phase 3**: 🔄 ACTIVE - Equipment System Removal (required before ranking system)
- **Phase 4**: 📋 TODO - Guard Ranking System Implementation (dual paths, emerald purchases)
- **Phase 5**: 📋 DEFERRED - AI Behaviors (moved after ranking system completion)

## IMMEDIATE PRIORITY TASKS (Next Work)

### 🔴 CRITICAL: Task 3.1 - Remove Equipment Storage System
**Ready to Start**: Remove equipment system foundation to prepare for ranking system
**Impact**: Clean foundation for ranking system - removes conflicting equipment data
**Estimated Time**: 4-6 hours
**Approach**: Remove equipment from GuardData, clean NBT serialization, preserve other data

### 🔴 CRITICAL: Task 3.2 - Remove Equipment GUI Components
**Depends on**: Task 3.1 completion
**Impact**: Clean GUI foundation - removes equipment tab for rank tab replacement
**Estimated Time**: 3-4 hours
**Approach**: Remove EquipmentTab, preserve role selection, update tabbed interface

### 🔴 CRITICAL: Task 3.3 - Remove Equipment Networking
**Depends on**: Task 3.2 completion
**Impact**: Clean networking foundation - removes equipment packets for rank packets
**Estimated Time**: 2-3 hours
**Approach**: Remove EquipGuardPacket, clean GuardDataSyncHandler, preserve base sync

## Epic 2: Guard Profession Tasks

### Phase 1: Foundation ✅ COMPLETED
**Completion Date**: 2025-09-18
**Priority**: ✅ COMPLETED - Core infrastructure established

#### Task 1.1: Create Guard Profession ✅ COMPLETED
- [x] Create GuardProfession class extending VillagerProfession
- [x] Design and implement Guard Post workstation block
- [x] Create block model and texture for Guard Post
- [x] Register profession with Minecraft registry system
- [x] Add guard profession icon (sword/shield combo)
- [x] Test profession appears in vanilla systems

#### Task 1.2: Configuration Integration ✅ COMPLETED
- [x] Add guardProfession section to ModConfig
- [x] Implement enabled/disabled toggle
- [x] Add guard-specific configuration options
- [x] Ensure profession list respects config
- [x] Test config hot-reload functionality

#### Task 1.3: Guard Entity Foundation ✅ COMPLETED (Basic Implementation)
- [x] Create basic Guard profession registration
- [x] Implement workstation block functionality
- [x] Add configuration integration for profession
- [x] Test profession appears and can be assigned
- [ ] Advanced equipment inventory capability (moved to Phase 3)
- [ ] Combat stats system (moved to Phase 5)
- [ ] Custom entity behaviors (moved to Phase 4)

**Phase 1 Status**: Guard profession is fully functional and appears in the profession selection GUI. Players can assign it to villagers and it respects configuration settings. Advanced features (equipment, AI, combat) are scheduled for later phases.

### Phase 2: GUI Overhaul ✅ COMPLETED
**Priority**: ✅ COMPLETED - Tabbed interface fully implemented
**Status**: ✅ COMPLETED - 2025-09-18
**Completion Date**: 2025-09-18
**Summary**: All Phase 2 tasks completed successfully. Comprehensive tabbed interface system with fully functional Equipment Tab ready for Phase 3.

**Phase 2 Final Achievements:**
- ✅ Complete tabbed interface architecture with extensible design
- ✅ Professional Equipment Tab with clean two-section layout (equipment slots + role selection)
- ✅ Resolved all UI positioning and visual issues through multiple iteration phases
- ✅ Simplified design focus on core equipment management functionality
- ✅ Enhanced equipment slot widgets with proper scaling and interaction handling
- ✅ Fully functional role selection system (Patrol/Guard/Follow modes)
- ✅ Comprehensive GUI blur resolution for Minecraft 1.21.1 compatibility

#### Task 2.1: Implement Tab System ✅ COMPLETED
**Completion Date**: 2025-09-18
- [x] Create TabbedManagementScreen base class ✅ COMPLETED
- [x] Design tab button UI components ✅ COMPLETED
- [x] Implement tab switching logic ✅ COMPLETED
- [x] Add smooth transition animations ✅ COMPLETED
- [x] Test tab persistence and state management ✅ COMPLETED
- [x] Dynamic tab visibility (Equipment tab only for Guards) ✅ COMPLETED
- [x] Maintain full backward compatibility ✅ COMPLETED

#### Task 2.2: Migrate Profession Screen ✅ COMPLETED
**Completion Date**: 2025-09-18
- [x] Create ProfessionTab extending from existing screen ✅ COMPLETED
- [x] Adapt current profession grid to tab format ✅ COMPLETED
- [x] Maintain all existing functionality ✅ COMPLETED
- [x] Update screen opening logic ✅ COMPLETED
- [x] Test profession selection still works ✅ COMPLETED
- [x] Remove legacy ProfessionSelectionScreen class ✅ COMPLETED

#### Task 2.3: Create Equipment Tab ✅ COMPLETED
**Completion Date**: 2025-09-18
**Final Status**: Equipment Tab fully implemented with clean two-section layout

**All Sub-tasks Completed:**
- [x] Design EquipmentTab layout (6 slots + preview) → **Final: Clean two-section layout without preview** ✅ COMPLETED
- [x] Implement equipment slot widgets → **Enhanced EquipmentSlot widgets with proper scaling** ✅ COMPLETED
- [x] Add drag-and-drop functionality → **Mouse event handling and click interactions** ✅ COMPLETED
- [x] Create villager preview renderer → **Removed for simplified design** ✅ COMPLETED
- [x] Add role selection buttons (Patrol/Guard/Follow) → **Fully functional role selection system** ✅ COMPLETED
- [x] Test equipment UI interactions → **All interactions working properly** ✅ COMPLETED

**Additional Improvements Completed:**
- [x] Fixed critical layout positioning issues (Phase 2.4) ✅ COMPLETED
- [x] Simplified design by removing problematic villager preview (Phase 2.5) ✅ COMPLETED
- [x] Final layout proportions with proper frame fitting (Phase 2.6) ✅ COMPLETED

### Phase 3: Equipment System Removal 🔴 CRITICAL
**Priority**: 🔴 CRITICAL - Required before ranking system implementation
**Status**: 📋 TODO - New priority implementation
**Estimated Duration**: 1-2 weeks
**Summary**: Remove the previously implemented equipment system to make way for the new ranking system.

**Removal Scope:**
- ❌ Remove Equipment Tab UI with 6 equipment slots (weapon, helmet, chestplate, leggings, boots, shield)
- ✅ Preserve Role selection system (Patrol/Guard/Follow) with visual feedback
- ❌ Remove Equipment storage and NBT serialization system
- ❌ Remove Equipment networking and synchronization
- ❌ Remove Visual sword rendering on guard villagers
- ✅ Keep SimplifiedGuardRenderer with texture fixes

#### Task 3.1: Remove Equipment Storage System 🔴 CRITICAL
**Priority**: 🔴 CRITICAL - Foundation for equipment system removal
**Status**: 📋 TODO - Ready to start
**Estimated Time**: 4-6 hours
**Dependencies**: None

**Implementation Details**:
- [ ] Remove equipment fields from GuardData class
- [ ] Remove EquipmentValidator class and all references
- [ ] Clean up equipment-related NBT serialization code
- [ ] Remove equipment restrictions and validation logic
- [ ] Update GuardDataManager to exclude equipment persistence
- [ ] Clean up VillagerEntityMixin equipment save/load code
- [ ] Remove equipment-related registry references
- [ ] Test that existing guard data still loads without equipment

**Files to Modify**:
- `GuardData.java` - Remove equipment fields and methods
- `GuardDataManager.java` - Remove equipment persistence
- `VillagerEntityMixin.java` - Clean up equipment serialization
- Remove `EquipmentValidator.java` entirely

**Success Criteria**:
- All equipment storage code removed without breaking existing guards
- Guard data still persists correctly for non-equipment properties
- No compilation errors from missing equipment references
- Existing save files load successfully (equipment data ignored)

#### Task 3.2: Remove Equipment GUI Components 🔴 CRITICAL
**Priority**: 🔴 CRITICAL - GUI system cleanup
**Status**: 📋 TODO - Depends on Task 3.1
**Estimated Time**: 3-4 hours
**Dependencies**: Task 3.1 (Remove Equipment Storage System)

**Implementation Details**:
- [ ] Remove EquipmentTab class and all references
- [ ] Remove EquipmentSlot widget class
- [ ] Remove VillagerPreview component (if still exists)
- [ ] Update TabbedManagementScreen to exclude Equipment tab
- [ ] Preserve role selection functionality for later integration
- [ ] Update tab navigation to skip equipment tab
- [ ] Remove equipment-related language file entries
- [ ] Clean up equipment GUI event handlers

**Files to Modify**:
- Remove `EquipmentTab.java` entirely
- Remove `EquipmentSlot.java` entirely
- Update `TabbedManagementScreen.java` - remove equipment tab references
- Update `VillagerManagementScreen.java` - remove equipment tab initialization
- Update language files - remove equipment-related entries

**Success Criteria**:
- Equipment tab no longer appears in GUI
- Role selection functionality preserved for ranking system
- No compilation errors from missing equipment GUI classes
- Tab navigation works correctly without equipment tab
- GUI opens without errors for guard villagers

#### Task 3.3: Remove Equipment Networking 🔴 CRITICAL
**Priority**: 🔴 CRITICAL - Network system cleanup
**Status**: 📋 TODO - Depends on Task 3.2
**Estimated Time**: 2-3 hours
**Dependencies**: Task 3.2 (Remove Equipment GUI Components)

**Implementation Details**:
- [ ] Remove EquipGuardPacket class entirely
- [ ] Remove equipment-related server-side validation
- [ ] Clean up GuardDataSyncHandler equipment synchronization
- [ ] Remove equipment-related packet handlers
- [ ] Update ClientGuardDataCache to exclude equipment
- [ ] Remove equipment multiplayer synchronization code
- [ ] Clean up equipment-related networking references

**Files to Modify**:
- Remove `EquipGuardPacket.java` entirely
- Update `GuardDataSyncHandler.java` - remove equipment sync
- Update `ClientGuardDataCache.java` - remove equipment cache
- Update `ServerPacketHandler.java` - remove equipment packet handling
- Clean up networking registration for equipment packets

**Success Criteria**:
- All equipment networking code removed
- Guard data synchronization still works for non-equipment properties
- No network packet errors in multiplayer
- Client-server synchronization remains stable

#### Task 3.4: Remove Equipment Rendering System 🟡 HIGH
**Priority**: 🟡 HIGH - Visual system cleanup
**Status**: 📋 TODO - Depends on Task 3.3
**Estimated Time**: 2-3 hours
**Dependencies**: Task 3.3 (Remove Equipment Networking)

**Implementation Details**:
- [ ] Remove equipment rendering from SimplifiedGuardRenderer
- [ ] Clean up equipment-related rendering mixins
- [ ] Remove equipment visual synchronization
- [ ] Preserve base guard texture fixes (keep SimplifiedGuardRenderer core)
- [ ] Remove equipment model loading and caching
- [ ] Clean up equipment-related client rendering code

**Files to Modify**:
- Update `SimplifiedGuardRenderer.java` - remove equipment rendering only
- Remove any equipment-specific rendering mixins
- Clean up equipment rendering in client initialization

**Success Criteria**:
- Equipment no longer renders on guard villagers
- Guard texture blending fixes remain intact
- No rendering errors or crashes
- Base guard villager appearance preserved

#### Task 3.5: Data Migration for Existing Guards 🟡 HIGH
**Priority**: 🟡 HIGH - Data integrity during transition
**Status**: 📋 TODO - Depends on Task 3.4
**Estimated Time**: 3-4 hours
**Dependencies**: Task 3.4 (Remove Equipment Rendering System)

**Implementation Details**:
- [ ] Convert all existing equipped guards to basic guards
- [ ] Set all guards to Recruit rank (new default)
- [ ] Preserve guard role assignments (Patrol/Guard/Follow)
- [ ] Clean up equipment data from existing save files
- [ ] Add migration logging for tracking conversions
- [ ] Test migration with various guard configurations
- [ ] Ensure no data loss for non-equipment properties

**Success Criteria**:
- All existing guards converted to Recruit rank
- Role assignments preserved through migration
- No equipment data remains in save files
- Guards maintain all other properties (name, profession, etc.)
- Migration completes without errors or crashes

### Phase 4: Guard Ranking System
**Priority**: 🔴 HIGH - Active development phase
**Status**: 📋 TODO - Depends on Phase 3 completion
**Estimated Duration**: 3-4 weeks
**Dependencies**: Complete Phase 3 (Equipment System Removal)

**Overview**: Implement comprehensive guard ranking system with dual specialization paths (Melee/Ranged), purchase-based progression, and meaningful stat bonuses. Based on complete design specification in `.claude/game-design/guard-rank-system.md`.

**Core System Features:**
- **5-Rank Progression**: Recruit → 4 specialization ranks per path
- **Dual Paths**: Melee (Man-at-Arms → Knight) vs Ranged (Marksman → Sharpshooter)
- **Emerald Costs**: Progressive scaling (15 → 20 → 45 → 75 emeralds)
- **Balanced Combat**: Melee (tanky, high HP) vs Ranged (glass cannon, high damage)
- **Special Abilities**: Knockback Strike (Knight) and Piercing Shot (Sharpshooter)
- **Visual Progression**: Rank-based cosmetic equipment and insignia
- **Path Selection**: Players choose specialization at Recruit level

**Technical Architecture:**
- GuardRank data structures with immutable rank definitions
- GuardRankData for per-guard progression tracking
- GuardRankManager singleton for rank logic and validation
- Purchase-based progression with emerald economy integration
- GUI replacement of Equipment Tab with Rank Tab
- Network synchronization for multiplayer rank purchases
- NBT persistence for rank data and purchase history

#### Task 4.1: Guard Rank Data Structure 🔴 CRITICAL
**Priority**: 🔴 CRITICAL - Foundation for entire ranking system
**Status**: 📋 TODO - Ready to start after Phase 3 completion
**Estimated Time**: 6-8 hours
**Dependencies**: Phase 3 complete (Equipment System Removal)

**Implementation Details**:
- [ ] Create `GuardRank.java` immutable data class (as per design spec)
- [ ] Create `GuardPath.java` enum (RECRUIT, MELEE, RANGED paths)
- [ ] Create `RankStats.java` immutable combat statistics class
- [ ] Create `SpecialAbility.java` abstract base class for rank abilities
- [ ] Implement `KnockbackAbility.java` for Knight rank (20% chance)
- [ ] Implement `PiercingShotAbility.java` for Sharpshooter rank
- [ ] Create `GuardRankManager.java` singleton with all rank definitions
- [ ] Define exact 5-rank progression per path with design specification stats
- [ ] Set emerald costs: 15 → 20 → 45 → 75 (progressive scaling)
- [ ] Create `GuardRankData.java` for per-guard rank tracking

**Rank Specifications to Implement**:
- **Recruit**: 25 HP, 4 DMG, no specialization (free)
- **Melee Path**: Man-at-Arms I/II/III → Knight (tank focus, 35→95 HP)
- **Ranged Path**: Marksman I/II/III → Sharpshooter (glass cannon, 25→45 HP)

**Success Criteria**:
- All 11 ranks defined with exact design specification stats
- Rank progression paths properly implemented
- Special abilities functional (knockback, piercing shot)
- Rank data integrates with GuardData NBT system
- Manager singleton provides all required utility methods

#### Task 4.2: Emerald Economy and Purchase System 🟡 HIGH
**Priority**: 🟡 HIGH - Core purchasing functionality
**Status**: 📋 TODO - Depends on Task 4.1
**Estimated Time**: 4-5 hours
**Dependencies**: Task 4.1 (Guard Rank Data Structure)

**Implementation Details**:
- [ ] Create `EmeraldEconomyManager.java` for currency operations
- [ ] Implement player inventory emerald detection and counting
- [ ] Add emerald deduction with transaction rollback capability
- [ ] Support emerald blocks (9 emeralds) with automatic conversion
- [ ] Create currency validation utility methods
- [ ] Add server-side currency verification (anti-exploit)
- [ ] Implement transaction logging for purchase audit trail
- [ ] Create purchase confirmation system with cost preview
- [ ] Add sequential rank purchase enforcement (no skipping)
- [ ] Test purchase mechanics with various emerald amounts

**Emerald Costs (Per Design Specification)**:
- Recruit → Tier 1: 15 emeralds
- Tier 1 → Tier 2: 20 emeralds
- Tier 2 → Tier 3: 45 emeralds
- Tier 3 → Tier 4: 75 emeralds
- **Total Max Investment**: 155 emeralds

**Success Criteria**:
- Emerald detection accurate in player inventories
- Currency deduction secure and cannot be exploited
- Sequential progression properly enforced
- Transaction logging functional for debugging
- Support for emerald blocks and automatic conversion

#### Task 4.3: Rank Tab GUI Implementation 🟡 HIGH
**Priority**: 🟡 HIGH - User interface for ranking system
**Status**: 📋 TODO - Depends on Task 4.1
**Estimated Time**: 6-7 hours
**Dependencies**: Task 4.1 (Guard Rank Data Structure)

**Implementation Details**:
- [ ] Create `RankTab.java` to replace removed EquipmentTab
- [ ] Design GUI layout per specification (header, path selection, upgrade section)
- [ ] Implement current rank display with progress bar
- [ ] Add path selection interface (Melee vs Ranged) for Recruits
- [ ] Create upgrade section with next rank preview
- [ ] Display emerald cost and player balance
- [ ] Add stat comparison (before/after stats with highlights)
- [ ] Implement purchase confirmation dialog
- [ ] Show "Max Rank Achieved" for completed progressions
- [ ] Preserve role selection (Patrol/Guard/Follow) in right section
- [ ] Update tab title to "Rank & Role Management"

**GUI Layout Structure (Per Design)**:
```
[Guard Info Header]
[Current Rank: Knight | Health: 95 HP | Damage: 12]
[Progress Bar: Rank 4/4 - Max Rank Achieved]

[Path Selection] (Only for Recruits)
[ Melee Path ]  [ Ranged Path ]

[Upgrade Section] (If not max rank)
[Next Rank: Sharpshooter]
[Cost: 75 Emeralds] [You have: 120 Emeralds]
[Health: 35 → 45 HP] [Damage: 12 → 16]
[Special: Piercing Shot ability]
[Upgrade Button]
```

**Success Criteria**:
- Rank tab replaces equipment tab completely
- Path selection functional for Recruit guards
- Upgrade interface shows accurate cost and benefits
- Purchase confirmation prevents accidental upgrades
- Role selection preserved and functional

#### Task 4.4: Combat Stat System Integration 🟡 HIGH
**Priority**: 🟡 HIGH - Functional rank benefits
**Status**: 📋 TODO - Depends on Task 4.1
**Estimated Time**: 5-6 hours
**Dependencies**: Task 4.1 (Guard Rank Data Structure)

**Implementation Details**:
- [ ] Create `RankStatModifier.java` for applying rank bonuses to villagers
- [ ] Integrate with villager attribute system (health, attack damage)
- [ ] Implement melee combat stat progression (health focused)
- [ ] Implement ranged combat stat progression (damage focused)
- [ ] Add special ability trigger system (KnockbackAbility, PiercingShotAbility)
- [ ] Create stat application on villager spawn/load
- [ ] Add stat recalculation on rank upgrade
- [ ] Implement combat behavior switching (melee vs ranged)
- [ ] Test stat modifications in combat scenarios
- [ ] Validate stat persistence through save/reload

**Exact Stat Progressions (Per Design Specification)**:

**Melee Path (Tank Focus)**:
- Recruit: 25 HP, 4 DMG
- Man-at-Arms I: 35 HP, 6 DMG
- Man-at-Arms II: 50 HP, 8 DMG
- Man-at-Arms III: 70 HP, 10 DMG
- Knight: 95 HP, 12 DMG + Knockback Strike (20% chance)

**Ranged Path (Glass Cannon Focus)**:
- Recruit: 25 HP, 4 DMG
- Marksman I: 25 HP, 6 ranged DMG (2.0s draw)
- Marksman II: 30 HP, 9 ranged DMG (1.5s draw)
- Marksman III: 35 HP, 12 ranged DMG (1.0s draw)
- Sharpshooter: 45 HP, 16 ranged DMG (0.8s draw) + Piercing Shot

**Success Criteria**:
- Rank bonuses apply correctly per specification
- Melee guards tank effectively, ranged guards deal high damage
- Special abilities trigger correctly (knockback, piercing)
- Combat behavior switches based on path (melee vs bow usage)
- Stat changes persist through save/reload

#### Task 4.5: Network Synchronization System 🟡 HIGH
**Priority**: 🟡 HIGH - Multiplayer support
**Status**: 📋 TODO - Depends on Tasks 4.1, 4.2
**Estimated Time**: 4-5 hours
**Dependencies**: Task 4.1 (Data Structure), Task 4.2 (Purchase System)

**Implementation Details**:
- [ ] Create `RankUpgradePacket.java` for client-server rank purchase requests
- [ ] Create `PathSelectionPacket.java` for specialization path selection
- [ ] Add rank data to existing GuardDataSyncHandler
- [ ] Implement server-side purchase validation and processing
- [ ] Add rank data to ClientGuardDataCache
- [ ] Create rank purchase event broadcasting for multiplayer
- [ ] Add currency validation and deduction on server
- [ ] Implement purchase confirmation/denial responses
- [ ] Test multiplayer rank synchronization
- [ ] Validate concurrent purchase attempts

**Network Protocol (Per Design Specification)**:
- C2S: Rank purchase request with guard ID and target rank
- C2S: Path selection request with guard ID and chosen path
- S2C: Purchase confirmation/denial with reason
- S2C: Full rank data sync on villager interaction
- S2C: Rank upgrade event packets for visual effects

**Success Criteria**:
- Rank purchases sync correctly between server and clients
- All players see accurate rank information after purchase
- Purchase validation prevents invalid transactions
- Currency deduction handled securely on server
- Path selection synchronizes properly

#### Task 4.6: Visual Rank Indicators and Cosmetic Equipment 🟡 HIGH
**Priority**: 🟡 HIGH - Visual feedback and progression
**Status**: 📋 TODO - Depends on Task 4.1, 4.4
**Estimated Time**: 5-6 hours
**Dependencies**: Task 4.1 (Data Structure), Task 4.4 (Combat Stats)

**Implementation Details**:
- [ ] Create rank insignia sprites per design specification
- [ ] Implement rank-based cosmetic equipment rendering
- [ ] Add rank badge rendering above villager nameplates
- [ ] Create rank-based equipment progression (leather → iron armor)
- [ ] Implement weapon switching (sword for melee, bow/crossbow for ranged)
- [ ] Add rank insignia to SimplifiedGuardRenderer
- [ ] Create rank tooltip with progression information
- [ ] Add rank particle effects for successful purchases
- [ ] Test visual indicators at various distances
- [ ] Ensure compatibility with other nameplate mods

**Cosmetic Equipment Progression (Per Design)**:
- **Melee Path**: Basic → Leather Tunic → +Helmet → Iron Armor → Full Iron + Diamond Sword
- **Ranged Path**: Basic → Bow + Leather Cap → +Tunic → Crossbow + Leather Set → Enchanted appearance

**Visual Design Elements**:
- Rank badges for each tier (chevrons, stars, etc.)
- Equipment appearance matches rank progression
- Clear visual distinction between melee and ranged paths
- Professional nameplate integration

**Success Criteria**:
- Rank badges clearly visible above guard nameplates
- Cosmetic equipment renders correctly per rank
- Visual progression clearly indicates rank hierarchy
- Path specialization visually distinguishable (melee vs ranged)
- Visual updates immediately on rank purchase

#### Task 4.7: Save/Load Persistence Integration 🟢 MEDIUM
**Priority**: 🟢 MEDIUM - Data integrity
**Status**: 📋 TODO - Depends on Task 4.1
**Estimated Time**: 3-4 hours
**Dependencies**: Task 4.1 (Guard Rank Data Structure)

**Implementation Details**:
- [ ] Integrate GuardRankData into existing GuardData NBT serialization
- [ ] Add rank data persistence to GuardDataManager
- [ ] Implement purchase history tracking in NBT
- [ ] Create data migration from equipment system to ranking system
- [ ] Add version compatibility checking for save files
- [ ] Test data integrity across save/reload cycles
- [ ] Validate persistence with large numbers of ranked guards
- [ ] Add comprehensive error handling and rollback
- [ ] Create data validation and repair utilities
- [ ] Test compatibility with existing save files

**NBT Data Structure (Per Design)**:
```nbt
guard_data: {
  rank_data: {
    current_rank: "man_at_arms_2",
    chosen_path: "MELEE",
    emeralds_spent: 35,
    last_rank_change: 1695456789L
  }
}
```

**Success Criteria**:
- Rank data persists correctly through save/reload cycles
- Purchase history and progression tracked accurately
- No data loss or corruption occurs during migration
- Large villages with many guards save/load efficiently
- Backward compatibility with existing save files maintained

#### Task 4.8: Security and Anti-Exploit Validation 🟡 HIGH
**Priority**: 🟡 HIGH - System security and integrity
**Status**: 📋 TODO - Depends on Task 4.2, 4.5
**Estimated Time**: 3-4 hours
**Dependencies**: Task 4.2 (Purchase System), Task 4.5 (Network Synchronization)

**Implementation Details**:
- [ ] Implement comprehensive server-side purchase validation
- [ ] Add rank progression permission checking
- [ ] Create anti-exploit protection (rapid purchases, duplication)
- [ ] Implement purchase cooldown mechanisms (1 second between attempts)
- [ ] Add transaction logging and audit trail system
- [ ] Create purchase reversal and rollback system for admins
- [ ] Add admin override commands for rank management
- [ ] Implement currency validation against duplication exploits
- [ ] Test security against common Minecraft exploits
- [ ] Validate multiplayer purchase conflicts and resolution

**Security Measures (Per Design)**:
- All purchases validated server-side only
- Sequential rank progression strictly enforced
- Currency deduction using atomic operations
- Purchase cooldown prevents rapid-fire exploits
- Transaction logging with timestamps for debugging
- Admin commands for purchase management and rollback

**Success Criteria**:
- No currency duplication or loss possible
- Cannot purchase ranks out of sequence or skip ranks
- Purchase conflicts resolved properly in multiplayer
- Admin tools available for purchase management
- System secure against common Minecraft exploits and edge cases

#### Task 4.9: Comprehensive Integration Testing 🟢 MEDIUM
**Priority**: 🟢 MEDIUM - System validation and quality assurance
**Status**: 📋 TODO - Depends on all previous Task 4.x tasks
**Estimated Time**: 4-5 hours
**Dependencies**: All Task 4.1-4.8 completed

**Implementation Details**:
- [ ] Test complete rank progression from Recruit to max rank for both paths
- [ ] Validate path selection and specialization switching at Recruit level
- [ ] Test emerald economy integration with various currency amounts
- [ ] Verify stat progression and combat effectiveness per rank
- [ ] Test special abilities (Knockback Strike, Piercing Shot) in combat
- [ ] Validate multiplayer rank synchronization and purchase conflicts
- [ ] Test GUI responsiveness and purchase confirmation flows
- [ ] Verify visual progression (cosmetic equipment, rank badges)
- [ ] Test save/load persistence across multiple sessions
- [ ] Validate security measures and anti-exploit protections
- [ ] Test integration with existing role system (Patrol/Guard/Follow)
- [ ] Verify migration from equipment system to ranking system

**Comprehensive Test Scenarios**:
- **Single Player**: Full rank progression for multiple guards
- **Multiplayer**: Concurrent purchases, synchronization validation
- **Economy**: Various emerald amounts, edge cases (0, insufficient, overflow)
- **Combat**: Rank effectiveness testing, special ability triggers
- **Persistence**: Save/reload, world migration, data integrity
- **GUI**: User experience, error handling, confirmation flows

**Success Criteria**:
- All rank progressions work correctly for both paths
- Special abilities trigger and function as designed
- Purchase system secure and exploit-resistant
- GUI provides excellent user experience
- System integrates seamlessly with existing features
- No regressions in existing functionality

### Phase 5: AI Behaviors (Deferred)
**Priority**: 🟢 LOW - Advanced functionality (moved from Phase 4)
**Status**: 📋 DEFERRED - Implement after ranking system completion
**Note**: Previously Phase 4, deferred to prioritize ranking system implementation

#### Task 5.1: Patrol AI Implementation
- [ ] Create GuardPatrolGoal class
- [ ] Implement village boundary detection
- [ ] Add patrol path generation
- [ ] Create waypoint system
- [ ] Test patrol coverage and efficiency

#### Task 5.2: Stationary Guard AI
- [ ] Create GuardStationaryGoal class
- [ ] Implement position anchoring
- [ ] Add threat detection radius
- [ ] Create alert state behavior
- [ ] Test guard stays at position

#### Task 5.3: Follow AI Implementation
- [ ] Create GuardFollowGoal class
- [ ] Implement player tracking
- [ ] Add distance maintenance logic
- [ ] Handle player teleportation
- [ ] Test following through obstacles

#### Task 5.4: Role Management
- [ ] Create SetGuardRolePacket
- [ ] Implement role switching logic
- [ ] Add role persistence
- [ ] Create role-specific particles/indicators
- [ ] Test smooth role transitions

### Phase 6: Combat Integration
**Priority**: 🟢 LOW - Enhancement features
**Status**: 📋 TODO - After AI Behaviors completion

#### Task 6.1: Rank-Based Combat Stats
- [ ] Integrate rank bonuses with combat system
- [ ] Implement rank-based damage calculations
- [ ] Add rank-based health and defense
- [ ] Create rank-based attack speed modifiers
- [ ] Test combat effectiveness by rank

#### Task 6.2: Combat Behavior
- [ ] Implement hostile mob detection
- [ ] Add attack goal with rank modifiers
- [ ] Create defensive behaviors based on rank
- [ ] Implement retreat mechanics for lower ranks
- [ ] Test combat effectiveness scaling

#### Task 6.3: Combat Effects
- [ ] Add combat sounds
- [ ] Implement hit particles
- [ ] Create death/respawn system
- [ ] Add experience loss on death
- [ ] Test combat feedback

### Phase 7: Polish & Testing
**Priority**: 🟢 LOW - Final refinements

#### Task 7.1: Visual Polish
- [ ] Enhance rank insignia graphics
- [ ] Create rank promotion animations
- [ ] Implement GUI transitions
- [ ] Add rank-based sound effects
- [ ] Test visual consistency

#### Task 7.2: Performance Optimization
- [ ] Profile ranking system performance
- [ ] Optimize experience calculation efficiency
- [ ] Reduce network packet frequency
- [ ] Cache rank rendering assets
- [ ] Test with 50+ ranked guards

#### Task 7.3: Comprehensive Testing
- [ ] Test all rank progressions
- [ ] Verify multiplayer rank synchronization
- [ ] Test config options thoroughly
- [ ] Check mod compatibility
- [ ] Stress test ranking scenarios

## Improvement Ideas (Optional Enhancements)

### Advanced Ranking Features
- [ ] Specialization paths (Combat, Defense, Support)
- [ ] Merit badges for specific achievements
- [ ] Rank ceremonies with village participation
- [ ] Rank-based uniform variations

### Schedule System
- [ ] Day/night shifts
- [ ] Guard barracks integration
- [ ] Shift handoff mechanics
- [ ] Sleep behavior

### Alert Network
- [ ] Guard communication system
- [ ] Village-wide threat alerts
- [ ] Reinforcement calling
- [ ] Alert UI for players

### Equipment Durability
- [ ] Durability tracking
- [ ] Auto-repair at Guard Post
- [ ] Material requirements
- [ ] Repair animations

### Command System
- [ ] Whistle item crafting
- [ ] Rally point designation
- [ ] Formation commands
- [ ] Squad management

## Bug Fixes & Improvements (Phase 2.1) ✅ COMPLETED
**Priority**: 🔴 HIGH - Critical GUI issues

#### Task 2.1.1: GUI Rendering Fixes ✅ COMPLETED
- [x] Fix blurry GUI rendering (override renderBackground method) ✅ COMPLETED
- [x] Research root cause (Minecraft 1.21+ blur feature) ✅ COMPLETED
- [x] Implement proper technical solution ✅ COMPLETED
- [x] Test and verify resolution ✅ COMPLETED
- [ ] Implement GUI responsive scaling for different screen sizes (Future)
- [ ] Optimize GUI performance and memory usage (Future)
- [ ] Test GUI on different resolutions and GUI scales (Future)

## Testing Checklist
- [x] Guard profession appears in GUI ✅ COMPLETED
- [x] Config enables/disables profession ✅ COMPLETED
- [x] Tabs switch correctly ✅ COMPLETED
- [x] GUI renders clearly without blur ✅ RESOLVED
- [x] Tabbed interface fully functional ✅ COMPLETED
- [x] Equipment tab shows fully functional UI ✅ COMPLETED
- [x] Equipment slots render and interact correctly ✅ COMPLETED
- [x] Villager 3D preview displays properly ✅ COMPLETED
- [x] Role selection buttons work correctly ✅ COMPLETED
- [ ] GUI scales properly on different screen sizes (Future)
- [ ] Equipment equips and persists (Phase 3)
- [ ] All three AI modes function (Phase 4)
- [ ] Combat works as expected (Phase 5)
- [ ] Multiplayer fully synchronized (Phase 6)
- [ ] No performance degradation (Phase 6)
- [ ] No conflicts with existing features (Phase 6)

## Documentation Tasks
- [ ] Document Guard profession usage
- [ ] Create equipment guide
- [ ] Explain AI behaviors
- [ ] Add configuration examples
- [ ] Create troubleshooting section

---

**Task Tracking Maintained By**: project-scope-manager
**Last Updated**: 2025-09-23
**Phase 1 Completion**: 2025-09-18 ✅ COMPLETED
**Phase 2 Completion**: 2025-09-18 ✅ COMPLETED
**Phase 3 Status**: 🔄 ACTIVE - Equipment System Removal (NEW PRIORITY)
**Phase 4 Status**: 📋 TODO - Guard Ranking System Implementation
**IMMEDIATE NEXT**: Task 3.1 - Remove Equipment Storage System (🔴 CRITICAL, Ready to Start)
**Following Priority**: Task 3.2 - Remove Equipment GUI Components (🔴 CRITICAL)
**Strategy Change**: Complete equipment system removal, then implement dual-path ranking system (Melee/Ranged)
**Estimated Duration**: 1-2 weeks for equipment removal, 3-4 weeks for ranking system implementation
**Key Benefits**: Clean foundation for ranking system, dual specialization paths, emerald-based economy integration
**Next Review**: After Phase 3 completion (equipment removal)

**Equipment Removal Priority Rationale**:
The equipment system must be completely removed before implementing the ranking system to avoid data conflicts and ensure clean architecture. The new ranking system with dual paths (Melee tank vs Ranged glass cannon) provides better game balance and progression than equipment slots. Purchase-based progression with emeralds creates meaningful economic decisions and integrates naturally with Minecraft's trading systems.