package com.xeenaa.villagermanager.tests;

import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.RankStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for guard rank statistics including HP, damage, movement speed, and path IDs.
 * Validates that all ranks have correct base stats as per design specification.
 */
@DisplayName("Guard Rank Stats Tests")
public class GuardRankStatsTest {

    @Test
    @DisplayName("Recruit (Tier 0) has correct HP value (10.0)")
    public void testRecruitHP() {
        RankStats stats = GuardRank.RECRUIT.getStats();
        assertEquals(10.0f, stats.getMaxHealth(), 0.01f,
            "Recruit should have 10.0 HP");
    }

    @Test
    @DisplayName("Tier 1 ranks have correct HP value (14.0)")
    public void testTier1HP() {
        assertEquals(14.0f, GuardRank.MAN_AT_ARMS_I.getStats().getMaxHealth(), 0.01f,
            "Man-at-Arms I should have 14.0 HP");
        assertEquals(14.0f, GuardRank.MARKSMAN_I.getStats().getMaxHealth(), 0.01f,
            "Marksman I should have 14.0 HP");
    }

    @Test
    @DisplayName("Tier 2 ranks have correct HP value (18.0)")
    public void testTier2HP() {
        assertEquals(18.0f, GuardRank.MAN_AT_ARMS_II.getStats().getMaxHealth(), 0.01f,
            "Man-at-Arms II should have 18.0 HP");
        assertEquals(18.0f, GuardRank.MARKSMAN_II.getStats().getMaxHealth(), 0.01f,
            "Marksman II should have 18.0 HP");
    }

    @Test
    @DisplayName("Tier 3 ranks have correct HP value (22.0)")
    public void testTier3HP() {
        assertEquals(22.0f, GuardRank.MAN_AT_ARMS_III.getStats().getMaxHealth(), 0.01f,
            "Man-at-Arms III should have 22.0 HP");
        assertEquals(22.0f, GuardRank.MARKSMAN_III.getStats().getMaxHealth(), 0.01f,
            "Marksman III should have 22.0 HP");
    }

    @Test
    @DisplayName("Tier 4 ranks have correct HP value (26.0)")
    public void testTier4HP() {
        assertEquals(26.0f, GuardRank.KNIGHT.getStats().getMaxHealth(), 0.01f,
            "Knight should have 26.0 HP");
        assertEquals(26.0f, GuardRank.SHARPSHOOTER.getStats().getMaxHealth(), 0.01f,
            "Sharpshooter should have 26.0 HP");
    }

    @Test
    @DisplayName("Recruit has correct base damage (0.5)")
    public void testRecruitDamage() {
        assertEquals(0.5f, GuardRank.RECRUIT.getStats().getAttackDamage(), 0.01f,
            "Recruit should have 0.5 base damage");
    }

    @Test
    @DisplayName("Tier 1 ranks have correct base damage (1.5)")
    public void testTier1Damage() {
        assertEquals(1.5f, GuardRank.MAN_AT_ARMS_I.getStats().getAttackDamage(), 0.01f,
            "Man-at-Arms I should have 1.5 base damage");
        assertEquals(1.5f, GuardRank.MARKSMAN_I.getStats().getAttackDamage(), 0.01f,
            "Marksman I should have 1.5 base damage");
    }

    @Test
    @DisplayName("Tier 2 ranks have correct base damage (2.5)")
    public void testTier2Damage() {
        assertEquals(2.5f, GuardRank.MAN_AT_ARMS_II.getStats().getAttackDamage(), 0.01f,
            "Man-at-Arms II should have 2.5 base damage");
        assertEquals(2.5f, GuardRank.MARKSMAN_II.getStats().getAttackDamage(), 0.01f,
            "Marksman II should have 2.5 base damage");
    }

    @Test
    @DisplayName("Tier 3 melee has correct base damage (3.0)")
    public void testTier3MeleeDamage() {
        assertEquals(3.0f, GuardRank.MAN_AT_ARMS_III.getStats().getAttackDamage(), 0.01f,
            "Man-at-Arms III should have 3.0 base damage");
    }

    @Test
    @DisplayName("Tier 3 ranged has correct base damage (3.5)")
    public void testTier3RangedDamage() {
        assertEquals(3.5f, GuardRank.MARKSMAN_III.getStats().getAttackDamage(), 0.01f,
            "Marksman III should have 3.5 base damage");
    }

