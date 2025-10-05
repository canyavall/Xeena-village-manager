package com.xeenaa.villagermanager.tests;

import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for guard path specialization system including path selection,
 * progression, and validation.
 */
@DisplayName("Guard Path Specialization Tests")
public class GuardPathSpecializationTest {

    // Path Selection Tests

    @Test
    @DisplayName("Recruit can choose melee or ranged path")
    public void testRecruitCanChoosePath() {
        GuardRank[] availableUpgrades = GuardRank.getAvailableUpgrades(GuardRank.RECRUIT);

        assertEquals(2, availableUpgrades.length,
            "Recruit should have 2 path choices");
        assertTrue(contains(availableUpgrades, GuardRank.MAN_AT_ARMS_I),
            "Recruit should be able to choose melee path (Man-at-Arms I)");
        assertTrue(contains(availableUpgrades, GuardRank.MARKSMAN_I),
            "Recruit should be able to choose ranged path (Marksman I)");
    }

    @Test
    @DisplayName("Once path chosen, progression stays in that path")
    public void testPathLocking() {
        // Melee path progression
        GuardRank[] meleeUpgrades = GuardRank.getAvailableUpgrades(GuardRank.MAN_AT_ARMS_I);
        assertEquals(1, meleeUpgrades.length,
            "After choosing melee, should only have 1 upgrade option");
        assertEquals(GuardRank.MAN_AT_ARMS_II, meleeUpgrades[0],
            "Melee path should progress to Man-at-Arms II");

        // Ranged path progression
        GuardRank[] rangedUpgrades = GuardRank.getAvailableUpgrades(GuardRank.MARKSMAN_I);
        assertEquals(1, rangedUpgrades.length,
            "After choosing ranged, should only have 1 upgrade option");
        assertEquals(GuardRank.MARKSMAN_II, rangedUpgrades[0],
            "Ranged path should progress to Marksman II");
    }

    @Test
    @DisplayName("Cannot switch from melee to ranged path")
    public void testCannotSwitchMeleeToRanged() {
        assertFalse(GuardPath.RANGED.canTransitionFrom(GuardPath.MELEE),
            "Should not be able to switch from MELEE to RANGED");
    }

    @Test
    @DisplayName("Cannot switch from ranged to melee path")
    public void testCannotSwitchRangedToMelee() {
        assertFalse(GuardPath.MELEE.canTransitionFrom(GuardPath.RANGED),
            "Should not be able to switch from RANGED to MELEE");
    }

    @Test
    @DisplayName("Cannot go back to recruit path")
    public void testCannotGoBackToRecruit() {
        assertFalse(GuardPath.RECRUIT.canTransitionFrom(GuardPath.MELEE),
            "Should not be able to go back to RECRUIT from MELEE");
        assertFalse(GuardPath.RECRUIT.canTransitionFrom(GuardPath.RANGED),
            "Should not be able to go back to RECRUIT from RANGED");
    }

    @Test
    @DisplayName("Can stay in same path for progression")
    public void testCanStayInSamePath() {
        assertTrue(GuardPath.MELEE.canTransitionFrom(GuardPath.MELEE),
            "Should be able to stay in MELEE path");
        assertTrue(GuardPath.RANGED.canTransitionFrom(GuardPath.RANGED),
            "Should be able to stay in RANGED path");
    }

    // Path Progression Tests

    @Test
    @DisplayName("Melee path progresses: MAN_AT_ARMS_I -> II -> III -> KNIGHT")
    public void testMeleePathProgression() {
        assertEquals(GuardRank.MAN_AT_ARMS_II, GuardRank.MAN_AT_ARMS_I.getNextRank(),
            "Man-at-Arms I should progress to Man-at-Arms II");
        assertEquals(GuardRank.MAN_AT_ARMS_III, GuardRank.MAN_AT_ARMS_II.getNextRank(),
            "Man-at-Arms II should progress to Man-at-Arms III");
        assertEquals(GuardRank.KNIGHT, GuardRank.MAN_AT_ARMS_III.getNextRank(),
            "Man-at-Arms III should progress to Knight");
        assertNull(GuardRank.KNIGHT.getNextRank(),
            "Knight should have no next rank (max tier)");
    }

