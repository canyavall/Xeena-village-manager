package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Handles server-side events for player connections and syncs guard data to new clients.
 */
public class PlayerJoinHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerJoinHandler");

    /**
     * Registers the player join handler to sync guard data to new clients.
     */
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            LOGGER.info("=== PLAYER JOIN: {} ===", player.getName().getString());

            // Send initial guard data sync to the player
            sendInitialGuardDataSync(player);
        });

        LOGGER.info("Registered player join handler for initial guard data sync");
    }

    /**
     * Sends all existing guard data to a newly connected player.
     */
    private static void sendInitialGuardDataSync(ServerPlayerEntity player) {
        try {
            LOGGER.info("=== SENDING INITIAL GUARD DATA SYNC to player {} ===", player.getName().getString());

            ServerWorld world = player.getServerWorld();
            GuardDataManager manager = GuardDataManager.get(world);
            Map<UUID, GuardData> allGuardData = manager.getAllGuardData();

            LOGGER.info("Found {} guard data entries to sync", allGuardData.size());

            if (allGuardData.isEmpty()) {
                LOGGER.info("No guard data to sync - player {} will have empty cache", player.getName().getString());
                // Still send an empty sync packet to clear any potential stale cache
                allGuardData = Map.of();
            }

            // Create and send initial sync packet
            InitialGuardDataSyncPacket packet = new InitialGuardDataSyncPacket(allGuardData);
            ServerPlayNetworking.send(player, packet);

            LOGGER.info("Sent initial guard data sync to player {} with {} entries",
                       player.getName().getString(), allGuardData.size());
            LOGGER.info("=== INITIAL GUARD DATA SYNC COMPLETE ===");

        } catch (Exception e) {
            LOGGER.error("Failed to send initial guard data sync to player {}: {}",
                        player.getName().getString(), e.getMessage(), e);
        }
    }
}