# Java Coding Standards for Xeenaa Village Picker

## Overview

This document serves as the **definitive source** for all Java coding standards, conventions, and architectural decisions for the Xeenaa Village Picker Fabric mod. These standards are **mandatory** and must be followed by all contributors.

## Core Java Principles

### Code Quality Fundamentals
- **Clarity over Cleverness**: Write code that clearly expresses intent rather than demonstrating advanced techniques
- **Fail Fast**: Validate inputs early and throw meaningful exceptions
- **Immutability**: Prefer immutable objects and final fields wherever possible
- **Defensive Programming**: Always validate preconditions and handle edge cases
- **Single Responsibility**: Each class and method should have one clear purpose

### Java 21 Modern Features
- **Records**: Use for simple data carriers (e.g., `ProfessionData`, network packets)
- **Pattern Matching**: Utilize switch expressions and pattern matching for instanceof
- **Text Blocks**: Use for multi-line strings (JSON, SQL, etc.)
- **Sealed Classes**: Use for restricted inheritance hierarchies
- **Local Variable Type Inference**: Use `var` judiciously for obviously typed variables

### Performance and Memory Management
- **String Concatenation**: Use `StringBuilder` for loops, string templates for complex formatting
- **Collection Sizing**: Pre-size collections when expected size is known
- **Resource Management**: Always use try-with-resources for AutoCloseable objects
- **Lazy Initialization**: Initialize expensive objects only when needed
- **Cache Strategically**: Cache expensive computations but avoid memory leaks

### Fabric-Specific Principles
- **Event-Driven Architecture**: Prefer Fabric API events over direct mixins
- **Client-Server Separation**: Maintain clear boundaries between client and server code
- **Mod Compatibility**: Design for compatibility with other mods from the start
- **Performance First**: Minecraft performance is critical - profile and optimize

## Package Organization and Architecture

### Directory Structure

```
src/
├── main/java/com/xeenaa/villagermanager/
│   ├── XeenaaVillagerManager.java       # Main mod initializer (server/common)
│   ├── network/                       # Client-server communication
│   │   ├── SelectProfessionPacket.java    # C2S profession change request
│   │   └── ServerPacketHandler.java       # Server-side packet processing
│   ├── registry/                      # Data management and caching
│   │   ├── ProfessionManager.java         # Singleton profession registry
│   │   └── ProfessionData.java            # Immutable profession data record
│   ├── util/                          # Utility classes and helpers
│   │   ├── Constants.java                 # Mod-wide constants
│   │   └── ValidationUtils.java           # Input validation utilities
│   └── mixin/                         # Mixins (use sparingly)
│       └── common/                    # Common/server mixins only
├── client/java/com/xeenaa/villagermanager/
│   ├── XeenaaVillagerManagerClient.java # Client mod initializer
│   ├── client/
│   │   ├── gui/                       # Client-side GUI components
│   │   │   ├── ProfessionSelectionScreen.java  # Main GUI screen
│   │   │   └── ProfessionButton.java           # Custom button widget
│   │   └── util/                      # Client-only utilities
│   │       └── ClientInteractionHandler.java   # Right-click handling
│   └── mixin/
│       └── client/                    # Client-only mixins
└── resources/
    ├── fabric.mod.json                # Mod metadata
    ├── xeenaa_villager_manager.mixins.json      # Common mixin config
    ├── xeenaa_villager_manager.client.mixins.json  # Client mixin config
    └── assets/xeenaa_villager_manager/
        ├── lang/en_us.json           # Localization
        └── icon.png                  # Mod icon
```

### Package Responsibility Matrix

| Package | Purpose | Side | Dependencies Allowed |
|---------|---------|------|---------------------|
| `root` | Entry points and core initialization | Both | Fabric API, Minecraft common |
| `network` | Client-server communication | Both | Fabric Networking |
| `registry` | Data management and caching | Both | Minecraft registries |
| `util` | Shared utilities | Both | Minimal external deps |
| `client.gui` | User interface components | Client | Minecraft client, rendering |
| `client.util` | Client-specific utilities | Client | Client-side Minecraft APIs |
| `mixin.common` | Server/common game logic mixins | Both | Target classes only |
| `mixin.client` | Client-side rendering/input mixins | Client | Client target classes only |

## Naming Conventions and Code Style

### Java Naming Standards

