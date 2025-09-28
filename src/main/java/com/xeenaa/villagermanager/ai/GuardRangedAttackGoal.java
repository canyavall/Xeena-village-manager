package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Ranged attack goal for guard villagers with specialized behavior patterns.
 * Implements glass cannon specialization with distance maintenance and kiting tactics.
 */
public class GuardRangedAttackGoal extends BowAttackGoal<VillagerEntity> {
    private final VillagerEntity guard;
    private int attackCooldown = 0;
    private int repositionCooldown = 0;
    private int specialAbilityCooldown = 0;
    private BlockPos lastHighGroundPos = null;

    // Distance management constants
    private static final double PREFERRED_MIN_DISTANCE = 8.0;
    private static final double PREFERRED_MAX_DISTANCE = 15.0;
    private static final double DANGER_DISTANCE = 5.0;
    private static final double HIGH_GROUND_SEARCH_RADIUS = 10.0;

    // Ability cooldowns (in ticks)
    private static final int BASE_ATTACK_COOLDOWN = 30; // 1.5 seconds
    private static final int REPOSITION_COOLDOWN = 60; // 3 seconds
    private static final int MULTISHOT_COOLDOWN = 200; // 10 seconds
    private static final int EXPLOSIVE_SHOT_COOLDOWN = 300; // 15 seconds

    public GuardRangedAttackGoal(VillagerEntity guard, double speed, int attackInterval, float maxShootRange) {
        super(guard, speed, attackInterval, maxShootRange);
        this.guard = guard;
    }

