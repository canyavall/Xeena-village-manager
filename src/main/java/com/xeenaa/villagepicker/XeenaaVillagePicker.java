package com.xeenaa.villagepicker;

import com.xeenaa.villagepicker.config.ModConfig;
import com.xeenaa.villagepicker.network.SelectProfessionPacket;
import com.xeenaa.villagepicker.network.ServerPacketHandler;
import com.xeenaa.villagepicker.registry.ProfessionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XeenaaVillagePicker implements ModInitializer {
    public static final String MOD_ID = "xeenaa_village_picker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Xeenaa Villager Manager mod - Version 1.0.0");
        LOGGER.info("Hot reload test: Development environment ready");

        // Initialize configuration
        ModConfig config = ModConfig.getInstance();
        LOGGER.info("Configuration loaded with {} blacklisted professions", 
            config.getBlacklistedProfessions().size());

        // Initialize profession manager
        ProfessionManager professionManager = ProfessionManager.getInstance();
        professionManager.initialize();

        // Log profession statistics
        ProfessionManager.ProfessionStats stats = professionManager.getStats();
        LOGGER.info("Profession system initialized: {} total ({} vanilla, {} modded)",
            stats.total(), stats.vanilla(), stats.modded());

        // Log detailed profession information for testing
        professionManager.logAllProfessions();

        // Register network packets
        PayloadTypeRegistry.playC2S().register(SelectProfessionPacket.PACKET_ID, SelectProfessionPacket.CODEC);

        // Register server-side packet handlers
        ServerPacketHandler.registerHandlers();

        // Note: Villager interaction event registration moved to client-side
    }
}