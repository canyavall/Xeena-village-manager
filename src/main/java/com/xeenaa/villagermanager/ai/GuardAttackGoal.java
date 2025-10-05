package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.threat.ThreatDetectionManager;
import com.xeenaa.villagermanager.threat.ThreatInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * AI goal that makes guard villagers target and attack hostile mobs.
 */
public class GuardAttackGoal extends ActiveTargetGoal<HostileEntity> {
    private final VillagerEntity guard;
    private ThreatInfo currentThreat;

    public GuardAttackGoal(VillagerEntity guard) {
        super(guard, HostileEntity.class, 10, true, false,
            entity -> entity instanceof HostileEntity);
        this.guard = guard;
    }

    @Override
    public boolean canStart() {
        // Only guards can use this goal
        if (!isGuard()) {
            System.out.println("GUARD ATTACK: Not a guard villager");
            return false;
        }

        // Use threat detection system for better targeting
        if (guard.getWorld() instanceof ServerWorld serverWorld) {
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(serverWorld);
            ThreatInfo threat = threatManager.detectPrimaryThreat(guard);

            if (threat != null) {
                System.out.println("GUARD ATTACK: Found threat - " + threat.getDescription() + " at distance " + threat.getDistance());
                this.currentThreat = threat;
                this.target = threat.getThreatEntity();
                System.out.println("GUARD ATTACK: Engaging target!");
                return true;
            }
        }

        // Fall back to parent implementation - simplified to just check for nearby hostiles
        boolean canStart = super.canStart();
        if (canStart) {
            System.out.println("GUARD ATTACK: Parent goal found target - engaging!");
        }
        return canStart;
    }

    @Override
    public void start() {
        super.start();

        // Log threat engagement
        if (currentThreat != null) {
            com.xeenaa.villagermanager.XeenaaVillagerManager.LOGGER.debug(
                "Guard {} attacking threat: {}",
                guard.getUuid(),
                currentThreat.getDescription()
            );
        }
    }

    @Override
    public void stop() {
        super.stop();
        currentThreat = null;
    }

    private boolean shouldEngageThreat(ThreatInfo threat, GuardData.GuardRole role) {
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        // Get tier-based detection range: 8 + (tier * 2) blocks
        int tier = guardData.getRankData().getCurrentTier();
        double detectionRange = 8.0 + (tier * 2.0);
        double distance = threat.getDistance();

        // Check specialization-specific targeting preferences
        boolean isMelee = isMeleeSpecialization(guardData.getRankData());
        boolean isRanged = isRangedSpecialization(guardData.getRankData());

        // Melee guards prefer closer targets
        if (isMelee && distance > detectionRange * 0.75) {
            // Only engage targets beyond 75% of detection range if high priority
            if (!threat.isHighPriority()) {
                return false;
            }
        }

        // Ranged guards prefer medium-distance targets
        if (isRanged && distance < 4.0 && !threat.isHighPriority()) {
            // Avoid engaging very close targets unless high priority
            return false;
        }

        // Role-specific engagement rules
        switch (role) {
            case PATROL:
                // Patrol guards engage all detected threats within their detection range
                return distance <= detectionRange * 1.25; // +25% range for patrol guards
            case GUARD:
                // Stationary guards engage threats within their area
                return isNearPost() && distance <= detectionRange;
            case FOLLOW:
                // Follow guards prioritize protecting their assigned player
                return isNearPlayer() && (threat.isHighPriority() || distance <= detectionRange * 0.9);
            default:
                return false;
        }
    }

    private boolean checkRoleConditions(GuardData.GuardRole role) {
        switch (role) {
            case PATROL:
                return true; // Patrol guards always look for threats
            case GUARD:
                return isNearPost(); // Stationary guards defend their area
            case FOLLOW:
                return isNearPlayer(); // Follow guards protect their assigned player
            default:
                return false;
        }
    }

    private boolean isGuard() {
        String professionId = guard.getVillagerData().getProfession().id();
        boolean isGuardProf = professionId.equals("xeenaa_villager_manager:guard") || professionId.equals("guard");
        System.out.println("GUARD ATTACK: Checking profession - " + professionId + " - isGuard: " + isGuardProf);
        return isGuardProf;
    }

    private boolean isNearPost() {
        // Check if guard is within their assigned area
        // For now, use a simple range check - TODO: Implement actual post tracking
        return true;
    }

    private boolean isNearPlayer() {
        // Check if guard is following a player and within range
        return guard.getWorld().getClosestPlayer(guard, 32) != null;
    }

    private boolean isMeleeSpecialization(com.xeenaa.villagermanager.data.rank.GuardRankData rankData) {
        if (rankData.getChosenPath() != null) {
            return rankData.getChosenPath().getId().equals("man_at_arms");
        }

        // Check current rank for specialization
        String rankId = rankData.getCurrentRank().getId();
        return rankId.startsWith("man_at_arms_") || rankId.equals("recruit");
    }

    private boolean isRangedSpecialization(com.xeenaa.villagermanager.data.rank.GuardRankData rankData) {
        if (rankData.getChosenPath() != null) {
            return rankData.getChosenPath().getId().equals("ranged");
        }

        // Check current rank for specialization
        String rankId = rankData.getCurrentRank().getId();
        return rankId.startsWith("marksman_");
    }
}