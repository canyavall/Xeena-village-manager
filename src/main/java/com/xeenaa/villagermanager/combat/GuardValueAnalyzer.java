package com.xeenaa.villagermanager.combat;

import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.data.rank.RankStats;

/**
 * Analyzes the cost-effectiveness and economic balance of guard ranks.
 * Provides mathematical validation of the combat effectiveness scaling system
 * to ensure balanced progression and meaningful rank upgrades.
 *
 * Features:
 * - Cost-effectiveness analysis per rank tier
 * - Combat power scaling validation
 * - Investment value calculations
 * - Balance recommendations
 *
 * @since 2.1.0
 */
public class GuardValueAnalyzer {

    /**
     * Calculates the combat power rating for a guard rank.
     * Combines all combat statistics into a single power metric.
     *
     * @param rank The guard rank to analyze
     * @return Combat power rating (higher = more powerful)
     */
    public static double calculateCombatPower(GuardRank rank) {
        RankStats stats = rank.getStats();

        // Base combat power from stats
        double healthPower = stats.getMaxHealth() * 0.8; // Health contributes 80% of its value
        double damagePower = stats.getAttackDamage() * 4.0; // Damage is highly valued
        double speedPower = stats.getMovementSpeed() * 20.0; // Speed multiplier for positioning
        double armorPower = stats.getArmorValue() * 3.0; // Armor value
        double attackSpeedPower = stats.getAttackSpeed() * 2.0; // Attack speed bonus

        return healthPower + damagePower + speedPower + armorPower + attackSpeedPower;
    }

    /**
     * Calculates the cost-effectiveness ratio for a guard rank.
     * Higher ratios indicate better value for emerald investment.
     *
     * @param rank The guard rank to analyze
     * @return Cost-effectiveness ratio (power per emerald)
     */
    public static double calculateCostEffectiveness(GuardRank rank) {
        if (rank.getEmeraldCost() == 0) {
            return Double.MAX_VALUE; // Recruit rank has infinite cost-effectiveness
        }

        double combatPower = calculateCombatPower(rank);
        return combatPower / rank.getEmeraldCost();
    }

    /**
     * Calculates the power increase percentage from one rank to another.
     *
     * @param fromRank The starting rank
     * @param toRank The target rank
     * @return Percentage power increase (e.g., 1.25 = 25% increase)
     */
    public static double calculatePowerIncrease(GuardRank fromRank, GuardRank toRank) {
        double fromPower = calculateCombatPower(fromRank);
        double toPower = calculateCombatPower(toRank);

        if (fromPower == 0) return toPower; // Avoid division by zero

        return (toPower / fromPower) - 1.0;
    }

    /**
     * Calculates the total investment value for a guard's current progression.
     *
     * @param rankData The guard's rank progression data
     * @return Investment efficiency score
     */
    public static double calculateInvestmentValue(GuardRankData rankData) {
        GuardRank currentRank = rankData.getCurrentRank();
        int totalSpent = rankData.getTotalEmeraldsSpent();

        if (totalSpent == 0) {
            return calculateCombatPower(currentRank); // Free power for recruits
        }

        double combatPower = calculateCombatPower(currentRank);
        return combatPower / totalSpent;
    }

    /**
     * Analyzes the balance of a specific rank upgrade path.
     *
     * @param ranks Array of ranks in progression order
     * @return Balance analysis report
     */
    public static BalanceReport analyzeRankProgression(GuardRank[] ranks) {
        BalanceReport report = new BalanceReport();

        for (int i = 0; i < ranks.length; i++) {
            GuardRank rank = ranks[i];
            double power = calculateCombatPower(rank);
            double costEffectiveness = calculateCostEffectiveness(rank);

            report.addRankAnalysis(rank, power, costEffectiveness);

            // Calculate progression value if not the first rank
            if (i > 0) {
                GuardRank previousRank = ranks[i - 1];
                double powerIncrease = calculatePowerIncrease(previousRank, rank);
                double costIncrement = rank.getEmeraldCost();
                double incrementalValue = powerIncrease / costIncrement;

                report.addProgressionValue(rank, powerIncrease, incrementalValue);
            }
        }

        return report;
    }

