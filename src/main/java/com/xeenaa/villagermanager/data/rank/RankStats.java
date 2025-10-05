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
     * Creates a new RankStats instance for melee guards with rank-appropriate scaling
     */
    public static RankStats melee(float maxHealth, float attackDamage) {
        // Melee guards get progressive speed increase: 0.4 -> 0.6 (Rank 1 -> 5)
        float movementSpeed = calculateMeleeMovementSpeed(maxHealth, attackDamage);
        // Melee guards get high knockback resistance for tanking
        float knockbackResistance = calculateMeleeKnockbackResistance(maxHealth);
        // Armor scales with health for tankiness
        float armorValue = calculateMeleeArmor(maxHealth);
        // Attack speed improves slightly with rank
        float attackSpeed = calculateMeleeAttackSpeed(attackDamage);

        return new RankStats(maxHealth, attackDamage, movementSpeed, knockbackResistance,
                           armorValue, attackSpeed, false, 0.0f);
    }

    /**
     * Creates a new RankStats instance for ranged guards with rank-appropriate scaling
     */
    public static RankStats ranged(float maxHealth, float attackDamage, float bowDrawSpeed) {
        // Ranged guards get higher speed: 0.45 -> 0.65 (Rank 1 -> 5)
        float movementSpeed = calculateRangedMovementSpeed(maxHealth, attackDamage);
        // Low knockback resistance for glass cannon design
        float knockbackResistance = calculateRangedKnockbackResistance(maxHealth);
        // Minimal armor for ranged guards
        float armorValue = calculateRangedArmor(maxHealth);
        // Higher attack speed for ranged combat
        float attackSpeed = calculateRangedAttackSpeed(attackDamage);

        return new RankStats(maxHealth, attackDamage, movementSpeed, knockbackResistance,
                           armorValue, attackSpeed, true, bowDrawSpeed);
    }

    // Helper methods for calculating rank-appropriate stats

    private static float calculateMeleeMovementSpeed(float health, float damage) {
        // Speed scaling: 0.4 (low rank) to 0.6 (high rank) - Reduced for better combat balance
        // Base on damage since it correlates with rank progression
        float speedScale = Math.min((damage - 4.0f) / 8.0f, 1.0f); // 0.0 to 1.0
        return 0.4f + (speedScale * 0.2f); // 0.4 to 0.6
    }

    private static float calculateMeleeKnockbackResistance(float health) {
        // Higher health = more knockback resistance (tank role)
        float healthScale = Math.min((health - 25.0f) / 70.0f, 1.0f); // 0.0 to 1.0
        return 0.2f + (healthScale * 0.6f); // 0.2 to 0.8
    }

    private static float calculateMeleeArmor(float health) {
        // Armor scales with health for tankiness
        float healthScale = Math.min((health - 25.0f) / 70.0f, 1.0f); // 0.0 to 1.0
        return 1.0f + (healthScale * 4.0f); // 1.0 to 5.0 armor points
    }

    private static float calculateMeleeAttackSpeed(float damage) {
        // Slightly faster attacks at higher ranks
        float damageScale = Math.min((damage - 4.0f) / 8.0f, 1.0f); // 0.0 to 1.0
        return 1.0f + (damageScale * 0.4f); // 1.0 to 1.4 attack speed
    }

    private static float calculateRangedMovementSpeed(float health, float damage) {
        // Ranged guards are faster: 0.45 to 0.65 - Reduced for better combat balance
        float damageScale = Math.min((damage - 5.0f) / 7.0f, 1.0f); // 0.0 to 1.0
        return 0.45f + (damageScale * 0.2f); // 0.45 to 0.65
    }

    private static float calculateRangedKnockbackResistance(float health) {
        // Low knockback resistance for glass cannon design
        float healthScale = Math.min((health - 20.0f) / 25.0f, 1.0f); // 0.0 to 1.0
        return 0.0f + (healthScale * 0.3f); // 0.0 to 0.3
    }

    private static float calculateRangedArmor(float health) {
        // Minimal armor for ranged guards
        float healthScale = Math.min((health - 20.0f) / 25.0f, 1.0f); // 0.0 to 1.0
        return 0.0f + (healthScale * 1.0f); // 0.0 to 1.0 armor point
    }

    private static float calculateRangedAttackSpeed(float damage) {
        // Higher attack speed for ranged combat
        float damageScale = Math.min((damage - 5.0f) / 7.0f, 1.0f); // 0.0 to 1.0
        return 1.2f + (damageScale * 0.6f); // 1.2 to 1.8 attack speed
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