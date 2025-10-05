package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.Optional;

/**
 * AI goal that makes guards retreat to safety when their health is low.
 * Guards will disengage from combat, find a safe location, and regenerate health.
 */
public class GuardRetreatGoal extends Goal {
    private final VillagerEntity guard;
    private BlockPos retreatTarget;
    private int retreatTicks;
    private int regenerationTicks;

    private static final float LOW_HEALTH_THRESHOLD = 0.2f; // 20% health (was 30%)
    private static final float SAFE_HEALTH_THRESHOLD = 0.5f; // 50% health (was 60%)
    private static final double RETREAT_DISTANCE = 20.0;
    private static final int MAX_RETREAT_TIME = 600; // 30 seconds
    private static final int REGENERATION_INTERVAL = 40; // 2 seconds between heals

    public GuardRetreatGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.MOVE, Control.TARGET));
    }

    @Override
    public boolean canStart() {
        // Only guards can retreat
        if (!isGuard()) {
            return false;
        }

        // Check if health is low
        float healthPercent = guard.getHealth() / guard.getMaxHealth();
        if (healthPercent > LOW_HEALTH_THRESHOLD) {
            return false;
        }

        // Only retreat if in combat or recently damaged
        if (guard.getTarget() == null && guard.getAttacker() == null) {
            return false;
        }

        // Find a retreat location
        retreatTarget = findRetreatLocation();
        return retreatTarget != null;
    }

    @Override
    public boolean shouldContinue() {
        // Stop retreating if health is back to safe levels
        float healthPercent = guard.getHealth() / guard.getMaxHealth();
        if (healthPercent >= SAFE_HEALTH_THRESHOLD) {
            return false;
        }

        // Stop if retreat took too long
        if (retreatTicks > MAX_RETREAT_TIME) {
            return false;
        }

        // Continue retreating
        return true;
    }

    @Override
    public void start() {
        // Clear target to stop combat
        guard.setTarget(null);

        // Start moving to retreat location
        if (retreatTarget != null) {
            guard.getNavigation().startMovingTo(
                retreatTarget.getX(),
                retreatTarget.getY(),
                retreatTarget.getZ(),
                1.0 // Full speed retreat
            );
        }

        retreatTicks = 0;
        regenerationTicks = 0;
    }

    @Override
    public void tick() {
        retreatTicks++;
        regenerationTicks++;

        // Continue moving to retreat location if not there yet
        if (retreatTarget != null && !hasReachedRetreatLocation()) {
            if (!guard.getNavigation().isFollowingPath()) {
                guard.getNavigation().startMovingTo(
                    retreatTarget.getX(),
                    retreatTarget.getY(),
                    retreatTarget.getZ(),
                    1.0
                );
            }
        } else {
            // At retreat location, regenerate health
            guard.getNavigation().stop();

            // Passive regeneration
            if (regenerationTicks >= REGENERATION_INTERVAL) {
                regenerateHealth();
                regenerationTicks = 0;
            }
        }
    }

    @Override
    public void stop() {
        guard.getNavigation().stop();
        retreatTarget = null;
        retreatTicks = 0;
        regenerationTicks = 0;
    }

    /**
     * Finds a safe location to retreat to
     */
    private BlockPos findRetreatLocation() {
        // Try to retreat to guard post if available
        BlockPos guardPost = findGuardPost();
        if (guardPost != null) {
            return guardPost;
        }

        // Otherwise, retreat away from current threat
        if (guard.getAttacker() != null) {
            Vec3d guardPos = guard.getPos();
            Vec3d attackerPos = guard.getAttacker().getPos();
            Vec3d retreatDirection = guardPos.subtract(attackerPos).normalize();
            Vec3d retreatPos = guardPos.add(retreatDirection.multiply(RETREAT_DISTANCE));

            return new BlockPos(
                (int) retreatPos.x,
                (int) retreatPos.y,
                (int) retreatPos.z
            );
        }

        // Fallback: retreat to current position
        return guard.getBlockPos();
    }

    /**
     * Finds the guard's associated guard post
     */
    private BlockPos findGuardPost() {
        // Check if guard has a job site (guard post)
        Optional<net.minecraft.util.math.GlobalPos> jobSite = guard.getBrain()
            .getOptionalMemory(net.minecraft.entity.ai.brain.MemoryModuleType.JOB_SITE);

        if (jobSite.isPresent()) {
            net.minecraft.util.math.GlobalPos globalPos = jobSite.get();
            // Check if job site is in the current dimension
            if (globalPos.dimension().equals(guard.getWorld().getRegistryKey())) {
                BlockPos pos = globalPos.pos();
                // Verify it's actually a guard post
                if (guard.getWorld().getBlockState(pos).getBlock()
                    instanceof com.xeenaa.villagermanager.block.GuardPostBlock) {
                    return pos;
                }
            }
        }

        return null;
    }

    /**
     * Checks if the guard has reached the retreat location
     */
    private boolean hasReachedRetreatLocation() {
        if (retreatTarget == null) {
            return true;
        }

        double distance = guard.getBlockPos().getSquaredDistance(retreatTarget);
        return distance < 4.0; // Within 2 blocks
    }

    /**
     * Regenerates health for the guard
     */
    private void regenerateHealth() {
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return;
        }

        // Regeneration amount scales with tier
        int tier = guardData.getRankData().getCurrentTier();
        float healAmount = 1.0f + (tier * 0.5f); // 1.0-3.0 HP per interval

        float newHealth = Math.min(guard.getHealth() + healAmount, guard.getMaxHealth());
        guard.setHealth(newHealth);
    }

    /**
     * Checks if this villager is a guard
     */
    private boolean isGuard() {
        String professionId = guard.getVillagerData().getProfession().id();
        return professionId.equals("xeenaa_villager_manager:guard") || professionId.equals("guard");
    }
}