    /**
     * Recommends the most cost-effective rank progression for a player.
     *
     * @param currentRank The guard's current rank
     * @param availableEmeralds The player's emerald budget
     * @param targetPowerIncrease Desired power improvement (e.g., 0.5 for 50% increase)
     * @return Recommended upgrade strategy
     */
    public static UpgradeRecommendation recommendUpgrade(GuardRank currentRank, int availableEmeralds,
                                                        double targetPowerIncrease) {
        GuardRank[] availableUpgrades = GuardRank.getAvailableUpgrades(currentRank);

        UpgradeRecommendation bestRecommendation = null;
        double bestValue = 0.0;

        for (GuardRank upgrade : availableUpgrades) {
            if (upgrade.getEmeraldCost() <= availableEmeralds) {
                double powerIncrease = calculatePowerIncrease(currentRank, upgrade);
                double costEffectiveness = calculateCostEffectiveness(upgrade);

                // Score based on power increase and cost effectiveness
                double score = powerIncrease * costEffectiveness;

                // Bonus if meets target power increase
                if (powerIncrease >= targetPowerIncrease) {
                    score *= 1.5;
                }

                if (bestRecommendation == null || score > bestValue) {
                    bestRecommendation = new UpgradeRecommendation(
                        upgrade, powerIncrease, costEffectiveness, score
                    );
                    bestValue = score;
                }
            }
        }

        return bestRecommendation;
    }

    /**
     * Validates that the rank progression provides meaningful upgrades.
     * Returns true if the scaling is balanced and provides good value progression.
     *
     * @return True if the rank system is well-balanced
     */
    public static boolean validateRankBalance() {
        // Test melee progression
        GuardRank[] meleeRanks = {
            GuardRank.RECRUIT,
            GuardRank.MAN_AT_ARMS_I,
            GuardRank.MAN_AT_ARMS_II,
            GuardRank.MAN_AT_ARMS_III,
            GuardRank.KNIGHT
        };

        // Test ranged progression
        GuardRank[] rangedRanks = {
            GuardRank.RECRUIT,
            GuardRank.MARKSMAN_I,
            GuardRank.MARKSMAN_II,
            GuardRank.MARKSMAN_III,
            GuardRank.SHARPSHOOTER
        };

        boolean meleeBalanced = validateProgression(meleeRanks);
        boolean rangedBalanced = validateProgression(rangedRanks);

        return meleeBalanced && rangedBalanced;
    }

    /**
     * Validates a specific rank progression for balance.
     */
    private static boolean validateProgression(GuardRank[] ranks) {
        double previousPower = 0.0;

        for (GuardRank rank : ranks) {
            double power = calculateCombatPower(rank);

            // Each rank should provide meaningful power increase
            if (previousPower > 0 && power <= previousPower * 1.15) {
                return false; // Less than 15% power increase is too small
            }

            // Cost effectiveness shouldn't drop too dramatically
            if (rank.getEmeraldCost() > 0) {
                double costEffectiveness = calculateCostEffectiveness(rank);
                if (costEffectiveness < 0.5) {
                    return false; // Very poor cost effectiveness
                }
            }

            previousPower = power;
        }

        return true;
    }

    /**
     * Balance analysis report containing rank progression data.
     */
    public static class BalanceReport {
        private final StringBuilder report = new StringBuilder();
        private double totalCostEffectiveness = 0.0;
        private int rankCount = 0;

        void addRankAnalysis(GuardRank rank, double power, double costEffectiveness) {
            report.append(String.format("Rank %s: Power=%.2f, Cost-Effectiveness=%.2f\n",
                rank.getDisplayName(), power, costEffectiveness));
            totalCostEffectiveness += costEffectiveness;
            rankCount++;
        }

        void addProgressionValue(GuardRank rank, double powerIncrease, double incrementalValue) {
            report.append(String.format("  -> Upgrade to %s: +%.1f%% power, Incremental Value=%.2f\n",
                rank.getDisplayName(), powerIncrease * 100, incrementalValue));
        }

        public double getAverageCostEffectiveness() {
            return rankCount > 0 ? totalCostEffectiveness / rankCount : 0.0;
        }

        @Override
        public String toString() {
            report.append(String.format("\nAverage Cost-Effectiveness: %.2f\n", getAverageCostEffectiveness()));
            return report.toString();
        }
    }

    /**
     * Upgrade recommendation with analysis data.
     */
    public static class UpgradeRecommendation {
        private final GuardRank recommendedRank;
        private final double powerIncrease;
        private final double costEffectiveness;
        private final double score;

        public UpgradeRecommendation(GuardRank rank, double powerIncrease, double costEffectiveness, double score) {
            this.recommendedRank = rank;
            this.powerIncrease = powerIncrease;
            this.costEffectiveness = costEffectiveness;
            this.score = score;
        }

        public GuardRank getRecommendedRank() { return recommendedRank; }
        public double getPowerIncrease() { return powerIncrease; }
        public double getCostEffectiveness() { return costEffectiveness; }
        public double getScore() { return score; }

        public String getRecommendationText() {
            return String.format("Recommended: %s (+%.1f%% power, %.2f cost-effectiveness)",
                recommendedRank.getDisplayName(), powerIncrease * 100, costEffectiveness);
        }
    }
}