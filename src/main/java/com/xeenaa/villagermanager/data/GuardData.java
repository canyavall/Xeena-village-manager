package com.xeenaa.villagermanager.data;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

/**
 * Manages persistent data for Guard villagers including roles and ranking progression.
 *
 * <p>This class handles NBT serialization/deserialization of guard-specific data:</p>
 * <ul>
 *   <li>Guard roles (AI behavior patterns: Patrol, Guard, Follow)</li>
 *   <li>Rank progression data (stats, abilities, emerald costs)</li>
 *   <li>Legacy equipment data compatibility (ignored if present)</li>
 * </ul>
 *
 * <p>The system maintains clean separation between behavioral roles and combat ranking.</p>
 *
 * @since 1.0.0
 */
public class GuardData {
    private static final String NBT_KEY_ROLE = "GuardRole";
    private static final String NBT_KEY_RANK_DATA = "GuardRankData";
    private static final String NBT_KEY_GUARD_DATA = "XeenaaGuardData";
    private static final String NBT_KEY_VERSION = "DataVersion";
    private static final String NBT_KEY_BEHAVIOR_CONFIG = "BehaviorConfig";
    private static final int CURRENT_VERSION = 4; // Incremented for behavior configuration addition

    private final UUID villagerId;
    private GuardRole currentRole;
    private GuardRankData rankData;
    private com.xeenaa.villagermanager.config.GuardBehaviorConfig behaviorConfig;


    /**
     * Guard role types for AI behavior
     */
    public enum GuardRole {
        PATROL("Patrol"),
        GUARD("Guard"),
        FOLLOW("Follow");

        private final String name;

        GuardRole(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static GuardRole fromString(String name) {
            for (GuardRole role : values()) {
                if (role.name.equalsIgnoreCase(name)) {
                    return role;
                }
            }
            return GUARD; // Default
        }
    }

    /**
     * Creates new guard data for a villager
     */
    public GuardData(UUID villagerId) {
        this.villagerId = villagerId;
        this.currentRole = GuardRole.GUARD; // Default role
        this.rankData = new GuardRankData(villagerId); // Initialize rank data
        this.behaviorConfig = loadDefaultBehaviorConfig(); // Load default behavior from config
    }

    /**
     * Loads default behavior configuration from ModConfig.
     * Note: ModConfig may still have old fields, but we use the new GuardBehaviorConfig.DEFAULT.
     */
    private static com.xeenaa.villagermanager.config.GuardBehaviorConfig loadDefaultBehaviorConfig() {
        // Always use the built-in default configuration
        // ModConfig will be updated separately if needed
        return com.xeenaa.villagermanager.config.GuardBehaviorConfig.DEFAULT;
    }

    /**
     * Loads guard data from a villager's NBT
     */
    public static GuardData fromNbt(VillagerEntity villager, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        UUID villagerId = villager.getUuid();
        GuardData data = new GuardData(villagerId);

        NbtCompound villagerNbt = new NbtCompound();
        villager.writeCustomDataToNbt(villagerNbt);

        if (villagerNbt.contains(NBT_KEY_GUARD_DATA)) {
            NbtCompound guardNbt = villagerNbt.getCompound(NBT_KEY_GUARD_DATA);
            data.deserializeNbt(guardNbt, registries);
        }

        return data;
    }

    /**
     * Saves guard data to a villager's NBT
     */
    public void saveToVillager(VillagerEntity villager, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        NbtCompound villagerNbt = new NbtCompound();
        villager.writeCustomDataToNbt(villagerNbt);

        NbtCompound guardNbt = serializeNbt(registries);
        villagerNbt.put(NBT_KEY_GUARD_DATA, guardNbt);

        villager.readCustomDataFromNbt(villagerNbt);

        XeenaaVillagerManager.LOGGER.debug("Saved guard data for villager {}", villagerId);
    }

    /**
     * Serializes guard data to NBT
     */
    public NbtCompound serializeNbt(net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(NBT_KEY_VERSION, CURRENT_VERSION);
        nbt.putString(NBT_KEY_ROLE, currentRole.getName());

        // Serialize rank data
        if (rankData != null) {
            nbt.put(NBT_KEY_RANK_DATA, rankData.writeToNbt());
        }

        // Serialize behavior configuration
        if (behaviorConfig != null) {
            nbt.put(NBT_KEY_BEHAVIOR_CONFIG, behaviorConfig.toNbt());
        }

        return nbt;
    }

    /**
     * Deserializes guard data from NBT
     */
    public void deserializeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        // Check version for future compatibility
        int version = nbt.getInt(NBT_KEY_VERSION);
        if (version > CURRENT_VERSION) {
            XeenaaVillagerManager.LOGGER.warn("Loading guard data from newer version: {} > {}",
                version, CURRENT_VERSION);
        }

        // Load role
        if (nbt.contains(NBT_KEY_ROLE)) {
            currentRole = GuardRole.fromString(nbt.getString(NBT_KEY_ROLE));
        }

        // Load rank data
        if (rankData == null) {
            rankData = new GuardRankData(villagerId);
        }
        if (nbt.contains(NBT_KEY_RANK_DATA)) {
            rankData.readFromNbt(nbt.getCompound(NBT_KEY_RANK_DATA));
        }