| Element | Convention | Example | Requirements |
|---------|------------|---------|-------------|
| **Packages** | lowercase, dot-separated | `com.xeenaa.villagermanager.network` | No abbreviations, clear hierarchy |
| **Classes** | PascalCase, descriptive nouns | `ProfessionSelectionScreen` | Avoid abbreviations, be specific |
| **Interfaces** | PascalCase, capability-focused | `ProfessionProvider`, `Configurable` | Use -able, -er, or descriptive nouns |
| **Records** | PascalCase, data-focused | `ProfessionData`, `NetworkMessage` | Represent immutable data structures |
| **Methods** | camelCase, verb-based | `openProfessionGui()`, `validateInput()` | Start with verbs, be descriptive |
| **Fields** | camelCase | `professionManager`, `selectedVillager` | Descriptive, avoid Hungarian notation |
| **Constants** | UPPER_SNAKE_CASE | `MAX_PROFESSION_COUNT`, `MOD_ID` | Use for true constants only |
| **Enums** | PascalCase for type, UPPER_CASE for values | `GuiState.OPEN`, `Side.CLIENT` | Clear hierarchy |
| **Generic Types** | Single uppercase letter | `T`, `K`, `V` | Standard conventions: T=Type, K=Key, V=Value |
| **Mixins** | Target + "Mixin" | `VillagerEntityMixin` | Must clearly indicate target class |
| **Packets** | Purpose + "Packet" | `SelectProfessionPacket` | Clear network message purpose |

### Code Formatting Standards

```java
// REQUIRED formatting example
public final class ProfessionManager {
    private static final int MAX_CACHE_SIZE = 100;
    private static ProfessionManager instance;
    
    private final Map<Identifier, ProfessionData> professionCache = new LinkedHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ProfessionManager.class);
    
    private ProfessionManager() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of the profession manager.
     * 
     * @return the profession manager instance
     * @throws IllegalStateException if not yet initialized
     */
    public static ProfessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ProfessionManager not initialized");
        }
        return instance;
    }
    
    /**
     * Retrieves profession data for the given identifier.
     * 
     * @param professionId the profession identifier, must not be null
     * @return the profession data, or empty if not found
     */
    public Optional<ProfessionData> getProfession(Identifier professionId) {
        Objects.requireNonNull(professionId, "Profession ID must not be null");
        
        return Optional.ofNullable(professionCache.get(professionId));
    }
}
```

#### Formatting Rules
- **Indentation**: 4 spaces (NO TABS EVER)
- **Line Length**: Maximum 120 characters
- **Braces**: K&R style (opening brace on same line)
- **Blank Lines**: One blank line between methods, two between classes
- **Import Organization**: 
  1. Java standard library
  2. Third-party libraries
  3. Minecraft/Fabric APIs
  4. Internal mod packages
- **Field Declaration**: One field per line, group by visibility

## Mixin Standards and Restrictions

### When to Use Mixins

**PREFER Fabric API Events** - Only use mixins when no suitable event exists:

```java
// PREFERRED: Use Fabric API events
UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
    if (entity instanceof VillagerEntity villager && hand == Hand.MAIN_HAND) {
        // Handle interaction
        return ActionResult.SUCCESS;
    }
    return ActionResult.PASS;
});

// AVOID: Direct mixins unless absolutely necessary
@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    // Only if no suitable event exists
}
```

### Mixin Architecture Rules

1. **One Mixin Per Target Class**: Never mix multiple target classes in one mixin
2. **Minimal Injection Points**: Each injection point adds runtime overhead
3. **Client/Server Separation**: Use appropriate mixin configuration files
4. **Descriptive Naming**: `TargetClassName` + `Mixin` suffix

### Mixin Implementation Standards

```java
@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    // 1. Use @Shadow for accessing private fields (preferred over reflection)
    @Shadow private VillagerData villagerData;
    
    // 2. Injection with proper error handling
    @Inject(
        method = "interactMob",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onInteractMob(
        PlayerEntity player, 
        Hand hand, 
        CallbackInfoReturnable<ActionResult> cir
    ) {
        try {
            if (shouldHandleInteraction(player, hand)) {
                handleProfessionInteraction(player);
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        } catch (Exception e) {
            LOGGER.error("Error in villager interaction mixin", e);
            // Don't cancel on error - let vanilla handle it
        }
    }
    
    // 3. Private helper methods for logic
    private boolean shouldHandleInteraction(PlayerEntity player, Hand hand) {
        return player.getWorld().isClient 
            && hand == Hand.MAIN_HAND 
            && !((VillagerEntity)(Object)this).isBaby();
    }
    
    // 4. Delegate complex logic to utility classes
    private void handleProfessionInteraction(PlayerEntity player) {
        ClientInteractionHandler.openProfessionGui(
            player, 
            (VillagerEntity)(Object)this
        );
    }
}
```

### Mixin Performance Requirements

- **Return Early**: Always check conditions before expensive operations
- **No Reflection**: Use `@Shadow` or `@Accessor` instead
- **Minimal Locals**: Avoid `LocalCapture` unless absolutely necessary
- **Exception Safety**: Never let mixin exceptions crash the game

## Network Programming Standards

### Packet Design Principles

