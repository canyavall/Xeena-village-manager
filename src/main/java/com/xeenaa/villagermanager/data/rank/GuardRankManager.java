package com.xeenaa.villagermanager.data.rank;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton manager for guard rank logic and validation.
 * Handles rank progression, emerald costs, and purchase validation.
 *
 * @since 2.0.0
 */
public class GuardRankManager {
    private static GuardRankManager INSTANCE;

    private final Map<UUID, GuardRankData> rankDataCache = new HashMap<>();

    private GuardRankManager() {
        XeenaaVillagerManager.LOGGER.info("GuardRankManager initialized with {} ranks defined",
                                         GuardRank.values().length);
    }

    public static GuardRankManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GuardRankManager();
        }
        return INSTANCE;
    }

    /**
     * Gets the total emerald cost to reach a specific rank from Recruit
     */
    public int getTotalCostToRank(GuardRank targetRank) {
        int totalCost = 0;
        GuardRank current = targetRank;

        while (current != GuardRank.RECRUIT) {
            totalCost += current.getEmeraldCost();
            current = current.getPreviousRank();
            if (current == null) break;
        }

        return totalCost;
    }

    /**
     * Checks if a player has enough emeralds for a rank purchase
     */
    public boolean canAffordRank(PlayerEntity player, GuardRank targetRank) {
        return countPlayerEmeralds(player) >= targetRank.getEmeraldCost();
    }

    /**
     * Counts total emeralds in player inventory (including blocks)
     */
    public int countPlayerEmeralds(PlayerEntity player) {
        int count = 0;

        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == Items.EMERALD) {
                count += stack.getCount();
            } else if (stack.getItem() == Items.EMERALD_BLOCK) {
                count += stack.getCount() * 9;
            }
        }

        return count;
    }

    /**
     * Deducts emeralds from player inventory for rank purchase
     * @return true if successful, false if insufficient funds
     */
    public boolean deductEmeralds(PlayerEntity player, int amount) {
        if (countPlayerEmeralds(player) < amount) {
            return false;
        }

        int remaining = amount;

        // First deduct from emerald stacks
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.getItem() == Items.EMERALD) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;

                if (remaining == 0) break;
            }
        }

        // Then convert emerald blocks if needed
        if (remaining > 0) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);

                if (stack.getItem() == Items.EMERALD_BLOCK) {
                    while (remaining > 0 && !stack.isEmpty()) {
                        stack.decrement(1);
                        int change = Math.min(9, remaining);
                        remaining -= change;

                        // Give back change if needed
                        if (change < 9) {
                            player.giveItemStack(new ItemStack(Items.EMERALD, 9 - change));
                        }
                    }

                    if (remaining == 0) break;
                }
            }
        }

        return true;
    }

    /**
     * Validates if a rank purchase is allowed
     */
    public PurchaseResult validatePurchase(VillagerEntity villager, GuardRank targetRank,
                                          GuardRank currentRank, PlayerEntity player) {
        // Check if already at max rank
        if (currentRank.getTier() >= 4) {
            return new PurchaseResult(false, Text.literal("Already at maximum rank"));
        }

        // Check sequential progression
        if (!targetRank.canPurchase(currentRank)) {
            return new PurchaseResult(false,
                Text.literal("Must purchase previous rank first"));
        }

        // Check emerald cost
        if (!canAffordRank(player, targetRank)) {
            int cost = targetRank.getEmeraldCost();
            int has = countPlayerEmeralds(player);
            return new PurchaseResult(false,
                Text.literal(String.format("Need %d emeralds (have %d)", cost, has)));
        }

        // Check path compatibility
        GuardPath currentPath = currentRank.getPath();
        GuardPath targetPath = targetRank.getPath();

        if (currentPath != GuardPath.RECRUIT && !targetPath.canTransitionFrom(currentPath)) {
            return new PurchaseResult(false,
                Text.literal("Cannot switch specialization paths"));
        }

        return new PurchaseResult(true, Text.literal("Purchase allowed"));
    }

    /**
     * Processes a rank purchase
     */
    public boolean purchaseRank(VillagerEntity villager, GuardRank newRank, PlayerEntity player) {
        // Deduct emeralds
        if (!deductEmeralds(player, newRank.getEmeraldCost())) {
            return false;
        }

        // Update rank data
        GuardRankData rankData = getRankData(villager.getUuid());
        rankData.setCurrentRank(newRank);
        rankData.addEmeraldsSpent(newRank.getEmeraldCost());

        XeenaaVillagerManager.LOGGER.info("Guard {} ranked up to {} by player {}",
                                         villager.getUuid(), newRank.getDisplayName(),
                                         player.getName().getString());

        return true;
    }

    /**
     * Gets or creates rank data for a villager
     */
    public GuardRankData getRankData(UUID villagerId) {
        return rankDataCache.computeIfAbsent(villagerId, GuardRankData::new);
    }

    /**
     * Result of a purchase validation check
     */
    public static record PurchaseResult(boolean allowed, Text reason) {}

    /**
     * Gets a formatted description of rank benefits
     */
    public Text getRankBenefitsText(GuardRank rank) {
        RankStats stats = rank.getStats();

        String benefits = String.format(
            "§6%s§r\nHealth: §a%.0f HP§r\nDamage: §c%.0f§r\n",
            rank.getDisplayName(),
            stats.getMaxHealth(),
            stats.getAttackDamage()
        );

        if (stats.isRanged()) {
            benefits += String.format("Bow Draw: §9%.1fs§r\n", stats.getBowDrawSpeed());
        }

        if (rank.hasSpecialAbility()) {
            benefits += "§5Special: " + rank.getSpecialAbility().getName() + "§r";
        }

        return Text.literal(benefits);
    }
}