    @Test
    @DisplayName("Tier 4 melee has correct base damage (4.0)")
    public void testTier4MeleeDamage() {
        assertEquals(4.0f, GuardRank.KNIGHT.getStats().getAttackDamage(), 0.01f,
            "Knight should have 4.0 base damage");
    }

    @Test
    @DisplayName("Tier 4 ranged has correct base damage (4.5)")
    public void testTier4RangedDamage() {
        assertEquals(4.5f, GuardRank.SHARPSHOOTER.getStats().getAttackDamage(), 0.01f,
            "Sharpshooter should have 4.5 base damage");
    }

    @Test
    @DisplayName("Melee path has movement speed scaling from low to high")
    public void testMeleeMovementSpeedRange() {
        // Test all melee ranks - movement speed scales with damage/tier
        // Formula: 0.4 + (speedScale * 0.2) where speedScale = min((damage - 4.0) / 8.0, 1.0)
        // Low tiers may have speeds below 0.4, high tiers approach 0.6

        float manAtArms1Speed = GuardRank.MAN_AT_ARMS_I.getStats().getMovementSpeed();
        float knightSpeed = GuardRank.KNIGHT.getStats().getMovementSpeed();

        // Verify speeds are reasonable (0.3-0.7 range to account for formula)
        assertTrue(manAtArms1Speed >= 0.3f && manAtArms1Speed <= 0.7f,
            "Man-at-Arms I movement speed should be in reasonable range: " + manAtArms1Speed);

        assertTrue(knightSpeed >= 0.3f && knightSpeed <= 0.7f,
            "Knight movement speed should be in reasonable range: " + knightSpeed);
    }

    @Test
    @DisplayName("Ranged path has movement speed scaling from low to high")
    public void testRangedMovementSpeedRange() {
        // Test all ranged ranks - movement speed scales with damage/tier
        // Formula: 0.45 + (damageScale * 0.2) where damageScale = min((damage - 5.0) / 7.0, 1.0)
        // Low tiers may have speeds below 0.45, high tiers approach 0.65

        float marksman1Speed = GuardRank.MARKSMAN_I.getStats().getMovementSpeed();
        float sharpshooterSpeed = GuardRank.SHARPSHOOTER.getStats().getMovementSpeed();

        // Verify speeds are reasonable (0.35-0.75 range to account for formula)
        assertTrue(marksman1Speed >= 0.35f && marksman1Speed <= 0.75f,
            "Marksman I movement speed should be in reasonable range: " + marksman1Speed);

        assertTrue(sharpshooterSpeed >= 0.35f && sharpshooterSpeed <= 0.75f,
            "Sharpshooter movement speed should be in reasonable range: " + sharpshooterSpeed);
    }

    @Test
    @DisplayName("Melee path ID is 'melee' not 'man_at_arms'")
    public void testMeleePathID() {
        assertEquals("melee", GuardPath.MELEE.getId(),
            "Melee path ID should be 'melee'");
    }

    @Test
    @DisplayName("Ranged path ID is 'ranged' not 'marksman'")
    public void testRangedPathID() {
        assertEquals("ranged", GuardPath.RANGED.getId(),
            "Ranged path ID should be 'ranged' (NOT 'marksman')");
    }

    @Test
    @DisplayName("All melee ranks return MELEE path")
    public void testMeleeRanksReturnMeleePath() {
        assertEquals(GuardPath.MELEE, GuardRank.MAN_AT_ARMS_I.getPath(),
            "Man-at-Arms I should return MELEE path");
        assertEquals(GuardPath.MELEE, GuardRank.MAN_AT_ARMS_II.getPath(),
            "Man-at-Arms II should return MELEE path");
        assertEquals(GuardPath.MELEE, GuardRank.MAN_AT_ARMS_III.getPath(),
            "Man-at-Arms III should return MELEE path");
        assertEquals(GuardPath.MELEE, GuardRank.KNIGHT.getPath(),
            "Knight should return MELEE path");
    }

