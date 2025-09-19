package com.xeenaa.villagermanager;

import com.xeenaa.villagermanager.block.ModBlocks;
import com.xeenaa.villagermanager.config.ModConfig;
import com.xeenaa.villagermanager.network.SelectProfessionPacket;
import com.xeenaa.villagermanager.network.ServerPacketHandler;
import com.xeenaa.villagermanager.profession.ModProfessions;
import com.xeenaa.villagermanager.registry.ProfessionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XeenaaVillagerManager implements ModInitializer {
    public static final String MOD_ID = "xeenaa_villager_manager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Xeenaa Villager Manager mod - Version 1.0.0");
        LOGGER.info("Hot reload test: Development environment ready");

        // Initialize configuration
        ModConfig config = ModConfig.getInstance();
        LOGGER.info("Configuration loaded with {} blacklisted professions",
            config.getBlacklistedProfessions().size());

        // Register custom blocks and POI types first (required for professions)
        LOGGER.info("Registering custom blocks and workstations");
        ModBlocks.registerBlocks();
        ModBlocks.logRegisteredBlocks();

        // Validate block registrations
        if (!ModBlocks.validateRegistrations()) {
            throw new IllegalStateException("Block registration validation failed");
        }

        // Register custom professions (requires blocks to be registered first)
        LOGGER.info("Registering custom villager professions");
        ModProfessions.registerProfessions();
        ModProfessions.logRegisteredProfessions();

        // Validate profession registrations
        if (!ModProfessions.validateRegistrations()) {
            throw new IllegalStateException("Profession registration validation failed");
        }

        // Initialize profession manager (now includes custom professions)
        ProfessionManager professionManager = ProfessionManager.getInstance();
        professionManager.initialize();

        // Log profession statistics (including custom professions)
        ProfessionManager.ProfessionStats stats = professionManager.getStats();
        LOGGER.info("Profession system initialized: {} total ({} vanilla, {} modded)",
            stats.total(), stats.vanilla(), stats.modded());

        // Log detailed profession information for testing
        professionManager.logAllProfessions();

        // Register network packets
        PayloadTypeRegistry.playC2S().register(SelectProfessionPacket.PACKET_ID, SelectProfessionPacket.CODEC);

        // Register server-side packet handlers
        ServerPacketHandler.registerHandlers();

        // Final initialization complete message
        LOGGER.info("Xeenaa Villager Manager initialization complete - Custom Guard profession available");

        // Note: Villager interaction event registration moved to client-side
    }
}