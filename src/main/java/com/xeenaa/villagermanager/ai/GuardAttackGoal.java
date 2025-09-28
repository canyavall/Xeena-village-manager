package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
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
            return false;
        }

        // Check if guard data exists
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        // Use threat detection system for better targeting
        if (guard.getWorld() instanceof ServerWorld serverWorld) {
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(serverWorld);
            ThreatInfo threat = threatManager.detectPrimaryThreat(guard);

            if (threat != null) {
                // Check role-specific engagement rules
                if (shouldEngageThreat(threat, guardData.getRole())) {
                    this.currentThreat = threat;
                    this.target = threat.getThreatEntity();
                    return true;
                }
            }
        }

        // Fall back to parent implementation if no threat system available
        return super.canStart() && checkRoleConditions(guardData.getRole());
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
        switch (role) {
            case PATROL:
                // Patrol guards engage all detected threats
                return true;
            case GUARD:
                // Stationary guards engage threats within their area
                return isNearPost() && threat.getDistance() <= 20.0;
            case FOLLOW:
                // Follow guards prioritize protecting their assigned player
                return isNearPlayer() && (threat.isHighPriority() || threat.getDistance() <= 12.0);
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
        return guard.getVillagerData().getProfession().id().equals("guard");
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
}