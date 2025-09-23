package com.xeenaa.villagermanager.client.data;

import com.xeenaa.villagermanager.data.GuardData;
import net.minecraft.entity.passive.VillagerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache for Guard villager data to support rendering and UI.
 * <p>
 * This cache stores guard data received from the server via synchronization packets,
 * allowing the client to render equipment and display accurate information in GUIs
 * without constantly querying the server.
 * </p>
 * <p>
 * <strong>Synchronization:</strong> Data is updated when received from server packets
 * and cleaned up when villagers are unloaded or change profession.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> This class is designed to be thread-safe for
 * concurrent access from rendering and networking threads.
 * </p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager
 */
public class ClientGuardDataCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientGuardDataCache.class);

    // Singleton instance for global client access
    private static ClientGuardDataCache instance;

    // Thread-safe storage for guard data
    private final Map<UUID, GuardData> guardDataCache;

    // Cache statistics for debugging
    private volatile long cacheHits = 0;
    private volatile long cacheMisses = 0;
    private volatile long cacheUpdates = 0;

    /**
     * Private constructor for singleton pattern.
     *
     * @since 1.0.0
     */
    private ClientGuardDataCache() {
        this.guardDataCache = new ConcurrentHashMap<>();
        LOGGER.debug("Client guard data cache initialized");
    }

    /**
     * Gets the singleton instance of the client guard data cache.
     * <p>
     * This method provides global access to the client-side guard data cache
     * for use by rendering systems and UI components.
     * </p>
     *
     * @return the singleton cache instance
     * @since 1.0.0
     */
    public static synchronized ClientGuardDataCache getInstance() {
        if (instance == null) {
            instance = new ClientGuardDataCache();
            LOGGER.info("Client guard data cache singleton created");
        }
        return instance;
    }

    /**
     * Retrieves guard data for the specified villager.
     * <p>
     * This method provides guard data for rendering and UI purposes.
     * Returns null if no data is cached for the villager.
     * </p>
     *
     * @param villagerId the UUID of the villager
     * @return the guard data, or null if not cached
     * @throws NullPointerException if villagerId is null
     * @since 1.0.0
     */
    public GuardData getGuardData(UUID villagerId) {
        Objects.requireNonNull(villagerId, "Villager ID must not be null");

        GuardData data = guardDataCache.get(villagerId);
        if (data != null) {
            cacheHits++;
            LOGGER.trace("Cache hit for villager: {}", villagerId);
        } else {
            cacheMisses++;
            LOGGER.trace("Cache miss for villager: {}", villagerId);
        }

        return data;
    }

    /**
     * Retrieves guard data for the specified villager entity.
     * <p>
     * Convenience method that extracts the UUID from the villager entity
     * and retrieves the corresponding guard data.
     * </p>
     *
     * @param villager the villager entity
     * @return the guard data, or null if not cached
     * @throws NullPointerException if villager is null
     * @since 1.0.0
     */
    public GuardData getGuardData(VillagerEntity villager) {
        Objects.requireNonNull(villager, "Villager entity must not be null");
        return getGuardData(villager.getUuid());
    }

    /**
     * Updates or adds guard data for the specified villager.
     * <p>
     * This method is called when receiving guard data synchronization packets
     * from the server to update the client-side cache.
     * </p>
     *
     * @param villagerId the UUID of the villager
     * @param guardData the guard data to cache
     * @throws NullPointerException if any parameter is null
     * @since 1.0.0
     */
    public void updateGuardData(UUID villagerId, GuardData guardData) {
        Objects.requireNonNull(villagerId, "Villager ID must not be null");
        Objects.requireNonNull(guardData, "Guard data must not be null");

        GuardData previousData = guardDataCache.put(villagerId, guardData);
        cacheUpdates++;

        if (previousData != null) {
            LOGGER.debug("Updated guard data for villager: {}", villagerId);
        } else {
            LOGGER.debug("Added new guard data for villager: {}", villagerId);
        }

        LOGGER.trace("Guard data updated: villager={}, role={}",
            villagerId, guardData.getRole());
    }

    /**
     * Updates guard data for the specified villager entity.
     * <p>
     * Convenience method that extracts the UUID from the villager entity
     * and updates the corresponding guard data.
     * </p>
     *
     * @param villager the villager entity
     * @param guardData the guard data to cache
     * @throws NullPointerException if any parameter is null
     * @since 1.0.0
     */
    public void updateGuardData(VillagerEntity villager, GuardData guardData) {
        Objects.requireNonNull(villager, "Villager entity must not be null");
        updateGuardData(villager.getUuid(), guardData);
    }

    /**
     * Removes guard data for the specified villager.
     * <p>
     * This method is called when a villager changes profession or is unloaded
     * to clean up the cache and prevent memory leaks.
     * </p>
     *
     * @param villagerId the UUID of the villager
     * @return the removed guard data, or null if not cached
     * @throws NullPointerException if villagerId is null
     * @since 1.0.0
     */
    public GuardData removeGuardData(UUID villagerId) {
        Objects.requireNonNull(villagerId, "Villager ID must not be null");

        GuardData removedData = guardDataCache.remove(villagerId);
        if (removedData != null) {
            LOGGER.debug("Removed guard data for villager: {}", villagerId);
        } else {
            LOGGER.trace("No guard data to remove for villager: {}", villagerId);
        }

        return removedData;
    }

    /**
     * Removes guard data for the specified villager entity.
     * <p>
     * Convenience method that extracts the UUID from the villager entity
     * and removes the corresponding guard data.
     * </p>
     *
     * @param villager the villager entity
     * @return the removed guard data, or null if not cached
     * @throws NullPointerException if villager is null
     * @since 1.0.0
     */
    public GuardData removeGuardData(VillagerEntity villager) {
        Objects.requireNonNull(villager, "Villager entity must not be null");
        return removeGuardData(villager.getUuid());
    }

    /**
     * Checks if guard data is cached for the specified villager.
     * <p>
     * This method provides a quick way to check if guard data is available
     * without retrieving the actual data object.
     * </p>
     *
     * @param villagerId the UUID of the villager
     * @return true if guard data is cached, false otherwise
     * @throws NullPointerException if villagerId is null
     * @since 1.0.0
     */
    public boolean hasGuardData(UUID villagerId) {
        Objects.requireNonNull(villagerId, "Villager ID must not be null");
        return guardDataCache.containsKey(villagerId);
    }

    /**
     * Checks if guard data is cached for the specified villager entity.
     * <p>
     * Convenience method that extracts the UUID from the villager entity
     * and checks if guard data is cached.
     * </p>
     *
     * @param villager the villager entity
     * @return true if guard data is cached, false otherwise
     * @throws NullPointerException if villager is null
     * @since 1.0.0
     */
    public boolean hasGuardData(VillagerEntity villager) {
        Objects.requireNonNull(villager, "Villager entity must not be null");
        return hasGuardData(villager.getUuid());
    }

    /**
     * Clears all cached guard data.
     * <p>
     * This method is typically called when disconnecting from a server
     * or changing worlds to prevent stale data.
     * </p>
     *
     * @since 1.0.0
     */
    public void clearAll() {
        int clearedCount = guardDataCache.size();
        guardDataCache.clear();

        // Reset statistics
        cacheHits = 0;
        cacheMisses = 0;
        cacheUpdates = 0;

        LOGGER.info("Cleared all guard data from client cache: {} entries removed", clearedCount);
    }

    /**
     * Gets the number of cached guard data entries.
     * <p>
     * This method provides information about cache size for debugging
     * and monitoring purposes.
     * </p>
     *
     * @return the number of cached entries
     * @since 1.0.0
     */
    public int getCacheSize() {
        return guardDataCache.size();
    }

    /**
     * Gets cache hit ratio for performance monitoring.
     * <p>
     * This method calculates the cache hit ratio as a percentage
     * for performance analysis and debugging.
     * </p>
     *
     * @return the cache hit ratio as a percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getCacheHitRatio() {
        long totalAccesses = cacheHits + cacheMisses;
        if (totalAccesses == 0) {
            return 0.0;
        }
        return (double) cacheHits / totalAccesses * 100.0;
    }

    /**
     * Gets cache statistics for debugging and monitoring.
     * <p>
     * This method provides comprehensive cache statistics including
     * hits, misses, updates, and hit ratio.
     * </p>
     *
     * @return a formatted string with cache statistics
     * @since 1.0.0
     */
    public String getCacheStatistics() {
        return String.format(
            "GuardDataCache[size=%d, hits=%d, misses=%d, updates=%d, hitRatio=%.2f%%]",
            getCacheSize(), cacheHits, cacheMisses, cacheUpdates, getCacheHitRatio()
        );
    }

    /**
     * Logs current cache statistics for debugging.
     * <p>
     * This method logs comprehensive cache statistics at debug level
     * for troubleshooting and performance monitoring.
     * </p>
     *
     * @since 1.0.0
     */
    public void logStatistics() {
        LOGGER.debug("Client guard data cache statistics: {}", getCacheStatistics());
    }

    /**
     * Validates the cache integrity and logs any issues.
     * <p>
     * This method performs validation checks on cached data to ensure
     * integrity and consistency.
     * </p>
     *
     * @return true if cache is valid, false if issues were found
     * @since 1.0.0
     */
    public boolean validateCache() {
        boolean isValid = true;
        int validatedEntries = 0;

        for (Map.Entry<UUID, GuardData> entry : guardDataCache.entrySet()) {
            UUID villagerId = entry.getKey();
            GuardData guardData = entry.getValue();

            if (villagerId == null) {
                LOGGER.error("Found null villager ID in cache");
                isValid = false;
                continue;
            }

            if (guardData == null) {
                LOGGER.error("Found null guard data for villager: {}", villagerId);
                isValid = false;
                continue;
            }

            if (!villagerId.equals(guardData.getVillagerId())) {
                LOGGER.error("Villager ID mismatch in cache: key={}, data.id={}",
                    villagerId, guardData.getVillagerId());
                isValid = false;
                continue;
            }

            validatedEntries++;
        }

        LOGGER.debug("Cache validation completed: valid={}, entries={}/{}",
            isValid, validatedEntries, guardDataCache.size());

        return isValid;
    }
}