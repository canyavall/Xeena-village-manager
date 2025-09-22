# GUI Layout Specification

## Current Implementation Analysis

Based on the existing codebase, here's the detailed layout specification for the Villager Manager GUI:

## Screen Dimensions & Layout

### TabbedManagementScreen Base Layout
```
┌─────────────────────────────────────────────────────────────┐
│ [Q] [Tab 1] [Tab 2] [Tab N...]                           [E] │ ← Tab Bar (32px height)
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                     Tab Content Area                        │ ← Main Content (backgroundHeight - 32px)
│                   (Dynamic based on tab)                    │
│                                                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Dimensions**:
- Total Width: `backgroundWidth` (standard Minecraft GUI width)
- Total Height: `backgroundHeight` (standard Minecraft GUI height)
- Tab Bar Height: 32 pixels
- Content Area: Full width × (backgroundHeight - 32px)

### ProfessionTab Layout
```
┌─────────────────────────────────────────────────────────────┐
│                    Profession Grid                          │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐          │
│  │ P │ │ P │ │ P │ │ P │ │ P │ │ P │ │ P │ │ P │          │ ← Row 1
│  └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘          │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐          │
│  │ P │ │ P │ │ P │ │ P │ │ P │ │ P │ │ P │ │ P │          │ ← Row 2
│  └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘          │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐          │
│  │ P │ │ P │ │ P │ │ P │ │ P │ │ P │ │ P │ │ P │          │ ← Row 3
│  └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘          │
└─────────────────────────────────────────────────────────────┘
```

**Grid Specifications**:
- Grid: 8 columns × 3 rows (24 profession slots maximum)
- Button Size: 32×32 pixels per profession
- Button Spacing: 4 pixels between buttons
- Grid Centering: Horizontally and vertically centered in content area

### EquipmentTab Layout
```
┌─────────────────────────────────────────────────────────────┐
│           Equipment Management (Two-Section Layout)         │
│                                                             │
│ ┌─────────────────────────┐  ┌─────────────────────────────┐ │
│ │     Equipment Slots     │  │      Role Selection         │ │
│ │                         │  │                             │ │
│ │  ┌───┐     ┌───┐       │  │   ┌─────────┐                │ │
│ │  │Wpn│     │Hmt│       │  │   │ Patrol  │                │ │ ← Top Row
│ │  └───┘     └───┘       │  │   └─────────┘                │ │
│ │                         │  │   ┌─────────┐                │ │
│ │  ┌───┐     ┌───┐       │  │   │  Guard  │                │ │ ← Middle Row
│ │  │Cht│     │Leg│       │  │   └─────────┘                │ │
│ │  └───┘     └───┘       │  │   ┌─────────┐                │ │
│ │                         │  │   │ Follow  │                │ │ ← Bottom Row
│ │  ┌───┐     ┌───┐       │  │   └─────────┘                │ │
│ │  │Bts│     │Shd│       │  │                             │ │
│ │  └───┘     └───┘       │  │                             │ │
│ └─────────────────────────┘  └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

**Equipment Section Specifications**:
- Left Section: Equipment slots (50% of content width)
- Right Section: Role selection (50% of content width)
- Equipment Slot Size: 32×32 pixels (standard Minecraft slot size)
- Equipment Slot Arrangement: 2×3 grid layout
  - Row 1: Weapon, Helmet
  - Row 2: Chestplate, Leggings
  - Row 3: Boots, Shield
- Role Button Size: 100×24 pixels
- Section Padding: 16 pixels internal margins

## Color Scheme & Visual Elements

### Current Theme
- **Background**: Standard Minecraft GUI texture (dirt/stone pattern)
- **Tabs**: Light gray with white text when active, darker gray when inactive
- **Equipment Slots**: Standard inventory slot appearance with item rendering
- **Role Buttons**: Standard button texture with colored text based on selection state

