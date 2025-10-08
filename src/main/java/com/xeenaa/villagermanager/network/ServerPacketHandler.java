package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.data.rank.RankStats;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
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
        ServerPlayNetworking.registerGlobalReceiver(GuardProfessionChangePacket.PACKET_ID, ServerPacketHandler::handleGuardProfessionChange);
        ServerPlayNetworking.registerGlobalReceiver(GuardConfigPacket.PACKET_ID, ServerPacketHandler::handleGuardConfig);
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

                // Check if profession is Guard and villager is a baby
                if (profession == ModProfessions.GUARD && villager.isBaby()) {
                    XeenaaVillagerManager.LOGGER.info("Cannot assign Guard profession to baby villager {}",
                        villager.getId());
                    player.sendMessage(
                        net.minecraft.text.Text.literal("Baby villagers cannot become guards. Wait for the villager to grow up."),
                        false
                    );
                    return;
                }

                // Check if profession is locked (for guards)
                if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
                    GuardDataManager guardManager = GuardDataManager.get(world);
                    GuardData guardData = guardManager.getGuardData(villager.getUuid());

                    if (guardData != null && guardData.getBehaviorConfig().professionLocked()) {
                        XeenaaVillagerManager.LOGGER.info("Profession change blocked - villager {} profession is locked",
                            villager.getId());
                        player.sendMessage(
                            net.minecraft.text.Text.literal("This guard's profession is locked. Unlock it in the Config tab first."),
                            false
                        );
                        return;
                    }
                }

                // Check if villager is currently a guard and requires warning dialog
                if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
                    handleGuardProfessionChangeWarning(player, villager, profession, packet.professionId());
                } else {
                    // Change the villager's profession directly for non-guard professions
                    changeProfession(villager, profession);

                    XeenaaVillagerManager.LOGGER.info("Successfully changed villager {} profession to {}",
                        packet.villagerEntityId(), packet.professionId());
                }

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
            // Clear HOME memory to release any claimed bed
            // Guards don't sleep, so they shouldn't keep beds claimed
            villager.getBrain().forget(MemoryModuleType.HOME);
            XeenaaVillagerManager.LOGGER.debug("Cleared HOME memory for new guard {} - bed released",
                villager.getUuid());

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

        // Set initial display name (Recruit rank)
        guardData.updateDisplayName(villager);

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
                    ServerPlayNetworking.send(player, RankPurchaseResponsePacket.failure(
                        packet.villagerId(), "Villager is not a guard"));
                    return;
                }

                // Validate the player can interact with this villager
                if (!canPlayerChangeProfession(player, villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Player {} cannot purchase rank for villager {}",
                        player.getName().getString(), packet.villagerId());
                    ServerPlayNetworking.send(player, RankPurchaseResponsePacket.failure(
                        packet.villagerId(), "Cannot interact with this villager"));
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

                // Validate purchase requirements (includes path validation)
                if (!rankData.canPurchaseRank(targetRank)) {
                    String reason = getRankPurchaseFailureReason(rankData, targetRank);
                    XeenaaVillagerManager.LOGGER.warn("Cannot purchase rank {} for villager {} - {}",
                        targetRank.getDisplayName(), packet.villagerId(), reason);
                    ServerPlayNetworking.send(player, RankPurchaseResponsePacket.failure(
                        packet.villagerId(), reason));
                    return;
                }

                // Check player has enough emeralds
                int playerEmeralds = player.getInventory().count(Items.EMERALD);
                int cost = targetRank.getEmeraldCost();

                if (playerEmeralds < cost) {
                    XeenaaVillagerManager.LOGGER.warn("Player {} has insufficient emeralds ({} < {}) for rank {}",
                        player.getName().getString(), playerEmeralds, cost, targetRank.getDisplayName());
                    ServerPlayNetworking.send(player, RankPurchaseResponsePacket.failure(
                        packet.villagerId(), String.format("Insufficient emeralds: need %d, have %d", cost, playerEmeralds)));
                    return;
                }

                // Deduct emeralds from player inventory
                for (int i = 0; i < cost; i++) {
                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(Items.EMERALD.getDefaultStack()), 1);
                }

                // Purchase the rank
                if (rankData.purchaseRank(targetRank, playerEmeralds)) {
                    // Update display name to show new rank
                    guardData.updateDisplayName(villager);

                    // Save guard data
                    guardData.saveToVillager(villager, world.getRegistryManager());

                    // Apply new rank attributes to guard villager
                    applyRankAttributesToGuard(villager, targetRank);

                    // Re-initialize combat AI goals if path has changed
                    reinitializeCombatGoals(villager, guardData);

                    // Re-equip weapon based on new rank/path (clear mainhand to trigger immediate re-equipment)
                    villager.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, net.minecraft.item.ItemStack.EMPTY);

                    // Send sync packet to all clients
                    GuardRankSyncPacket syncPacket = new GuardRankSyncPacket(
                        packet.villagerId(),
                        targetRank,
                        rankData.getTotalEmeraldsSpent(),
                        rankData.getChosenPath()
                    );

                    // Send to all players in the area
                    world.getPlayers().forEach(p -> {
                        if (p.squaredDistanceTo(villager) < 1024) { // 32 block radius
                            ServerPlayNetworking.send(p, syncPacket);
                        }
                    });

                    // Send success response to client
                    ServerPlayNetworking.send(player, RankPurchaseResponsePacket.success(
                        packet.villagerId(), String.format("Rank upgraded to %s!", targetRank.getDisplayName())));

                    XeenaaVillagerManager.LOGGER.info("Successfully purchased rank {} for villager {} (cost: {} emeralds)",
                        targetRank.getDisplayName(), packet.villagerId(), cost);
                } else {
                    // Send failure response to client
                    ServerPlayNetworking.send(player, RankPurchaseResponsePacket.failure(
                        packet.villagerId(), "Rank purchase failed"));

                    XeenaaVillagerManager.LOGGER.error("Rank purchase failed for unknown reason - villager {}",
                        packet.villagerId());
                }

            } catch (Exception e) {
                XeenaaVillagerManager.LOGGER.error("Error processing rank purchase packet", e);
                // Send error response to client
                ServerPlayNetworking.send(player, RankPurchaseResponsePacket.failure(
                    packet.villagerId(), "An error occurred during rank purchase"));
            }
        });
    }

    /**
     * Gets a detailed reason why a rank purchase failed
     */
    private static String getRankPurchaseFailureReason(GuardRankData rankData, GuardRank targetRank) {
        if (targetRank == null) {
            return "Invalid rank";
        }

        if (targetRank == rankData.getCurrentRank()) {
            return "Already have this rank";
        }

        if (targetRank == GuardRank.RECRUIT) {
            return "Cannot purchase recruit rank";
        }

        // Check path restrictions
        GuardPath targetPath = targetRank.getPath();
        GuardPath chosenPath = rankData.getChosenPath();

        if (chosenPath != null && targetPath != chosenPath) {
            return String.format("Path locked: Guard specialized as %s, cannot purchase %s ranks",
                chosenPath.getDisplayName(), targetPath.getDisplayName());
        }

        // Check if previous rank requirement is met
        if (!targetRank.canPurchase(rankData.getCurrentRank())) {
            GuardRank requiredPrevious = targetRank.getPreviousRank();
            return String.format("Must have rank %s before purchasing %s",
                requiredPrevious != null ? requiredPrevious.getDisplayName() : "Unknown",
                targetRank.getDisplayName());
        }

        return "Requirements not met";
    }


    /**
     * Handle guard profession change warning - sends emerald loss warning to client for confirmation
     */
    private static void handleGuardProfessionChangeWarning(ServerPlayerEntity player, VillagerEntity villager,
                                                          VillagerProfession newProfession, Identifier newProfessionId) {
        XeenaaVillagerManager.LOGGER.info("Guard profession change requested for villager {} - warning about emerald loss",
            villager.getId());

        ServerWorld world = (ServerWorld) villager.getWorld();
        GuardDataManager guardManager = GuardDataManager.get(world);
        GuardData guardData = guardManager.getGuardData(villager.getUuid());

        int emeraldsToLose = 0;
        String currentRankName = "Recruit";

        if (guardData != null) {
            GuardRankData rankData = guardData.getRankData();
            emeraldsToLose = rankData.getTotalEmeraldsSpent();
            currentRankName = rankData.getCurrentRank().getDisplayName();
        }

        // Send emerald loss warning packet to client for confirmation dialog
        GuardEmeraldRefundPacket lossWarningPacket = new GuardEmeraldRefundPacket(
            villager.getId(),
            villager.getUuid(),
            newProfessionId,
            emeraldsToLose,
            currentRankName
        );

        ServerPlayNetworking.send(player, lossWarningPacket);

        XeenaaVillagerManager.LOGGER.info("Sent guard profession change warning to player {} - {} emeralds will be lost",
            player.getName().getString(), emeraldsToLose);
    }

    /**
     * Handle confirmed guard profession change packet from client
     */
    private static void handleGuardProfessionChange(GuardProfessionChangePacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        // Ensure we're running on the server thread
        player.getServer().execute(() -> {
            try {
                XeenaaVillagerManager.LOGGER.info("Processing confirmed guard profession change from player: {}",
                    player.getName().getString());

                // Find the villager entity
                ServerWorld world = (ServerWorld) player.getWorld();
                Entity entity = world.getEntityById(packet.villagerEntityId());

                if (!(entity instanceof VillagerEntity villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Entity {} is not a villager or not found", packet.villagerEntityId());
                    return;
                }

                // Validate the profession exists
                VillagerProfession profession = Registries.VILLAGER_PROFESSION.get(packet.newProfessionId());
                if (profession == null) {
                    XeenaaVillagerManager.LOGGER.warn("Unknown profession: {}", packet.newProfessionId());
                    return;
                }

                // Validate the player can interact with this villager
                if (!canPlayerChangeProfession(player, villager)) {
                    XeenaaVillagerManager.LOGGER.warn("Player {} cannot change profession of villager {}",
                        player.getName().getString(), packet.villagerEntityId());
                    return;
                }

                // Validate villager is still a guard
                if (villager.getVillagerData().getProfession() != ModProfessions.GUARD) {
                    XeenaaVillagerManager.LOGGER.warn("Villager {} is no longer a guard, ignoring profession change",
                        packet.villagerEntityId());
                    return;
                }

                if (packet.confirmed()) {
                    // Process emerald loss and guard data cleanup
                    processGuardProfessionChangeWithEmeraldLoss(player, villager, profession);

                    XeenaaVillagerManager.LOGGER.info("Successfully processed guard profession change for villager {} to {}",
                        packet.villagerEntityId(), packet.newProfessionId());
                } else {
                    XeenaaVillagerManager.LOGGER.info("Guard profession change cancelled by player for villager {}",
                        packet.villagerEntityId());
                }

            } catch (Exception e) {
                XeenaaVillagerManager.LOGGER.error("Error processing guard profession change packet", e);
            }
        });
    }

    /**
     * Process guard profession change with emerald loss and data cleanup
     */
    private static void processGuardProfessionChangeWithEmeraldLoss(ServerPlayerEntity player, VillagerEntity villager,
                                                                   VillagerProfession newProfession) {
        ServerWorld world = (ServerWorld) villager.getWorld();
        GuardDataManager guardManager = GuardDataManager.get(world);
        GuardData guardData = guardManager.getGuardData(villager.getUuid());

        int emeraldsLost = 0;

        // Calculate emeralds that will be lost (no refund)
        if (guardData != null) {
            GuardRankData rankData = guardData.getRankData();
            emeraldsLost = rankData.getTotalEmeraldsSpent();

            if (emeraldsLost > 0) {
                XeenaaVillagerManager.LOGGER.info("Player {} lost {} emeralds as penalty for guard profession change",
                    player.getName().getString(), emeraldsLost);
            }
        }

        // Remove guard data from manager (cleanup)
        guardManager.removeGuardData(villager.getUuid());

        // Change the villager's profession
        changeProfession(villager, newProfession);

        XeenaaVillagerManager.LOGGER.info("Completed guard profession change: villager {} changed to {}, {} emeralds lost as penalty, guard data cleaned up",
            villager.getId(), Registries.VILLAGER_PROFESSION.getId(newProfession), emeraldsLost);
    }

    /**
     * Applies rank-based attributes to a guard villager using RankStats.
     * This ensures combat effectiveness scales with rank progression.
     */
    private static void applyRankAttributesToGuard(VillagerEntity villager, GuardRank rank) {
        com.xeenaa.villagermanager.data.rank.RankStats stats = rank.getStats();

        // Apply health from RankStats
        net.minecraft.entity.attribute.EntityAttributeInstance healthAttribute =
            villager.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            double oldMaxHealth = healthAttribute.getValue();
            healthAttribute.setBaseValue(stats.getMaxHealth());

            // Adjust current health proportionally to prevent death/overheal
            float currentHealth = villager.getHealth();
            if (currentHealth > stats.getMaxHealth()) {
                // Cap health if new max is lower
                villager.setHealth((float) stats.getMaxHealth());
            } else if (oldMaxHealth > 0) {
                // Scale health proportionally if gaining max health
                float healthRatio = currentHealth / (float) oldMaxHealth;
                villager.setHealth((float) (stats.getMaxHealth() * healthRatio));
            }
        }

        // Apply movement speed from RankStats
        net.minecraft.entity.attribute.EntityAttributeInstance speedAttribute =
            villager.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(stats.getMovementSpeed());
        }

        // Apply attack damage from RankStats
        net.minecraft.entity.attribute.EntityAttributeInstance attackAttribute =
            villager.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(stats.getAttackDamage());
        }

        // Apply knockback resistance from RankStats
        net.minecraft.entity.attribute.EntityAttributeInstance knockbackAttribute =
            villager.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            knockbackAttribute.setBaseValue(stats.getKnockbackResistance());
        }

        // Apply armor value from RankStats
        net.minecraft.entity.attribute.EntityAttributeInstance armorAttribute =
            villager.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR);
        if (armorAttribute != null) {
            armorAttribute.setBaseValue(stats.getArmorValue());
        }

        // Apply attack speed from RankStats
        net.minecraft.entity.attribute.EntityAttributeInstance attackSpeedAttribute =
            villager.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttribute != null) {
            attackSpeedAttribute.setBaseValue(stats.getAttackSpeed());
        }

        XeenaaVillagerManager.LOGGER.info("Applied rank attributes to guard {} - Rank: {}, HP: {}, DMG: {}, SPD: {}, Armor: {}",
            villager.getUuid(), rank.getDisplayName(), stats.getMaxHealth(), stats.getAttackDamage(),
            stats.getMovementSpeed(), stats.getArmorValue());
    }

    /**
     * Re-initializes combat AI goals when guard specialization path changes
     */
    private static void reinitializeCombatGoals(VillagerEntity villager, GuardData guardData) {
        GuardRankData rankData = guardData.getRankData();
        String pathId = rankData.getChosenPath() != null ?
            rankData.getChosenPath().getId() : rankData.getCurrentRank().getPath().getId();
        boolean isRangedSpecialization = pathId.equals("ranged");

        XeenaaVillagerManager.LOGGER.info("Re-initializing combat goals for guard {} - Path: {}, Is Marksman: {}",
            villager.getUuid(), pathId, isRangedSpecialization);

        // Call the accessor method through the interface
        ((com.xeenaa.villagermanager.ai.GuardAIAccessor)villager).xeenaa$reinitializeCombatGoals(isRangedSpecialization);
    }

    /**
     * Handle guard configuration update packet from client
     */
    private static void handleGuardConfig(GuardConfigPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        // Ensure we're running on the server thread
        player.getServer().execute(() -> {
            try {
                XeenaaVillagerManager.LOGGER.info("Processing guard config update from player: {} for villager: {}",
                    player.getName().getString(), packet.villagerId());

                // Validate packet data
                if (!packet.isValid()) {
                    XeenaaVillagerManager.LOGGER.warn("Invalid guard config packet from {}",
                        player.getName().getString());
                    return;
                }

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
                    XeenaaVillagerManager.LOGGER.warn("Player {} cannot configure villager {}",
                        player.getName().getString(), packet.villagerId());
                    return;
                }

                // Get or create guard data
                GuardDataManager guardManager = GuardDataManager.get(world);
                GuardData guardData = guardManager.getGuardData(packet.villagerId());
                if (guardData == null) {
                    XeenaaVillagerManager.LOGGER.warn("Guard data not found for villager {}", packet.villagerId());
                    return;
                }

                // Update configuration
                guardData.setBehaviorConfig(packet.config());

                // Save guard data
                guardData.saveToVillager(villager, world.getRegistryManager());

                // NOTE: No need to reinitialize brain - AI goals check guardData.getBehaviorConfig() every tick
                // villager.reinitializeBrain(world); // REMOVED: This destroys guard AI goals!

                // Send sync packet to all clients
                GuardConfigSyncPacket syncPacket = new GuardConfigSyncPacket(
                    packet.villagerId(),
                    packet.config()
                );

                // Send to all players in the area
                world.getPlayers().forEach(p -> {
                    if (p.squaredDistanceTo(villager) < 1024) { // 32 block radius
                        ServerPlayNetworking.send(p, syncPacket);
                    }
                });

                XeenaaVillagerManager.LOGGER.info("Successfully updated guard config for villager {} - Detection: {}, GuardMode: {}, ProfessionLocked: {}, FollowTarget: {}",
                    packet.villagerId(),
                    packet.config().detectionRange(),
                    packet.config().guardMode().getDisplayName(),
                    packet.config().professionLocked(),
                    packet.config().followTargetPlayerId());

            } catch (Exception e) {
                XeenaaVillagerManager.LOGGER.error("Error processing guard config packet", e);
            }
        });
    }

}