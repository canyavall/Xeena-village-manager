# Concrete Solutions for Villager Texture Blending Issue

## Problem Root Cause Analysis

After extensive research, the texture blending issue is caused by **Minecraft's multi-layer villager texture system** introduced in 1.14+. The rendering system applies multiple texture layers:

1. **Base villager texture** (never seen in-game)
2. **Biome overlay** (desert, jungle, plains, savanna, snow, swamp, taiga)
3. **Profession overlay** (our guard texture)
4. **Level overlay** (stone, iron, gold, diamond, emerald)

**Critical Finding**: Our `getTexture()` method interception only affects the profession overlay layer, but the biome and level overlays are still being applied on top, causing the "mixed" appearance.

## Tested Solutions and Analysis

### Current Approach Issues
- ✅ **Profession detection**: Working correctly
- ✅ **Texture path setting**: Successfully intercepted
- ❌ **Visual result**: Blended appearance due to overlay system

## Recommended Solutions (Ranked by Feasibility)

### Solution 1: Complete Layer Renderer Replacement 🏆 RECOMMENDED
**Approach**: Create a custom feature renderer that completely replaces villager appearance for guard profession.

**Implementation**:
```java
// Register in client initializer
LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
    if (entityRenderer instanceof VillagerEntityRenderer) {
        registrationHelper.register(new GuardVillagerOverrideRenderer<>((VillagerEntityRenderer) entityRenderer));
    }
});

// Custom feature renderer that overrides appearance
public class GuardVillagerOverrideRenderer<T extends VillagerEntity, M extends VillagerEntityModel<T>>
        extends FeatureRenderer<T, M> {

    private static final Identifier GUARD_TEXTURE = new Identifier("xeenaa_villager_manager",
        "textures/entity/villager/guard_complete.png");

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                      T entity, float limbAngle, float limbDistance, float tickDelta,
                      float animationProgress, float headYaw, float headPitch) {

        if (entity.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Render complete guard texture, bypassing all overlays
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(GUARD_TEXTURE));
            this.getContextModel().render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
```

**Advantages**:
- Complete control over rendering
- Bypasses overlay system entirely
- No texture blending
- Compatible with Fabric API

**Requirements**:
- Design complete guard texture (not overlay)
- Implement feature renderer properly
- Register with proper entity filtering

### Solution 2: Resource Pack Integration Approach
**Approach**: Structure textures as a complete resource pack with base texture override.

**Implementation**:
```
src/main/resources/assets/xeenaa_villager_manager/
  textures/
    entity/
      villager/
        type/
          guard.png           # Base villager texture for guards
        profession/
          guard.png           # Profession overlay (transparent/minimal)
```

**Custom Resource Manager**:
```java
// Override texture loading for guard villagers
@Mixin(VillagerEntityRenderer.class)
public class GuardVillagerTextureMixin {

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void replaceGuardTexture(VillagerEntity villager, CallbackInfoReturnable<Identifier> cir) {
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Use complete texture, not overlay
            cir.setReturnValue(new Identifier("xeenaa_villager_manager",
                "textures/entity/villager/guard_complete.png"));
        }
    }
}
```

### Solution 3: Overlay System Modification
**Approach**: Modify the overlay rendering system to disable biome/level overlays for guards.

**Implementation**:
```java
// Mixin to intercept overlay application
@Mixin(VillagerEntityRenderer.class)
public class VillagerOverlayControlMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void controlOverlayRendering(VillagerEntity villager, float yaw, float tickDelta,
                                       MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                       int light, CallbackInfo ci) {
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Temporarily modify villager data to use plains biome (neutral)
            // and base level to minimize overlay interference
        }
    }
}
```

### Solution 4: Entity Model Replacement
**Approach**: Create a custom villager entity model specifically for guards.

**Implementation**:
```java
// Custom model layer for guard villagers
public class GuardVillagerEntityModel<T extends VillagerEntity> extends VillagerEntityModel<T> {

    public GuardVillagerEntityModel(ModelPart root) {
        super(root);
    }

    // Override texture coordinates or model parts if needed
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
                      float red, float green, float blue, float alpha) {
        // Custom rendering logic for guards
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
```

## Implementation Recommendations

### Immediate Action Plan

1. **Start with Solution 1** (Feature Renderer Replacement)
   - Highest success probability
   - Complete control over appearance
   - Minimal interference with existing code

2. **Prepare complete guard texture**
   - Design full villager texture (not overlay)
   - Include all necessary villager parts
   - Size: 64x64 pixels matching vanilla format

3. **Test feature renderer registration**
   - Implement basic feature renderer
   - Verify registration with villager entity renderer
   - Test texture replacement effectiveness

### Testing Strategy

```java
// Debug helper to verify overlay behavior
@Environment(EnvType.CLIENT)
public class VillagerTextureDebugger {

    public static void logVillagerTextures(VillagerEntity villager) {
        System.out.println("=== Villager Texture Debug ===");
        System.out.println("Profession: " + villager.getVillagerData().getProfession());
        System.out.println("Biome: " + villager.getVillagerData().getType());
        System.out.println("Level: " + villager.getVillagerData().getLevel());
        System.out.println("Expected overlays: " + calculateExpectedOverlays(villager));
    }
}
```

### Fallback Options

If primary solutions fail:
1. **Entity Texture Features compatibility**: Design textures in ETF format
2. **Resource pack distribution**: Include complete resource pack with mod
3. **Player-side configuration**: Allow users to install compatible resource packs

## Technical Requirements

### Dependencies
- Fabric API
- Client-side rendering capabilities
- Feature renderer registration support

### File Structure
```
src/
├── main/java/com/xeenaa/villagermanager/
│   ├── client/renderer/GuardVillagerOverrideRenderer.java
│   └── client/VillagerManagerClient.java
└── main/resources/assets/xeenaa_villager_manager/
    └── textures/entity/villager/
        └── guard_complete.png
```

### Testing Checklist
- [ ] Feature renderer registers correctly
- [ ] Guard villagers show pure guard texture
- [ ] No blending with biome overlays
- [ ] No blending with level overlays
- [ ] Performance impact acceptable
- [ ] Compatible with other mods

## Success Metrics

✅ **Complete texture replacement**: Guard villagers show only guard appearance
✅ **No visual blending**: Clean, pure texture display
✅ **Consistent behavior**: Works across all biomes and levels
✅ **Performance**: No noticeable rendering impact
✅ **Compatibility**: Works with existing mod ecosystem