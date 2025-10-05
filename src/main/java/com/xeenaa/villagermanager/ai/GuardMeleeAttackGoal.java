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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

/**
 * Melee attack goal for guard villagers with tank specialization behaviors.
 * Implements aggressive positioning, area damage, shield mechanics, and special abilities.
 */
public class GuardMeleeAttackGoal extends MeleeAttackGoal {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardMeleeAttackGoal.class);
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
        if (attackCooldown > 0) {
            attackCooldown--;
            // Log when ready to attack again (every 20 ticks to avoid spam)
            if (attackCooldown == 0 && guard.age % 20 == 0) {
                LOGGER.info("[MELEE READY] Guard {} attack cooldown finished, ready to strike",
                    guard.getUuid().toString().substring(0, 8));
            }
        }

        // Look at target
        guard.getLookControl().lookAt(target, 30.0f, 30.0f);

        // Calculate distance to target
        double distanceToTarget = guard.squaredDistanceTo(target);
        double actualDistance = Math.sqrt(distanceToTarget);

        // Move toward target if not in melee range with increased speed
        if (actualDistance > 2.5) {
            // Use faster speed (1.2) to close distance quickly in combat
            guard.getNavigation().startMovingTo(target, 1.2);
        }

        super.tick();
    }

    @Override
    protected void attack(LivingEntity target) {
        if (attackCooldown > 0) {
            return;
        }

        // Check if guard is in melee range (within 2.5 blocks)
        double distanceToTarget = guard.squaredDistanceTo(target);
        double actualDistance = Math.sqrt(distanceToTarget);
        if (distanceToTarget > 6.25) { // 2.5 * 2.5 = 6.25
            // Log range issue occasionally
            if (guard.age % 40 == 0) {
                LOGGER.info("[MELEE OUT OF RANGE] Guard {} too far to attack: {:.2f} blocks (need ≤2.5)",
                    guard.getUuid().toString().substring(0, 8),
                    actualDistance);
            }
            return; // Too far, don't attack yet
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return;
        }

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();

        // Log attack attempt
        LOGGER.info("[MELEE ATTEMPT] Guard {} attempting attack at {:.2f} blocks",
            guard.getUuid().toString().substring(0, 8),
            actualDistance);

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
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        String rankName = guardData != null ? guardData.getRankData().getCurrentRank().getDisplayName() : "UNKNOWN";

        // Base damage from entity attributes (includes rank-based scaling)
        float baseDamage = (float) guard.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float originalBaseDamage = baseDamage;

        // Add weapon damage if equipped
        ItemStack weapon = guard.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
        String weaponName = weapon.isEmpty() ? "Fist" : weapon.getName().getString();
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

        // Show attack animation
        guard.swingHand(Hand.MAIN_HAND);

        // Deal damage
        DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        boolean damaged = target.damage(damageSource, baseDamage);

        // LOG COMBAT EVENT
        LOGGER.info("[MELEE COMBAT] Guard {} (Rank: {}, Tier: {}) attacked {} with {} | Base Dmg: {:.2f} + Weapon: {:.2f} = Final: {:.2f} | Hit: {}",
            guard.getUuid().toString().substring(0, 8),
            rankName,
            tier,
            target.getName().getString(),
            weaponName,
            originalBaseDamage,
            baseDamage - originalBaseDamage,
            baseDamage,
            damaged);

        if (damaged) {
            // Enhanced knockback scaling for high-tier guards
            // Tier 0-2: 0.5-0.7 | Tier 3: 1.0 | Tier 4 (Knight): 1.5
            double knockbackStrength;
            if (tier >= 4) {
                knockbackStrength = 1.5; // Knight - powerful knockback
            } else if (tier >= 3) {
                knockbackStrength = 1.0; // Man-at-Arms III - strong knockback
            } else {
                knockbackStrength = 0.5 + (tier * 0.1); // Lower tiers - moderate knockback
            }

            target.takeKnockback(knockbackStrength,
                guard.getX() - target.getX(),
                guard.getZ() - target.getZ());

            // Tier 4 (Knight) gets minor area damage
            if (tier >= 4) {
                damageNearbyEnemies(target, 1.5, baseDamage * 0.3f);
            }

            // Tier 4 (Knight): Apply additional stun effect
            if (tier >= 4 && guard.getRandom().nextFloat() < 0.3f) { // 30% chance
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1)); // 2 seconds of Slowness II
                LOGGER.info("[SPECIAL ABILITY] Guard {} (Knight) applied Slowness II to {}",
                    guard.getUuid().toString().substring(0, 8),
                    target.getName().getString());
            }

            // Play attack sound
            guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, guard.getSoundCategory(), 1.0f, 1.0f);

            // LOG KNOCKBACK
            LOGGER.info("[KNOCKBACK] Guard {} applied {:.2f} knockback strength to {}",
                guard.getUuid().toString().substring(0, 8),
                knockbackStrength,
                target.getName().getString());
        }
    }


    /**
     * Gets attack cooldown based on tier
     * Zombies attack every ~20 ticks, so guards need to be competitive
     */
    private int getAttackCooldown(int tier) {
        return Math.max(8, 18 - (tier * 2)); // Tier 0: 18 ticks (0.9s), Tier 4: 8 ticks (0.4s)
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