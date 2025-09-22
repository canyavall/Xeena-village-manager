# Fabric 1.21.1 Specific Implementation Guide for Guard Villager Visuals

## Overview

This document provides specific implementation guidance for creating guard villager visuals in Minecraft 1.21.1 using the Fabric modding framework, based on research of successful guard villager mods and their approaches.

## Minecraft 1.21.1 Changes Affecting Villager Rendering

### Key Rendering System Updates

**Entity Rendering Pipeline Changes**:
- Enhanced entity texture system with better caching
- Improved equipment rendering performance
- Updated model layer registration system
- New animation interpolation features

**Villager-Specific Changes**:
- Profession texture loading improvements
- Better mod compatibility for villager modifications
- Enhanced equipment slot rendering
- Updated trade interface integration

## Fabric API Requirements

### Essential Dependencies

**Required Fabric API Modules** (for 1.21.1):
```gradle
dependencies {
    modImplementation "net.fabricmc:fabric-api:0.100.8+1.21.1"

    // Specific modules for villager/entity work
    include "net.fabricmc.fabric-api:fabric-entity-events-v1:1.6.12+1.21.1"
    include "net.fabricmc.fabric-api:fabric-networking-api-v1:4.2.2+1.21.1"
    include "net.fabricmc.fabric-api:fabric-renderer-api-v1:3.4.0+1.21.1"
}
```

**Version Compatibility**:
- Minecraft: 1.21.1
- Fabric Loader: 0.16.0+
- Fabric API: 0.100.8+
- Java: 21 (required for MC 1.21.1)

## Implementation Approach 1: Profession Texture Solution (Quick Fix)

### Solving the Purple Villager Issue

**Problem Root Cause**:
The purple villager issue occurs because custom professions lack proper texture assets in the villager profession texture system.

**Solution Implementation**:

1. **Create Guard Profession Texture**:
```
src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/profession/
└── guard.png    # 64x64 villager texture with guard appearance
```

2. **Register Profession Texture**:
```java
// In XeenaaVillagerManagerClient.java
@Override
public void onInitializeClient() {
    // Register the guard profession texture
    VillagerClientRegistrar.registerProfessionTexture(
        CustomProfessions.GUARD,
        new Identifier("xeenaa_villager_manager", "textures/entity/villager/profession/guard.png")
    );
}
```

3. **Texture Creation Guidelines**:
- Base on standard villager texture layout
- Modify clothing colors to indicate guard role
- Add visual elements like belt, armor details
- Maintain villager proportions and features

### Basic Equipment Overlay System

**Equipment Rendering Implementation**:
```java
public class GuardEquipmentRenderer {
    private static final Identifier SWORD_OVERLAY =
        new Identifier("xeenaa_villager_manager", "textures/entity/equipment/sword_overlay.png");
    private static final Identifier ARMOR_OVERLAY =
        new Identifier("xeenaa_villager_manager", "textures/entity/equipment/armor_overlay.png");

    public static void renderEquipment(VillagerEntity villager, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light) {
        if (isGuardVillager(villager)) {
            ItemStack mainHand = villager.getMainHandStack();
            if (mainHand.getItem() instanceof SwordItem) {
                renderSwordOverlay(matrices, vertexConsumers, light);
            }

            if (hasArmor(villager)) {
                renderArmorOverlay(matrices, vertexConsumers, light);
            }
        }
    }
}
```

## Implementation Approach 2: Custom Guard Entity (Recommended)

### Creating a Separate Guard Entity

**Entity Registration**:
```java
// In XeenaaVillagerManager.java (main mod class)
public static final EntityType<GuardEntity> GUARD_ENTITY = Registry.register(
    Registries.ENTITY_TYPE,
    new Identifier("xeenaa_villager_manager", "guard"),
    FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GuardEntity::new)
        .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
        .build()
);
```

