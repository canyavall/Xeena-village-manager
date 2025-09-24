# Rank Tab UI/UX Design Document

## Current State Assessment

### Existing Architecture Analysis
The Xeenaa Villager Manager mod has a well-established tabbed interface system:

**Strengths:**
- **Clean Tab Architecture**: `TabbedManagementScreen` provides solid foundation with 400x280 pixel layout
- **Professional Visual Design**: Consistent with Minecraft 1.21.1 GUI patterns
- **Existing Components**: Reusable button widgets (`TabButton`, `RoleButton`, `ProfessionButton`)
- **Solid Data Model**: Complete rank system with `GuardRank`, `GuardRankData`, and `GuardPath` enums

**Current Implementation Issues:**
- **Separate Screen Problem**: `GuardRankScreen` opens independently instead of integrating with tabbed interface
- **Workflow Disconnect**: Users must close profession tab and shift+right-click to access ranks
- **Limited Visual Feedback**: Current rank screen lacks progression visualization
- **Role Management Separation**: Roles handled separately from rank progression

## Research Findings: Mod UI Patterns

### Successful Tab Integration Examples

**1. JEI (Just Enough Items) - Recipe Categories Tab**
- **Pattern**: Left panel (ingredient list) + right panel (recipes) + bottom panel (actions)
- **Success Factor**: Clear information hierarchy with related data grouped logically
- **Applicable Lesson**: Rank info (left) + progression tree (right) + upgrade actions (bottom)

**2. REI (Roughly Enough Items) - Item Categories**
- **Pattern**: Visual progression indicators with unlock status
- **Success Factor**: Clear visual distinction between available/locked/current states
- **Applicable Lesson**: Rank progression should show clear unlock status with costs

**3. Create Mod - Contraption Management**
- **Pattern**: Current state display + available actions + resource requirements
- **Success Factor**: All related information consolidated in one interface
- **Applicable Lesson**: Rank + role + upgrade costs in unified interface

**4. Iron Chests - Inventory Management**
- **Pattern**: Equipment preview + stats comparison + upgrade path
- **Success Factor**: Real-time feedback for changes
- **Applicable Lesson**: Show stat changes before confirming rank purchases

### Interaction Flow Analysis
Popular mods excel at **context-aware interfaces** - showing relevant options based on current state:
- **EMI**: Hides irrelevant categories, shows only applicable recipes
- **Jade/WTHIT**: Information density adapts to available space
- **Inventory Tweaks**: Role-based organization with visual grouping

## Proposed RankTab Design

### 1. Visual Layout Mockup

