package com.xeenaa.villagermanager;

import com.xeenaa.villagermanager.block.ModBlocks;
import com.xeenaa.villagermanager.config.ModConfig;
import com.xeenaa.villagermanager.event.ThreatEventHandler;
import com.xeenaa.villagermanager.network.SelectProfessionPacket;
import com.xeenaa.villagermanager.network.GuardDataSyncPacket;
import com.xeenaa.villagermanager.network.GuardEmeraldRefundPacket;
import com.xeenaa.villagermanager.network.GuardProfessionChangePacket;
import com.xeenaa.villagermanager.network.InitialGuardDataSyncPacket;
import com.xeenaa.villagermanager.network.GuardRankSyncPacket;
import com.xeenaa.villagermanager.network.PurchaseRankPacket;
import com.xeenaa.villagermanager.network.RankPurchaseResponsePacket;
import com.xeenaa.villagermanager.network.GuardConfigPacket;
import com.xeenaa.villagermanager.network.GuardConfigSyncPacket;
import com.xeenaa.villagermanager.network.PlayerJoinHandler;
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
        PayloadTypeRegistry.playC2S().register(PurchaseRankPacket.PACKET_ID, PurchaseRankPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GuardProfessionChangePacket.PACKET_ID, GuardProfessionChangePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GuardConfigPacket.PACKET_ID, GuardConfigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GuardDataSyncPacket.ID, GuardDataSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GuardEmeraldRefundPacket.PACKET_ID, GuardEmeraldRefundPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(InitialGuardDataSyncPacket.PACKET_ID, InitialGuardDataSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GuardRankSyncPacket.PACKET_ID, GuardRankSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RankPurchaseResponsePacket.PACKET_ID, RankPurchaseResponsePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GuardConfigSyncPacket.PACKET_ID, GuardConfigSyncPacket.CODEC);

        // Register server-side packet handlers
        ServerPacketHandler.registerHandlers();

        // Register player join handler for initial guard data sync
        PlayerJoinHandler.register();

        // Initialize threat detection system
        LOGGER.info("Initializing threat detection system for guard villagers");
        ThreatEventHandler.initialize();

        // Final initialization complete message
        LOGGER.info("Xeenaa Villager Manager initialization complete - Guard profession ready");

        // Note: Villager interaction event registration moved to client-side
    }
}