```java
// REQUIRED: Use records for immutable packet data
public record SelectProfessionPacket(
    int villagerEntityId,
    Identifier professionId
) implements CustomPayload {
    
    public static final CustomPayload.Id<SelectProfessionPacket> ID = 
        new CustomPayload.Id<>(Identifier.of(XeenaaVillagerManager.MOD_ID, "select_profession"));
    
    // REQUIRED: PacketCodec for Minecraft 1.21.1 serialization
    public static final PacketCodec<RegistryByteBuf, SelectProfessionPacket> CODEC = 
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, SelectProfessionPacket::villagerEntityId,
            Identifier.PACKET_CODEC, SelectProfessionPacket::professionId,
            SelectProfessionPacket::new
        );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    // REQUIRED: Validation method
    public boolean isValid() {
        return villagerEntityId > 0 && professionId != null;
    }
}
```

### Server-Side Packet Handling

```java
public class ServerPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketHandler.class);
    private static final double MAX_INTERACTION_DISTANCE = 8.0;
    
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
            SelectProfessionPacket.ID, 
            ServerPacketHandler::handleSelectProfession
        );
    }
    
    private static void handleSelectProfession(
        SelectProfessionPacket packet, 
        ServerPlayNetworking.Context context
    ) {
        // REQUIRED: Validate packet data
        if (!packet.isValid()) {
            LOGGER.warn("Invalid profession selection packet from {}", 
                context.player().getName().getString());
            return;
        }
        
        // REQUIRED: Execute on server thread
        context.server().execute(() -> {
            processOnServerThread(packet, context);
        });
    }
    
    private static void processOnServerThread(
        SelectProfessionPacket packet, 
        ServerPlayNetworking.Context context
    ) {
        try {
            ServerPlayerEntity player = context.player();
            
            // REQUIRED: Security validation
            if (!validatePlayerPermissions(player, packet)) {
                return;
            }
            
            // REQUIRED: Entity validation
            Entity entity = player.getServerWorld().getEntityById(packet.villagerEntityId());
            if (!(entity instanceof VillagerEntity villager)) {
                LOGGER.debug("Villager entity not found or invalid type");
                return;
            }
            
            // REQUIRED: Distance check for security
            if (player.squaredDistanceTo(villager) > MAX_INTERACTION_DISTANCE * MAX_INTERACTION_DISTANCE) {
                LOGGER.warn("Player {} attempted profession change from too far away", 
                    player.getName().getString());
                return;
            }
            
            // Process the profession change
            changeProfession(villager, packet.professionId());
            
        } catch (Exception e) {
            LOGGER.error("Error processing profession selection", e);
        }
    }
}
```

## GUI Development Standards

### Screen Architecture

```java
public class ProfessionSelectionScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfessionSelectionScreen.class);
    
    // REQUIRED: Constants for layout
    private static final int BACKGROUND_WIDTH = 380;
    private static final int BACKGROUND_HEIGHT = 240;
    private static final int BUTTON_WIDTH = 115;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_SPACING = 5;
    private static final int COLUMNS = 3;
    
    // REQUIRED: Final fields for immutable state
    private final VillagerEntity targetVillager;
    private final List<ProfessionData> professions;
    
    // REQUIRED: Non-null assertion in constructor
    public ProfessionSelectionScreen(VillagerEntity villager) {
        super(Text.translatable("gui.xeenaa_villager_manager.select_profession"));
        this.targetVillager = Objects.requireNonNull(villager, "Villager cannot be null");
        this.professions = ProfessionManager.getInstance().getAllProfessions();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // REQUIRED: Calculate centered positions
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - (BACKGROUND_WIDTH / 2);
        int startY = centerY - (BACKGROUND_HEIGHT / 2);
        
        addProfessionButtons(startX, startY);
        addCloseButton(startX, startY);
    }
    
    // REQUIRED: Proper rendering order
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Render background first
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // 2. Render custom GUI elements
        renderCustomBackground(context);
        
        // 3. Render widgets
        super.render(context, mouseX, mouseY, delta);
        
        // 4. Render title and tooltips last
        renderTitle(context);
    }
    
    // REQUIRED: Resource cleanup
    @Override
    public void close() {
        super.close();
        // Clean up any resources if needed
    }
    
    // REQUIRED: Proper game pause handling
    @Override
    public boolean shouldPause() {
        return false; // Keep game running for multiplayer compatibility
    }
}
```

### Widget Implementation Standards

```java
public class ProfessionButton extends ButtonWidget {
    private final ProfessionData profession;
    private final ItemStack iconStack;
    
    public ProfessionButton(
        int x, int y, int width, int height,
        ProfessionData profession,
        PressAction onPress
    ) {
        // REQUIRED: Pass empty text to avoid duplicate rendering
        super(x, y, width, height, Text.empty(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        
        this.profession = Objects.requireNonNull(profession, "Profession cannot be null");
        this.iconStack = createIconStack(profession);
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // REQUIRED: Call super for button background
        super.renderWidget(context, mouseX, mouseY, delta);
        
        // REQUIRED: Render icon with proper positioning
        int iconX = this.getX() + 4;
        int iconY = this.getY() + (this.getHeight() - 16) / 2;
        context.drawItem(iconStack, iconX, iconY);
        
        // REQUIRED: Render text with truncation
        int textX = iconX + 20;
        int textY = this.getY() + (this.getHeight() - 8) / 2;
        int maxTextWidth = this.getWidth() - 28;
        
        String professionName = profession.getDisplayName();
        Text truncatedText = Text.literal(truncateText(professionName, maxTextWidth));
        
        context.drawText(
            MinecraftClient.getInstance().textRenderer,
            truncatedText,
            textX, textY,
            0xFFFFFF, // White text
            true // Drop shadow
        );
    }
    
    // REQUIRED: Text truncation for long profession names
    private String truncateText(String text, int maxWidth) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        
        return textRenderer.trimToWidth(text, maxWidth - textRenderer.getWidth("...")) + "...";
    }
}
```