```
┌─────────────────────────────────────────────────────────────────────┐
│  [Profession] [⭐ RANK] [Equipment] [Other...]                      │
├─────────────────────────────────────────────────────────────────────┤
│                        GUARD RANK MANAGEMENT                        │
│                                                                     │
│  ┌─────────────────────┐  ┌─────────────────────────────────────────┐ │
│  │   CURRENT STATUS    │  │           RANK PROGRESSION              │ │
│  │                     │  │                                         │ │
│  │    🛡️ RECRUIT        │  │  CHOOSE SPECIALIZATION:                 │ │
│  │                     │  │                                         │ │
│  │    ❤️  25 HP         │  │  ┌─────────────┐  ┌─────────────────┐  │ │
│  │    ⚔️   4 DMG        │  │  │   MELEE     │  │     RANGED      │  │ │
│  │                     │  │  │  🛡️ Tank     │  │   🏹 Marksman    │  │ │
│  │    Tier: 0/4        │  │  │ High HP/Def │  │  High Damage    │  │ │
│  │    Emeralds: 0      │  │  │             │  │                 │  │ │
│  │                     │  │  │  Cost: 15💎  │  │   Cost: 15💎     │  │ │
│  │  ┌─────────────────┐ │  │  │ [SELECT]    │  │   [SELECT]      │  │ │
│  │  │   ROLE: GUARD   │ │  │  └─────────────┘  └─────────────────┘  │ │
│  │  │ [PATROL][GUARD] │ │  │                                         │ │
│  │  │   [FOLLOW]      │ │  │  "Once chosen, specialization is       │ │
│  │  └─────────────────┘ │  │   permanent for this guard."           │ │
│  └─────────────────────┘  └─────────────────────────────────────────┘ │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                      UPGRADE PREVIEW                            │ │
│  │                                                                 │ │
│  │  📈 STAT COMPARISON:                    💰 COST BREAKDOWN:      │ │
│  │     Current → Next Rank                    Rank Cost: 15 💎     │ │
│  │     ❤️ 25 HP  →  35 HP (+10)                Your Balance: 42 💎  │ │
│  │     ⚔️  4 DMG →   6 DMG (+2)                After Purchase: 27💎 │ │
│  │                                                                 │ │
│  │              [UPGRADE TO MAN-AT-ARMS I] [CANCEL]                │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### Alternative Layout for Specialized Guards (Tier 1-4):

```
┌─────────────────────────────────────────────────────────────────────┐
│  [Profession] [⭐ RANK] [Equipment] [Other...]                      │
├─────────────────────────────────────────────────────────────────────┤
│                        GUARD RANK MANAGEMENT                        │
│                                                                     │
│  ┌─────────────────────┐  ┌─────────────────────────────────────────┐ │
│  │   CURRENT STATUS    │  │        MELEE PROGRESSION TREE           │ │
│  │                     │  │                                         │ │
│  │  ⚔️ MAN-AT-ARMS II   │  │  Tier 0: ✅ Recruit                     │ │
│  │                     │  │  Tier 1: ✅ Man-at-Arms I (15💎)        │ │
│  │    ❤️  50 HP         │  │  Tier 2: ⭐ Man-at-Arms II (20💎)      │ │
│  │    ⚔️   8 DMG        │  │  Tier 3: 🔒 Man-at-Arms III (45💎)     │ │
│  │                     │  │  Tier 4: 🔒 Knight (75💎)              │ │
│  │  Path: MELEE        │  │                                         │ │
│  │  Tier: 2/4          │  │  Progress: ████▒▒▒▒ (2/4)              │ │
│  │  Invested: 35💎      │  │                                         │ │
│  │                     │  │  ✨ UPCOMING ABILITIES:                   │ │
│  │  ┌─────────────────┐ │  │     🔒 Shield Bash (Tier 3)            │ │
│  │  │   ROLE: PATROL  │ │  │     🔒 Knockback Strike (Tier 4)       │ │
│  │  │ [PATROL][Guard] │ │  │                                         │ │
│  │  │   [Follow]      │ │  │                                         │ │
│  │  └─────────────────┘ │  └─────────────────────────────────────────┘ │
│  └─────────────────────┘                                            │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                    NEXT RANK UPGRADE                            │ │
│  │                                                                 │ │
│  │  🎯 TARGET: Man-at-Arms III            💰 REQUIREMENTS:         │ │
│  │      "Elite melee warrior with         Cost: 45 💎 emeralds    │ │
│  │       defensive capabilities"           Your Balance: 67 💎      │ │
│  │                                        After Purchase: 22 💎    │ │
│  │  📈 STAT IMPROVEMENTS:                  ✨ NEW ABILITIES:        │ │
│  │     ❤️ 50 → 70 HP (+20)                   🛡️ Shield Bash         │ │
│  │     ⚔️  8 → 10 DMG (+2)                                         │ │
│  │                                                                 │ │
│  │              [UPGRADE TO MAN-AT-ARMS III] [CANCEL]              │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### Max Rank Display:

