package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.Random;
import java.util.Optional;
import java.util.List;

/**
 * AI goal that makes guards patrol around their assigned area when not in combat.
 * Guards will randomly move around within a defined radius of their patrol center.
 */
public class GuardPatrolGoal extends Goal {
    private final VillagerEntity guard;
    private final Random random = new Random();

    private BlockPos patrolCenter;
    private BlockPos currentTarget;
    private int patrolRadius;
    private int cooldownTicks;
    private int patrolTicks;

    private static final int DEFAULT_PATROL_RADIUS = 16;
    private static final int MIN_PATROL_DISTANCE = 4;
    private static final int PATROL_COOLDOWN = 100; // 5 seconds between patrol moves
    private static final int MAX_PATROL_TIME = 1200; // 1 minute max patrol time

    public GuardPatrolGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.MOVE));
        this.patrolRadius = DEFAULT_PATROL_RADIUS;
    }

    @Override
    public boolean canStart() {
        // Only guards can patrol
        if (!isGuard()) {
            return false;
        }

        // Don't patrol if in combat or has a target
        if (guard.getTarget() != null) {
            return false;
        }

        // Check cooldown
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        // Only patrol if role allows it
        GuardData.GuardRole role = guardData.getRole();
        if (role != GuardData.GuardRole.PATROL && role != GuardData.GuardRole.GUARD) {
            return false;
        }

        // Set patrol center to guard post workstation if available
        if (patrolCenter == null) {
            BlockPos workstation = findGuardPostWorkstation();
            if (workstation != null) {
                patrolCenter = workstation;
            } else {
                // Fallback to current position if no workstation found
                patrolCenter = guard.getBlockPos();
            }
        }

        // Check if we need to find a new patrol target
        if (currentTarget == null || hasReachedTarget() || patrolTicks > MAX_PATROL_TIME) {
            currentTarget = findBasicPatrolTarget();
            patrolTicks = 0;
        }

        return currentTarget != null;
    }

    @Override
    public boolean shouldContinue() {
        // Stop if in combat
        if (guard.getTarget() != null) {
            return false;
        }

        // Stop if reached target or took too long
        if (hasReachedTarget() || patrolTicks > MAX_PATROL_TIME) {
            return false;
        }

        // Continue if still moving to target
        return currentTarget != null && guard.getNavigation().isFollowingPath();
    }

    @Override
    public void start() {
        if (currentTarget != null) {
            guard.getNavigation().startMovingTo(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), 0.5);
            patrolTicks = 0;
        }
    }

    @Override
    public void tick() {
        patrolTicks++;

        // Check if navigation failed
        if (!guard.getNavigation().isFollowingPath() && currentTarget != null) {
            // Try to find a new target if navigation failed
            currentTarget = findBasicPatrolTarget();
            if (currentTarget != null) {
                guard.getNavigation().startMovingTo(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), 0.5);
            }
        }
    }

    @Override
    public void stop() {
        guard.getNavigation().stop();
        currentTarget = null;
        cooldownTicks = PATROL_COOLDOWN;
        patrolTicks = 0;
    }

    /**
     * Finds a basic patrol target
     */
    private BlockPos findBasicPatrolTarget() {
        if (patrolCenter == null) {
            patrolCenter = guard.getBlockPos();
        }

        // Try multiple times to find a good patrol target
        for (int attempts = 0; attempts < 10; attempts++) {
            int offsetX = random.nextInt(patrolRadius * 2) - patrolRadius;
            int offsetZ = random.nextInt(patrolRadius * 2) - patrolRadius;

            BlockPos candidate = patrolCenter.add(offsetX, 0, offsetZ);

            // Find the surface at this position
            BlockPos surface = findSurface(candidate);
            if (surface != null) {
                double distance = guard.getBlockPos().getSquaredDistance(surface);

                // Make sure it's not too close (avoid micro-movements)
                if (distance >= MIN_PATROL_DISTANCE * MIN_PATROL_DISTANCE) {
                    // Check if the position is navigable
                    if (isValidPatrolPosition(surface)) {
                        return surface;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds the surface level at a given position
     */
    private BlockPos findSurface(BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();

        // Start from a bit above and work down
        mutable.setY(Math.min(pos.getY() + 5, guard.getWorld().getTopY()));

        // Find the first solid block
        while (mutable.getY() > guard.getWorld().getBottomY()) {
            if (guard.getWorld().getBlockState(mutable).isSolidBlock(guard.getWorld(), mutable)) {
                // Check if there's space above for the guard
                BlockPos above1 = mutable.up();
                BlockPos above2 = mutable.up(2);

                if (!guard.getWorld().getBlockState(above1).isSolidBlock(guard.getWorld(), above1) &&
                    !guard.getWorld().getBlockState(above2).isSolidBlock(guard.getWorld(), above2)) {
                    return above1; // Return the position where the guard can stand
                }
            }
            mutable.move(0, -1, 0);
        }

        return null;
    }

    /**
     * Checks if a position is valid for patrolling
     */
    private boolean isValidPatrolPosition(BlockPos pos) {
        // Check if position is within world bounds
        if (!guard.getWorld().isInBuildLimit(pos)) {
            return false;
        }

        // Basic validation - check if the space is clear for movement
        // This is a simplified check, the navigation system will handle detailed pathfinding
        return !guard.getWorld().getBlockState(pos).isSolidBlock(guard.getWorld(), pos) &&
               !guard.getWorld().getBlockState(pos.up()).isSolidBlock(guard.getWorld(), pos.up());
    }

    /**
     * Checks if the guard has reached the current patrol target
     */
    private boolean hasReachedTarget() {
        if (currentTarget == null) {
            return true;
        }

        double distance = guard.getBlockPos().getSquaredDistance(currentTarget);
        return distance < 4.0; // Within 2 blocks
    }

    /**
     * Checks if this villager is a guard
     */
    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }

    /**
     * Checks if another villager is a guard
     */
    private boolean isGuard(VillagerEntity villager) {
        return villager.getVillagerData().getProfession().id().equals("guard");
    }

    /**
     * Sets the patrol center for this guard
     */
    public void setPatrolCenter(BlockPos center) {
        this.patrolCenter = center;
    }

    /**
     * Sets the patrol radius for this guard
     */
    public void setPatrolRadius(int radius) {
        this.patrolRadius = Math.max(4, Math.min(32, radius)); // Clamp between 4 and 32 blocks
    }

    /**
     * Finds the guard post workstation for this guard
     */
    private BlockPos findGuardPostWorkstation() {
        // Check if guard has a job site (workstation)
        Optional<net.minecraft.util.math.GlobalPos> jobSite = guard.getBrain().getOptionalMemory(net.minecraft.entity.ai.brain.MemoryModuleType.JOB_SITE);
        if (jobSite.isPresent()) {
            net.minecraft.util.math.GlobalPos globalPos = jobSite.get();
            // Check if job site is in the current dimension
            if (globalPos.dimension().equals(guard.getWorld().getRegistryKey())) {
                BlockPos pos = globalPos.pos();
                // Verify it's actually a guard post
                if (guard.getWorld().getBlockState(pos).getBlock() instanceof com.xeenaa.villagermanager.block.GuardPostBlock) {
                    return pos;
                }
            }
        }

        // If no job site in memory, search for nearby guard posts
        return findNearbyGuardPost();
    }

    /**
     * Searches for a guard post within a reasonable range
     */
    private BlockPos findNearbyGuardPost() {
        BlockPos guardPos = guard.getBlockPos();
        int searchRadius = 48; // Search within 48 blocks

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius/2; y <= searchRadius/2; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = guardPos.add(x, y, z);
                    if (guard.getWorld().getBlockState(checkPos).getBlock() instanceof com.xeenaa.villagermanager.block.GuardPostBlock) {
                        return checkPos;
                    }
                }
            }
        }

        return null;
    }
}