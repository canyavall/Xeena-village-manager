# Issue Tracker

## Active Issues

### ✅ RESOLVED: GUI Blur Issue (Epic 2 - Phase 2.1)
**Issue ID**: GUI-BLUR-001
**Reported**: 2025-09-18
**Resolved**: 2025-09-18
**Status**: RESOLVED - Successfully implemented renderBackground override
**Priority**: HIGH

#### Problem Description
Custom tabbed GUI (TabbedManagementScreen) renders with blur effect, making interface unclear and difficult to use.

#### Root Cause Analysis
**DISCOVERED**: The blur is a **new feature introduced in Minecraft 1.21+**, not a bug in our code. Our attempts to "fix" it were misguided because we thought it was unintended.

#### Research Findings

**What We've Tried (All Failed)**:
1. ❌ Removed `renderBackground()` call completely
2. ❌ Changed render order (super.render() before/after custom rendering)
3. ❌ Changed from semi-transparent to solid colors
4. ❌ Modified color constants and backgrounds
5. ❌ Various combinations of the above

**Key Discovery**:
- Minecraft 1.21 introduced blur effects for menu backgrounds by default
- The blur is applied by the game engine, not by our mod
- `renderBackground()` method applies the new blur shader in 1.21+
- Official mod "NoMenuBlur" exists specifically to disable this feature

#### Technical Analysis

**Why Our Approaches Failed**:
- Removing `renderBackground()`: Only removes the background texture, not the blur shader
- Render order changes: Blur is applied at engine level, not affected by render order
- Color changes: Blur is a post-processing effect, independent of colors

**Proper Solutions Available**:
1. **Override `renderBackground()` method** to bypass blur shader
2. **Use alternative background rendering methods** (`renderBackgroundTexture`, `renderInGameBackground`)
3. **Implement custom background rendering** without blur shader
4. **Add configuration option** to enable/disable blur

#### Proposed Solution
Override the `renderBackground()` method in `TabbedManagementScreen` to use non-blur rendering:

```java
@Override
protected void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    // Use alternative rendering that doesn't apply blur
    context.fill(0, 0, this.width, this.height, 0x66000000); // Semi-transparent overlay
}
```

#### Documentation References
- NoMenuBlur mod: https://modrinth.com/mod/nomenublur
- Fabric Custom Screens: https://docs.fabricmc.net/develop/rendering/gui/custom-screens
- Stack Overflow DrawContext: https://stackoverflow.com/questions/78935043/how-to-use-drawcontext-minecraft-fabric

#### Impact Assessment
- **User Experience**: Critical - GUI unusability due to blur
- **Development**: Medium - Affects GUI development workflow
- **Release**: Blocker - Cannot release with blurry GUI

#### ✅ FINAL RESOLUTION
**Implemented Solution**: Override `renderBackground()` method in `TabbedManagementScreen`
```java
@Override
public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    context.fill(0, 0, this.width, this.height, 0x66000000);
}
```

**Result**: GUI now renders crystal clear without any blur effects
**Verification**: Testing confirmed complete resolution of blur issue
**Documentation**: Full technical analysis documented in changelog.md

---

## Resolved Issues

### GUI Responsive Scaling (GUI-SCALE-001)
**Status**: DOCUMENTED - Task created for future implementation
**Priority**: MEDIUM
**Issue**: GUI too large on auto scale, not responsive to different resolutions
**Resolution**: Added to tasks.md as Phase 2.1 improvement task

---

**Issue Tracker Maintained By**: minecraft-qa-specialist
**Last Updated**: 2025-09-18
**Next Review**: After GUI-BLUR-001 resolution