```
┌─────────────────────────────────────────────────────────────────────┐
│                          MAX RANK ACHIEVED                          │
│                                                                     │
│  ┌─────────────────────┐  ┌─────────────────────────────────────────┐ │
│  │   ELITE STATUS      │  │        MASTERY COMPLETED                │ │
│  │                     │  │                                         │ │
│  │   ⚔️👑 KNIGHT         │  │  Tier 0: ✅ Recruit                     │ │
│  │                     │  │  Tier 1: ✅ Man-at-Arms I (15💎)        │ │
│  │    ❤️  95 HP         │  │  Tier 2: ✅ Man-at-Arms II (20💎)       │ │
│  │    ⚔️  12 DMG        │  │  Tier 3: ✅ Man-at-Arms III (45💎)      │ │
│  │                     │  │  Tier 4: ✅👑 Knight (75💎)             │ │
│  │  Path: MELEE        │  │                                         │ │
│  │  Tier: 4/4 MAX      │  │  Progress: ████████ COMPLETE           │ │
│  │  Total: 155💎        │  │                                         │ │
│  │                     │  │  ⭐ MASTERED ABILITIES:                   │ │
│  │  ┌─────────────────┐ │  │     ✅ Shield Bash                       │ │
│  │  │   ROLE: GUARD   │ │  │     ✅ Knockback Strike                  │ │
│  │  │ [Patrol][GUARD] │ │  │     ✅ Battle Roar                       │ │
│  │  │   [Follow]      │ │  │                                         │ │
│  │  └─────────────────┘ │  └─────────────────────────────────────────┘ │
│  └─────────────────────┘                                            │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │               🎉 MAXIMUM RANK ACHIEVED! 🎉                      │ │
│  │                                                                 │ │
│  │        This guard has reached the pinnacle of their            │ │
│  │        chosen specialization path. They are now an             │ │
│  │        elite warrior ready for the toughest battles.           │ │
│  │                                                                 │ │
│  │               [MANAGE EQUIPMENT] [VIEW ACHIEVEMENTS]            │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

## 2. Component Specifications

### 2.1 Current Status Panel (Left Panel)
**Size**: 180x240 pixels
**Background**: `0x80000000` (semi-transparent dark)
**Border**: `0xFF4A4A4A` (medium gray)

**Contents**:
- **Rank Icon**: 24x24 pixel rank-specific icon
- **Rank Name**: Large text (scale 1.2), center-aligned
- **Health & Damage**: Red heart + green sword icons with values
- **Path Display**: Colored path indicator (Blue for melee, Orange for ranged)
- **Tier Progress**: "Tier X/4" with colored text based on progress
- **Investment Tracking**: Total emeralds spent
- **Role Section**: Integrated `RoleButton` group (3 buttons, 90x20 each)

### 2.2 Progression Panel (Right Panel)
**Size**: 280x240 pixels
**Background**: Gradient from `0x80000000` to `0x80001122`
**Adaptive Content**: Changes based on guard specialization status

#### For Recruit Guards (Path Selection):
- **Path Choice Buttons**: 120x80 pixels each, side-by-side
- **Visual Icons**: Large specialization icons (32x32)
- **Cost Display**: Emerald icons with clear pricing
- **Description Text**: 2-3 line path explanation
- **Warning Message**: Permanent choice notification

#### For Specialized Guards (Progression Tree):
- **Linear Progression**: Vertical list of ranks with status icons
- **Status Icons**:
  - ✅ (Completed/Current) - Green
  - ⭐ (Current Active) - Gold with glow effect
  - 🔒 (Locked) - Gray with cost display
- **Progress Bar**: Visual progress representation (8 segments)
- **Abilities Preview**: Show locked abilities with tier requirements

### 2.3 Upgrade Panel (Bottom Panel)
**Size**: 460x80 pixels
**Background**: Dynamic - Green tint if affordable, red tint if not
**Border**: Glowing border effect for available upgrades

**Contents**:
- **Stat Comparison**: Side-by-side current vs. next rank stats
- **Cost Breakdown**: Emerald requirements + player balance
- **After Purchase**: Remaining emerald calculation
- **Action Buttons**: "UPGRADE TO [RANK]" (140x20) + "CANCEL" (80x20)

## 3. Color Scheme & Visual Design

### 3.1 Color Palette
```java
// Primary colors
private static final int BACKGROUND_DARK = 0x80000000;
private static final int BORDER_LIGHT = 0xFF4A4A4A;
private static final int TEXT_WHITE = 0xFFFFFF;
private static final int TEXT_GRAY = 0xFFAAAAA;

// Status colors
private static final int RANK_CURRENT = 0xFFFFD700;  // Gold
private static final int RANK_AVAILABLE = 0xFF00FF00; // Green
private static final int RANK_LOCKED = 0xFF888888;    // Gray
private static final int COST_AFFORDABLE = 0xFF55FF55; // Light green
private static final int COST_EXPENSIVE = 0xFFFF5555;  // Light red

// Path colors
private static final int PATH_MELEE = 0xFF4169E1;     // Royal blue
private static final int PATH_RANGED = 0xFFFF8C00;    // Dark orange
private static final int PATH_RECRUIT = 0xFF888888;   // Gray

