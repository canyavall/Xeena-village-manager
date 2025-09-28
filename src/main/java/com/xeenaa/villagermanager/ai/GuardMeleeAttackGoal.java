package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.List;

/**
 * Melee attack goal for guard villagers with tank specialization behaviors.
 * Implements aggressive positioning, area damage, shield mechanics, and special abilities.
 */
public class GuardMeleeAttackGoal extends MeleeAttackGoal {
    private final VillagerEntity guard;
    private int attackCooldown = 0;
    private int specialAbilityCooldown = 0;
    private int tauntCooldown = 0;
    private boolean isBlocking = false;
    private int blockingDuration = 0;

    // Combat constants
    private static final double PREFERRED_COMBAT_RANGE = 3.0;
    private static final double AREA_DAMAGE_RADIUS = 2.5;
    private static final int SWEEP_ATTACK_COOLDOWN = 120; // 6 seconds
    private static final int TAUNT_COOLDOWN = 160; // 8 seconds
    private static final int BLOCKING_DURATION = 40; // 2 seconds

    public GuardMeleeAttackGoal(VillagerEntity guard, double speed, boolean pauseWhenMobIdle) {
        super(guard, speed, pauseWhenMobIdle);
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

        // Only melee guards use this goal
        GuardRankData rankData = guardData.getRankData();
        if (!isMeleeSpecialization(rankData)) {
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
        if (specialAbilityCooldown > 0) specialAbilityCooldown--;
        if (tauntCooldown > 0) tauntCooldown--;
        if (blockingDuration > 0) {
            blockingDuration--;
            if (blockingDuration <= 0) {
                isBlocking = false;
            }
        }

        double distanceToTarget = guard.squaredDistanceTo(target);
        double actualDistance = Math.sqrt(distanceToTarget);

        // Handle tank positioning and combat
        handleTankPositioning(target, actualDistance);
        handleShieldMechanics(target, actualDistance);
        handleSpecialAbilities(target, actualDistance);

        super.tick();
    }

    @Override
    protected void attack(LivingEntity target) {
        if (attackCooldown > 0 || isBlocking) {
            return;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return;
        }

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();

        // Check for special attacks
        if (tier >= 5 && specialAbilityCooldown <= 0 && guard.getRandom().nextFloat() < 0.4f) {
            performSweepAttack(target, tier);
            specialAbilityCooldown = SWEEP_ATTACK_COOLDOWN;
        } else {
            performBasicMeleeAttack(target, tier);
        }

        // Set attack cooldown based on tier
        attackCooldown = getAttackCooldown(tier);
    }


    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }


