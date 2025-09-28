package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.threat.ThreatDetectionManager;
import com.xeenaa.villagermanager.threat.ThreatInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;
import java.util.List;

/**
 * AI goal that makes guards defend other villagers and players from threats.
 */
public class GuardDefendVillageGoal extends Goal {
    private final VillagerEntity guard;
    private LivingEntity targetEntity;
    private LivingEntity entityToDefend;
    private ThreatInfo currentThreat;
    private int cooldown;

    public GuardDefendVillageGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!isGuard()) {
            return false;
        }

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        // Use the new threat detection system
        if (guard.getWorld() instanceof ServerWorld serverWorld) {
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(serverWorld);
            ThreatInfo threat = threatManager.detectPrimaryThreat(guard);

            if (threat != null && threat.isHighPriority()) {
                this.currentThreat = threat;
                this.targetEntity = threat.getThreatEntity();
                this.entityToDefend = threat.getVictimEntity();
                return true;
            }

            // Fall back to proximity threats for village defense
            if (threat != null && threat.isProximityThreat()) {
                // Only engage proximity threats if guard role allows it
                switch (guardData.getRole()) {
                    case PATROL:
                    case GUARD:
                        this.currentThreat = threat;
                        this.targetEntity = threat.getThreatEntity();
                        this.entityToDefend = threat.getVictimEntity();
                        return true;
                    case FOLLOW:
                        // Follow guards only engage proximity threats near their player
                        if (isNearAssignedPlayer()) {
                            this.currentThreat = threat;
                            this.targetEntity = threat.getThreatEntity();
                            this.entityToDefend = threat.getVictimEntity();
                            return true;
                        }
                        break;
                }
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (targetEntity == null || !targetEntity.isAlive()) {
            return false;
        }

        if (entityToDefend != null && !entityToDefend.isAlive()) {
            return false;
        }

        // Check if threat is still valid using the threat detection system
        if (guard.getWorld() instanceof ServerWorld serverWorld && currentThreat != null) {
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(serverWorld);
            ThreatInfo updatedThreat = threatManager.getThreatInfo(targetEntity);

            // If threat is no longer detected or priority dropped significantly, stop
            if (updatedThreat == null || (!updatedThreat.isHighPriority() && !updatedThreat.isProximityThreat())) {
                return false;
            }
        }

        // Continue defending until threat is eliminated or too far away
        double maxDistance = calculateMaxEngagementDistance();
        return guard.squaredDistanceTo(targetEntity) < maxDistance * maxDistance;
    }

    @Override
    public void start() {
        guard.setTarget(targetEntity);
        cooldown = 0;

        // Log threat engagement for debugging
        if (currentThreat != null) {
            com.xeenaa.villagermanager.XeenaaVillagerManager.LOGGER.debug(
                "Guard {} engaging threat: {}",
                guard.getUuid(),
                currentThreat.getDescription()
            );
        }

        // Alert nearby guards - the threat detection system handles this more efficiently
        alertNearbyGuards();
    }

    @Override
    public void stop() {
        guard.setTarget(null);
        targetEntity = null;
        entityToDefend = null;
        currentThreat = null;
        cooldown = 40; // 2 second cooldown
    }

    private void alertNearbyGuards() {
        // Let the threat detection system handle this more efficiently
        // The system already alerts guards when high-priority threats are detected
        if (currentThreat != null && !currentThreat.isHighPriority()) {
            // Only manually alert for lower priority threats
            List<VillagerEntity> nearbyGuards = guard.getWorld().getEntitiesByClass(
                VillagerEntity.class,
                guard.getBoundingBox().expand(24.0),
                villager -> villager != guard && isGuard(villager) && villager.getTarget() == null
            );

            for (VillagerEntity otherGuard : nearbyGuards) {
                otherGuard.setTarget(targetEntity);
            }
        }
    }

    private double calculateMaxEngagementDistance() {
        if (currentThreat == null) {
            return 16.0; // Default range
        }

        // Extend range for high priority threats
        if (currentThreat.isHighPriority()) {
            return 20.0;
        }

        // Standard range for proximity threats
        return 16.0;
    }

    private boolean isNearAssignedPlayer() {
        // For follow guards, check if assigned player is nearby
        // TODO: Implement actual player assignment tracking
        return guard.getWorld().getClosestPlayer(guard, 32) != null;
    }

    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }

    private boolean isGuard(VillagerEntity villager) {
        return villager.getVillagerData().getProfession().id().equals("guard");
    }
}