package com.xeenaa.villagermanager.block;

import com.google.common.collect.ImmutableSet;
import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.item.Item;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * Registry for custom blocks added by this mod.
 * <p>
 * This class manages the registration of custom blocks, their associated items,
 * and Point of Interest (POI) types for villager workstations. All blocks are
 * registered during mod initialization following Minecraft 1.21.1 and Fabric API conventions.
 * </p>
 * <p>
 * The class handles both the block registration and the creation of POI types
 * that villagers use to identify workstations for their professions.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> This class is designed to be called only during mod
 * initialization and is not thread-safe for concurrent registration.
 * </p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager
 */
public final class ModBlocks {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModBlocks.class);

    // Block constants following naming conventions
    public static final String GUARD_POST_BLOCK_ID = "guard_post";
    public static final String GUARD_POST_POI_ID = "guard_post_poi";

    // Registered block instances
    public static Block GUARD_POST;
    public static Item GUARD_POST_ITEM;
    public static PointOfInterestType GUARD_POST_POI;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ModBlocks() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Registers all custom blocks with the Minecraft registry.
     * <p>
     * This method must be called during mod initialization before any world loading
     * occurs. It registers blocks, their associated items, and POI types.
     * The registration order is important: blocks first, then POI types, then items.
     * </p>
     *
     * @throws IllegalStateException if registration fails for any block
     * @since 1.0.0
     */
    public static void registerBlocks() {
        LOGGER.info("Registering custom blocks and POI types");

        try {
            // Register blocks first
            registerBlocksOnly();

            // Register POI types (requires blocks to be registered first)
            registerPointOfInterestTypes();

            // Register block items last
            registerBlockItems();

            LOGGER.info("Successfully registered {} custom block(s) with POI types", 1);

        } catch (Exception e) {
            LOGGER.error("Failed to register custom blocks", e);
            throw new IllegalStateException("Block registration failed", e);
        }
    }

    /**
     * Registers the block instances without items or POI types.
     * <p>
     * This is the first step of block registration and must be completed
     * before POI types can be created.
     * </p>
     *
     * @throws IllegalStateException if block registration fails
     * @since 1.0.0
     */
    private static void registerBlocksOnly() {
        LOGGER.debug("Registering block instances");

        // Register Guard Post block
        GUARD_POST = registerBlock(
            GUARD_POST_BLOCK_ID,
            new GuardPostBlock(GuardPostBlock.getDefaultSettings())
        );

        LOGGER.info("Block instances registered successfully");
    }

    /**
     * Registers Point of Interest types for villager workstations.
     * <p>
     * POI types are required for villagers to recognize blocks as workstations.
     * This method must be called after blocks are registered but before
     * professions are registered.
     * </p>
     *
     * @throws IllegalStateException if POI registration fails
     * @since 1.0.0
     */
    private static void registerPointOfInterestTypes() {
        LOGGER.debug("Registering Point of Interest types");

        // Register Guard Post POI
        GUARD_POST_POI = registerPointOfInterestType(
            GUARD_POST_POI_ID,
            GUARD_POST
        );

        LOGGER.info("POI types registered successfully");
    }

    /**
     * Registers block items for the custom blocks.
     * <p>
     * Block items allow players to carry and place the blocks in the world.
     * This is the final step of block registration.
     * </p>
     *
     * @throws IllegalStateException if item registration fails
     * @since 1.0.0
     */
    private static void registerBlockItems() {
        LOGGER.debug("Registering block items");

        // Register Guard Post item
        GUARD_POST_ITEM = registerBlockItem(
            GUARD_POST_BLOCK_ID,
            GUARD_POST
        );

        LOGGER.info("Block items registered successfully");
    }

    /**
     * Registers a single block with the Minecraft registry.
     * <p>
     * This helper method handles the boilerplate of block registration,
     * including proper identifier creation and registry validation.
     * </p>
     *
     * @param blockId the block identifier (without namespace)
     * @param block the block instance to register
     * @return the registered Block instance
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException if registration fails
     * @since 1.0.0
     */
    private static Block registerBlock(String blockId, Block block) {
        // Validate input parameters
        Objects.requireNonNull(blockId, "Block ID must not be null");
        Objects.requireNonNull(block, "Block instance must not be null");

        if (blockId.trim().isEmpty()) {
            throw new IllegalArgumentException("Block ID must not be empty");
        }

        // Create block identifier
        Identifier identifier = Identifier.of(XeenaaVillagerManager.MOD_ID, blockId);

        LOGGER.debug("Registering block: {}", identifier);

        // Register with Minecraft registry
        Block registeredBlock = Registry.register(
            Registries.BLOCK,
            identifier,
            block
        );

        // Verify registration succeeded
        if (registeredBlock != block) {
            throw new IllegalStateException(
                String.format("Failed to register block: %s", identifier)
            );
        }

        LOGGER.info("Successfully registered block: {}", identifier);
        return registeredBlock;
    }

    /**
     * Registers a Point of Interest type for a workstation block.
     * <p>
     * POI types are used by villagers to locate and pathfind to workstations.
     * The POI type includes all possible block states for the given block.
     * </p>
     *
     * @param poiId the POI identifier (without namespace)
     * @param workstationBlock the block that serves as the workstation
     * @return the registered PointOfInterestType instance
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException if registration fails
     * @since 1.0.0
     */
    private static PointOfInterestType registerPointOfInterestType(
        String poiId,
        Block workstationBlock
    ) {
        // Validate input parameters
        Objects.requireNonNull(poiId, "POI ID must not be null");
        Objects.requireNonNull(workstationBlock, "Workstation block must not be null");

        if (poiId.trim().isEmpty()) {
            throw new IllegalArgumentException("POI ID must not be empty");
        }

        // Create POI identifier
        Identifier identifier = Identifier.of(XeenaaVillagerManager.MOD_ID, poiId);

        LOGGER.debug("Registering POI type: {} for block: {}",
            identifier, Registries.BLOCK.getId(workstationBlock));

        // Collect all possible block states for the workstation
        Set<BlockState> blockStates = ImmutableSet.copyOf(workstationBlock.getStateManager().getStates());

        // Create registry key for the POI type
        RegistryKey<PointOfInterestType> registryKey = RegistryKey.of(
            RegistryKeys.POINT_OF_INTEREST_TYPE,
            identifier
        );

        // Register POI type using Fabric helper
        PointOfInterestType poiType = PointOfInterestHelper.register(
            identifier,
            1,              // ticketCount (how many villagers can use this POI)
            1,              // searchDistance (blocks to search for this POI)
            blockStates     // All block states that match this POI
        );

        // Verify registration succeeded
        if (poiType == null) {
            throw new IllegalStateException(
                String.format("Failed to register POI type: %s", identifier)
            );
        }

        LOGGER.info("Successfully registered POI type: {} with {} block states",
            identifier, blockStates.size());

        return poiType;
    }

    /**
     * Registers a block item for a given block.
     * <p>
     * Block items allow blocks to exist as items in inventories and be placed
     * by players. The item uses standard settings appropriate for building blocks.
     * </p>
     *
     * @param itemId the item identifier (without namespace)
     * @param block the block to create an item for
     * @return the registered Item instance
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException if registration fails
     * @since 1.0.0
     */
    private static Item registerBlockItem(String itemId, Block block) {
        // Validate input parameters
        Objects.requireNonNull(itemId, "Item ID must not be null");
        Objects.requireNonNull(block, "Block must not be null");

        if (itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID must not be empty");
        }

        // Create item identifier
        Identifier identifier = Identifier.of(XeenaaVillagerManager.MOD_ID, itemId);

        LOGGER.debug("Registering block item: {}", identifier);

        // Create block item with standard settings
        BlockItem blockItem = new BlockItem(block, new Item.Settings());

        // Register with Minecraft registry
        Item registeredItem = Registry.register(
            Registries.ITEM,
            identifier,
            blockItem
        );

        // Verify registration succeeded
        if (registeredItem != blockItem) {
            throw new IllegalStateException(
                String.format("Failed to register block item: %s", identifier)
            );
        }

        LOGGER.info("Successfully registered block item: {}", identifier);
        return registeredItem;
    }

    /**
     * Validates that all registered blocks and POI types are accessible through registries.
     * <p>
     * This method should be called after registration to ensure all blocks
     * were properly registered and can be retrieved from Minecraft registries.
     * </p>
     *
     * @return true if all registrations are valid, false otherwise
     * @since 1.0.0
     */
    public static boolean validateRegistrations() {
        LOGGER.debug("Validating block and POI registrations");

        try {
            // Validate Guard Post block
            Identifier guardPostId = Identifier.of(XeenaaVillagerManager.MOD_ID, GUARD_POST_BLOCK_ID);
            Block guardPostFromRegistry = Registries.BLOCK.get(guardPostId);

            if (guardPostFromRegistry == null || guardPostFromRegistry != GUARD_POST) {
                LOGGER.error("Guard Post block validation failed - not found in registry");
                return false;
            }

            // Validate Guard Post item
            Item guardPostItemFromRegistry = Registries.ITEM.get(guardPostId);

            if (guardPostItemFromRegistry == null || guardPostItemFromRegistry != GUARD_POST_ITEM) {
                LOGGER.error("Guard Post item validation failed - not found in registry");
                return false;
            }

            // Validate Guard Post POI
            Identifier guardPostPoiId = Identifier.of(XeenaaVillagerManager.MOD_ID, GUARD_POST_POI_ID);
            PointOfInterestType poiFromRegistry = Registries.POINT_OF_INTEREST_TYPE.get(guardPostPoiId);

            if (poiFromRegistry == null || poiFromRegistry != GUARD_POST_POI) {
                LOGGER.error("Guard Post POI validation failed - not found in registry");
                return false;
            }

            LOGGER.info("All block and POI registrations validated successfully");
            return true;

        } catch (Exception e) {
            LOGGER.error("Error validating block registrations", e);
            return false;
        }
    }

    /**
     * Gets the identifier for a registered block.
     * <p>
     * This utility method creates the proper identifier for accessing
     * registered blocks from external code.
     * </p>
     *
     * @param blockId the block ID (without namespace)
     * @return the full identifier for the block
     * @throws IllegalArgumentException if blockId is null or empty
     * @since 1.0.0
     */
    public static Identifier getBlockIdentifier(String blockId) {
        Objects.requireNonNull(blockId, "Block ID must not be null");

        if (blockId.trim().isEmpty()) {
            throw new IllegalArgumentException("Block ID must not be empty");
        }

        return Identifier.of(XeenaaVillagerManager.MOD_ID, blockId);
    }

    /**
     * Logs detailed information about all registered blocks and POI types.
     * <p>
     * This method is useful for debugging and verifying that blocks
     * were registered correctly with their associated POI types.
     * </p>
     *
     * @since 1.0.0
     */
    public static void logRegisteredBlocks() {
        LOGGER.info("=== CUSTOM BLOCK REGISTRATION SUMMARY ===");

        // Log Guard Post details
        if (GUARD_POST != null) {
            Identifier guardPostId = getBlockIdentifier(GUARD_POST_BLOCK_ID);
            LOGGER.info("Guard Post Block: {} - Registered: {}",
                guardPostId,
                Registries.BLOCK.containsId(guardPostId));

            if (GUARD_POST_ITEM != null) {
                LOGGER.info("Guard Post Item: {} - Registered: {}",
                    guardPostId,
                    Registries.ITEM.containsId(guardPostId));
            }

            if (GUARD_POST_POI != null) {
                Identifier guardPostPoiId = Identifier.of(XeenaaVillagerManager.MOD_ID, GUARD_POST_POI_ID);
                LOGGER.info("Guard Post POI: {} - Registered: {}",
                    guardPostPoiId,
                    Registries.POINT_OF_INTEREST_TYPE.containsId(guardPostPoiId));
            }
        }

        LOGGER.info("=== END CUSTOM BLOCK SUMMARY ===");
    }
}