package com.xeenaa.villagermanager.threat;

/**
 * Defines the priority levels for different types of threats.
 *
 * <p>Higher priority values indicate more urgent threats that should be
 * addressed first by guard villagers. The priority system ensures that
 * guards respond appropriately to different threat scenarios.</p>
 *
 * <p>Priority hierarchy (highest to lowest):</p>
 * <ol>
 *   <li>Players under attack - Highest priority</li>
 *   <li>Villagers under attack - High priority</li>
 *   <li>Guards under attack - High priority</li>
 *   <li>Close proximity threats - Medium priority</li>
 *   <li>General proximity threats - Lower priority</li>
 *   <li>Property damage threats - Lowest priority</li>
 * </ol>
 *
 * @since 1.0.0
 */
public enum ThreatPriority {
    /**
     * No threat detected
     */
    NONE(0, "None"),

    /**
     * Property damage threats (future implementation)
     */
    PROPERTY_DAMAGE(1, "Property Damage"),

    /**
     * Low priority proximity threat - hostile mob near but not immediately dangerous
     */
    PROXIMITY_THREAT_LOW(2, "Proximity Threat (Low)"),

    /**
     * Medium priority proximity threat - hostile mob near villagers
     */
    PROXIMITY_THREAT_MEDIUM(3, "Proximity Threat (Medium)"),

    /**
     * High priority proximity threat - hostile mob very close
     */
    PROXIMITY_THREAT_HIGH(4, "Proximity Threat (High)"),

    /**
     * Guard villager is under attack
     */
    GUARD_UNDER_ATTACK(5, "Guard Under Attack"),

    /**
     * Regular villager is under attack
     */
    VILLAGER_UNDER_ATTACK(6, "Villager Under Attack"),

    /**
     * Player is under attack - highest priority
     */
    PLAYER_UNDER_ATTACK(7, "Player Under Attack");

    private final int value;
    private final String displayName;

    ThreatPriority(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Gets the numeric priority value for comparison
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this priority is higher than another
     */
    public boolean isHigherThan(ThreatPriority other) {
        return this.value > other.value;
    }

    /**
     * Checks if this priority is lower than another
     */
    public boolean isLowerThan(ThreatPriority other) {
        return this.value < other.value;
    }

    /**
     * Checks if this priority is critical (requires immediate attention)
     */
    public boolean isCritical() {
        return value >= GUARD_UNDER_ATTACK.value;
    }

    /**
     * Checks if this priority involves an active attack
     */
    public boolean isActiveAttack() {
        return this == PLAYER_UNDER_ATTACK ||
               this == VILLAGER_UNDER_ATTACK ||
               this == GUARD_UNDER_ATTACK;
    }

    /**
     * Checks if this is a proximity-based threat
     */
    public boolean isProximityThreat() {
        return this == PROXIMITY_THREAT_LOW ||
               this == PROXIMITY_THREAT_MEDIUM ||
               this == PROXIMITY_THREAT_HIGH;
    }

    /**
     * Gets the appropriate priority for a proximity threat based on distance
     *
     * @param distanceSquared The squared distance to the threat
     * @param closeThreshold The squared distance threshold for close threats
     * @param nearVillagers Whether the threat is near other villagers
     * @return The appropriate proximity threat priority
     */
    public static ThreatPriority getProximityPriority(double distanceSquared,
                                                      double closeThreshold,
                                                      boolean nearVillagers) {
        if (distanceSquared <= closeThreshold) {
            return PROXIMITY_THREAT_HIGH;
        } else if (nearVillagers) {
            return PROXIMITY_THREAT_MEDIUM;
        } else {
            return PROXIMITY_THREAT_LOW;
        }
    }

    /**
     * Gets a priority based on the type of entity under attack
     *
     * @param entityType The class of the entity under attack
     * @return The appropriate attack priority
     */
    public static ThreatPriority getAttackPriority(Class<?> entityType) {
        if (net.minecraft.entity.player.PlayerEntity.class.isAssignableFrom(entityType)) {
            return PLAYER_UNDER_ATTACK;
        } else if (net.minecraft.entity.passive.VillagerEntity.class.isAssignableFrom(entityType)) {
            return VILLAGER_UNDER_ATTACK; // Can be upgraded to GUARD_UNDER_ATTACK in context
        }
        return NONE;
    }

    @Override
    public String toString() {
        return displayName + " (" + value + ")";
    }
}