    @Override
    public boolean canStart() {
        if (!isGuard()) {
            return false;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        // Only ranged guards use this goal
        GuardRankData rankData = guardData.getRankData();
        if (!isRangedSpecialization(rankData)) {
            return false;
        }

        return super.canStart();
    }

    @Override
    public void tick() {
        LivingEntity target = guard.getTarget();
        if (target == null) {
            return;
        }

        // Update cooldowns
        if (attackCooldown > 0) attackCooldown--;
        if (repositionCooldown > 0) repositionCooldown--;
        if (specialAbilityCooldown > 0) specialAbilityCooldown--;

        double distanceToTarget = guard.squaredDistanceTo(target);
        double actualDistance = Math.sqrt(distanceToTarget);

        // Handle positioning and movement
        handleRangedPositioning(target, actualDistance);

        // Handle attacking
        if (canAttack(actualDistance)) {
            performRangedAttack(target, actualDistance);
        }
    }

    /**
     * Handles positioning logic for ranged combat specialization
     */
    private void handleRangedPositioning(LivingEntity target, double distance) {
        // Emergency retreat if too close
        if (distance <= DANGER_DISTANCE) {
            performEmergencyRetreat(target);
            return;
        }

        // Seek high ground if available and not in danger
        if (repositionCooldown <= 0 && distance > DANGER_DISTANCE) {
            BlockPos highGround = findHighGround(target);
            if (highGround != null && !highGround.equals(lastHighGroundPos)) {
                guard.getNavigation().startMovingTo(highGround.getX(), highGround.getY(), highGround.getZ(), 1.0);
                lastHighGroundPos = highGround;
                repositionCooldown = REPOSITION_COOLDOWN;
                return;
            }
        }

        // Maintain optimal distance
        if (distance < PREFERRED_MIN_DISTANCE) {
            // Too close - kite away
            performKiting(target);
        } else if (distance > PREFERRED_MAX_DISTANCE) {
            // Too far - move closer but maintain range
            Vec3d targetPos = target.getPos();
            Vec3d guardPos = guard.getPos();
            Vec3d direction = targetPos.subtract(guardPos).normalize();
            Vec3d moveTarget = targetPos.subtract(direction.multiply(PREFERRED_MIN_DISTANCE + 2.0));

            guard.getNavigation().startMovingTo(moveTarget.x, moveTarget.y, moveTarget.z, 0.8);
        }
    }

    /**
     * Performs emergency retreat when enemy gets too close
     */
    private void performEmergencyRetreat(LivingEntity target) {
        Vec3d guardPos = guard.getPos();
        Vec3d targetPos = target.getPos();
        Vec3d retreatDirection = guardPos.subtract(targetPos).normalize();
        Vec3d retreatTarget = guardPos.add(retreatDirection.multiply(8.0));

        guard.getNavigation().startMovingTo(retreatTarget.x, retreatTarget.y, retreatTarget.z, 1.2);
        repositionCooldown = REPOSITION_COOLDOWN / 2; // Shorter cooldown for emergency moves
    }

    /**
     * Performs kiting behavior - moving away while maintaining line of sight
     */
    private void performKiting(LivingEntity target) {
        if (repositionCooldown > 0) return;

        Vec3d guardPos = guard.getPos();
        Vec3d targetPos = target.getPos();
        Vec3d kiteDirection = guardPos.subtract(targetPos).normalize();

        // Add some perpendicular movement for unpredictability
        Vec3d perpendicular = new Vec3d(-kiteDirection.z, 0, kiteDirection.x);
        if (guard.getRandom().nextBoolean()) {
            perpendicular = perpendicular.multiply(-1);
        }

        Vec3d kiteTarget = guardPos.add(kiteDirection.multiply(6.0)).add(perpendicular.multiply(2.0));
        guard.getNavigation().startMovingTo(kiteTarget.x, kiteTarget.y, kiteTarget.z, 1.0);

        repositionCooldown = REPOSITION_COOLDOWN;
    }

    /**
     * Finds high ground positions for tactical advantage
     */
    private BlockPos findHighGround(LivingEntity target) {
        BlockPos guardPos = guard.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        int guardY = guardPos.getY();

        // Search for positions 2-4 blocks higher within radius
        for (int y = guardY + 2; y <= guardY + 4; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = guardPos.add(x, y - guardY, z);

                    // Check if position is valid and provides line of sight to target
                    if (isValidHighGroundPosition(checkPos, targetPos)) {
                        double distance = Math.sqrt(checkPos.getSquaredDistance(targetPos));
                        if (distance >= PREFERRED_MIN_DISTANCE && distance <= PREFERRED_MAX_DISTANCE) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Checks if a position is valid for high ground positioning
     */
    private boolean isValidHighGroundPosition(BlockPos pos, BlockPos targetPos) {
        // Check if position is safe to stand on
        if (!guard.getWorld().getBlockState(pos).isAir() ||
            !guard.getWorld().getBlockState(pos.up()).isAir()) {
            return false;
        }

        if (guard.getWorld().getBlockState(pos.down()).isAir()) {
            return false;
        }

        // Check line of sight to target
        Vec3d start = Vec3d.ofCenter(pos);
        Vec3d end = Vec3d.ofCenter(targetPos);
        return guard.getWorld().raycast(new net.minecraft.world.RaycastContext(
            start, end,
            net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
            net.minecraft.world.RaycastContext.FluidHandling.NONE,
            guard
        )).getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }

    /**
     * Checks if the guard can attack based on distance and cooldowns
     */
    private boolean canAttack(double distance) {
        return attackCooldown <= 0 &&
               distance >= 4.0 &&
               distance <= PREFERRED_MAX_DISTANCE &&
               guard.canSee(guard.getTarget());
    }

    /**
     * Performs ranged attack with rank-based special abilities
     */
    private void performRangedAttack(LivingEntity target, double distance) {
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) return;

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();

        // Special abilities based on rank
        if (tier >= 5 && specialAbilityCooldown <= 0 && guard.getRandom().nextFloat() < 0.3f) {
            performExplosiveShot(target);
            specialAbilityCooldown = EXPLOSIVE_SHOT_COOLDOWN;
        } else if (tier >= 4 && specialAbilityCooldown <= 0 && guard.getRandom().nextFloat() < 0.4f) {
            performMultishot(target);
            specialAbilityCooldown = MULTISHOT_COOLDOWN;
        } else {
            performBasicRangedAttack(target, tier);
        }

        attackCooldown = getAttackCooldown(tier);
    }

    /**
     * Performs basic ranged attack with accuracy based on tier
     */
    private void performBasicRangedAttack(LivingEntity target, int tier) {
        ItemStack bow = new ItemStack(Items.BOW);
        ItemStack arrow = new ItemStack(Items.ARROW);

        // Improved accuracy for higher tiers
        float accuracy = 1.0f + (tier * 0.2f);
        float velocity = 1.6f;

        guard.swingHand(Hand.MAIN_HAND);

        PersistentProjectileEntity projectile = ProjectileUtil.createArrowProjectile(guard, arrow, velocity);

        // Calculate aim with accuracy bonus
        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        projectile.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, velocity, (14 - tier * 2));

        // Add slowing effect for tier 4+
        if (tier >= 4) {
            // Arrow will apply slowness effect on hit (handled in projectile impact)
        }

        guard.getWorld().spawnEntity(projectile);
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ENTITY_ARROW_SHOOT, guard.getSoundCategory(), 1.0f, 1.0f);
    }

    /**
     * Performs multishot ability (tier 4+)
     */
    private void performMultishot(LivingEntity target) {
        guard.swingHand(Hand.MAIN_HAND);

        // Fire 3 arrows in a spread
        for (int i = 0; i < 3; i++) {
            ItemStack arrow = new ItemStack(Items.ARROW);
            PersistentProjectileEntity projectile = ProjectileUtil.createArrowProjectile(guard, arrow, 1.6f);

            double deltaX = target.getX() - guard.getX();
            double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
            double deltaZ = target.getZ() - guard.getZ();
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            // Add spread to the arrows
            double spreadAngle = (i - 1) * 0.2; // -0.2, 0, 0.2 radians
            double cos = Math.cos(spreadAngle);
            double sin = Math.sin(spreadAngle);
            double newDeltaX = deltaX * cos - deltaZ * sin;
            double newDeltaZ = deltaX * sin + deltaZ * cos;

            projectile.setVelocity(newDeltaX, deltaY + horizontalDistance * 0.20000000298023224, newDeltaZ, 1.6f, 12);
            guard.getWorld().spawnEntity(projectile);
        }

        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ENTITY_ARROW_SHOOT, guard.getSoundCategory(), 1.0f, 0.8f);
    }