    @Test
    @DisplayName("All ranged ranks return RANGED path")
    public void testRangedRanksReturnRangedPath() {
        assertEquals(GuardPath.RANGED, GuardRank.MARKSMAN_I.getPath(),
            "Marksman I should return RANGED path");
        assertEquals(GuardPath.RANGED, GuardRank.MARKSMAN_II.getPath(),
            "Marksman II should return RANGED path");
        assertEquals(GuardPath.RANGED, GuardRank.MARKSMAN_III.getPath(),
            "Marksman III should return RANGED path");
        assertEquals(GuardPath.RANGED, GuardRank.SHARPSHOOTER.getPath(),
            "Sharpshooter should return RANGED path");
    }

    @Test
    @DisplayName("Recruit returns RECRUIT path")
    public void testRecruitPath() {
        assertEquals(GuardPath.RECRUIT, GuardRank.RECRUIT.getPath(),
            "Recruit should return RECRUIT path");
    }

    @Test
    @DisplayName("All ranks return correct tier levels")
    public void testTierLevels() {
        assertEquals(0, GuardRank.RECRUIT.getTier(), "Recruit should be tier 0");

        assertEquals(1, GuardRank.MAN_AT_ARMS_I.getTier(), "Man-at-Arms I should be tier 1");
        assertEquals(1, GuardRank.MARKSMAN_I.getTier(), "Marksman I should be tier 1");

        assertEquals(2, GuardRank.MAN_AT_ARMS_II.getTier(), "Man-at-Arms II should be tier 2");
        assertEquals(2, GuardRank.MARKSMAN_II.getTier(), "Marksman II should be tier 2");

        assertEquals(3, GuardRank.MAN_AT_ARMS_III.getTier(), "Man-at-Arms III should be tier 3");
        assertEquals(3, GuardRank.MARKSMAN_III.getTier(), "Marksman III should be tier 3");

        assertEquals(4, GuardRank.KNIGHT.getTier(), "Knight should be tier 4");
        assertEquals(4, GuardRank.SHARPSHOOTER.getTier(), "Sharpshooter should be tier 4");
    }

    @Test
    @DisplayName("Movement speed scales with rank progression")
    public void testMovementSpeedScaling() {
        // Melee path should have increasing speed
        assertTrue(GuardRank.MAN_AT_ARMS_I.getStats().getMovementSpeed() <
                   GuardRank.KNIGHT.getStats().getMovementSpeed(),
            "Knight should be faster than Man-at-Arms I");

        // Ranged path should have increasing speed
        assertTrue(GuardRank.MARKSMAN_I.getStats().getMovementSpeed() <
                   GuardRank.SHARPSHOOTER.getStats().getMovementSpeed(),
            "Sharpshooter should be faster than Marksman I");
    }

    @Test
    @DisplayName("Ranged guards are faster than melee guards at same tier")
    public void testRangedFasterThanMelee() {
        assertTrue(GuardRank.MARKSMAN_I.getStats().getMovementSpeed() >
                   GuardRank.MAN_AT_ARMS_I.getStats().getMovementSpeed(),
            "Marksman I should be faster than Man-at-Arms I");

        assertTrue(GuardRank.SHARPSHOOTER.getStats().getMovementSpeed() >
                   GuardRank.KNIGHT.getStats().getMovementSpeed(),
            "Sharpshooter should be faster than Knight");
    }

    @Test
    @DisplayName("Melee ranks are not marked as ranged")
    public void testMeleeNotRanged() {
        assertFalse(GuardRank.MAN_AT_ARMS_I.getStats().isRanged(),
            "Man-at-Arms I should not be marked as ranged");
        assertFalse(GuardRank.KNIGHT.getStats().isRanged(),
            "Knight should not be marked as ranged");
    }

    @Test
    @DisplayName("Ranged ranks are correctly marked as ranged")
    public void testRangedMarkedAsRanged() {
        assertTrue(GuardRank.MARKSMAN_I.getStats().isRanged(),
            "Marksman I should be marked as ranged");
        assertTrue(GuardRank.SHARPSHOOTER.getStats().isRanged(),
            "Sharpshooter should be marked as ranged");
    }

    @Test
    @DisplayName("Recruit is not marked as ranged")
    public void testRecruitNotRanged() {
        assertFalse(GuardRank.RECRUIT.getStats().isRanged(),
            "Recruit should not be marked as ranged (defaults to melee)");
    }
}
