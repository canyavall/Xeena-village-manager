package com.xeenaa.villagermanager;

import com.xeenaa.villagermanager.client.util.ClientInteractionHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XeenaaVillagerManagerClient implements ClientModInitializer {
    public static final Logger CLIENT_LOGGER = LoggerFactory.getLogger(XeenaaVillagerManager.MOD_ID + "-client");

    @Override
    public void onInitializeClient() {
        CLIENT_LOGGER.info("Initializing Xeenaa Villager Manager client");

        // Register villager interaction event for GUI opening
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof VillagerEntity villager) {
                CLIENT_LOGGER.info("UseEntityCallback: Villager interaction detected on client");
                return ClientInteractionHandler.handleVillagerInteraction(villager, player, hand);
            }
            return ActionResult.PASS;
        });

        CLIENT_LOGGER.info("Client-side villager interaction handler registered");
    }
}