// Upgrade panel backgrounds
private static final int UPGRADE_AVAILABLE = 0x80004400; // Green tint
private static final int UPGRADE_LOCKED = 0x80440000;    // Red tint
private static final int UPGRADE_MAXRANK = 0x80444400;   // Gold tint
```

### 3.2 Typography
- **Headers**: Minecraft font, scale 1.2, with shadow
- **Stats**: Minecraft font, scale 1.0, colored by type
- **Costs**: Minecraft font, scale 1.0, colored by affordability
- **Descriptions**: Minecraft font, scale 0.9, gray text

### 3.3 Icon System
**Rank Icons** (24x24 PNG):
- `rank_recruit.png` - Basic helmet
- `rank_man_at_arms_1.png` - Leather armor
- `rank_man_at_arms_2.png` - Mixed leather/iron
- `rank_man_at_arms_3.png` - Iron armor
- `rank_knight.png` - Full iron with plume
- `rank_marksman_1.png` - Bow + cap
- `rank_marksman_2.png` - Enhanced bow gear
- `rank_marksman_3.png` - Elite archer outfit
- `rank_sharpshooter.png` - Master archer regalia

**UI Icons** (16x16 PNG):
- `emerald_small.png` - Currency display
- `heart_red.png` - Health indicator
- `sword_iron.png` - Damage indicator
- `progress_filled.png` - Progress bar segment
- `progress_empty.png` - Empty progress segment

## 4. User Interaction Flows

### 4.1 First-Time Guard Creation Flow
1. **Profession Selection**: Player selects Guard profession in Profession tab
2. **Automatic Transition**: Screen automatically switches to Rank tab
3. **Welcome State**: Rank tab shows Recruit status with path choice
4. **Path Selection**: Player chooses Melee or Ranged specialization
5. **First Upgrade**: Immediate upgrade to tier 1 rank
6. **Role Assignment**: Player sets initial role (Patrol/Guard/Follow)

### 4.2 Ongoing Rank Management Flow
1. **Tab Access**: Click Rank tab in villager management screen
2. **Status Review**: View current rank, stats, and investment
3. **Upgrade Decision**: Review next rank benefits and costs
4. **Purchase Confirmation**: Click upgrade button if affordable
5. **Immediate Feedback**: Stats update with visual confirmation
6. **Role Adjustment**: Modify role as needed for new capabilities

### 4.3 Max Rank Achievement Flow
1. **Final Upgrade**: Purchase tier 4 rank (Knight/Sharpshooter)
2. **Celebration Display**: Special max rank UI with achievements
3. **Feature Unlock**: Access to equipment management and advanced features
4. **Mastery Status**: Clear indication of completed progression

## 5. User Experience Improvements

### 5.1 Over Current Implementation
**Problems Solved**:
- ❌ **Workflow Break**: No more separate GuardRankScreen
- ❌ **Context Switching**: Ranks integrated with other management
- ❌ **Information Scatter**: All guard data in unified interface
- ❌ **Poor Visual Feedback**: Clear progression and cost visualization
- ❌ **Role Separation**: Roles integrated with rank management

**New Benefits**:
- ✅ **Unified Workflow**: Profession → Rank → Equipment in same interface
- ✅ **Smart Defaults**: Automatic tab switching for new guards
- ✅ **Visual Clarity**: Clear progression trees and stat comparisons
- ✅ **Cost Transparency**: Upfront emerald requirements and balance display
- ✅ **Role Context**: Roles displayed with rank for logical grouping

### 5.2 Accessibility Features
- **Keyboard Navigation**: Full tab and button navigation support
- **Screen Reader Support**: Proper ARIA labels and descriptions
- **Color Blind Support**: Icons + text labels for all status indicators
- **Tooltip System**: Comprehensive hover information
- **High Contrast Mode**: Alternative color scheme for visibility

### 5.3 Performance Optimizations
- **Lazy Rendering**: Only render visible tab content
- **Cached Icons**: Reuse rendered icons across sessions
- **Batch Updates**: Group related network packets
- **Smart Refreshing**: Only update changed UI elements

## 6. Implementation Architecture

### 6.1 Class Structure
```java
public class RankTab extends Tab {
    // Main tab implementation

    private RankStatusWidget statusWidget;      // Left panel
    private RankProgressionWidget progressWidget; // Right panel
    private RankUpgradeWidget upgradeWidget;     // Bottom panel
    private List<RoleButton> roleButtons;        // Role selection
}

public class RankStatusWidget extends Widget {
    // Current rank display with stats and role buttons