### Visual State Indicators
- **Active Tab**: White text, slightly brighter background
- **Inactive Tab**: Gray text, standard background
- **Selected Role**: Green text color or highlighted button state
- **Empty Equipment Slot**: Standard empty slot texture with subtle border
- **Equipped Item**: Item rendered with standard enchantment glow if applicable

## Interaction Specifications

### Mouse Interactions
1. **Tab Clicking**: Standard left-click to switch tabs
2. **Equipment Slots**:
   - Left-click to place/remove equipment
   - Right-click for context menu (future enhancement)
   - Drag-and-drop support from player inventory
3. **Role Buttons**: Left-click to select role with visual feedback
4. **Profession Buttons**: Left-click to assign profession

### Keyboard Navigation (Future Enhancement)
- Tab key to cycle through interactive elements
- Arrow keys for grid navigation
- Enter to confirm selections
- Escape to close screen

## Responsive Design Considerations

### GUI Scale Support
The current implementation should support Minecraft's GUI scale options:
- **1x Scale**: Full resolution, detailed layout
- **2x Scale**: Larger elements, maintained proportions
- **3x Scale**: Maximum size with simplified layout if necessary
- **Auto Scale**: System-determined optimal scale

### Screen Resolution Adaptability
- **1080p (1920×1080)**: Standard layout with full features
- **1440p (2560×1440)**: Enhanced spacing, larger hit targets
- **4K (3840×2160)**: Crisp rendering with maintained proportions
- **Ultra-wide**: Centered layout with appropriate margins

## Technical Implementation Notes

### Rendering Performance
- Equipment preview rendering should be optimized for real-time updates
- Use cached textures for profession icons
- Implement lazy loading for large profession lists
- Minimize draw calls through batched rendering

### Memory Management
- Cache frequently used GUI textures
- Dispose of unused resources when screens close
- Implement proper texture cleanup for equipment previews

### Accessibility Features
- High contrast mode support
- Screen reader compatibility hooks
- Keyboard navigation framework
- Colorblind-friendly visual indicators

## Future Layout Enhancements

### Phase 4 Additions
```
┌─────────────────────────────────────────────────────────────┐
│ [Profession] [Equipment] [Schedule] [Village] [Settings]    │ ← Expanded Tab Bar
├─────────────────────────────────────────────────────────────┤
│                   Enhanced Content Area                     │
│                                                             │
│  Potential new features:                                    │
│  - Village overview map                                     │
│  - Schedule management calendar                             │
│  - Equipment workshop interface                             │
│  - Performance analytics dashboard                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Enhanced Equipment Preview
```
┌─────────────────────────────────────────────────────────────┐
│ ┌─────────────────┐  ┌───────────────┐  ┌─────────────────┐ │
│ │ Equipment Slots │  │ 3D Preview    │  │ Role & Status   │ │
│ │                 │  │               │  │                 │ │
│ │ ┌───┐ ┌───┐    │  │   ┌─────┐     │  │ Role: [Patrol]  │ │
│ │ │Wpn│ │Hmt│    │  │   │  👤  │     │  │ Status: Active  │ │
│ │ └───┘ └───┘    │  │   │ /|\ │     │  │ Health: 20/20   │ │
│ │ ┌───┐ ┌───┐    │  │   │ / \ │     │  │ Equipment: 4/6  │ │
│ │ │Cht│ │Leg│    │  │   └─────┘     │  │                 │ │
│ │ └───┘ └───┘    │  │               │  │ ┌─────────────┐ │ │
│ │ ┌───┐ ┌───┐    │  │               │  │ │ Set Patrol  │ │ │
│ │ │Bts│ │Shd│    │  │               │  │ │    Route    │ │ │
│ │ └───┘ └───┘    │  │               │  │ └─────────────┘ │ │
│ └─────────────────┘  └───────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

This layout specification provides a comprehensive guide for the current implementation and future enhancements, ensuring consistency and scalability as new features are added to the villager manager mod.

---

**Document Version**: 1.0
**Layout Standard**: Minecraft 1.21.1 GUI Conventions
**Last Updated**: 2025-09-20
**Implementation Status**: Phase 2 Complete, Phase 3 Active