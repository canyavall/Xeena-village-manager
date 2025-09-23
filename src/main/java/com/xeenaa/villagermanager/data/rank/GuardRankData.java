package com.xeenaa.villagermanager.data.rank;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Data class for storing guard rank information.
 * Handles rank progression, persistence, and validation.
 */
public class GuardRankData {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardRankData.class);
    private static final String NBT_VERSION_KEY = "version";
    private static final String NBT_RANK_KEY = "rank";
    private static final String NBT_EMERALDS_SPENT_KEY = "emeralds_spent";
    private static final int CURRENT_NBT_VERSION = 1;

    private final UUID villagerId;
    private GuardRank currentRank;
    private int totalEmeraldsSpent;

    public GuardRankData(UUID villagerId) {
        this.villagerId = villagerId;
        this.currentRank = GuardRank.RECRUIT; // Default starting rank
        this.totalEmeraldsSpent = 0;
    }

    public UUID getVillagerId() {
        return villagerId;
    }

    public GuardRank getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(GuardRank rank) {
        if (rank == null) {
            LOGGER.warn("Attempted to set null rank for villager {}, defaulting to RECRUIT", villagerId);
            this.currentRank = GuardRank.RECRUIT;
            return;
        }

        GuardRank previousRank = this.currentRank;
        this.currentRank = rank;

        LOGGER.debug("Rank changed for villager {}: {} -> {}",
                    villagerId, previousRank.getDisplayName(), rank.getDisplayName());
    }

    public int getTotalEmeraldsSpent() {
        return totalEmeraldsSpent;
    }

    /**
     * Attempts to purchase the specified rank.
     *
     * @param targetRank the rank to purchase
     * @param playerEmeralds the number of emeralds the player has
     * @return true if purchase was successful, false otherwise
     */
    public boolean purchaseRank(GuardRank targetRank, int playerEmeralds) {
        // Validate purchase requirements
        if (!canPurchaseRank(targetRank)) {
            LOGGER.debug("Cannot purchase rank {} for villager {}: requirements not met",
                        targetRank.getDisplayName(), villagerId);
            return false;
        }

        int cost = targetRank.getEmeraldCost();
        if (playerEmeralds < cost) {
            LOGGER.debug("Cannot purchase rank {} for villager {}: insufficient emeralds ({} < {})",
                        targetRank.getDisplayName(), villagerId, playerEmeralds, cost);
            return false;
        }

        // Purchase successful
        setCurrentRank(targetRank);
        totalEmeraldsSpent += cost;

        LOGGER.info("Rank purchased for villager {}: {} (cost: {} emeralds, total spent: {})",
                   villagerId, targetRank.getDisplayName(), cost, totalEmeraldsSpent);
        return true;
    }

    /**
     * Checks if a rank can be purchased.
     *
     * @param targetRank the rank to check
     * @return true if the rank can be purchased
     */
    public boolean canPurchaseRank(GuardRank targetRank) {
        if (targetRank == null) return false;
        if (targetRank == currentRank) return false; // Already have this rank
        if (targetRank == GuardRank.RECRUIT) return false; // Cannot "purchase" recruit rank

        // Check if previous rank requirement is met
        return targetRank.canPurchase(currentRank);
    }

    /**
     * Gets available upgrade options from current rank.
     *
     * @return array of ranks that can be purchased next
     */
    public GuardRank[] getAvailableUpgrades() {
        return GuardRank.getAvailableUpgrades(currentRank);
    }

    /**
     * Gets the specialization path of the current rank.
     */
    public GuardPath getCurrentPath() {
        return currentRank.getPath();
    }

    /**
     * Gets the tier level of the current rank.
     */
    public int getCurrentTier() {
        return currentRank.getTier();
    }

    /**
     * Checks if the guard is at maximum rank for their path.
     */
    public boolean isMaxRank() {
        return getCurrentTier() >= 4;
    }

    /**
     * Gets display information about current rank and progression.
     */
    public String getRankProgressionInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Rank: ").append(currentRank.getDisplayName());
        info.append(" (").append(getCurrentPath().getDisplayName()).append(")");
        info.append(" - Tier ").append(getCurrentTier()).append("/4");

        if (totalEmeraldsSpent > 0) {
            info.append(" - ").append(totalEmeraldsSpent).append(" emeralds invested");
        }

        return info.toString();
    }

    /**
     * Serializes rank data to NBT.
     */
    public NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(NBT_VERSION_KEY, CURRENT_NBT_VERSION);
        nbt.putString(NBT_RANK_KEY, currentRank.getId());
        nbt.putInt(NBT_EMERALDS_SPENT_KEY, totalEmeraldsSpent);
        return nbt;
    }

    /**
     * Deserializes rank data from NBT.
     */
    public void readFromNbt(NbtCompound nbt) {
        try {
            int version = nbt.getInt(NBT_VERSION_KEY);

            if (version > CURRENT_NBT_VERSION) {
                LOGGER.warn("Loading rank data with newer version {} for villager {}, current version is {}",
                           version, villagerId, CURRENT_NBT_VERSION);
            }

            // Load rank
            if (nbt.contains(NBT_RANK_KEY, NbtElement.STRING_TYPE)) {
                String rankId = nbt.getString(NBT_RANK_KEY);
                this.currentRank = GuardRank.fromId(rankId);
                LOGGER.debug("Loaded rank {} for villager {}", currentRank.getDisplayName(), villagerId);
            } else {
                LOGGER.debug("No rank data found for villager {}, using default RECRUIT", villagerId);
                this.currentRank = GuardRank.RECRUIT;
            }

            // Load emeralds spent
            if (nbt.contains(NBT_EMERALDS_SPENT_KEY, NbtElement.INT_TYPE)) {
                this.totalEmeraldsSpent = nbt.getInt(NBT_EMERALDS_SPENT_KEY);
            } else {
                this.totalEmeraldsSpent = 0;
            }

            LOGGER.debug("Loaded rank data for villager {}: rank={}, emeralds_spent={}",
                        villagerId, currentRank.getDisplayName(), totalEmeraldsSpent);

        } catch (Exception e) {
            LOGGER.error("Failed to load rank data for villager {}, using defaults: {}", villagerId, e.getMessage());
            this.currentRank = GuardRank.RECRUIT;
            this.totalEmeraldsSpent = 0;
        }
    }

    /**
     * Creates a deep copy of this rank data.
     */
    public GuardRankData copy() {
        GuardRankData copy = new GuardRankData(this.villagerId);
        copy.currentRank = this.currentRank;
        copy.totalEmeraldsSpent = this.totalEmeraldsSpent;
        return copy;
    }

    @Override
    public String toString() {
        return String.format("GuardRankData{villager=%s, rank=%s, tier=%d, emeralds=%d}",
                           villagerId, currentRank.getDisplayName(), getCurrentTier(), totalEmeraldsSpent);
    }
}