    @Test
    @DisplayName("Ranged path progresses: MARKSMAN_I -> II -> III -> SHARPSHOOTER")
    public void testRangedPathProgression() {
        assertEquals(GuardRank.MARKSMAN_II, GuardRank.MARKSMAN_I.getNextRank(),
            "Marksman I should progress to Marksman II");
        assertEquals(GuardRank.MARKSMAN_III, GuardRank.MARKSMAN_II.getNextRank(),
            "Marksman II should progress to Marksman III");
        assertEquals(GuardRank.SHARPSHOOTER, GuardRank.MARKSMAN_III.getNextRank(),
            "Marksman III should progress to Sharpshooter");
        assertNull(GuardRank.SHARPSHOOTER.getNextRank(),
            "Sharpshooter should have no next rank (max tier)");
    }

    @Test
    @DisplayName("Recruit has no automatic progression (player must choose)")
    public void testRecruitNoAutoProgression() {
        assertNull(GuardRank.RECRUIT.getNextRank(),
            "Recruit should have no automatic next rank (player chooses path)");
    }

    @Test
    @DisplayName("Max tier ranks have no next rank")
    public void testMaxTierNoNextRank() {
        assertNull(GuardRank.KNIGHT.getNextRank(),
            "Knight (max tier melee) should have no next rank");
        assertNull(GuardRank.SHARPSHOOTER.getNextRank(),
            "Sharpshooter (max tier ranged) should have no next rank");
    }

    // Path Validation Tests

    @Test
    @DisplayName("Melee ranks have 'melee' path ID")
    public void testMeleePathID() {
        assertEquals("melee", GuardPath.MELEE.getId(),
            "Melee path should have ID 'melee'");
    }

    @Test
    @DisplayName("Ranged ranks have 'ranged' path ID")
    public void testRangedPathID() {
        assertEquals("ranged", GuardPath.RANGED.getId(),
            "Ranged path should have ID 'ranged'");
    }

    @Test
    @DisplayName("All melee ranks belong to MELEE path")
    public void testAllMeleeRanksBelongToMeleePath() {
        assertEquals(GuardPath.MELEE, GuardRank.MAN_AT_ARMS_I.getPath());
        assertEquals(GuardPath.MELEE, GuardRank.MAN_AT_ARMS_II.getPath());
        assertEquals(GuardPath.MELEE, GuardRank.MAN_AT_ARMS_III.getPath());
        assertEquals(GuardPath.MELEE, GuardRank.KNIGHT.getPath());
    }

    @Test
    @DisplayName("All ranged ranks belong to RANGED path")
    public void testAllRangedRanksBelongToRangedPath() {
        assertEquals(GuardPath.RANGED, GuardRank.MARKSMAN_I.getPath());
        assertEquals(GuardPath.RANGED, GuardRank.MARKSMAN_II.getPath());
        assertEquals(GuardPath.RANGED, GuardRank.MARKSMAN_III.getPath());
        assertEquals(GuardPath.RANGED, GuardRank.SHARPSHOOTER.getPath());
    }

    // Path Display Names

    @Test
    @DisplayName("Melee path has display name 'Man-at-Arms'")
    public void testMeleePathDisplayName() {
        assertEquals("Man-at-Arms", GuardPath.MELEE.getDisplayName(),
            "Melee path should display as 'Man-at-Arms'");
    }

    @Test
    @DisplayName("Ranged path has display name 'Marksman'")
    public void testRangedPathDisplayName() {
        assertEquals("Marksman", GuardPath.RANGED.getDisplayName(),
            "Ranged path should display as 'Marksman'");
    }

