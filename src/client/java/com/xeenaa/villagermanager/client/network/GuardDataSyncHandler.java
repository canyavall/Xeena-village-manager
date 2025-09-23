package com.xeenaa.villagermanager.client.network;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.network.GuardDataSyncPacket;
import com.xeenaa.villagermanager.network.GuardRankSyncPacket;
import com.xeenaa.villagermanager.network.InitialGuardDataSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Client-side handler for guard data synchronization packets.
 * <p>
 * This handler processes guard data synchronization packets from the server
 * and updates the client-side cache to ensure accurate rendering and GUI display.
 * </p>
 * <p>
 * <strong>Packet Processing:</strong> All packets are processed on the main client
 * thread to ensure thread safety with rendering and GUI systems.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> This class uses the client's main thread for
 * all processing to maintain thread safety with Minecraft's systems.
 * </p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager
 */
public class GuardDataSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardDataSyncHandler.class);

    // Statistics for monitoring packet handling
    private static volatile long packetsReceived = 0;
    private static volatile long packetsProcessed = 0;
    private static volatile long processingErrors = 0;

    /**
     * Registers the client-side packet handler for guard data synchronization.
     * <p>
     * This method must be called during client initialization to enable
     * proper guard data synchronization from server to client.
     * </p>
     *
     * @since 1.0.0
     */
    public static void register() {
        try {
            ClientPlayNetworking.registerGlobalReceiver(GuardDataSyncPacket.ID, GuardDataSyncHandler::handle);
            LOGGER.info("Successfully registered client-side GuardDataSyncPacket handler");

            ClientPlayNetworking.registerGlobalReceiver(InitialGuardDataSyncPacket.PACKET_ID, GuardDataSyncHandler::handleInitialSync);
            LOGGER.info("Successfully registered client-side InitialGuardDataSyncPacket handler");

            ClientPlayNetworking.registerGlobalReceiver(GuardRankSyncPacket.PACKET_ID, GuardDataSyncHandler::handleRankSync);
            LOGGER.info("Successfully registered client-side GuardRankSyncPacket handler");
        } catch (Exception e) {
            LOGGER.error("Failed to register guard data sync handlers", e);
            throw new RuntimeException("Guard data sync handler registration failed", e);
        }
    }

    /**
     * Handles incoming guard data synchronization packets from the server.
     * <p>
     * This method processes packets on the client main thread to ensure
     * thread safety with rendering and GUI systems.
     * </p>
     *
     * @param packet the guard data synchronization packet
     * @param context the client networking context
     * @throws NullPointerException if packet or context is null
     * @since 1.0.0
     */
    private static void handle(GuardDataSyncPacket packet, ClientPlayNetworking.Context context) {
        Objects.requireNonNull(packet, "Guard data sync packet must not be null");
        Objects.requireNonNull(context, "Client networking context must not be null");

        packetsReceived++;
        MinecraftClient client = context.client();

        // Execute on client main thread for thread safety
        client.execute(() -> {
            try {
                processGuardDataSync(packet, client);
                packetsProcessed++;
                LOGGER.trace("Successfully processed guard data sync packet for villager: {}",
                    packet.villagerId());
            } catch (Exception e) {
                processingErrors++;
                LOGGER.error("Error processing guard data sync packet for villager {}: {}",
                    packet.villagerId(), e.getMessage(), e);
            }
        });
    }

    /**
     * Handles incoming initial guard data synchronization packets from the server.
     * This packet contains all existing guard data for initial client sync.
     */
    private static void handleInitialSync(InitialGuardDataSyncPacket packet, ClientPlayNetworking.Context context) {
        Objects.requireNonNull(packet, "Initial guard data sync packet must not be null");
        Objects.requireNonNull(context, "Client networking context must not be null");

        packetsReceived++;
        MinecraftClient client = context.client();

        // Execute on client main thread for thread safety
        client.execute(() -> {
            try {
                processInitialGuardDataSync(packet, client);
                packetsProcessed++;
                LOGGER.info("Successfully processed initial guard data sync with {} entries", packet.guardDataMap().size());
            } catch (Exception e) {
                processingErrors++;
                LOGGER.error("Error processing initial guard data sync: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Processes the initial guard data synchronization packet and updates the client cache.
     */
    private static void processInitialGuardDataSync(InitialGuardDataSyncPacket packet, MinecraftClient client) {
        Objects.requireNonNull(packet, "Initial guard data sync packet must not be null");
        Objects.requireNonNull(client, "Minecraft client must not be null");

        try {
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();

            LOGGER.info("=== PROCESSING INITIAL GUARD DATA SYNC ===");
            LOGGER.info("Clearing existing client cache and loading {} guard data entries", packet.guardDataMap().size());

            // Clear existing cache data to ensure clean state
            cache.clearAll();

            // Add all guard data from the packet
            for (var entry : packet.guardDataMap().entrySet()) {
                cache.updateGuardData(entry.getKey(), entry.getValue());
                LOGGER.info("Loaded guard data for villager {}: role = {}",
                           entry.getKey(), entry.getValue().getRole());
            }

            LOGGER.info("Client guard data cache initialized with {} entries", packet.guardDataMap().size());
            LOGGER.info("=== INITIAL GUARD DATA SYNC COMPLETE ===");

        } catch (Exception e) {
            LOGGER.error("Failed to process initial guard data sync", e);
            throw e;
        }
    }

    /**
     * Processes the guard data synchronization packet and updates the client cache.
     * <p>
     * This method extracts data from the packet and updates the client-side
     * cache to ensure rendering and GUI systems have access to current data.
     * </p>
     *
     * @param packet the guard data synchronization packet
     * @param client the Minecraft client instance
     * @throws NullPointerException if packet or client is null
     * @since 1.0.0
     */
    private static void processGuardDataSync(GuardDataSyncPacket packet, MinecraftClient client) {
        Objects.requireNonNull(packet, "Guard data sync packet must not be null");
        Objects.requireNonNull(client, "Minecraft client must not be null");

        try {
            // Create guard data from packet information
            GuardData guardData = createGuardDataFromPacket(packet);

            // Update client-side cache
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
            cache.updateGuardData(packet.villagerId(), guardData);

            LOGGER.debug("Updated client guard data for villager {}: role={}",
                packet.villagerId(), guardData.getRole());

            // Guard data updated successfully

        } catch (Exception e) {
            LOGGER.error("Failed to process guard data sync for villager: {}", packet.villagerId(), e);
            throw e;
        }
    }

    /**
     * Creates a GuardData instance from the synchronization packet.
     * <p>
     * This method reconstructs guard data from the packet's serialized
     * information for use in the client-side cache.
     * </p>
     *
     * @param packet the guard data synchronization packet
     * @return the reconstructed guard data
     * @throws NullPointerException if packet is null
     * @since 1.0.0
     */
    private static GuardData createGuardDataFromPacket(GuardDataSyncPacket packet) {
        Objects.requireNonNull(packet, "Guard data sync packet must not be null");

        // Create new guard data instance
        GuardData guardData = new GuardData(packet.villagerId());

        // Set role from packet
        guardData.setRole(packet.role());

        // Equipment system removed - role only sync

        return guardData;
    }


    /**
     * Gets synchronization statistics for monitoring and debugging.
     * <p>
     * This method provides statistics about packet handling performance
     * and error rates for system monitoring.
     * </p>
     *
     * @return a formatted string with synchronization statistics
     * @since 1.0.0
     */
    public static String getSyncStatistics() {
        return String.format(
            "GuardDataSync[received=%d, processed=%d, errors=%d, successRate=%.2f%%]",
            packetsReceived, packetsProcessed, processingErrors,
            packetsReceived > 0 ? (double) packetsProcessed / packetsReceived * 100.0 : 0.0
        );
    }

    /**
     * Logs synchronization statistics for debugging.
     * <p>
     * This method logs comprehensive synchronization statistics at debug level
     * for troubleshooting and performance monitoring.
     * </p>
     *
     * @since 1.0.0
     */
    public static void logStatistics() {
        LOGGER.debug("Guard data synchronization statistics: {}", getSyncStatistics());
    }

    /**
     * Resets synchronization statistics.
     * <p>
     * This method resets all statistics counters, typically called when
     * connecting to a new server or for testing purposes.
     * </p>
     *
     * @since 1.0.0
     */
    public static void resetStatistics() {
        packetsReceived = 0;
        packetsProcessed = 0;
        processingErrors = 0;
        LOGGER.debug("Reset guard data synchronization statistics");
    }

    /**
     * Validates the synchronization handler setup.
     * <p>
     * This method performs validation checks to ensure the handler
     * is properly configured and ready to process packets.
     * </p>
     *
     * @return true if the handler is properly configured, false otherwise
     * @since 1.0.0
     */
    public static boolean validateHandler() {
        boolean isValid = true;

        try {
            // Check if packet ID is properly configured
            if (GuardDataSyncPacket.ID == null) {
                LOGGER.error("GuardDataSyncPacket ID is null - handler cannot function");
                isValid = false;
            }

            // Check if client cache is available
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
            if (cache == null) {
                LOGGER.error("ClientGuardDataCache instance is null - synchronization will fail");
                isValid = false;
            }

            LOGGER.debug("Guard data sync handler validation: {}", isValid ? "PASSED" : "FAILED");
        } catch (Exception e) {
            LOGGER.error("Error during handler validation", e);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Handles incoming guard rank synchronization packets from the server.
     */
    private static void handleRankSync(GuardRankSyncPacket packet, ClientPlayNetworking.Context context) {
        Objects.requireNonNull(packet, "Guard rank sync packet must not be null");
        Objects.requireNonNull(context, "Client networking context must not be null");

        packetsReceived++;
        MinecraftClient client = context.client();

        // Execute on client main thread for thread safety
        client.execute(() -> {
            try {
                processRankSync(packet, client);
                packetsProcessed++;
                LOGGER.info("Successfully processed rank sync packet for villager: {} (rank: {})",
                    packet.villagerId(), packet.currentRank().getDisplayName());
            } catch (Exception e) {
                processingErrors++;
                LOGGER.error("Error processing rank sync packet for villager {}: {}",
                    packet.villagerId(), e.getMessage(), e);
            }
        });
    }

    /**
     * Processes the rank synchronization packet and updates the client cache.
     */
    private static void processRankSync(GuardRankSyncPacket packet, MinecraftClient client) {
        Objects.requireNonNull(packet, "Guard rank sync packet must not be null");
        Objects.requireNonNull(client, "Minecraft client must not be null");

        try {
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();

            // Get or create guard data in client cache
            GuardData guardData = cache.getGuardData(packet.villagerId());
            if (guardData == null) {
                guardData = new GuardData(packet.villagerId());
                cache.updateGuardData(packet.villagerId(), guardData);
            }

            // Update rank data
            GuardRankData rankData = guardData.getRankData();
            rankData.setCurrentRank(packet.currentRank());

            // Update the cache
            cache.updateGuardData(packet.villagerId(), guardData);

            LOGGER.debug("Updated client rank data for villager {}: rank={}, emeralds_spent={}",
                packet.villagerId(), packet.currentRank().getDisplayName(), packet.totalEmeraldsSpent());

        } catch (Exception e) {
            LOGGER.error("Failed to process rank sync for villager: {}", packet.villagerId(), e);
            throw e;
        }
    }
}