## Performance and Memory Management

### Caching Strategy

```java
public class ProfessionManager {
    private static final int MAX_CACHE_SIZE = 256;
    private static final long CACHE_REFRESH_INTERVAL = 300_000; // 5 minutes
    
    // REQUIRED: Use LinkedHashMap for LRU behavior
    private final Map<Identifier, ProfessionData> professionCache = 
        new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Identifier, ProfessionData> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    
    private volatile long lastCacheRefresh = 0;
    
    // REQUIRED: Lazy initialization with double-checked locking
    public List<ProfessionData> getAllProfessions() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheRefresh > CACHE_REFRESH_INTERVAL || professionCache.isEmpty()) {
            synchronized (this) {
                if (currentTime - lastCacheRefresh > CACHE_REFRESH_INTERVAL || professionCache.isEmpty()) {
                    refreshCache();
                    lastCacheRefresh = currentTime;
                }
            }
        }
        
        return new ArrayList<>(professionCache.values());
    }
    
    // REQUIRED: Efficient cache refresh
    private void refreshCache() {
        professionCache.clear();
        
        // Use registry iteration for minimal allocations
        for (Map.Entry<RegistryKey<VillagerProfession>, VillagerProfession> entry : 
             Registries.VILLAGER_PROFESSION.getEntrySet()) {
            
            Identifier id = entry.getKey().getValue();
            VillagerProfession profession = entry.getValue();
            
            professionCache.put(id, new ProfessionData(
                id,
                profession,
                getWorkstationBlock(profession),
                createIconStack(profession)
            ));
        }
    }
}
```

### Memory-Efficient Patterns

```java
// REQUIRED: Use object pooling for frequently created objects
public class PacketPool {
    private static final Queue<SelectProfessionPacket.Builder> PACKET_BUILDERS = 
        new ConcurrentLinkedQueue<>();
    
    public static SelectProfessionPacket.Builder getBuilder() {
        SelectProfessionPacket.Builder builder = PACKET_BUILDERS.poll();
        return builder != null ? builder.reset() : new SelectProfessionPacket.Builder();
    }
    
    public static void returnBuilder(SelectProfessionPacket.Builder builder) {
        if (PACKET_BUILDERS.size() < 10) { // Limit pool size
            PACKET_BUILDERS.offer(builder);
        }
    }
}

// REQUIRED: Minimize allocations in render methods
public void renderProfessionList(DrawContext context, int mouseX, int mouseY) {
    // Pre-calculate positions to avoid repeated calculations
    int startX = (width - BACKGROUND_WIDTH) / 2;
    int startY = (height - BACKGROUND_HEIGHT) / 2;
    
    // Use enhanced for-loop to avoid iterator allocation
    int index = 0;
    for (ProfessionData profession : professions) {
        if (index >= MAX_VISIBLE_PROFESSIONS) break;
        
        int x = startX + (index % COLUMNS) * (BUTTON_WIDTH + BUTTON_SPACING);
        int y = startY + (index / COLUMNS) * (BUTTON_HEIGHT + BUTTON_SPACING);
        
        renderProfessionButton(context, profession, x, y, mouseX, mouseY);
        index++;
    }
}
```

### Thread Safety Requirements

```java
// REQUIRED: Thread-safe singleton pattern
public class ProfessionManager {
    private static volatile ProfessionManager instance;
    private static final Object LOCK = new Object();
    
    public static ProfessionManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ProfessionManager();
                }
            }
        }
        return instance;
    }
    
    // REQUIRED: Concurrent collections for thread safety
    private final ConcurrentHashMap<Identifier, ProfessionData> threadSafeCache = 
        new ConcurrentHashMap<>();
    
    // REQUIRED: Immutable data structures where possible
    public List<ProfessionData> getAllProfessions() {
        return List.copyOf(threadSafeCache.values());
    }
}
```

## Testing and Quality Assurance

### Unit Testing Requirements