    @Test
    @DisplayName("Recruit path has display name 'Recruit'")
    public void testRecruitPathDisplayName() {
        assertEquals("Recruit", GuardPath.RECRUIT.getDisplayName(),
            "Recruit path should display as 'Recruit'");
    }

    // Rank Purchase Validation

    @Test
    @DisplayName("Can purchase next rank if on previous rank")
    public void testCanPurchaseNextRank() {
        assertTrue(GuardRank.MAN_AT_ARMS_II.canPurchase(GuardRank.MAN_AT_ARMS_I),
            "Should be able to purchase Man-at-Arms II from Man-at-Arms I");
        assertTrue(GuardRank.MARKSMAN_II.canPurchase(GuardRank.MARKSMAN_I),
            "Should be able to purchase Marksman II from Marksman I");
    }

    @Test
    @DisplayName("Cannot skip ranks in progression")
    public void testCannotSkipRanks() {
        assertFalse(GuardRank.MAN_AT_ARMS_III.canPurchase(GuardRank.MAN_AT_ARMS_I),
            "Should not be able to skip from Man-at-Arms I to Man-at-Arms III");
        assertFalse(GuardRank.KNIGHT.canPurchase(GuardRank.MAN_AT_ARMS_II),
            "Should not be able to skip from Man-at-Arms II to Knight");
    }

    @Test
    @DisplayName("Path restriction enforced by available upgrades")
    public void testPathRestrictionByAvailableUpgrades() {
        // Path restriction is enforced by getAvailableUpgrades(), not by canPurchase()
        // Once a path is chosen, only ranks from that path are available
        GuardRank[] meleeUpgrades = GuardRank.getAvailableUpgrades(GuardRank.MAN_AT_ARMS_I);

        // Should only contain melee ranks, no ranged ranks
        for (GuardRank rank : meleeUpgrades) {
            assertEquals(GuardPath.MELEE, rank.getPath(),
                "After choosing melee path, only melee ranks should be available");
        }

        GuardRank[] rangedUpgrades = GuardRank.getAvailableUpgrades(GuardRank.MARKSMAN_I);

        // Should only contain ranged ranks, no melee ranks
        for (GuardRank rank : rangedUpgrades) {
            assertEquals(GuardPath.RANGED, rank.getPath(),
                "After choosing ranged path, only ranged ranks should be available");
        }
    }

    @Test
    @DisplayName("Tier 1 ranks can be purchased from Recruit")
    public void testCanPurchaseTier1FromRecruit() {
        assertTrue(GuardRank.MAN_AT_ARMS_I.canPurchase(GuardRank.RECRUIT),
            "Should be able to purchase Man-at-Arms I from Recruit");
        assertTrue(GuardRank.MARKSMAN_I.canPurchase(GuardRank.RECRUIT),
            "Should be able to purchase Marksman I from Recruit");
    }

    // Rank Costs

    @Test
    @DisplayName("Recruit is free (0 emeralds)")
    public void testRecruitCost() {
        assertEquals(0, GuardRank.RECRUIT.getEmeraldCost(),
            "Recruit should cost 0 emeralds");
    }

    @Test
    @DisplayName("Tier 1 costs 15 emeralds")
    public void testTier1Cost() {
        assertEquals(15, GuardRank.MAN_AT_ARMS_I.getEmeraldCost(),
            "Man-at-Arms I should cost 15 emeralds");
        assertEquals(15, GuardRank.MARKSMAN_I.getEmeraldCost(),
            "Marksman I should cost 15 emeralds");
    }

    @Test
    @DisplayName("Tier 2 costs 20 emeralds")
    public void testTier2Cost() {
        assertEquals(20, GuardRank.MAN_AT_ARMS_II.getEmeraldCost(),
            "Man-at-Arms II should cost 20 emeralds");
        assertEquals(20, GuardRank.MARKSMAN_II.getEmeraldCost(),
            "Marksman II should cost 20 emeralds");
    }

