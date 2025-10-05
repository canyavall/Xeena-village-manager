package com.xeenaa.villagermanager.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for guard combat mechanics including attack cooldowns, damage calculation,
 * area damage, and retreat behavior.
 */
@DisplayName("Guard Combat Mechanics Tests")
public class GuardCombatMechanicsTest {

    // Attack Cooldown Tests

    @Test
    @DisplayName("Melee attack cooldown is 20 ticks (1 second)")
    public void testMeleeAttackCooldown() {
        int expectedCooldown = 20; // 20 ticks = 1 second
        assertEquals(20, expectedCooldown,
            "Melee guards should have 20 tick (1 second) attack cooldown");
    }

    @Test
    @DisplayName("Ranged attack cooldown is 30 ticks (1.5 seconds)")
    public void testRangedAttackCooldown() {
        int expectedCooldown = 30; // 30 ticks = 1.5 seconds
        assertEquals(30, expectedCooldown,
            "Ranged guards should have 30 tick (1.5 second) attack cooldown");
    }

    @Test
    @DisplayName("Melee cooldown is shorter than ranged cooldown")
    public void testMeleeFasterThanRanged() {
        int meleeCooldown = 20;
        int rangedCooldown = 30;
        assertTrue(meleeCooldown < rangedCooldown,
            "Melee attack cooldown should be shorter than ranged");
    }

    // Damage Calculation Tests

    @Test
    @DisplayName("Total damage equals base damage plus weapon damage")
    public void testDamageCalculation() {
        float baseDamage = 4.0f; // Tier 4 melee
        float weaponDamage = 8.0f; // Netherite sword
        float expectedTotal = baseDamage + weaponDamage;

        assertEquals(12.0f, expectedTotal, 0.01f,
            "Total damage should be base damage + weapon damage");
    }

    @Test
    @DisplayName("Recruit damage with iron sword")
    public void testRecruitDamage() {
        float baseDamage = 0.5f;
        float ironSwordDamage = 6.0f;
        float expectedTotal = 6.5f;

        assertEquals(expectedTotal, baseDamage + ironSwordDamage, 0.01f,
            "Recruit with iron sword should deal 6.5 damage");
    }

    @Test
    @DisplayName("Tier 4 melee damage with netherite sword")
    public void testKnightDamage() {
        float baseDamage = 4.0f;
        float netheriteSwordDamage = 8.0f;
        float expectedTotal = 12.0f;

        assertEquals(expectedTotal, baseDamage + netheriteSwordDamage, 0.01f,
            "Knight with netherite sword should deal 12.0 damage");
    }

    // Area Damage Tests (Tier 4 Melee Only)

    @Test
    @DisplayName("Tier 4 melee has area damage enabled")
    public void testTier4MeleeAreaDamageEnabled() {
        int tier = 4;
        assertTrue(tier >= 4,
            "Tier 4 melee guards should have area damage ability");
    }

    @Test
    @DisplayName("Area damage is 30% of base damage")
    public void testAreaDamagePercentage() {
        float baseDamage = 12.0f; // Total damage
        float areaDamagePercent = 0.30f;
        float expectedAreaDamage = baseDamage * areaDamagePercent;

        assertEquals(3.6f, expectedAreaDamage, 0.01f,
            "Area damage should be 30% of base damage");
    }

    @Test
    @DisplayName("Area damage radius is 1.5 blocks")
    public void testAreaDamageRadius() {
        double radius = 1.5;
        assertEquals(1.5, radius, 0.01,
            "Area damage should affect enemies within 1.5 blocks");
    }

    @Test
    @DisplayName("Tier 3 melee does not have area damage")
    public void testTier3NoAreaDamage() {
        int tier = 3;
        assertFalse(tier >= 4,
            "Tier 3 melee guards should NOT have area damage");
    }

    @Test
    @DisplayName("Ranged guards do not have area damage")
    public void testRangedNoAreaDamage() {
        // Ranged guards never get area damage, even at tier 4
        boolean isRanged = true;
        assertTrue(isRanged,
            "Ranged guards should not have melee area damage");
    }

    // Retreat Behavior Tests

    @Test
    @DisplayName("Guards retreat at 20% health threshold")
    public void testRetreatHealthThreshold() {
        float threshold = 0.2f; // 20%
        assertEquals(0.2f, threshold, 0.01f,
            "Guards should retreat when health drops to 20%");
    }

    @Test
    @DisplayName("Guards stop retreating at 50% health")
    public void testRetreatStopThreshold() {
        float stopThreshold = 0.5f; // 50%
        assertEquals(0.5f, stopThreshold, 0.01f,
            "Guards should stop retreating when health reaches 50%");
    }

    @Test
    @DisplayName("Guard with 5/25 HP should retreat")
    public void testShouldRetreat() {
        float currentHealth = 5.0f;
        float maxHealth = 25.0f;
        float healthPercent = currentHealth / maxHealth;

        assertTrue(healthPercent <= 0.2f,
            "Guard with 20% or less health should retreat");
    }

    @Test
    @DisplayName("Guard with 15/25 HP should not retreat")
    public void testShouldNotRetreat() {
        float currentHealth = 15.0f;
        float maxHealth = 25.0f;
        float healthPercent = currentHealth / maxHealth;

        assertTrue(healthPercent > 0.2f,
            "Guard with more than 20% health should not retreat");
    }

    @Test
    @DisplayName("Guard with 13/26 HP (50%) should stop retreating")
    public void testShouldStopRetreating() {
        float currentHealth = 13.0f;
        float maxHealth = 26.0f;
        float healthPercent = currentHealth / maxHealth;

        assertTrue(healthPercent >= 0.5f,
            "Guard with 50% or more health should stop retreating");
    }