        // Load behavior configuration
        if (nbt.contains(NBT_KEY_BEHAVIOR_CONFIG)) {
            behaviorConfig = com.xeenaa.villagermanager.config.GuardBehaviorConfig.fromNbt(
                nbt.getCompound(NBT_KEY_BEHAVIOR_CONFIG));
        } else {
            // Use default configuration for guards without saved config
            behaviorConfig = loadDefaultBehaviorConfig();
        }

        // Equipment data is ignored if present (legacy compatibility)
        if (nbt.contains("GuardEquipment")) {
            XeenaaVillagerManager.LOGGER.debug("Ignoring legacy equipment data for villager {}", villagerId);
        }

        XeenaaVillagerManager.LOGGER.debug("Loaded guard data for villager {}: role={}, rank={}",
            villagerId, currentRole, rankData.getCurrentRank().getDisplayName());
    }


    /**
     * Gets the current guard role
     */
    public GuardRole getRole() {
        return currentRole;
    }

    /**
     * Sets the guard role
     */
    public void setRole(GuardRole role) {
        if (role == null) {
            XeenaaVillagerManager.LOGGER.warn("Attempted to set null role");
            return;
        }

        this.currentRole = role;
        XeenaaVillagerManager.LOGGER.debug("Set guard role to {} for villager {}", role, villagerId);
    }


    /**
     * Gets the villager ID
     */
    public UUID getVillagerId() {
        return villagerId;
    }

    /**
     * Gets the guard rank data
     */
    public GuardRankData getRankData() {
        if (rankData == null) {
            rankData = new GuardRankData(villagerId);
        }
        return rankData;
    }

    /**
     * Gets the guard behavior configuration.
     *
     * @return the current behavior configuration
     */
    public com.xeenaa.villagermanager.config.GuardBehaviorConfig getBehaviorConfig() {
        if (behaviorConfig == null) {
            behaviorConfig = loadDefaultBehaviorConfig();
        }
        return behaviorConfig;
    }

    /**
     * Sets the guard behavior configuration.
     *
     * @param config the new behavior configuration
     */
    public void setBehaviorConfig(com.xeenaa.villagermanager.config.GuardBehaviorConfig config) {
        if (config == null) {
            XeenaaVillagerManager.LOGGER.warn("Attempted to set null behavior config");
            return;
        }

        this.behaviorConfig = config;
        XeenaaVillagerManager.LOGGER.debug("Updated behavior config for guard {}", villagerId);
    }

    /**
     * Updates the guard villager's display name to show rank and tier.
     * Format: "Rank Name ⭐⭐⭐⭐" with color-coded ranks based on tier.
     *
     * @param villager the villager entity to update
     */
    public void updateDisplayName(VillagerEntity villager) {
        com.xeenaa.villagermanager.data.rank.GuardRank rank = rankData.getCurrentRank();
        net.minecraft.text.Text displayName = createRankDisplayName(rank);
        villager.setCustomName(displayName);
        villager.setCustomNameVisible(true);

        XeenaaVillagerManager.LOGGER.debug("Updated display name for guard {} to: {}",
            villagerId, displayName.getString());
    }

    /**
     * Creates a formatted display name for the guard's rank with tier stars and color coding.
     *
     * @param rank the guard's current rank
     * @return formatted Text component with rank name, stars, and color
     */
    private net.minecraft.text.Text createRankDisplayName(com.xeenaa.villagermanager.data.rank.GuardRank rank) {
        // Get rank name
        String rankName = rank.getDisplayName();

        // Get tier (0-4)
        int tier = rank.getTier();

        // Create display format: "Rank Name ⭐⭐⭐⭐"
        String displayText;
        if (tier > 0) {
            String stars = "⭐".repeat(tier);
            displayText = rankName + " " + stars;
        } else {
            // Tier 0 (Recruit) - no stars
            displayText = rankName;
        }

        // Get color based on tier and path
        int color = getRankColor(rank);

        // Build formatted text with color
        return net.minecraft.text.Text.literal(displayText)
            .styled(style -> style.withColor(net.minecraft.text.TextColor.fromRgb(color)));
    }

    /**
     * Gets the RGB color code for a rank based on its tier and specialization path.
     *
     * Color scheme:
     * - Tier 0 (Recruit): White (0xFFFFFF)
     * - Tier 1: Light Gray (0xAAAAAA)
     * - Tier 2: Yellow (0xFFFF55)
     * - Tier 3: Gold (0xFFAA00)
     * - Tier 4 Melee (Knight): Aqua (0x55FFFF)
     * - Tier 4 Ranged (Sharpshooter): Light Purple (0xFF55FF)
     *
     * @param rank the guard's rank
     * @return RGB color code
     */
    private int getRankColor(com.xeenaa.villagermanager.data.rank.GuardRank rank) {
        int tier = rank.getTier();
        com.xeenaa.villagermanager.data.rank.GuardPath path = rank.getPath();

        return switch (tier) {
            case 0 -> 0xFFFFFF;  // White for Recruit
            case 1 -> 0xAAAAAA;  // Light Gray for Tier 1
            case 2 -> 0xFFFF55;  // Yellow for Tier 2
            case 3 -> 0xFFAA00;  // Gold for Tier 3
            case 4 -> path == com.xeenaa.villagermanager.data.rank.GuardPath.MELEE
                ? 0x55FFFF   // Aqua for Knight (melee)
                : 0xFF55FF;  // Light Purple for Sharpshooter (ranged)
            default -> 0xFFFFFF; // Default to white
        };
    }
}