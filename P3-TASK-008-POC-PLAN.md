# P3-TASK-008: Unified Tab UI Design - Proof of Concept Plan

## Overview
Merge three separate tabs (Profession, Rank, Config) into a single unified interface for improved UX.

## Current Tab Structure
1. **Profession Tab**: Profession selection grid
2. **Rank Tab**: Rank progression and purchase interface
3. **Config Tab**: Guard behavior configuration (detection range, guard mode, profession lock)

## Proposed Unified Layout

```
┌─────────────────────────────────────────────────────────────┐
│ Header: [Villager Name] - [Guard Icon] Knight ⭐⭐⭐⭐      │
│         HP: 40 | Damage: 10 | Detection: 20 blocks          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  LEFT COLUMN (40%)         │  RIGHT COLUMN (60%)             │
│  ┌──────────────────────┐  │  ┌──────────────────────────┐  │
│  │ RANK PROGRESSION      │  │  │ BEHAVIOR SETTINGS        │  │
│  │                       │  │  │                          │  │
│  │ Current: Knight ⭐⭐⭐⭐│  │  │ Guard Mode:              │  │
│  │                       │  │  │ [FOLLOW][PATROL][STAND]  │  │
│  │ Path: Melee (Knight)  │  │  │                          │  │
│  │                       │  │  │ Detection Range: [====|] │  │
│  │ ┌─────────────────┐  │  │  │ 20 blocks                │  │
│  │ │ Next Rank:      │  │  │  │                          │  │
│  │ │ (Max Tier)      │  │  │  │ Profession Lock: [X]     │  │
│  │ │                 │  │  │  │                          │  │
│  │ └─────────────────┘  │  │  └──────────────────────────┘  │
│  │                       │  │                                │
│  │ Stats Comparison:     │  │  ┌──────────────────────────┐  │
│  │ HP:  40 → 40 (max)   │  │  │ EQUIPMENT STATUS         │  │
│  │ DMG: 10 → 10 (max)   │  │  │                          │  │
│  │                       │  │  │ Weapon: Iron Sword       │  │
│  └──────────────────────┘  │  │ Armor:  Iron Armor       │  │
│                             │  └──────────────────────────┘  │
│                             │                                │
│                             │  ┌──────────────────────────┐  │
│                             │  │ QUICK ACTIONS            │  │
│                             │  │ [Save Config] [Reset]    │  │
│                             │  └──────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│ Footer: [Change Profession] Status: Configuration Saved ✓   │
└─────────────────────────────────────────────────────────────┘
```

## POC Goals
1. Create basic single-screen layout with column structure
2. Migrate rank widgets from RankTab to left column
3. Migrate config widgets from ConfigTab to right column
4. Add header section with quick stats
5. Add footer with profession change button
6. Test layout responsiveness and usability

## Technical Approach

### Step 1: Create New Screen Class
- Create `UnifiedGuardManagementScreen.java`
- Extend vanilla `Screen` class
- Set up basic layout structure

### Step 2: Layout System
- Define column widths (40/60 split)
- Create section separators
- Implement responsive positioning

### Step 3: Widget Migration
- **Left Column**:
  - Current rank display
  - Path indicator
  - Next rank info (if available)
  - Stats comparison
  - Purchase button (if upgradeable)

- **Right Column**:
  - Guard mode selector (3 buttons)
  - Detection range slider
  - Profession lock toggle
  - Equipment display (future)
  - Action buttons (save/reset)

### Step 4: Header/Footer
- Header: Name, rank display, quick stats
- Footer: Profession change button, status indicator

## Files to Reference
- `src/client/java/com/xeenaa/villagermanager/client/gui/TabbedManagementScreen.java`
- `src/client/java/com/xeenaa/villagermanager/client/gui/tabs/RankTab.java`
- `src/client/java/com/xeenaa/villagermanager/client/gui/tabs/ConfigTab.java`
- `src/client/java/com/xeenaa/villagermanager/client/gui/tabs/ProfessionTab.java`

## Success Criteria for POC
- [ ] Single screen renders without tabs
- [ ] All rank information visible in left column
- [ ] All config controls functional in right column
- [ ] Header shows villager info and stats
- [ ] Footer has profession change access
- [ ] Layout is clean and organized
- [ ] No functionality lost from tabbed version

## Next Steps After POC
1. User testing and feedback
2. Polish visual design and spacing
3. Add animations/transitions
4. Implement collapsible sections if needed
5. Full integration and migration
6. Remove old tabbed system

## Notes
- Keep old tab system files until POC is validated
- Test on different screen resolutions
- Consider adding scrolling if content exceeds screen height
- Maintain vanilla Minecraft UI style consistency