```java
// REQUIRED: Test class naming convention
class ProfessionManagerTest {
    private ProfessionManager professionManager;
    
    @BeforeEach
    void setUp() {
        professionManager = ProfessionManager.getInstance();
    }
    
    // REQUIRED: Test method naming - should_ExpectedBehavior_When_StateUnderTest
    @Test
    void should_ReturnAllProfessions_When_CacheIsPopulated() {
        // Given
        professionManager.refreshCache();
        
        // When
        List<ProfessionData> professions = professionManager.getAllProfessions();
        
        // Then
        assertThat(professions)
            .isNotEmpty()
            .allSatisfy(profession -> {
                assertThat(profession.getId()).isNotNull();
                assertThat(profession.getDisplayName()).isNotBlank();
            });
    }
    
    @Test
    void should_HandleNullInput_When_ValidatingProfessionId() {
        // When/Then
        assertThatThrownBy(() -> professionManager.getProfession(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Profession ID must not be null");
    }
    
    // REQUIRED: Performance testing for critical paths
    @Test
    void should_CompleteWithinTimeLimit_When_LoadingManyProfessions() {
        // Given
        long startTime = System.nanoTime();
        
        // When
        for (int i = 0; i < 1000; i++) {
            professionManager.getAllProfessions();
        }
        
        // Then
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        assertThat(elapsedMs).isLessThan(100); // Should complete within 100ms
    }
}
```

### Integration Testing Standards

```java
// REQUIRED: Test with real Minecraft environment
@ExtendWith(MinecraftExtension.class)
class VillagerInteractionIntegrationTest {
    
    @Test
    void should_OpenProfessionGui_When_RightClickingVillager() {
        // Given
        MinecraftServer server = getTestServer();
        ServerWorld world = server.getOverworld();
        VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
        
        // When
        ActionResult result = simulateRightClick(villager);
        
        // Then
        assertThat(result).isEqualTo(ActionResult.SUCCESS);
        // Verify GUI opens (client-side test)
    }
}
```

### Compatibility Testing Matrix

| Mod Category | Required Test Mods | Test Scenarios | Expected Behavior |
|--------------|-------------------|----------------|------------------|
| **Performance** | Sodium, Lithium, Phosphor | GUI rendering, villager interaction | No conflicts, maintain 60+ FPS |
| **Villager Mods** | Easy Villagers, VillagerConfig | Profession changing, trade modification | Graceful coexistence |
| **World Generation** | Biomes O' Plenty, Terralith | Custom biomes with villagers | Detect modded professions |
| **Server Management** | Fabric Proxy Lite, Carpet | Multiplayer synchronization | Proper packet handling |

### Performance Benchmarking

```java
// REQUIRED: Benchmark critical operations
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ProfessionManagerBenchmark {
    
    @Benchmark
    public List<ProfessionData> benchmarkGetAllProfessions() {
        return ProfessionManager.getInstance().getAllProfessions();
    }
    
    @Benchmark
    public Optional<ProfessionData> benchmarkGetSpecificProfession() {
        return ProfessionManager.getInstance().getProfession(
            Identifier.of("minecraft", "farmer")
        );
    }
}
```

## Documentation Standards

### Javadoc Requirements

```java
/**
 * Manages profession data and provides caching for villager profession selection.
 * <p>
 * This singleton class maintains a cache of all available villager professions,
 * including both vanilla and modded professions. The cache is automatically
 * refreshed periodically to detect newly registered professions.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> This class is thread-safe and can be safely
 * accessed from multiple threads simultaneously.
 * </p>
 * 
 * @since 1.0.0
 * @author Xeenaa
 */
public class ProfessionManager {
    
    /**
     * Gets all available villager professions.
     * <p>
     * This method returns a cached list of professions. The cache is automatically
     * refreshed if it's older than {@value #CACHE_REFRESH_INTERVAL} milliseconds
     * or if it's empty.
     * </p>
     * 
     * @return an immutable list of all profession data, never null
     * @throws IllegalStateException if the profession registry is not accessible
     * @see #refreshCache() for manual cache refresh
     * @since 1.0.0
     */
    public List<ProfessionData> getAllProfessions() {
        // Implementation
    }
    
    /**
     * Retrieves profession data for a specific profession identifier.
     * 
     * @param professionId the profession identifier to look up, must not be null
     * @return an Optional containing the profession data if found, empty otherwise
     * @throws IllegalArgumentException if professionId is null
     * @see Identifier for valid identifier format
     * @since 1.0.0
     */
    public Optional<ProfessionData> getProfession(Identifier professionId) {
        // Implementation
    }
}
```

### Code Comments Standards

```java
public class ServerPacketHandler {
    
    private static void handleSelectProfession(SelectProfessionPacket packet, Context context) {
        // Validate packet data early to prevent processing invalid requests
        if (!packet.isValid()) {
            LOGGER.warn("Received invalid profession selection packet from {}", 
                context.player().getName().getString());
            return;
        }
        
        // Execute on server thread to ensure thread safety with world modifications
        // This is critical because villager profession changes modify world state
        context.server().execute(() -> {
            try {
                processOnServerThread(packet, context);
            } catch (Exception e) {
                // Log errors but don't crash the server - fail gracefully
                LOGGER.error("Failed to process profession selection", e);
            }
        });
    }
    
    private static void validateDistance(ServerPlayerEntity player, VillagerEntity villager) {
        double distance = player.squaredDistanceTo(villager);
        
        // Security check: Prevent players from changing professions of distant villagers
        // Using squared distance for performance (avoids expensive sqrt calculation)
        if (distance > MAX_INTERACTION_DISTANCE * MAX_INTERACTION_DISTANCE) {
            throw new IllegalStateException(
                String.format("Player %s attempted profession change from distance %.2f blocks",
                    player.getName().getString(), Math.sqrt(distance))
            );
        }
    }
}
```