**Guard Entity Class**:
```java
public class GuardEntity extends PathAwareEntity implements InventoryOwner {
    private static final TrackedData<ItemStack> MAIN_HAND =
        DataTracker.registerData(GuardEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<ItemStack> ARMOR_HELMET =
        DataTracker.registerData(GuardEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    // Additional armor slots...

    private final SimpleInventory equipment = new SimpleInventory(6); // Equipment slots

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(MAIN_HAND, ItemStack.EMPTY);
        this.dataTracker.startTracking(ARMOR_HELMET, ItemStack.EMPTY);
        // Initialize other equipment slots
    }

    public ItemStack getMainHandStack() {
        return this.dataTracker.get(MAIN_HAND);
    }

    public void setMainHandStack(ItemStack stack) {
        this.dataTracker.set(MAIN_HAND, stack);
        this.equipment.setStack(0, stack);
    }

    // Equipment management methods
    public void equipArmor(EquipmentSlot slot, ItemStack armor) {
        switch (slot) {
            case HEAD -> {
                this.dataTracker.set(ARMOR_HELMET, armor);
                this.equipment.setStack(1, armor);
            }
            // Handle other armor slots
        }
    }
}
```

### Custom Model and Renderer

**Guard Model Implementation**:
```java
public class GuardModel extends HumanoidModel<GuardEntity> {
    private final ModelPart nose;
    private final ModelPart quiver;
    private final ModelPart shoulderPads;

    public GuardModel(ModelPart root) {
        super(root);
        this.nose = root.getChild("nose");
        this.quiver = root.getChild("quiver");
        this.shoulderPads = root.getChild("shoulder_pads");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = HumanoidModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData modelPartData = modelData.getRoot();

        // Add custom parts
        modelPartData.addChild("nose",
            ModelPartBuilder.create()
                .uv(24, 0)
                .cuboid(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f),
            ModelTransform.pivot(0.0f, -2.0f, 0.0f));

        modelPartData.addChild("quiver",
            ModelPartBuilder.create()
                .uv(0, 32)
                .cuboid(-2.0f, 0.0f, 2.0f, 4.0f, 12.0f, 4.0f),
            ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(GuardEntity entity, float limbAngle, float limbDistance,
                         float animationProgress, float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

        // Equipment-specific animations
        ItemStack mainHand = entity.getMainHandStack();
        if (mainHand.getItem() instanceof CrossbowItem) {
            setupCrossbowAnimation();
            this.quiver.visible = true;
        } else if (mainHand.getItem() instanceof SwordItem) {
            setupSwordAnimation();
            this.quiver.visible = false;
        }

        // Show shoulder pads if wearing chest armor
        this.shoulderPads.visible = !entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty();
    }

    private void setupCrossbowAnimation() {
        this.rightArm.pitch = -0.97079635F;
        this.leftArm.pitch = -0.97079635F;
    }

    private void setupSwordAnimation() {
        this.rightArm.pitch = -1.2F;
    }
}
```

**Guard Renderer**:
```java
public class GuardRenderer extends MobEntityRenderer<GuardEntity, GuardModel> {
    private static final Identifier TEXTURE =
        new Identifier("xeenaa_villager_manager", "textures/entity/guard.png");

    public GuardRenderer(EntityRendererFactory.Context context) {
        super(context, new GuardModel(context.getPart(ModModelLayers.GUARD)), 0.5f);

        // Add equipment layers
        this.addFeature(new GuardArmorFeatureRenderer(this, context.getModelLoader()));
        this.addFeature(new GuardHeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(GuardEntity entity) {
        return TEXTURE;
    }
}
```

### Client-Side Registration

**Model Layer Registration**:
```java
// In XeenaaVillagerManagerClient.java
@Override
public void onInitializeClient() {
    // Register model layer
    EntityModelLayerRegistry.registerModelLayer(ModModelLayers.GUARD, GuardModel::getTexturedModelData);

    // Register entity renderer
    EntityRendererRegistry.register(ModEntities.GUARD_ENTITY, GuardRenderer::new);
}

public class ModModelLayers {
    public static final EntityModelLayer GUARD = new EntityModelLayer(
        new Identifier("xeenaa_villager_manager", "guard"), "main");
}
```

