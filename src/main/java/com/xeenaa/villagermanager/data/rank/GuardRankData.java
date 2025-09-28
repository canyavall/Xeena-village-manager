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
    private static final String NBT_CHOSEN_PATH_KEY = "chosen_path";
    private static final int CURRENT_NBT_VERSION = 2;

    private final UUID villagerId;
    private GuardRank currentRank;
    private int totalEmeraldsSpent;
    private GuardPath chosenPath;

    public GuardRankData(UUID villagerId) {
        this.villagerId = villagerId;
        this.currentRank = GuardRank.RECRUIT; // Default starting rank
        this.totalEmeraldsSpent = 0;
        this.chosenPath = null; // No path chosen initially
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
     * Adds to the total emeralds spent counter
     */
    public void addEmeraldsSpent(int amount) {
        this.totalEmeraldsSpent += amount;
    }

    /**
     * Sets the total emeralds spent (used for client synchronization)
     */
    public void setTotalEmeraldsSpent(int amount) {
        this.totalEmeraldsSpent = amount;
        LOGGER.debug("Set total emeralds spent for villager {}: {}", villagerId, amount);
    }

    /**
     * Sets the chosen specialization path (used for client synchronization)
     */
    public void setChosenPath(GuardPath path) {
        this.chosenPath = path;
        LOGGER.debug("Set chosen path for villager {}: {}", villagerId,
                    path != null ? path.getDisplayName() : "none");
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

        // Set chosen path if this is first specialization purchase
        if (chosenPath == null && targetRank.getPath() != GuardPath.RECRUIT) {
            chosenPath = targetRank.getPath();
            LOGGER.info("Guard {} chose specialization path: {}", villagerId, chosenPath.getDisplayName());
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

        // Check path restrictions
        GuardPath targetPath = targetRank.getPath();

        // If no path is chosen yet, can choose any specialization path
        if (chosenPath == null) {
            // Can only choose first tier specializations from recruit
            if (currentRank == GuardRank.RECRUIT) {
                return targetRank == GuardRank.MAN_AT_ARMS_I || targetRank == GuardRank.MARKSMAN_I;
            }
            return false; // Should not happen - path should be set after first specialization
        }

        // If path is already chosen, can only progress within that path
        if (targetPath != chosenPath) {
            LOGGER.debug("Cannot purchase rank {} for villager {}: wrong path (chosen: {}, target: {})",
                        targetRank.getDisplayName(), villagerId, chosenPath.getDisplayName(), targetPath.getDisplayName());
            return false;
        }

        // Check if previous rank requirement is met
        return targetRank.canPurchase(currentRank);
    }

    /**
     * Gets available upgrade options from current rank.
     *
     * @return array of ranks that can be purchased next
     */
    public GuardRank[] getAvailableUpgrades() {
        GuardRank[] allUpgrades = GuardRank.getAvailableUpgrades(currentRank);

        // If no path chosen yet, return all options
        if (chosenPath == null) {
            return allUpgrades;
        }

        // Filter upgrades to only include those in the chosen path
        return java.util.Arrays.stream(allUpgrades)
            .filter(rank -> rank.getPath() == chosenPath)
            .toArray(GuardRank[]::new);
    }

    /**
     * Gets the specialization path of the current rank.
     */
    public GuardPath getCurrentPath() {
        return currentRank.getPath();
    }

    /**
     * Gets the chosen specialization path (may be null if no path chosen yet).
     */
    public GuardPath getChosenPath() {
        return chosenPath;
    }

    /**
     * Checks if a specialization path has been chosen.
     */
    public boolean hasChosenPath() {
        return chosenPath != null;
    }

    /**
     * Checks if the given path is available for selection.
     */
    public boolean isPathAvailable(GuardPath path) {
        if (path == GuardPath.RECRUIT) {
            return false; // Cannot choose recruit as specialization
        }

        // If no path chosen, all paths are available
        if (chosenPath == null) {
            return currentRank == GuardRank.RECRUIT;
        }

        // If path is chosen, only the chosen path is available
        return path == chosenPath;
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
        if (chosenPath != null) {
            nbt.putString(NBT_CHOSEN_PATH_KEY, chosenPath.getId());
        }
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

            // Load chosen path (version 2+ feature)
            if (nbt.contains(NBT_CHOSEN_PATH_KEY, NbtElement.STRING_TYPE)) {
                String pathId = nbt.getString(NBT_CHOSEN_PATH_KEY);
                this.chosenPath = GuardPath.fromId(pathId);
                LOGGER.debug("Loaded chosen path {} for villager {}", chosenPath.getDisplayName(), villagerId);
            } else {
                // Backward compatibility: infer path from current rank for version 1 data
                if (version < 2 && currentRank != GuardRank.RECRUIT) {
                    this.chosenPath = currentRank.getPath();
                    LOGGER.info("Inferred chosen path {} from current rank for villager {} (version 1 data)",
                               chosenPath.getDisplayName(), villagerId);
                } else {
                    this.chosenPath = null;
                }
            }

            // CRITICAL: Validate and fix path consistency for existing data
            validateAndFixPathConsistency();

            LOGGER.debug("Loaded rank data for villager {}: rank={}, emeralds_spent={}, chosen_path={}",
                        villagerId, currentRank.getDisplayName(), totalEmeraldsSpent,
                        chosenPath != null ? chosenPath.getDisplayName() : "none");

        } catch (Exception e) {
            LOGGER.error("Failed to load rank data for villager {}, using defaults: {}", villagerId, e.getMessage());
            this.currentRank = GuardRank.RECRUIT;
            this.totalEmeraldsSpent = 0;
            this.chosenPath = null;
        }
    }

    /**
     * Validates and fixes path consistency for existing data.
     * This method handles edge cases where guards might have inconsistent path data.
     */
    private void validateAndFixPathConsistency() {
        // If guard is at recruit rank, chosen path should be null
        if (currentRank == GuardRank.RECRUIT) {
            if (chosenPath != null && chosenPath != GuardPath.RECRUIT) {
                LOGGER.warn("Fixed inconsistency: Recruit guard {} had chosen path {}, clearing it",
                           villagerId, chosenPath.getDisplayName());
                chosenPath = null;
            }
            return;
        }

        // If guard has a specialized rank but no chosen path, infer it
        if (chosenPath == null && currentRank != GuardRank.RECRUIT) {
            chosenPath = currentRank.getPath();
            LOGGER.info("Fixed missing path: Inferred path {} for guard {} with rank {}",
                       chosenPath.getDisplayName(), villagerId, currentRank.getDisplayName());
            return;
        }

        // If guard has a chosen path that doesn't match their current rank path, fix it
        if (chosenPath != null && currentRank != GuardRank.RECRUIT) {
            GuardPath currentRankPath = currentRank.getPath();
            if (chosenPath != currentRankPath) {
                LOGGER.warn("Fixed path inconsistency: Guard {} had chosen path {} but current rank {} is from path {}, fixing",
                           villagerId, chosenPath.getDisplayName(), currentRank.getDisplayName(), currentRankPath.getDisplayName());

                // Prefer to keep the current rank and fix the chosen path
                // This preserves the guard's actual progression
                chosenPath = currentRankPath;
            }
        }
    }

    /**
     * Creates a deep copy of this rank data.
     */
    public GuardRankData copy() {
        GuardRankData copy = new GuardRankData(this.villagerId);
        copy.currentRank = this.currentRank;
        copy.totalEmeraldsSpent = this.totalEmeraldsSpent;
        copy.chosenPath = this.chosenPath;
        return copy;
    }

    @Override
    public String toString() {
        return String.format("GuardRankData{villager=%s, rank=%s, tier=%d, emeralds=%d, path=%s}",
                           villagerId, currentRank.getDisplayName(), getCurrentTier(), totalEmeraldsSpent,
                           chosenPath != null ? chosenPath.getDisplayName() : "none");
    }
}