    @Test
    @DisplayName("Tier 3 costs 45 emeralds")
    public void testTier3Cost() {
        assertEquals(45, GuardRank.MAN_AT_ARMS_III.getEmeraldCost(),
            "Man-at-Arms III should cost 45 emeralds");
        assertEquals(45, GuardRank.MARKSMAN_III.getEmeraldCost(),
            "Marksman III should cost 45 emeralds");
    }

    @Test
    @DisplayName("Tier 4 costs 75 emeralds")
    public void testTier4Cost() {
        assertEquals(75, GuardRank.KNIGHT.getEmeraldCost(),
            "Knight should cost 75 emeralds");
        assertEquals(75, GuardRank.SHARPSHOOTER.getEmeraldCost(),
            "Sharpshooter should cost 75 emeralds");
    }

    @Test
    @DisplayName("Costs increase with tier progression")
    public void testCostsIncrease() {
        assertTrue(GuardRank.MAN_AT_ARMS_I.getEmeraldCost() <
                   GuardRank.MAN_AT_ARMS_II.getEmeraldCost(),
            "Tier 2 should cost more than Tier 1");
        assertTrue(GuardRank.MAN_AT_ARMS_II.getEmeraldCost() <
                   GuardRank.MAN_AT_ARMS_III.getEmeraldCost(),
            "Tier 3 should cost more than Tier 2");
        assertTrue(GuardRank.MAN_AT_ARMS_III.getEmeraldCost() <
                   GuardRank.KNIGHT.getEmeraldCost(),
            "Tier 4 should cost more than Tier 3");
    }

    // Previous Rank Tests

    @Test
    @DisplayName("Ranks can trace back to previous rank")
    public void testPreviousRank() {
        assertEquals(GuardRank.MAN_AT_ARMS_II, GuardRank.MAN_AT_ARMS_III.getPreviousRank(),
            "Man-at-Arms III should have Man-at-Arms II as previous rank");
        assertEquals(GuardRank.MARKSMAN_II, GuardRank.MARKSMAN_III.getPreviousRank(),
            "Marksman III should have Marksman II as previous rank");
    }

    @Test
    @DisplayName("Tier 1 ranks have Recruit as previous rank")
    public void testTier1PreviousRank() {
        assertEquals(GuardRank.RECRUIT, GuardRank.MAN_AT_ARMS_I.getPreviousRank(),
            "Man-at-Arms I should have Recruit as previous rank");
        assertEquals(GuardRank.RECRUIT, GuardRank.MARKSMAN_I.getPreviousRank(),
            "Marksman I should have Recruit as previous rank");
    }

    @Test
    @DisplayName("Recruit has no previous rank")
    public void testRecruitNoPreviousRank() {
        assertNull(GuardRank.RECRUIT.getPreviousRank(),
            "Recruit should have no previous rank");
    }

    // Path Description Tests

    @Test
    @DisplayName("Melee path has correct description")
    public void testMeleePathDescription() {
        assertEquals("Melee combat specialization path", GuardPath.MELEE.getDescription(),
            "Melee path should have correct description");
    }

    @Test
    @DisplayName("Ranged path has correct description")
    public void testRangedPathDescription() {
        assertEquals("Ranged combat specialization path", GuardPath.RANGED.getDescription(),
            "Ranged path should have correct description");
    }

    @Test
    @DisplayName("Recruit path has correct description")
    public void testRecruitPathDescription() {
        assertEquals("Basic guard without specialization", GuardPath.RECRUIT.getDescription(),
            "Recruit path should have correct description");
    }

    // Helper method
    private boolean contains(GuardRank[] ranks, GuardRank target) {
        for (GuardRank rank : ranks) {
            if (rank == target) {
                return true;
            }
        }
        return false;
    }
}
