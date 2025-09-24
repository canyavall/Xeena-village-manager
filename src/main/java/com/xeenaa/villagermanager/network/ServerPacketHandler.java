package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
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
        XeenaaVillagerManager.LOGGER.info("Registering server-side packet handlers");

        ServerPlayNetworking.registerGlobalReceiver(SelectProfessionPacket.PACKET_ID, ServerPacketHandler::handleSelectProfession);
        ServerPlayNetworking.registerGlobalReceiver(PurchaseRankPacket.PACKET_ID, ServerPacketHandler::handlePurchaseRank);
    }

    /**
     * Handle profession selection packet from client
     */
    private static void handleSelectProfession(SelectProfessionPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        // Ensure we're running on the server thread
        player.getServer().execute(() -> {
            try {
                XeenaaVillagerManager.LOGGER.info("Processing profession selection from player: {}", player.getName().getString());

                // Find the villager entity
                ServerWorld world = (ServerWorld) player.getWorld();
                Entity entity = world.getEntityById(packet.villagerEntityId());

                if (!(entity instanceof VillagerEntity villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Entity {} is not a villager or not found", packet.villagerEntityId());
                    return;
                }

                // Validate the profession exists
                VillagerProfession profession = Registries.VILLAGER_PROFESSION.get(packet.professionId());
                if (profession == null) {
                    XeenaaVillagerManager.LOGGER.warn("Unknown profession: {}", packet.professionId());
                    return;
                }

                // Validate the player can interact with this villager
                if (!canPlayerChangeProfession(player, villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Player {} cannot change profession of villager {}",
                        player.getName().getString(), packet.villagerEntityId());
                    return;
                }

                // Change the villager's profession
                changeProfession(villager, profession);

                XeenaaVillagerManager.LOGGER.info("Successfully changed villager {} profession to {}",
                    packet.villagerEntityId(), packet.professionId());

            } catch (Exception e) {
                XeenaaVillagerManager.LOGGER.error("Error processing profession selection packet", e);
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
            XeenaaVillagerManager.LOGGER.debug("Cannot change profession of baby villager");
            return false;
        }

        // Check distance (prevent cheating with distant villagers)
        double distance = player.squaredDistanceTo(villager);
        if (distance > 64.0) { // 8 block radius
            XeenaaVillagerManager.LOGGER.debug("Player too far from villager: {} blocks", Math.sqrt(distance));
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
        XeenaaVillagerManager.LOGGER.info("Changing villager {} profession from {} to {}",
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

        // If the villager is now a guard, create guard data and sync to clients
        if (profession == ModProfessions.GUARD) {
            createAndSyncGuardData(villager);
        }
    }

    /**
     * Creates guard data for a new guard villager and syncs it to nearby clients
     */
    private static void createAndSyncGuardData(VillagerEntity villager) {
        ServerWorld world = (ServerWorld) villager.getWorld();
        GuardDataManager guardManager = GuardDataManager.get(world);

        // Create new guard data
        GuardData guardData = new GuardData(villager.getUuid());
        guardManager.updateGuardData(villager, guardData);

        // Save to villager NBT
        guardData.saveToVillager(villager, world.getRegistryManager());

        // Send sync packet to nearby clients
        GuardDataSyncPacket syncPacket = new GuardDataSyncPacket(
            villager.getUuid(),
            guardData.getRole()
        );

        world.getPlayers().forEach(player -> {
            if (player.squaredDistanceTo(villager) < 1024) { // 32 block radius
                ServerPlayNetworking.send(player, syncPacket);
            }
        });

        XeenaaVillagerManager.LOGGER.info("Created and synced guard data for villager {}", villager.getUuid());
    }

    /**
     * Handle rank purchase packet from client
     */
    private static void handlePurchaseRank(PurchaseRankPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        // Ensure we're running on the server thread
        player.getServer().execute(() -> {
            try {
                XeenaaVillagerManager.LOGGER.info("Processing rank purchase from player: {} for villager: {} to rank: {}",
                    player.getName().getString(), packet.villagerId(), packet.targetRank().getDisplayName());

                // Find the villager entity
                ServerWorld world = (ServerWorld) player.getWorld();
                Entity entity = null;

                // Search for villager by UUID in all loaded entities
                for (Entity e : world.iterateEntities()) {
                    if (e instanceof VillagerEntity && e.getUuid().equals(packet.villagerId())) {
                        entity = e;
                        break;
                    }
                }

                if (!(entity instanceof VillagerEntity villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Villager {} not found or not loaded", packet.villagerId());
                    return;
                }

                // Validate this is a guard villager
                if (villager.getVillagerData().getProfession() != ModProfessions.GUARD) {
                    XeenaaVillagerManager.LOGGER.warn("Villager {} is not a guard", packet.villagerId());
                    return;
                }

                // Validate the player can interact with this villager
                if (!canPlayerChangeProfession(player, villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Player {} cannot purchase rank for villager {}",
                        player.getName().getString(), packet.villagerId());
                    return;
                }

                // Get or create guard data
                GuardDataManager guardManager = GuardDataManager.get(world);
                GuardData guardData = guardManager.getGuardData(packet.villagerId());
                if (guardData == null) {
                    guardData = new GuardData(packet.villagerId());
                    guardManager.updateGuardData(villager, guardData);
                }

                GuardRankData rankData = guardData.getRankData();
                GuardRank targetRank = packet.targetRank();

                // Validate purchase requirements
                if (!rankData.canPurchaseRank(targetRank)) {
                    XeenaaVillagerManager.LOGGER.warn("Cannot purchase rank {} for villager {} - requirements not met",
                        targetRank.getDisplayName(), packet.villagerId());
                    return;
                }

                // Check player has enough emeralds
                int playerEmeralds = player.getInventory().count(Items.EMERALD);
                int cost = targetRank.getEmeraldCost();

                if (playerEmeralds < cost) {
                    XeenaaVillagerManager.LOGGER.warn("Player {} has insufficient emeralds ({} < {}) for rank {}",
                        player.getName().getString(), playerEmeralds, cost, targetRank.getDisplayName());
                    return;
                }

                // Deduct emeralds from player inventory
                for (int i = 0; i < cost; i++) {
                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(Items.EMERALD.getDefaultStack()), 1);
                }

                // Purchase the rank
                if (rankData.purchaseRank(targetRank, playerEmeralds)) {
                    // Apply rank stats to villager
                    applyRankStats(villager, targetRank);

                    // Save guard data
                    guardData.saveToVillager(villager, world.getRegistryManager());

                    // Send sync packet to all clients
                    GuardRankSyncPacket syncPacket = new GuardRankSyncPacket(
                        packet.villagerId(),
                        targetRank,
                        rankData.getTotalEmeraldsSpent()
                    );

                    // Send to all players in the area
                    world.getPlayers().forEach(p -> {
                        if (p.squaredDistanceTo(villager) < 1024) { // 32 block radius
                            ServerPlayNetworking.send(p, syncPacket);
                        }
                    });

                    XeenaaVillagerManager.LOGGER.info("Successfully purchased rank {} for villager {} (cost: {} emeralds)",
                        targetRank.getDisplayName(), packet.villagerId(), cost);
                } else {
                    XeenaaVillagerManager.LOGGER.error("Rank purchase failed for unknown reason - villager {}",
                        packet.villagerId());
                }

            } catch (Exception e) {
                XeenaaVillagerManager.LOGGER.error("Error processing rank purchase packet", e);
            }
        });
    }

    /**
     * Apply rank-based stats to a guard villager
     */
    private static void applyRankStats(VillagerEntity villager, GuardRank rank) {
        // Apply health
        EntityAttributeInstance healthAttribute = villager.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(rank.getHealth());
            villager.setHealth(rank.getHealth()); // Heal to new max health
        }

        // Apply attack damage
        EntityAttributeInstance attackAttribute = villager.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(rank.getAttackDamage());
        }

        XeenaaVillagerManager.LOGGER.debug("Applied rank {} stats to villager {}: HP={}, Attack={}",
            rank.getDisplayName(), villager.getUuid(), rank.getHealth(), rank.getAttackDamage());
    }

}