### Architecture Decision Records (ADRs)

```markdown
# ADR-001: Use Fabric API Events Over Mixins for Entity Interaction

## Status
Accepted

## Context
We need to handle right-click interactions with villagers. Two approaches are possible:
1. Direct mixins into VillagerEntity.interactMob()
2. Fabric API UseEntityCallback event

## Decision
Use Fabric API UseEntityCallback.EVENT for villager interaction handling.

## Consequences
### Positive
- Better compatibility with other mods
- More resilient to Minecraft version changes
- Follows Fabric best practices
- Easier to test and debug

### Negative
- Slightly less control over interaction timing
- Must check entity type in event handler

## Implementation
```java
UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
    if (entity instanceof VillagerEntity villager && hand == Hand.MAIN_HAND) {
        return ClientInteractionHandler.handleVillagerInteraction(player, villager);
    }
    return ActionResult.PASS;
});
```
```

## Error Handling and Logging

### Exception Handling Standards

```java
public class ProfessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfessionManager.class);
    
    public Optional<ProfessionData> getProfession(Identifier professionId) {
        // REQUIRED: Validate inputs early
        Objects.requireNonNull(professionId, "Profession ID must not be null");
        
        try {
            return Optional.ofNullable(professionCache.get(professionId));
            
        } catch (Exception e) {
            // REQUIRED: Log with context, don't expose internal errors to users
            LOGGER.error("Failed to retrieve profession data for ID: {}", professionId, e);
            
            // REQUIRED: Return safe default instead of throwing
            return Optional.empty();
        }
    }
    
    // REQUIRED: Specific exception types for different failure modes
    public void changeProfession(VillagerEntity villager, Identifier professionId) 
            throws ProfessionNotFoundException, InvalidVillagerException {
        
        if (villager.isBaby()) {
            throw new InvalidVillagerException("Cannot change profession of baby villager");
        }
        
        Optional<VillagerProfession> profession = getProfessionFromRegistry(professionId);
        if (profession.isEmpty()) {
            throw new ProfessionNotFoundException(
                String.format("Profession not found: %s", professionId)
            );
        }
        
        // Implementation...
    }
}

// REQUIRED: Custom exception hierarchy
public class ProfessionException extends Exception {
    protected ProfessionException(String message) {
        super(message);
    }
    
    protected ProfessionException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class ProfessionNotFoundException extends ProfessionException {
    public ProfessionNotFoundException(String message) {
        super(message);
    }
}

public class InvalidVillagerException extends ProfessionException {
    public InvalidVillagerException(String message) {
        super(message);
    }
}
```

### Logging Standards

```java
public class ServerPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketHandler.class);
    
    // REQUIRED: Structured logging with proper levels
    private static void handleProfessionChange(SelectProfessionPacket packet, Context context) {
        ServerPlayerEntity player = context.player();
        
        // DEBUG: Detailed information for debugging
        LOGGER.debug("Processing profession change request: villager={}, profession={}, player={}",
            packet.villagerEntityId(), packet.professionId(), player.getName().getString());
        
        try {
            validateRequest(packet, player);
            
            // INFO: Significant business events
            LOGGER.info("Player {} changed villager {} profession to {}",
                player.getName().getString(), 
                packet.villagerEntityId(), 
                packet.professionId());
                
        } catch (SecurityException e) {
            // WARN: Security violations and recoverable errors
            LOGGER.warn("Security violation in profession change: player={}, error={}",
                player.getName().getString(), e.getMessage());
                
        } catch (Exception e) {
            // ERROR: Unexpected errors that prevent operation
            LOGGER.error("Failed to process profession change for player {}",
                player.getName().getString(), e);
        }
    }
}
```

## Version Control and Release Management

### Git Workflow Standards

```bash
# REQUIRED: Branch naming convention
main                    # Stable releases only
develop                 # Active development
feature/gui-redesign    # Feature branches
hotfix/crash-fix        # Critical bug fixes
release/v1.2.0         # Release preparation
```

### Commit Message Standards

```
# REQUIRED: Conventional commit format
<type>(<scope>): <description>

[optional body]

[optional footer(s)]

# Examples:
feat(gui): add profession search functionality

fix(network): resolve packet deserialization error
  
Closes #123

docs(standards): update coding standards for error handling

perf(cache): optimize profession lookup with LRU cache

Benchmarks show 40% improvement in lookup time.

breaking: remove deprecated ProfessionHelper.getProfessions() method

REASON: Method was replaced by ProfessionManager.getAllProfessions()
MIGRATION: Use ProfessionManager.getInstance().getAllProfessions()
```

