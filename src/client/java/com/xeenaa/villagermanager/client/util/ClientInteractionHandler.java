package com.xeenaa.villagermanager.client.util;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.client.gui.UnifiedGuardManagementScreen;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ClientInteractionHandler {

    public static ActionResult handleVillagerInteraction(VillagerEntity villager, PlayerEntity player, Hand hand) {
        // Only handle main hand interactions
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        // Only open our GUI if player is sneaking (shift + right-click)
        // This allows normal right-click for trading
        if (!player.isSneaking()) {
            return ActionResult.PASS;
        }

        // Only handle client-side for GUI opening
        if (!player.getWorld().isClient) {
            return ActionResult.PASS;
        }

        // Basic player validation
        if (player == null || !player.isAlive()) {
            XeenaaVillagerManager.LOGGER.warn("Invalid player for villager interaction");
            return ActionResult.PASS;
        }

        // Villager validation
        if (villager == null || !villager.isAlive()) {
            XeenaaVillagerManager.LOGGER.warn("Invalid villager for interaction");
            return ActionResult.PASS;
        }

        XeenaaVillagerManager.LOGGER.info("Processing valid villager interaction (shift + right-click) - Player: {}, Villager: {}",
            player.getName().getString(), villager.getClass().getSimpleName());

        // Check permissions and villager eligibility
        if (!hasPermission(player) || !canChangeProfession(villager)) {
            XeenaaVillagerManager.LOGGER.info("Interaction not allowed - Permission: {}, Can change: {}",
                hasPermission(player), canChangeProfession(villager));
            return ActionResult.PASS;
        }

        // Open the new unified management screen for all villagers
        MinecraftClient client = MinecraftClient.getInstance();

        // Open UnifiedGuardManagementScreen for all villagers (works for guards and non-guards)
        UnifiedGuardManagementScreen screen = new UnifiedGuardManagementScreen(villager);
        client.setScreen(screen);

        if (isGuard(villager)) {
            XeenaaVillagerManager.LOGGER.info("Opened UnifiedGuardManagementScreen for guard villager (shift + right-click)");
        } else {
            XeenaaVillagerManager.LOGGER.info("Opened UnifiedGuardManagementScreen for villager (shift + right-click)");
        }

        // Consume the interaction to prevent vanilla trading GUI
        return ActionResult.SUCCESS;
    }

    /**
     * Check if player has permission to change villager professions
     * TODO: Implement permission system if needed
     */
    public static boolean hasPermission(PlayerEntity player) {
        // For now, allow all players
        return true;
    }

    /**
     * Check if villager can have their profession changed
     */
    public static boolean canChangeProfession(VillagerEntity villager) {
        // Check if villager is alive and not a baby
        if (!villager.isAlive() || villager.isBaby()) {
            return false;
        }

        // TODO: Add more checks (e.g., zombie villager, special villager types)
        return true;
    }

    /**
     * Check if villager is a guard
     */
    public static boolean isGuard(VillagerEntity villager) {
        return villager.getVillagerData().getProfession() == ModProfessions.GUARD;
    }
}