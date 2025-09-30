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
import java.util.Optional;

/**
 * Melee attack goal for guard villagers with tank specialization behaviors.
 * Implements aggressive positioning, area damage, shield mechanics, and special abilities.
 */
public class GuardMeleeAttackGoal extends MeleeAttackGoal {
    private final VillagerEntity guard;
    private int attackCooldown = 0;

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

        // Look at target
        guard.getLookControl().lookAt(target, 30.0f, 30.0f);

        // Calculate distance to target
        double distanceToTarget = guard.squaredDistanceTo(target);
        double actualDistance = Math.sqrt(distanceToTarget);

        // Move toward target if not in melee range
        if (actualDistance > 3.0) {
            guard.getNavigation().startMovingTo(target, 1.0);
        }

        super.tick();
    }

    @Override
    protected void attack(LivingEntity target) {
        if (attackCooldown > 0) {
            return;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return;
        }

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();

        // Perform enhanced melee attack
        performBasicMeleeAttackGuard(target, tier);

        // Set attack cooldown based on tier
        attackCooldown = getAttackCooldown(tier);
    }


    private boolean isGuard() {
        String professionId = guard.getVillagerData().getProfession().id();
        return professionId.equals("xeenaa_villager_manager:guard") || professionId.equals("guard");
    }


    /**
     * Handles basic melee positioning
     */
    private void handleBasicMeleePositioning(LivingEntity target, double distance) {
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
     * Performs basic melee attack with tier-based enhancements
     */
    private void performBasicMeleeAttackGuard(LivingEntity target, int tier) {
        // Base damage from entity attributes (includes rank-based scaling)
        float baseDamage = (float) guard.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        // Add weapon damage if equipped
        ItemStack weapon = guard.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
        if (weapon.getItem() instanceof SwordItem || weapon.getItem() instanceof ToolItem) {
            // Get attack damage from weapon attributes
            double weaponDamage = weapon.getOrDefault(
                net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS,
                net.minecraft.component.type.AttributeModifiersComponent.DEFAULT
            ).modifiers().stream()
                .filter(entry -> entry.attribute().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE))
                .mapToDouble(entry -> entry.modifier().value())
                .sum();
            baseDamage += (float) weaponDamage;
        }

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

    @Override
    public void stop() {
        super.stop();
    }
}