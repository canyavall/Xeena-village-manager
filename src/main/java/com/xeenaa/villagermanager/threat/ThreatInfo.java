package com.xeenaa.villagermanager.threat;

import net.minecraft.entity.LivingEntity;

/**
 * Represents information about a detected threat including the threatening entity,
 * potential victim, threat priority, type, and distance.
 *
 * <p>This class is used by the {@link ThreatDetectionManager} to provide
 * structured threat information to guard AI systems.</p>
 *
 * @since 1.0.0
 */
public class ThreatInfo {
    private final LivingEntity threatEntity;
    private final LivingEntity victimEntity; // May be null for proximity threats
    private final ThreatPriority priority;
    private final ThreatType type;
    private final double distanceSquared;
    private int lastSeenTick;

    /**
     * Creates a new threat information instance
     *
     * @param threatEntity The entity posing the threat
     * @param victimEntity The entity being threatened (null for proximity threats)
     * @param priority The priority level of this threat
     * @param type The type of threat
     * @param distanceSquared The squared distance from the guard to the threat
     */
    public ThreatInfo(LivingEntity threatEntity, LivingEntity victimEntity,
                      ThreatPriority priority, ThreatType type, double distanceSquared) {
        this.threatEntity = threatEntity;
        this.victimEntity = victimEntity;
        this.priority = priority;
        this.type = type;
        this.distanceSquared = distanceSquared;
        this.lastSeenTick = 0;
    }

    /**
     * Gets the entity posing the threat
     */
    public LivingEntity getThreatEntity() {
        return threatEntity;
    }

    /**
     * Gets the entity being threatened (may be null for proximity threats)
     */
    public LivingEntity getVictimEntity() {
        return victimEntity;
    }

    /**
     * Gets the priority level of this threat
     */
    public ThreatPriority getPriority() {
        return priority;
    }

    /**
     * Gets the priority value for easy comparison
     */
    public int getPriorityValue() {
        return priority.getValue();
    }

    /**
     * Gets the type of threat
     */
    public ThreatType getType() {
        return type;
    }

    /**
     * Gets the squared distance from the guard to the threat
     */
    public double getDistanceSquared() {
        return distanceSquared;
    }

    /**
     * Gets the actual distance from the guard to the threat
     */
    public double getDistance() {
        return Math.sqrt(distanceSquared);
    }

    /**
     * Gets the last tick this threat was seen
     */
    public int getLastSeenTick() {
        return lastSeenTick;
    }

    /**
     * Sets the last tick this threat was seen
     */
    public void setLastSeenTick(int tick) {
        this.lastSeenTick = tick;
    }

    /**
     * Checks if this threat is currently active (involves an active attack)
     */
    public boolean isActiveAttack() {
        return type == ThreatType.ACTIVE_ATTACK;
    }

    /**
     * Checks if this threat is a proximity-based threat
     */
    public boolean isProximityThreat() {
        return type == ThreatType.PROXIMITY_THREAT;
    }

    /**
     * Checks if this threat is high priority (should be handled immediately)
     */
    public boolean isHighPriority() {
        return priority.getValue() >= ThreatPriority.GUARD_UNDER_ATTACK.getValue();
    }

    /**
     * Checks if this threat involves a player
     */
    public boolean involvesPlayer() {
        return priority == ThreatPriority.PLAYER_UNDER_ATTACK;
    }

    /**
     * Checks if this threat involves a villager
     */
    public boolean involvesVillager() {
        return priority == ThreatPriority.VILLAGER_UNDER_ATTACK ||
               priority == ThreatPriority.GUARD_UNDER_ATTACK;
    }

    /**
     * Gets a human-readable description of this threat
     */
    public String getDescription() {
        String threatName = threatEntity.getType().getTranslationKey();
        String victimName = victimEntity != null ? victimEntity.getType().getTranslationKey() : "none";
        return String.format("Threat[%s -> %s, Priority: %s, Type: %s, Distance: %.1f]",
            threatName, victimName, priority, type, getDistance());
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ThreatInfo)) return false;

        ThreatInfo other = (ThreatInfo) obj;
        return threatEntity.equals(other.threatEntity) &&
               priority == other.priority &&
               type == other.type;
    }

    @Override
    public int hashCode() {
        return threatEntity.hashCode() * 31 + priority.hashCode() * 17 + type.hashCode();
    }
}