    private void renderRankIcon(DrawContext context);
    private void renderRankStats(DrawContext context);
    private void renderInvestmentInfo(DrawContext context);
    private void renderRoleSection(DrawContext context);
}

public class RankProgressionWidget extends Widget {
    // Adaptive progression display

    private PathSelectionPanel pathPanel;      // For recruits
    private ProgressTreePanel progressPanel;   // For specialized guards

    private void switchDisplayMode(GuardRank currentRank);
}

public class RankUpgradeWidget extends Widget {
    // Upgrade preview and purchase interface

    private void renderStatComparison(DrawContext context);
    private void renderCostBreakdown(DrawContext context);
    private void renderUpgradeButtons(DrawContext context);
}
```

### 6.2 Integration Points
- **Tab System**: Extends existing `Tab` base class
- **Data Access**: Uses `GuardRankData` and `GuardData` from current system
- **Network Layer**: Integrates with existing `PurchaseRankPacket` system
- **Role System**: Incorporates existing `RoleButton` components
- **Resource Management**: Uses established texture and translation systems

### 6.3 Network Packets
```java
// Existing - will be reused
PurchaseRankPacket (C2S) - Rank upgrade requests
GuardRankSyncPacket (S2C) - Rank data synchronization

// New packets needed
RoleChangePacket (C2S) - Role modification from rank tab
PathSelectionPacket (C2S) - Initial specialization choice
```

## 7. Priority Implementation Roadmap

### Phase 1: Core Integration (Week 1-2)
- ✅ Create `RankTab` class extending `Tab`
- ✅ Implement basic layout with three widget panels
- ✅ Integration with `TabbedManagementScreen`
- ✅ Replace `GuardRankScreen` workflow

### Phase 2: Status & Progression Display (Week 2-3)
- ✅ `RankStatusWidget` with current rank display
- ✅ `RankProgressionWidget` with adaptive content
- ✅ Role button integration
- ✅ Basic path selection interface

### Phase 3: Upgrade System (Week 3-4)
- ✅ `RankUpgradeWidget` with stat comparison
- ✅ Cost calculation and validation
- ✅ Purchase confirmation flow
- ✅ Network packet integration

### Phase 4: Polish & Enhancement (Week 4-5)
- ✅ Visual effects and animations
- ✅ Comprehensive tooltip system
- ✅ Accessibility features
- ✅ Performance optimization

### Phase 5: Advanced Features (Week 5-6)
- ✅ Ability preview system
- ✅ Achievement integration
- ✅ Equipment tab transition preparation
- ✅ Comprehensive testing

## 8. Quality Assurance & Testing

### 8.1 User Experience Testing
- **New Player Flow**: First-time guard creation and rank progression
- **Experienced Player Flow**: Quick rank management and role switching
- **Edge Cases**: Max rank display, insufficient emeralds, network issues
- **Accessibility Testing**: Keyboard navigation, screen reader compatibility

### 8.2 Technical Testing
- **Performance**: Frame rate with multiple villagers
- **Memory**: Resource cleanup and leak prevention
- **Network**: Packet efficiency and error handling
- **Compatibility**: Integration with existing mods and resource packs

### 8.3 Visual Testing
- **GUI Scales**: 1x, 2x, 3x, Auto scaling support
- **Resolutions**: 1080p, 1440p, 4K display compatibility
- **Themes**: Dark mode and high contrast support
- **Languages**: Multi-language text fitting and wrapping

## Conclusion

This RankTab design provides a comprehensive solution that:

1. **Solves Current Problems**: Eliminates workflow breaks and integrates rank management into the main tabbed interface
2. **Follows Best Practices**: Uses proven UI patterns from successful Minecraft mods
3. **Enhances User Experience**: Provides clear visual feedback, cost transparency, and logical information grouping
4. **Maintains Design Consistency**: Builds on existing visual language and component architecture
5. **Enables Future Growth**: Architecture supports additional features like equipment integration and achievement systems

The implementation roadmap provides a clear path from basic integration to advanced features, ensuring each phase delivers immediate value while building toward the complete vision.

**Key Success Metrics**:
- Reduced clicks to access rank management (from 4-5 to 2-3)
- Improved user comprehension of progression costs and benefits
- Seamless integration with existing profession and equipment workflows
- Enhanced accessibility and performance compared to current implementation

This design positions the rank system as a core feature of the villager management experience rather than a separate secondary function, significantly improving the overall user experience and mod usability.