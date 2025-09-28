# Development Changelog

## Active Development
**Current Focus**: Critical Bug Fix Mode - User-reported ranking system issues

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