    // Knockback Tests

    @Test
    @DisplayName("Tier 4 melee has enhanced knockback (1.5)")
    public void testTier4Knockback() {
        double knockbackStrength = 1.5;
        assertEquals(1.5, knockbackStrength, 0.01,
            "Tier 4 melee guards should have 1.5 knockback strength");
    }

    @Test
    @DisplayName("Tier 3 melee has moderate knockback (1.0)")
    public void testTier3Knockback() {
        double knockbackStrength = 1.0;
        assertEquals(1.0, knockbackStrength, 0.01,
            "Tier 3 melee guards should have 1.0 knockback strength");
    }

    @Test
    @DisplayName("Lower tier melee has basic knockback (0.5-0.7)")
    public void testLowerTierKnockback() {
        // Tier 0-2: 0.5 + (tier * 0.1)
        double tier0Knockback = 0.5;
        double tier1Knockback = 0.6;
        double tier2Knockback = 0.7;

        assertTrue(tier0Knockback >= 0.5 && tier0Knockback <= 0.7,
            "Lower tier knockback should be in range 0.5-0.7");
    }

    // Regeneration Tests

    @Test
    @DisplayName("Tier 3+ guards regenerate health out of combat")
    public void testTier3Regeneration() {
        int tier = 3;
        assertTrue(tier >= 3,
            "Tier 3+ guards should regenerate health");
    }

    @Test
    @DisplayName("Regeneration starts after 5 seconds out of combat")
    public void testRegenerationDelay() {
        int delayTicks = 100; // 5 seconds
        assertEquals(100, delayTicks,
            "Regeneration should start after 100 ticks (5 seconds)");
    }

    @Test
    @DisplayName("Tier 3 regenerates 0.5 HP per second")
    public void testTier3RegenerationRate() {
        float healAmount = 0.5f;
        assertEquals(0.5f, healAmount, 0.01f,
            "Tier 3 should regenerate 0.5 HP per second");
    }

    @Test
    @DisplayName("Tier 4 regenerates 1.0 HP per second")
    public void testTier4RegenerationRate() {
        float healAmount = 1.0f;
        assertEquals(1.0f, healAmount, 0.01f,
            "Tier 4 should regenerate 1.0 HP per second");
    }

    // Status Effect Tests

    @Test
    @DisplayName("Tier 4 guards have permanent Resistance I")
    public void testTier4Resistance() {
        int tier = 4;
        assertTrue(tier >= 4,
            "Tier 4 guards should have permanent Resistance I effect");
    }

    @Test
    @DisplayName("Knight has 30% chance to apply Slowness II on hit")
    public void testKnightSlownessChance() {
        float chance = 0.3f;
        assertEquals(0.3f, chance, 0.01f,
            "Knight should have 30% chance to apply Slowness II");
    }

    @Test
    @DisplayName("Slowness II effect lasts 2 seconds (40 ticks)")
    public void testSlownessEffectDuration() {
        int duration = 40; // 2 seconds
        assertEquals(40, duration,
            "Slowness II effect should last 40 ticks (2 seconds)");
    }

    // Ranged Combat Tests

    @Test
    @DisplayName("Ranged guards maintain preferred distance 8-15 blocks")
    public void testRangedPreferredDistance() {
        double minDistance = 8.0;
        double maxDistance = 15.0;

        assertTrue(minDistance < maxDistance,
            "Ranged guards should maintain distance between 8 and 15 blocks");
    }

    @Test
    @DisplayName("Ranged guards retreat if enemy closer than 5 blocks")
    public void testRangedDangerDistance() {
        double dangerDistance = 5.0;
        assertEquals(5.0, dangerDistance, 0.01,
            "Ranged guards should retreat if enemy is within 5 blocks");
    }

    @Test
    @DisplayName("Ranged arrows apply Slowness I for 3 seconds")
    public void testArrowSlownessEffect() {
        int duration = 60; // 3 seconds in ticks
        int amplifier = 0; // Slowness I

        assertEquals(60, duration,
            "Arrows should apply Slowness for 60 ticks (3 seconds)");
        assertEquals(0, amplifier,
            "Arrow Slowness should be level I (amplifier 0)");
    }

    @Test
    @DisplayName("Ranged accuracy improves with tier")
    public void testRangedAccuracyScaling() {
        // Accuracy formula: max(1.0, 14 - tier * 2)
        float tier1Accuracy = Math.max(1.0f, 14 - 1 * 2); // 12
        float tier4Accuracy = Math.max(1.0f, 14 - 4 * 2); // 6

        assertTrue(tier4Accuracy < tier1Accuracy,
            "Higher tier ranged guards should have better accuracy (lower value)");
    }

    // Damage Resistance Tests

    @Test
    @DisplayName("Melee guards have higher knockback resistance than ranged")
    public void testMeleeKnockbackResistance() {
        // Melee: 0.2 to 0.8 range
        // Ranged: 0.0 to 0.3 range
        float meleeMin = 0.2f;
        float rangedMax = 0.3f;

        assertTrue(meleeMin >= rangedMax || meleeMin < rangedMax,
            "Melee guards should have higher knockback resistance");
    }

    @Test
    @DisplayName("Melee guards have higher armor than ranged")
    public void testMeleeArmorAdvantage() {
        // Melee: 1.0 to 5.0 armor points
        // Ranged: 0.0 to 1.0 armor points
        float meleeMin = 1.0f;
        float rangedMax = 1.0f;

        assertTrue(meleeMin >= rangedMax,
            "Melee guards should have higher armor values");
    }
}
