package com.xeenaa.villagermanager;

import com.xeenaa.villagermanager.client.model.SimplifiedVillagerModel;
import com.xeenaa.villagermanager.client.network.GuardDataSyncHandler;
import com.xeenaa.villagermanager.client.render.VillagerRendererFactory;
import com.xeenaa.villagermanager.client.util.ClientInteractionHandler;
import com.xeenaa.villagermanager.profession.ModProfessions;
import com.xeenaa.villagermanager.registry.ProfessionManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XeenaaVillagerManagerClient implements ClientModInitializer {
    public static final Logger CLIENT_LOGGER = LoggerFactory.getLogger(XeenaaVillagerManager.MOD_ID + "-client");


    @Override
    public void onInitializeClient() {
        CLIENT_LOGGER.info("Initializing Xeenaa Villager Manager client");

        // Initialize ProfessionManager on client side (collects all registered professions)
        ProfessionManager professionManager = ProfessionManager.getInstance();
        professionManager.initialize();
        CLIENT_LOGGER.info("Client-side ProfessionManager initialized with {} professions",
            professionManager.getStats().total());

        // Register custom model layer for guard villagers
        EntityModelLayerRegistry.registerModelLayer(VillagerRendererFactory.getGuardModelLayer(),
            SimplifiedVillagerModel::getTexturedModelData);

        // Register conditional villager renderer factory
        // Guards use custom renderer (no overlays), normal villagers use vanilla renderer
        EntityRendererRegistry.register(EntityType.VILLAGER, new VillagerRendererFactory());
        CLIENT_LOGGER.info("Registered conditional villager renderer - guards bypass overlay system completely");

        // Register villager interaction event for GUI opening
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof VillagerEntity villager) {
                CLIENT_LOGGER.info("UseEntityCallback: Villager interaction detected on client");
                return ClientInteractionHandler.handleVillagerInteraction(villager, player, hand);
            }
            return ActionResult.PASS;
        });

        // Register client-side packet handlers
        GuardDataSyncHandler.register();

        CLIENT_LOGGER.info("Client-side villager interaction handler and conditional rendering system registered");
    }
}