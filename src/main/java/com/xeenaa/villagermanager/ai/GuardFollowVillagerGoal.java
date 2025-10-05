package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.config.GuardMode;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * AI goal that makes guards follow nearby villagers or players for protection.
 * Guards maintain a protective distance based on their specialization and role.
 */
public class GuardFollowVillagerGoal extends Goal {
    private final VillagerEntity guard;
    private LivingEntity followTarget;
    private int updatePathCooldown;
    private int timeToRecalculatePath;

    private static final TargetPredicate FOLLOW_PREDICATE = TargetPredicate.createNonAttackable()
        .setBaseMaxDistance(16.0)
        .ignoreVisibility();

    // Distance management
    private static final double MIN_FOLLOW_DISTANCE = 3.0;
    private static final double MAX_FOLLOW_DISTANCE = 12.0;
    private static final double FOLLOW_SPEED = 0.6;

    public GuardFollowVillagerGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        System.out.println("GUARD FOLLOW: canStart() called for guard " + guard.getUuid());

        // Don't follow if in combat
        if (guard.getTarget() != null) {
            System.out.println("GUARD FOLLOW: In combat, skipping");
            return false;
        }

        // Check if this villager has guard data (better check than profession)
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            System.out.println("GUARD FOLLOW: No guard data for " + guard.getUuid() + " - not a guard");
            return false;
        }

        // Check if guard mode is FOLLOW
        GuardMode guardMode = guardData.getBehaviorConfig().guardMode();
        System.out.println("GUARD FOLLOW: Guard " + guard.getUuid() + " mode is " + guardMode);
        if (guardMode != GuardMode.FOLLOW) {
            return false;
        }

        // Find someone to follow (prioritize follow target player if set)
        followTarget = findFollowTarget(guardData);
        System.out.println("GUARD FOLLOW: Found follow target: " + (followTarget != null ? followTarget.getName().getString() : "null"));
        return followTarget != null;
    }

    @Override
    public boolean shouldContinue() {
        // Stop if in combat
        if (guard.getTarget() != null) {
            return false;
        }

        // Stop if guard mode changed (important for mode switching)
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null || guardData.getBehaviorConfig().guardMode() != GuardMode.FOLLOW) {
            return false;
        }

        // Stop if follow target is invalid
        if (followTarget == null || !followTarget.isAlive()) {
            return false;
        }

        // Stop if target is too far away
        double distance = guard.squaredDistanceTo(followTarget);
        if (distance > MAX_FOLLOW_DISTANCE * MAX_FOLLOW_DISTANCE * 4) { // Give some leeway
            return false;
        }

        // Continue following
        return true;
    }

    @Override
    public void start() {
        timeToRecalculatePath = 0;
        updatePathCooldown = 0;
    }

    @Override
    public void tick() {
        if (followTarget == null) {
            return;
        }

        // Look at the follow target occasionally
        guard.getLookControl().lookAt(followTarget, 10.0F, (float) guard.getMaxLookPitchChange());

        // Update path periodically
        if (--timeToRecalculatePath <= 0) {
            timeToRecalculatePath = 10; // Update every 0.5 seconds

            double distance = guard.squaredDistanceTo(followTarget);

            // Only move if too far away
            if (distance > MIN_FOLLOW_DISTANCE * MIN_FOLLOW_DISTANCE) {
                if (distance < MAX_FOLLOW_DISTANCE * MAX_FOLLOW_DISTANCE) {
                    // Follow at normal speed
                    guard.getNavigation().startMovingTo(followTarget, FOLLOW_SPEED);
                } else {
                    // Run to catch up if far away
                    guard.getNavigation().startMovingTo(followTarget, FOLLOW_SPEED * 1.5);
                }
            } else {
                // Stop if too close
                guard.getNavigation().stop();
            }
        }

        // Handle path failure
        if (updatePathCooldown > 0) {
            updatePathCooldown--;
        } else if (!guard.getNavigation().isFollowingPath()) {
            updatePathCooldown = 20; // Retry after 1 second
            // Try to recalculate path
            guard.getNavigation().startMovingTo(followTarget, FOLLOW_SPEED);
        }
    }

    @Override
    public void stop() {
        followTarget = null;
        guard.getNavigation().stop();
        timeToRecalculatePath = 0;
        updatePathCooldown = 0;
    }

    /**
     * Finds a suitable target to follow based on guard configuration and proximity.
     * Prioritizes the follow target player if set in configuration.
     */
    private LivingEntity findFollowTarget(GuardData guardData) {
        UUID followTargetPlayerId = guardData.getBehaviorConfig().followTargetPlayerId();

        // If a specific player is set as follow target, try to find them first
        if (followTargetPlayerId != null && guard.getWorld() instanceof ServerWorld serverWorld) {
            PlayerEntity targetPlayer = serverWorld.getPlayerByUuid(followTargetPlayerId);
            if (targetPlayer != null && !targetPlayer.isSpectator()) {
                double distance = guard.squaredDistanceTo(targetPlayer);
                if (distance <= MAX_FOLLOW_DISTANCE * MAX_FOLLOW_DISTANCE) {
                    System.out.println("GUARD FOLLOW: Found specific player " + targetPlayer.getName().getString() + " at distance " + Math.sqrt(distance));
                    return targetPlayer;
                }
            }
        }

        // Fall back to nearest player if follow target not found
        PlayerEntity nearestPlayer = guard.getWorld().getClosestPlayer(guard, MAX_FOLLOW_DISTANCE);
        if (nearestPlayer != null && !nearestPlayer.isSpectator()) {
            System.out.println("GUARD FOLLOW: Found nearest player " + nearestPlayer.getName().getString());
            return nearestPlayer;
        }

        // Look for villagers to protect as last resort
        List<VillagerEntity> nearbyVillagers = guard.getWorld().getTargets(
            VillagerEntity.class,
            FOLLOW_PREDICATE,
            guard,
            guard.getBoundingBox().expand(MAX_FOLLOW_DISTANCE)
        );

        VillagerEntity bestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (VillagerEntity villager : nearbyVillagers) {
            // Don't follow self or other guards
            if (villager == guard || isGuard(villager)) {
                continue;
            }

            double distance = guard.squaredDistanceTo(villager);

            // Prioritize villagers that are closer or in danger
            boolean inDanger = villager.getAttacker() != null ||
                              hasNearbyThreats(villager);

            // Prefer villagers in danger, or closest if none in danger
            if (inDanger || (bestTarget == null && distance < closestDistance)) {
                bestTarget = villager;
                closestDistance = distance;

                // If this villager is in danger, prioritize them immediately
                if (inDanger) {
                    break;
                }
            }
        }

        return bestTarget;
    }

    /**
     * Checks if a villager has nearby threats
     */
    private boolean hasNearbyThreats(VillagerEntity villager) {
        return !villager.getWorld().getEntitiesByClass(
            net.minecraft.entity.mob.HostileEntity.class,
            villager.getBoundingBox().expand(8.0),
            hostile -> hostile.isAlive() && hostile.canSee(villager)
        ).isEmpty();
    }

    /**
     * Checks if another villager is a guard by checking for guard data
     */
    private boolean isGuard(VillagerEntity villager) {
        return GuardDataManager.get(villager.getWorld()).getGuardData(villager.getUuid()) != null;
    }

    /**
     * Gets the current follow target
     */
    public LivingEntity getFollowTarget() {
        return followTarget;
    }

    /**
     * Sets a specific follow target (useful for player-assigned following)
     */
    public void setFollowTarget(LivingEntity target) {
        this.followTarget = target;
    }
}