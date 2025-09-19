package com.xeenaa.villagermanager.profession;

import com.google.common.collect.ImmutableSet;
import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Registry for custom villager professions added by this mod.
 * <p>
 * This class manages the registration of custom professions following Minecraft 1.21.1
 * and Fabric API conventions. All professions are registered during mod initialization
 * to ensure they are available before world loading.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> This class is designed to be called only during mod
 * initialization and is not thread-safe for concurrent registration.
 * </p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager
 */
public final class ModProfessions {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModProfessions.class);

    // Profession constants following naming conventions
    public static final String GUARD_PROFESSION_ID = "guard";

    // Registered profession instances
    public static VillagerProfession GUARD;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ModProfessions() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Registers all custom professions with the Minecraft registry.
     * <p>
     * This method must be called during mod initialization before any world loading
     * occurs. It registers the professions with their associated Point of Interest
     * types and work sound events.
     * </p>
     *
     * @throws IllegalStateException if registration fails for any profession
     * @since 1.0.0
     */
    public static void registerProfessions() {
        LOGGER.info("Registering custom villager professions");

        try {
            // Register Guard profession
            GUARD = registerProfession(
                GUARD_PROFESSION_ID,
                ModBlocks.GUARD_POST_POI,
                SoundEvents.BLOCK_ANVIL_USE
            );

            LOGGER.info("Successfully registered {} custom profession(s)", 1);

        } catch (Exception e) {
            LOGGER.error("Failed to register custom professions", e);
            throw new IllegalStateException("Profession registration failed", e);
        }
    }

    /**
     * Registers a single villager profession with the specified parameters.
     * <p>
     * This helper method handles the boilerplate of profession registration,
     * including proper identifier creation and registry validation.
     * </p>
     *
     * @param professionId the profession identifier (without namespace)
     * @param workstationPoi the Point of Interest type for the profession's workstation
     * @param workSound the sound played when the villager works
     * @return the registered VillagerProfession instance
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException if registration fails
     * @since 1.0.0
     */
    private static VillagerProfession registerProfession(
        String professionId,
        PointOfInterestType workstationPoi,
        SoundEvent workSound
    ) {
        // Validate input parameters
        Objects.requireNonNull(professionId, "Profession ID must not be null");
        Objects.requireNonNull(workstationPoi, "Workstation POI must not be null");
        Objects.requireNonNull(workSound, "Work sound must not be null");

        if (professionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Profession ID must not be empty");
        }

        // Create profession identifier
        Identifier identifier = Identifier.of(XeenaaVillagerManager.MOD_ID, professionId);

        LOGGER.debug("Registering profession: {}", identifier);

        // Get the registry key for the POI type
        RegistryKey<PointOfInterestType> poiKey = Registries.POINT_OF_INTEREST_TYPE.getKey(workstationPoi)
            .orElseThrow(() -> new IllegalStateException("POI type not found in registry"));

        // Create profession with workstation and work sound
        VillagerProfession profession = new VillagerProfession(
            identifier.toString(),
            entry -> entry.matchesKey(poiKey),
            entry -> entry.matchesKey(poiKey),
            ImmutableSet.of(),      // Gatherables (items the villager can pick up)
            ImmutableSet.of(),      // Secondary job sites
            workSound               // Work sound
        );

        // Register with Minecraft registry
        VillagerProfession registeredProfession = Registry.register(
            Registries.VILLAGER_PROFESSION,
            identifier,
            profession
        );

        // Verify registration succeeded
        if (registeredProfession != profession) {
            throw new IllegalStateException(
                String.format("Failed to register profession: %s", identifier)
            );
        }

        LOGGER.info("Successfully registered profession: {} with workstation POI and work sound",
            identifier);

        return registeredProfession;
    }

    /**
     * Validates that all registered professions are accessible through the registry.
     * <p>
     * This method should be called after registration to ensure all professions
     * were properly registered and can be retrieved from the Minecraft registry.
     * </p>
     *
     * @return true if all professions are properly registered, false otherwise
     * @since 1.0.0
     */
    public static boolean validateRegistrations() {
        LOGGER.debug("Validating profession registrations");

        try {
            // Validate Guard profession
            Identifier guardId = Identifier.of(XeenaaVillagerManager.MOD_ID, GUARD_PROFESSION_ID);
            VillagerProfession guardFromRegistry = Registries.VILLAGER_PROFESSION.get(guardId);

            if (guardFromRegistry == null || guardFromRegistry != GUARD) {
                LOGGER.error("Guard profession validation failed - not found in registry");
                return false;
            }

            LOGGER.info("All profession registrations validated successfully");
            return true;

        } catch (Exception e) {
            LOGGER.error("Error validating profession registrations", e);
            return false;
        }
    }

    /**
     * Gets the identifier for a registered profession.
     * <p>
     * This utility method creates the proper identifier for accessing
     * registered professions from external code.
     * </p>
     *
     * @param professionId the profession ID (without namespace)
     * @return the full identifier for the profession
     * @throws IllegalArgumentException if professionId is null or empty
     * @since 1.0.0
     */
    public static Identifier getProfessionIdentifier(String professionId) {
        Objects.requireNonNull(professionId, "Profession ID must not be null");

        if (professionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Profession ID must not be empty");
        }

        return Identifier.of(XeenaaVillagerManager.MOD_ID, professionId);
    }

    /**
     * Logs detailed information about all registered custom professions.
     * <p>
     * This method is useful for debugging and verifying that professions
     * were registered correctly with their associated workstations.
     * </p>
     *
     * @since 1.0.0
     */
    public static void logRegisteredProfessions() {
        LOGGER.info("=== CUSTOM PROFESSION REGISTRATION SUMMARY ===");

        // Log Guard profession details
        if (GUARD != null) {
            Identifier guardId = getProfessionIdentifier(GUARD_PROFESSION_ID);
            LOGGER.info("Guard Profession: {} - Registered: {}",
                guardId,
                Registries.VILLAGER_PROFESSION.containsId(guardId));
        }

        LOGGER.info("=== END CUSTOM PROFESSION SUMMARY ===");
    }
}