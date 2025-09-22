# UI Specifications - Xeenaa Villager Manager

## Overview
This document defines the visual design specifications for all GUI elements in the Xeenaa Villager Manager mod.

## Style Guide

### Frame & Border Styling
- **Outer Frame Border**:
  - Dark brown: `#35322b`
  - Secondary accent: `#5b533e`
  - Use **double border effect**: outer `#1c1b16` (shadow), inner `#5b533e` (highlight).
- **Inner Divider Lines**:
  - Default: `#4a4234`
  - Hover/active: `#5c543f`.
- **Corners**: Pixel-sharp, no rounded edges. Thicker corners (2px) in `#35322b` for framed parchment look.

### Backgrounds
- **Main Panel**: `#fcf7e3` (primary parchment)
- **Secondary Panels**: `#fdf8e4`, `#fef9e5` (lighter parchment)
- **Tab Bar**:
  - Active: `#706a5c`
  - Inactive: `#6f695b`

### Fonts
- **Primary Text**: `#fffff3` (labels, active tab text)
- **Secondary Text**: `#dad3b6` (disabled, tooltips)
- **Highlight Text**: `#f7f6e4` (selected states)

### Buttons
- **Default**: Fill `#5a523d`, border `#35322b`
- **Hover**: Fill `#59513c`
- **Active/Pressed**: Fill `#706a5c`, border `#1c1b16`
- **Role Selection**: Highlighted option uses green text on base button background

### Visual States
- **Active Tab**: Background `#706a5c`, text `#fffff3`
- **Inactive Tab**: Background `#6f695b`, text `#dad3b6`
- **Empty Slot**: Standard inventory style with border `#4a4234`
- **Equipped Item**: Standard render with enchantment glow overlay

---

## Color Palette JSON

```json
{
  "frame": {
    "outer": "#35322b",
    "inner": "#5b533e",
    "shadow": "#1c1b16"
  },
  "background": {
    "primary": "#fcf7e3",
    "secondary": "#fdf8e4",
    "tertiary": "#fef9e5"
  },
  "text": {
    "primary": "#fffff3",
    "secondary": "#dad3b6",
    "highlight": "#f7f6e4"
  },
  "button": {
    "default": "#5a523d",
    "hover": "#59513c",
    "active": "#706a5c",
    "border": "#35322b"
  },
  "divider": {
    "default": "#4a4234",
    "hover": "#5c543f"
  },
  "tab": {
    "active_bg": "#706a5c",
    "inactive_bg": "#6f695b",
    "active_text": "#fffff3",
    "inactive_text": "#dad3b6"
  }
}
```

---

## Screen Specifications

### VillagerManagementScreen (Main Container)
- **Size**: 256x166 pixels
- **Background**: Primary parchment (`#fcf7e3`)
- **Border**: Double border with outer shadow (`#1c1b16`) and inner highlight (`#5b533e`)
- **Tab Bar**: Top-aligned, 30px height

### ProfessionsTab
- **Grid Layout**: 4x4 profession buttons
- **Button Size**: 20x20 pixels each
- **Spacing**: 4px between buttons
- **Hover Effect**: Border highlight (`#5c543f`)
- **Selected**: Green overlay with checkmark

### EquipmentTab
- **Layout**: Two-column design
- **Left Column**: Equipment slots (6 slots vertical)
- **Right Column**: Role selection (3 radio buttons)
- **Slot Size**: Standard 18x18 inventory slots
- **Role Buttons**: Full width, 20px height each

### TradesTab (Future)
- **Layout**: Scrollable list of trade offers
- **Trade Row Height**: 32px
- **Scroll Bar**: Right-aligned, Minecraft standard style

### InventoryTab (Future)
- **Layout**: Standard Minecraft chest interface
- **Slots**: 3x9 villager inventory grid
- **Player Inventory**: Bottom section, standard layout

---

## Component Library

### TabButton
- **Size**: Dynamic width based on text, 30px height
- **Active State**: `#706a5c` background, `#fffff3` text
- **Inactive State**: `#6f695b` background, `#dad3b6` text
- **Hover**: Slight brightness increase (+10%)

### ItemSlot
- **Size**: 18x18 pixels
- **Border**: `#4a4234` default, `#5c543f` on hover
- **Background**: Transparent when empty
- **Highlight**: Blue overlay when valid drop target

### RoleButton
- **Type**: Radio button style
- **Size**: Full width, 20px height
- **Selected**: Green text (`#7fc97f`)
- **Unselected**: Secondary text color (`#dad3b6`)

### ProfessionButton
- **Size**: 20x20 pixels
- **Icon**: 16x16 centered
- **Border**: 2px, follows button color scheme
- **Tooltip**: Shows profession name on hover

---

## Animation Guidelines

### Transitions
- **Tab Switch**: Instant, no animation
- **Button Hover**: 100ms ease-in-out
- **Item Drag**: Follow cursor with slight lag (2 frames)

### Feedback
- **Button Click**: Visual press state for 150ms
- **Invalid Action**: Red flash overlay for 200ms
- **Success Action**: Green particle effect

---

## Accessibility

### Contrast Ratios
- Primary text on background: 7.2:1 (AAA)
- Secondary text on background: 4.8:1 (AA)
- Button text on button: 5.1:1 (AA)

### Interaction Targets
- Minimum clickable area: 20x20 pixels
- Touch-friendly spacing: 4px minimum between interactive elements

---

## Implementation Notes

### Color Conversion
All hex colors should be converted to Minecraft's ARGB integer format:
- Use `0xFF` prefix for full opacity
- Example: `#fcf7e3` → `0xFFFCF7E3`

### Texture Assets
- All UI textures stored in: `assets/xeenaa_villager_manager/textures/gui/`
- Use 256x256 texture atlases where possible
- 9-slice borders for scalable panels

### Rendering Order
1. Background panel
2. Frame borders
3. Tab bar
4. Active tab content
5. Interactive elements
6. Tooltips (top layer)