### Release Process

1. **Version Bumping**: Follow semantic versioning (MAJOR.MINOR.PATCH)
2. **Changelog Generation**: Automatic from conventional commits
3. **Testing**: Full compatibility test suite before release
4. **Tagging**: Git tags match version numbers exactly

### Code Review Requirements

- [ ] All public APIs have comprehensive Javadoc
- [ ] Unit tests cover new functionality
- [ ] Performance impact assessed
- [ ] Compatibility tested with major mods
- [ ] Security implications reviewed
- [ ] Error handling follows standards
- [ ] Logging is appropriate and structured

## Mod Compatibility and Integration

### Compatibility Testing Matrix

| Category | Required Mods | Test Scenarios | Expected Behavior |
|----------|---------------|----------------|------------------|
| **Performance** | Sodium, Lithium, Phosphor | GUI rendering, villager spawning | No conflicts, maintain 60+ FPS |
| **Villager Enhancement** | Easy Villagers, VillagerConfig | Profession changing, trade modification | Graceful coexistence |
| **World Generation** | Biomes O' Plenty, Terralith | Custom biomes with villagers | Detect modded professions |
| **Server Management** | Fabric Proxy Lite, Carpet | Multiplayer synchronization | Proper packet handling |

### Modded Profession Support

```java
public class ModCompatibilityHandler {
    private static final Set<String> KNOWN_PROFESSION_MODS = Set.of(
        "guardvillagers",
        "mca",
        "villagersrespawn",
        "easiervillagertrading"
    );
    
    // REQUIRED: Automatic modded profession detection
    public static List<ProfessionData> detectModdedProfessions() {
        return Registries.VILLAGER_PROFESSION.getEntrySet()
            .stream()
            .filter(entry -> !"minecraft".equals(entry.getKey().getValue().getNamespace()))
            .map(entry -> createProfessionData(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    // REQUIRED: Graceful handling of unknown professions
    public static boolean isValidProfession(Identifier professionId) {
        try {
            return Registries.VILLAGER_PROFESSION.containsId(professionId);
        } catch (Exception e) {
            LOGGER.debug("Failed to validate profession ID: {}", professionId, e);
            return false;
        }
    }
}
```

### API Compatibility Guarantees

```java
/**
 * Public API for external mod integration.
 * 
 * <p><strong>Compatibility Promise:</strong></p>
 * <ul>
 *   <li>Methods marked with {@code @ApiStable} will not change in patch versions</li>
 *   <li>Deprecated methods will be supported for at least one major version</li>
 *   <li>New methods may be added in minor versions</li>
 * </ul>
 */
@ApiVersion("1.0")
public interface ProfessionPickerAPI {
    
    /**
     * Registers a custom profession change listener.
     * 
     * @param listener the listener to register
     * @since 1.0.0
     * @apiNote This method is stable and will not change
     */
    @ApiStable
    void registerProfessionChangeListener(ProfessionChangeListener listener);
    
    /**
     * Gets all available professions visible to the profession picker.
     * 
     * @return immutable list of profession data
     * @since 1.0.0
     */
    @ApiStable
    List<ProfessionData> getAvailableProfessions();
}
```

## Security and Validation

### Input Validation Framework

```java
public class ValidationUtils {
    
    // REQUIRED: Comprehensive input validation
    public static void validateProfessionChangeRequest(
        ServerPlayerEntity player,
        VillagerEntity villager,
        Identifier professionId
    ) throws ValidationException {
        
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(villager, "Villager cannot be null");
        Objects.requireNonNull(professionId, "Profession ID cannot be null");
        
        // Distance validation
        if (player.squaredDistanceTo(villager) > MAX_INTERACTION_DISTANCE_SQUARED) {
            throw new ValidationException("Player too far from villager");
        }
        
        // Age validation
        if (villager.isBaby()) {
            throw new ValidationException("Cannot change profession of baby villager");
        }
        
        // Profession existence validation
        if (!Registries.VILLAGER_PROFESSION.containsId(professionId)) {
            throw new ValidationException("Invalid profession ID: " + professionId);
        }
        
        // World state validation
        if (villager.isRemoved() || !villager.isAlive()) {
            throw new ValidationException("Villager is no longer valid");
        }
    }
    
    // REQUIRED: Rate limiting for security
    public static boolean checkRateLimit(UUID playerId) {
        return RateLimitManager.getInstance().checkLimit(playerId, 5, 60); // 5 per minute
    }
}
```

## Configuration Management

### Configuration Schema