    /**
     * Performs explosive shot ability (tier 5)
     */
    private void performExplosiveShot(LivingEntity target) {
        guard.swingHand(Hand.MAIN_HAND);

        ItemStack arrow = new ItemStack(Items.ARROW);
        PersistentProjectileEntity projectile = ProjectileUtil.createArrowProjectile(guard, arrow, 2.0f);

        // Tag the arrow for explosive behavior (handled in projectile impact mixin)
        projectile.getDataTracker().set(projectile.getDataTracker().get(projectile.PIERCING_LEVEL), 999); // Special marker

        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        projectile.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, 2.0f, 8);
        guard.getWorld().spawnEntity(projectile);

        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, guard.getSoundCategory(), 1.0f, 1.2f);
    }

    /**
     * Gets attack cooldown based on tier
     */
    private int getAttackCooldown(int tier) {
        return Math.max(15, BASE_ATTACK_COOLDOWN - (tier * 3)); // Faster attacks at higher tiers
    }

    /**
     * Checks if the guard uses ranged specialization
     */
    private boolean isRangedSpecialization(GuardRankData rankData) {
        if (rankData.getChosenPath() != null) {
            return rankData.getChosenPath().getId().equals("marksman");
        }

        // Check current rank for specialization
        String rankId = rankData.getCurrentRank().getId();
        return rankId.startsWith("marksman_");
    }

    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }

    @Override
    public void stop() {
        super.stop();
        lastHighGroundPos = null;
    }
}