package com.xeenaa.villagepicker.client.util;

import com.xeenaa.villagepicker.XeenaaVillagePicker;
import com.xeenaa.villagepicker.client.gui.ProfessionSelectionScreen;
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
            XeenaaVillagePicker.LOGGER.warn("Invalid player for villager interaction");
            return ActionResult.PASS;
        }

        // Villager validation
        if (villager == null || !villager.isAlive()) {
            XeenaaVillagePicker.LOGGER.warn("Invalid villager for interaction");
            return ActionResult.PASS;
        }

        XeenaaVillagePicker.LOGGER.info("Processing valid villager interaction (shift + right-click) - Player: {}, Villager: {}",
            player.getName().getString(), villager.getClass().getSimpleName());

        // Check permissions and villager eligibility
        if (!hasPermission(player) || !canChangeProfession(villager)) {
            XeenaaVillagePicker.LOGGER.info("Interaction not allowed - Permission: {}, Can change: {}",
                hasPermission(player), canChangeProfession(villager));
            return ActionResult.PASS;
        }

        // Open profession selection GUI
        MinecraftClient client = MinecraftClient.getInstance();
        ProfessionSelectionScreen screen = new ProfessionSelectionScreen(villager);
        client.setScreen(screen);

        XeenaaVillagePicker.LOGGER.info("Opened ProfessionSelectionScreen for villager (shift + right-click)");

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
}