```java
@ConfigClass
public class ModConfig {
    
    @ConfigEntry(category = "interaction")
    @Range(min = 1.0, max = 16.0)
    public double maxInteractionDistance = 8.0;
    
    @ConfigEntry(category = "gui")
    public boolean showProfessionIcons = true;
    
    @ConfigEntry(category = "gui")
    @Range(min = 1, max = 5)
    public int guiColumns = 3;
    
    @ConfigEntry(category = "performance")
    @Range(min = 50, max = 1000)
    public int cacheRefreshIntervalSeconds = 300;
    
    @ConfigEntry(category = "compatibility")
    public Set<String> disabledProfessions = new HashSet<>();
    
    // REQUIRED: Validation method
    public void validate() throws ConfigValidationException {
        if (maxInteractionDistance <= 0) {
            throw new ConfigValidationException("Max interaction distance must be positive");
        }
        
        if (guiColumns < 1 || guiColumns > 5) {
            throw new ConfigValidationException("GUI columns must be between 1 and 5");
        }
    }
}
```

---

## Standards Compliance Checklist

Before submitting any code, verify compliance with these standards:

### Code Quality
- [ ] All public methods have comprehensive Javadoc
- [ ] Input validation for all public methods
- [ ] Proper exception handling with specific exception types
- [ ] Thread safety considerations documented
- [ ] Performance impact assessed and optimized

### Architecture
- [ ] Follows single responsibility principle
- [ ] Uses appropriate design patterns
- [ ] Minimal coupling between components
- [ ] Clear separation of client and server code
- [ ] Proper use of Fabric API events over mixins

### Testing
- [ ] Unit tests for all utility classes
- [ ] Integration tests for mixin functionality
- [ ] Performance benchmarks for critical paths
- [ ] Compatibility testing with major mods

### Documentation
- [ ] Architecture decisions recorded
- [ ] API changes documented
- [ ] Breaking changes clearly marked
- [ ] Examples provided for complex usage

## Guard Ranking System Standards

### Rank Data Structure Standards

```java
// REQUIRED: Immutable rank definitions using enums
public enum GuardRank implements StringIdentifiable {
    RECRUIT("recruit", "Recruit", 0, 2.0f, 20.0f),
    GUARD_MELEE("guard_melee", "Guard", 15, 4.0f, 25.0f);

    // REQUIRED: PacketCodec for network serialization
    public static final PacketCodec<RegistryByteBuf, GuardRank> CODEC =
        new PacketCodec<RegistryByteBuf, GuardRank>() {
            // Implementation
        };
}

// REQUIRED: Per-guard progression tracking
public class GuardRankData {
    private final UUID villagerId;
    private GuardRank currentRank;
    private int totalEmeraldsSpent;

    // REQUIRED: Validation methods for progression
    public boolean canPurchaseRank(GuardRank targetRank) {
        return targetRank.canPurchase(currentRank);
    }
}
```

### Emerald Economy Standards

```java
// REQUIRED: Server-side currency validation
public class EmeraldValidator {
    public static boolean validateCurrency(ServerPlayerEntity player, int cost) {
        int emeralds = player.getInventory().count(Items.EMERALD);
        return emeralds >= cost;
    }

    // REQUIRED: Atomic emerald deduction
    public static boolean deductEmeralds(ServerPlayerEntity player, int amount) {
        return player.getInventory().removeStack(Items.EMERALD, amount).getCount() == amount;
    }
}
```

### Network Protocol Standards

```java
// REQUIRED: Rank purchase packet structure
public record PurchaseRankPacket(UUID villagerId, GuardRank targetRank) implements CustomPayload {
    // REQUIRED: Validation method
    public boolean isValid() {
        return villagerId != null && targetRank != null;
    }
}

// REQUIRED: Server-side purchase validation
private static void handlePurchaseRank(PurchaseRankPacket packet, Context context) {
    // 1. Validate packet data
    if (!packet.isValid()) return;

    // 2. Execute on server thread
    context.server().execute(() -> {
        // 3. Validate player permissions and currency
        // 4. Process rank upgrade
        // 5. Sync to clients
    });
}
```

### GUI Standards for Ranking System

```java
// REQUIRED: Rank screen architecture
public class GuardRankScreen extends Screen {
    private final VillagerEntity targetVillager;
    private final GuardRankData rankData;

    // REQUIRED: Progress visualization
    private void renderRankProgress(DrawContext context) {
        int currentTier = rankData.getCurrentTier();
        int maxTier = 4;
        float progress = (float) currentTier / maxTier;

        // Render progress bar with clear visual feedback
        renderProgressBar(context, progress, 0xFF00FF00);
    }

    // REQUIRED: Purchase confirmation
    private void showPurchaseConfirmation(GuardRank targetRank) {
        Text message = Text.literal("Purchase " + targetRank.getDisplayName() +
                                  " for " + targetRank.getEmeraldCost() + " emeralds?");
        // Show confirmation dialog
    }
}
```

---

**Last Updated**: 2025-09-23 - Added Guard Ranking System standards
**Maintainer**: minecraft-java-engineer
**Version**: 2.1.0 - Enhanced with ranking system patterns

*These standards are mandatory and must be followed by all contributors. Any deviations must be approved by the minecraft-java-engineer and documented as architectural decisions.*