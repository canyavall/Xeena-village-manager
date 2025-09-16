package com.xeenaa.villagepicker.network;

import com.xeenaa.villagepicker.XeenaaVillagePicker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

/**
 * Handles incoming network packets on the server side
 */
public class ServerPacketHandler {

    /**
     * Register all server-side packet handlers
     */
    public static void registerHandlers() {
        XeenaaVillagePicker.LOGGER.info("Registering server-side packet handlers");

        ServerPlayNetworking.registerGlobalReceiver(SelectProfessionPacket.PACKET_ID, ServerPacketHandler::handleSelectProfession);
    }

    /**
     * Handle profession selection packet from client
     */
    private static void handleSelectProfession(SelectProfessionPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        // Ensure we're running on the server thread
        player.getServer().execute(() -> {
            try {
                XeenaaVillagePicker.LOGGER.info("Processing profession selection from player: {}", player.getName().getString());

                // Find the villager entity
                ServerWorld world = (ServerWorld) player.getWorld();
                Entity entity = world.getEntityById(packet.villagerEntityId());

                if (!(entity instanceof VillagerEntity villager)) {
                    XeenaaVillagePicker.LOGGER.warn("Entity {} is not a villager or not found", packet.villagerEntityId());
                    return;
                }

                // Validate the profession exists
                VillagerProfession profession = Registries.VILLAGER_PROFESSION.get(packet.professionId());
                if (profession == null) {
                    XeenaaVillagePicker.LOGGER.warn("Unknown profession: {}", packet.professionId());
                    return;
                }

                // Validate the player can interact with this villager
                if (!canPlayerChangeProfession(player, villager)) {
                    XeenaaVillagePicker.LOGGER.warn("Player {} cannot change profession of villager {}",
                        player.getName().getString(), packet.villagerEntityId());
                    return;
                }

                // Change the villager's profession
                changeProfession(villager, profession);

                XeenaaVillagePicker.LOGGER.info("Successfully changed villager {} profession to {}",
                    packet.villagerEntityId(), packet.professionId());

            } catch (Exception e) {
                XeenaaVillagePicker.LOGGER.error("Error processing profession selection packet", e);
            }
        });
    }

    /**
     * Check if the player can change the profession of this villager
     */
    private static boolean canPlayerChangeProfession(ServerPlayerEntity player, VillagerEntity villager) {
        // Basic validation checks
        if (villager.isRemoved()) {
            return false;
        }

        // Check if villager is a baby (babies cannot have professions changed)
        if (villager.isBaby()) {
            XeenaaVillagePicker.LOGGER.debug("Cannot change profession of baby villager");
            return false;
        }

        // Check distance (prevent cheating with distant villagers)
        double distance = player.squaredDistanceTo(villager);
        if (distance > 64.0) { // 8 block radius
            XeenaaVillagePicker.LOGGER.debug("Player too far from villager: {} blocks", Math.sqrt(distance));
            return false;
        }

        // Additional permission checks can be added here
        // For now, allow all profession changes
        return true;
    }

    /**
     * Change a villager's profession
     */
    private static void changeProfession(VillagerEntity villager, VillagerProfession profession) {
        // Store the original profession and villager data for logging
        VillagerProfession originalProfession = villager.getVillagerData().getProfession();
        int originalLevel = villager.getVillagerData().getLevel();

        // Apply profession change with trade locking for persistence
        XeenaaVillagePicker.LOGGER.info("Changing villager {} profession from {} to {}",
            villager.getId(),
            Registries.VILLAGER_PROFESSION.getId(originalProfession),
            Registries.VILLAGER_PROFESSION.getId(profession));

        // Set the new profession
        villager.setVillagerData(villager.getVillagerData().withProfession(profession));

        // Lock profession by setting experience to master level (250 XP)
        // This prevents the profession from being reset by villager AI
        villager.setExperience(250);
        villager.setVillagerData(villager.getVillagerData().withLevel(5));

        // Reinitialize brain for normal AI behavior
        villager.reinitializeBrain((ServerWorld) villager.getWorld());
    }

}