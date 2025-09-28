package com.xeenaa.villagermanager.threat;

/**
 * Defines the different types of threats that can be detected by the threat detection system.
 *
 * <p>Threat types help categorize different kinds of dangers and determine
 * appropriate response strategies for guard villagers.</p>
 *
 * @since 1.0.0
 */
public enum ThreatType {
    /**
     * Active attack in progress - entity is currently being damaged
     */
    ACTIVE_ATTACK("Active Attack"),

    /**
     * Proximity threat - hostile entity nearby but not actively attacking
     */
    PROXIMITY_THREAT("Proximity Threat"),

    /**
     * Property damage threat - potential or active damage to structures
     * (Future implementation for blocks like TNT, Creepers near buildings, etc.)
     */
    PROPERTY_DAMAGE("Property Damage"),

    /**
     * Environmental threat - dangerous environmental conditions
     * (Future implementation for lava, fire spread, etc.)
     */
    ENVIRONMENTAL("Environmental"),

    /**
     * Unknown threat type
     */
    UNKNOWN("Unknown");

    private final String displayName;

    ThreatType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this threat type requires immediate action
     */
    public boolean requiresImmediateAction() {
        return this == ACTIVE_ATTACK || this == PROPERTY_DAMAGE;
    }

    /**
     * Checks if this threat type involves combat
     */
    public boolean involvesCombat() {
        return this == ACTIVE_ATTACK || this == PROXIMITY_THREAT;
    }

    /**
     * Checks if this threat type is defensive (protecting something)
     */
    public boolean isDefensive() {
        return this == ACTIVE_ATTACK || this == PROPERTY_DAMAGE;
    }

    /**
     * Checks if this threat type is preventive (stopping potential danger)
     */
    public boolean isPreventive() {
        return this == PROXIMITY_THREAT || this == ENVIRONMENTAL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}