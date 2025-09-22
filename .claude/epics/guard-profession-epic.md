# Guard Profession Epic - PLANNED

## Epic Overview
**Feature**: Custom Guard Profession with Equipment and AI Behaviors
**Status**: 📋 PLANNING
**Estimated Complexity**: High
**Dependencies**: Profession Management System (Epic 1) ✅

## Feature Description
Introduce a custom "Guard" profession that transforms villagers into protective entities with customizable equipment and AI behaviors. Guards can be equipped with armor and weapons, assigned specific roles (Patrol, Guard, Follow), and provide village defense capabilities.

## Core Requirements

### 1. Custom Guard Profession
- New profession "xeenaa_villager_manager:guard"
- Custom workstation block (Guard Post/Barracks)
- Configurable in config file (can be disabled)
- Custom texture/model for guard villagers
- Integration with existing profession selection GUI

### 2. GUI Enhancement - Tab System
- Transform current single-screen GUI into tabbed interface
- **Tab 1**: Professions (existing functionality)
- **Tab 2**: Equipment (guard-specific, only shows for guards)
- Equipment slots: Helmet, Chestplate, Leggings, Boots, Main Hand, Off Hand
- Drag-and-drop or click-to-equip interface
- Visual equipment preview on villager model

### 3. Equipment Management
- Guards can equip any armor/weapons player provides
- Equipment stored in villager's persistent data
- Equipment renders on villager model
- Equipment affects guard's combat stats
- Profession lock: Cannot remove guard profession while equipped

### 4. Guard AI Behaviors
Three selectable modes via Equipment tab:
- **Patrol Mode**: Guard patrols village boundaries
- **Guard Mode**: Stationary position defense
- **Follow Mode**: Follows the player who assigned them

### 5. Combat System
- Guards attack hostile mobs within range
- Damage/defense calculated from equipment
- Guards can be healed with appropriate items
- Death handling (drop equipment, respawn mechanics)

## Proposed Improvements & Considerations

### Enhanced Features
1. **Guard Levels System**
   - Guards gain experience from combat
   - Higher levels = better combat effectiveness
   - Visual indicators (badges/ranks)

2. **Guard Schedule**
   - Day/Night shift system
   - Guards sleep in barracks at night (configurable)
   - Multiple guards can share patrol routes

3. **Alert System**
   - Guards emit alert signals when detecting threats
   - Other guards respond to alerts
   - Visual/audio feedback for players

4. **Equipment Durability**
   - Equipment degrades over time/combat
   - Repair station at Guard Post
   - Auto-repair with materials in nearby chests

5. **Command System**
   - Whistle item to summon guards
   - Rally point designation
   - Formation commands for multiple guards

### Technical Architecture

#### New Classes Required
```
com.xeenaa.villagermanager/
├── profession/
│   ├── GuardProfession.java          # Custom profession registration
│   └── GuardWorkstation.java         # Guard Post block
├── entity/
│   ├── GuardVillagerEntity.java      # Extended villager with combat
│   ├── ai/
│   │   ├── GuardPatrolGoal.java     # Patrol AI
│   │   ├── GuardStationaryGoal.java # Guard position AI
│   │   └── GuardFollowGoal.java     # Follow player AI
│   └── data/
│       └── GuardData.java           # Equipment & role persistence
├── client/
│   └── gui/
│       ├── TabbedManagementScreen.java  # New tabbed GUI base
│       ├── tabs/
│       │   ├── ProfessionTab.java      # Existing profession UI
│       │   └── EquipmentTab.java       # Guard equipment UI
│       └── renderer/
│           └── GuardRenderer.java      # Render equipment on model
└── network/
    ├── packets/
    │   ├── EquipGuardPacket.java      # C2S equipment changes
    │   ├── SetGuardRolePacket.java    # C2S role assignment
    │   └── GuardDataSyncPacket.java   # S2C guard state sync
    └── GuardPacketHandler.java        # Handle guard packets
```

#### Data Storage
- NBT tags for equipment inventory
- Custom data attachments for guard state
- World saved data for guard positions

#### Config Additions
```json
{
  "guardProfession": {
    "enabled": true,
    "maxGuardsPerVillage": 10,
    "guardRange": 16,
    "patrolRadius": 48,
    "allowedEquipment": ["minecraft:iron_sword", "minecraft:shield"],
    "experienceGainRate": 1.0
  }
}
```

## Implementation Phases

### Phase 1: Foundation (Week 1)
- Create Guard profession and workstation block
- Register with Minecraft and Fabric systems
- Add to profession selection GUI
- Basic guard villager entity

### Phase 2: GUI Overhaul (Week 1-2)
- Implement tabbed GUI system
- Migrate existing profession screen to tab
- Create equipment management tab
- Add tab switching animations

### Phase 3: Equipment System (Week 2)
- Implement equipment slots and storage
- Create equipment rendering on villager
- Add drag-and-drop functionality
- Implement profession lock when equipped

### Phase 4: AI Behaviors (Week 3)
- Implement three AI goal classes
- Add role switching mechanism
- Create patrol path finding
- Implement follow behavior

### Phase 5: Combat Integration (Week 3-4)
- Add combat stats calculation
- Implement attack/defense behaviors
- Add threat detection system
- Handle guard death/respawn

### Phase 6: Polish & Testing (Week 4)
- Add sound effects and particles
- Optimize AI performance
- Comprehensive testing
- Bug fixes and balancing

## Technical Challenges

### 1. Custom Profession Registration
- Need to register during mod initialization
- Ensure compatibility with vanilla systems
- Handle profession icon generation

### 2. Equipment Rendering
- Custom armor layer rendering
- Handle different armor types/models
- Ensure client-server sync

### 3. AI Goal Priority
- Balance with existing villager AI
- Prevent goal conflicts
- Smooth transitions between modes

### 4. Persistence
- Save equipment through world saves
- Maintain guard state on chunk unload
- Handle dimension changes

### 5. Multiplayer Sync
- Equipment visibility for all players
- Role changes propagation
- Combat state synchronization

## Success Criteria
- [ ] Guard profession appears in selection GUI
- [ ] Tabbed interface works smoothly
- [ ] Equipment system fully functional
- [ ] All three AI modes work correctly
- [ ] Guards effectively defend against threats
- [ ] Multiplayer compatible
- [ ] Configurable via JSON
- [ ] No performance degradation

## Future Expansion Possibilities
- Guard squads and formations
- Training system for guards
- Guard-specific trades/rewards
- Integration with village reputation
- Custom guard weapons/armor
- Guard towers and fortifications
- Inter-village guard patrols

---

**Epic Status**: PLANNED - Awaiting implementation approval
**Estimated Duration**: 4 weeks
**Priority**: High - Core feature for villager management expansion