## Equipment System Implementation

### Equipment GUI Integration

**Guard Equipment Screen**:
```java
public class GuardEquipmentScreen extends HandledScreen<GuardEquipmentScreenHandler> {
    private static final Identifier TEXTURE =
        new Identifier("xeenaa_villager_manager", "textures/gui/guard_equipment.png");

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Draw equipment slots
        drawEquipmentSlots(context);
        drawArmorPreview(context);
    }

    private void drawEquipmentSlots(DrawContext context) {
        // Main hand slot
        context.drawTexture(TEXTURE, this.x + 62, this.y + 76, 0, 166, 18, 18);

        // Armor slots
        for (int i = 0; i < 4; i++) {
            context.drawTexture(TEXTURE, this.x + 8, this.y + 18 + i * 18, 18, 166, 18, 18);
        }
    }
}
```

### Network Packet System

**Equipment Update Packet**:
```java
public record GuardEquipmentUpdatePacket(int entityId, int slot, ItemStack stack) {
    public static final Identifier ID =
        new Identifier("xeenaa_villager_manager", "guard_equipment_update");

    public static void handle(GuardEquipmentUpdatePacket packet, ServerPlayerEntity player) {
        Entity entity = player.getServerWorld().getEntityById(packet.entityId());
        if (entity instanceof GuardEntity guard) {
            switch (packet.slot()) {
                case 0 -> guard.setMainHandStack(packet.stack());
                case 1 -> guard.equipArmor(EquipmentSlot.HEAD, packet.stack());
                // Handle other slots...
            }
        }
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(slot);
        buf.writeItemStack(stack);
    }

    public static GuardEquipmentUpdatePacket read(PacketByteBuf buf) {
        return new GuardEquipmentUpdatePacket(
            buf.readInt(),
            buf.readInt(),
            buf.readItemStack()
        );
    }
}
```

## Performance Optimization for 1.21.1

### Rendering Optimization

**LOD System**:
```java
public class GuardLODManager {
    private static final double HIGH_DETAIL_DISTANCE = 16.0;
    private static final double MEDIUM_DETAIL_DISTANCE = 32.0;

    public static GuardRenderDetail getRenderDetail(GuardEntity guard, Entity viewer) {
        double distance = guard.squaredDistanceTo(viewer);

        if (distance < HIGH_DETAIL_DISTANCE * HIGH_DETAIL_DISTANCE) {
            return GuardRenderDetail.HIGH; // Full equipment rendering
        } else if (distance < MEDIUM_DETAIL_DISTANCE * MEDIUM_DETAIL_DISTANCE) {
            return GuardRenderDetail.MEDIUM; // Basic equipment only
        } else {
            return GuardRenderDetail.LOW; // No equipment details
        }
    }
}
```

### Memory Management

**Texture Caching**:
```java
public class GuardTextureCache {
    private static final Map<String, Identifier> CACHED_TEXTURES = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 256;

    public static Identifier getCachedTexture(GuardEntity guard) {
        String textureKey = generateTextureKey(guard);
        return CACHED_TEXTURES.computeIfAbsent(textureKey, key -> {
            if (CACHED_TEXTURES.size() >= MAX_CACHE_SIZE) {
                clearOldestEntries();
            }
            return generateTexture(guard);
        });
    }
}
```

## Testing and Validation

### Required Test Cases

1. **Purple Villager Fix Validation**:
   - Assign guard profession to villager
   - Verify proper texture loading
   - Test with different villager variants

2. **Equipment Rendering Tests**:
   - Test all armor combinations
   - Verify weapon positioning
   - Check animation smoothness

3. **Performance Testing**:
   - Large village performance
   - Memory usage monitoring
   - Render distance optimization

4. **Mod Compatibility**:
   - Test with other villager mods
   - Equipment mod integration
   - Resource pack compatibility

This implementation guide provides both immediate solutions for the purple villager issue and a comprehensive path toward a professional-grade guard villager system in Minecraft 1.21.1 Fabric.