    /**
     * Handles aggressive tank positioning for melee combat
     */
    private void handleTankPositioning(LivingEntity target, double distance) {
        // Aggressive positioning - move directly toward enemy
        if (distance > PREFERRED_COMBAT_RANGE) {
            // Close the distance aggressively
            guard.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), 1.2);
        } else if (distance < 1.5) {
            // Too close for effective sword combat - back up slightly
            Vec3d guardPos = guard.getPos();
            Vec3d targetPos = target.getPos();
            Vec3d backupDirection = guardPos.subtract(targetPos).normalize();
            Vec3d backupTarget = guardPos.add(backupDirection.multiply(0.5));

            guard.getNavigation().startMovingTo(backupTarget.x, backupTarget.y, backupTarget.z, 0.8);
        }
    }

    /**
     * Handles shield blocking mechanics for higher tier guards
     */
    private void handleShieldMechanics(LivingEntity target, double distance) {
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) return;

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();

        // Shield blocking available at tier 4+
        if (tier >= 4 && !isBlocking && distance <= 4.0) {
            // Start blocking if enemy is about to attack (simplified logic)
            if (target.handSwinging && guard.getRandom().nextFloat() < 0.6f) {
                startBlocking();
            }
        }
    }

    /**
     * Handles special abilities based on rank
     */
    private void handleSpecialAbilities(LivingEntity target, double distance) {
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) return;

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();

        // Taunt ability at tier 5
        if (tier >= 5 && tauntCooldown <= 0 && distance <= 8.0) {
            if (guard.getRandom().nextFloat() < 0.2f) {
                performTaunt();
                tauntCooldown = TAUNT_COOLDOWN;
            }
        }
    }

    /**
     * Performs basic melee attack with tier-based enhancements
     */
    private void performBasicMeleeAttack(LivingEntity target, int tier) {
        // Base damage from entity attributes (includes rank-based scaling)
        float baseDamage = (float) guard.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        // Tier 3+ gets area damage bonus
        if (tier >= 3) {
            baseDamage *= 1.2f; // 20% damage bonus
        }

        // Show attack animation
        guard.swingHand(Hand.MAIN_HAND);

        // Deal damage
        DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        boolean damaged = target.damage(damageSource, baseDamage);

        if (damaged) {
            // Apply enhanced knockback for higher tiers
            double knockbackStrength = 0.5 + (tier * 0.1);
            target.takeKnockback(knockbackStrength,
                guard.getX() - target.getX(),
                guard.getZ() - target.getZ());

            // Tier 3+ gets minor area damage
            if (tier >= 3) {
                damageNearbyEnemies(target, 1.5, baseDamage * 0.5f);
            }

            // Play attack sound
            guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, guard.getSoundCategory(), 1.0f, 1.0f);
        }
    }

    /**
     * Performs sweep attack (tier 5 special ability)
     */
    private void performSweepAttack(LivingEntity target, int tier) {
        float baseDamage = (float) guard.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float sweepDamage = baseDamage * 1.5f; // 50% more damage for sweep

        // Show enhanced attack animation
        guard.swingHand(Hand.MAIN_HAND);

        // Deal damage to primary target
        DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        target.damage(damageSource, sweepDamage);

        // Apply strong knockback to primary target
        target.takeKnockback(1.5,
            guard.getX() - target.getX(),
            guard.getZ() - target.getZ());

        // Damage all nearby enemies
        damageNearbyEnemies(target, AREA_DAMAGE_RADIUS, sweepDamage * 0.8f);

        // Enhanced visual and audio effects
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, guard.getSoundCategory(), 1.0f, 0.8f);
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, guard.getSoundCategory(), 0.8f, 1.2f);
    }

    /**
     * Damages enemies within radius of the target
     */
    private void damageNearbyEnemies(LivingEntity primaryTarget, double radius, float damage) {
        Box searchBox = Box.of(primaryTarget.getPos(), radius * 2, radius * 2, radius * 2);
        List<LivingEntity> nearbyEntities = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard && entity != primaryTarget &&
                     guard.canTarget(entity) && entity.isAlive());

        for (LivingEntity entity : nearbyEntities) {
            if (guard.squaredDistanceTo(entity) <= radius * radius) {
                DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
                entity.damage(damageSource, damage);

                // Apply knockback
                entity.takeKnockback(0.3,
                    guard.getX() - entity.getX(),
                    guard.getZ() - entity.getZ());
            }
        }
    }

    /**
     * Starts blocking stance
     */
    private void startBlocking() {
        isBlocking = true;
        blockingDuration = BLOCKING_DURATION;

        // Apply temporary resistance effect
        guard.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, BLOCKING_DURATION, 1));

        // Play shield sound
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ITEM_SHIELD_BLOCK, guard.getSoundCategory(), 1.0f, 1.0f);
    }

    /**
     * Performs taunt ability to draw enemy attention
     */
    private void performTaunt() {
        // Find nearby enemies and force them to target this guard
        Box searchBox = Box.of(guard.getPos(), 16, 8, 16);
        List<LivingEntity> nearbyEnemies = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard && guard.canTarget(entity) && entity.isAlive());

        for (LivingEntity enemy : nearbyEnemies) {
            if (guard.squaredDistanceTo(enemy) <= 64) { // 8 block radius
                // Force enemy to target this guard (simplified - would need proper AI goal manipulation)
                if (enemy instanceof net.minecraft.entity.mob.MobEntity mobEntity) {
                    mobEntity.setTarget(guard);
                }
            }
        }

        // Visual and audio feedback
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            SoundEvents.ENTITY_VILLAGER_CELEBRATE, guard.getSoundCategory(), 1.0f, 0.8f);
    }

    /**
     * Gets attack cooldown based on tier
     */
    private int getAttackCooldown(int tier) {
        return Math.max(15, 25 - (tier * 2)); // Faster attacks at higher tiers
    }

    /**
     * Checks if the guard uses melee specialization
     */
    private boolean isMeleeSpecialization(GuardRankData rankData) {
        if (rankData.getChosenPath() != null) {
            return rankData.getChosenPath().getId().equals("man_at_arms");
        }

        // Check current rank for specialization
        String rankId = rankData.getCurrentRank().getId();
        return rankId.startsWith("man_at_arms_") || rankId.equals("recruit");
    }

    @Override
    public void stop() {
        super.stop();
        isBlocking = false;
        blockingDuration = 0;
    }
}