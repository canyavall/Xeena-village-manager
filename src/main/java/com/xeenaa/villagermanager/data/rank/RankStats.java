package com.xeenaa.villagermanager.data.rank;

/**
 * Immutable class representing combat statistics for a guard rank.
 * Each rank has specific health, damage, and combat modifiers.
 *
 * @since 2.0.0
 */
public final class RankStats {
    private final float maxHealth;
    private final float attackDamage;
    private final float movementSpeed;
    private final float knockbackResistance;
    private final float armorValue;
    private final float attackSpeed;
    private final boolean isRanged;
    private final float bowDrawSpeed;  // For ranged guards

    /**
     * Creates a new RankStats instance for melee guards
     */
    public static RankStats melee(float maxHealth, float attackDamage) {
        return new RankStats(maxHealth, attackDamage, 0.5f, 0.3f, 2.0f, 1.0f, false, 0.0f);
    }

    /**
     * Creates a new RankStats instance for ranged guards
     */
    public static RankStats ranged(float maxHealth, float attackDamage, float bowDrawSpeed) {
        return new RankStats(maxHealth, attackDamage, 0.6f, 0.1f, 0.0f, 1.2f, true, bowDrawSpeed);
    }

    private RankStats(float maxHealth, float attackDamage, float movementSpeed,
                     float knockbackResistance, float armorValue, float attackSpeed,
                     boolean isRanged, float bowDrawSpeed) {
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.knockbackResistance = knockbackResistance;
        this.armorValue = armorValue;
        this.attackSpeed = attackSpeed;
        this.isRanged = isRanged;
        this.bowDrawSpeed = bowDrawSpeed;
    }

    // Getters
    public float getMaxHealth() { return maxHealth; }
    public float getAttackDamage() { return attackDamage; }
    public float getMovementSpeed() { return movementSpeed; }
    public float getKnockbackResistance() { return knockbackResistance; }
    public float getArmorValue() { return armorValue; }
    public float getAttackSpeed() { return attackSpeed; }
    public boolean isRanged() { return isRanged; }
    public float getBowDrawSpeed() { return bowDrawSpeed; }

    @Override
    public String toString() {
        return String.format("RankStats{health=%.1f, damage=%.1f, ranged=%b}",
                           maxHealth, attackDamage, isRanged);
    }
}