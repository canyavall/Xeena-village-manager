package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

/**
 * Melee attack goal for guard villagers with weapon damage calculation.
 */
public class GuardMeleeAttackGoal extends MeleeAttackGoal {
    private final VillagerEntity guard;
    private int attackCooldown = 0;

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

        return super.canStart();
    }

    @Override
    public void tick() {
        super.tick();

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Rank-based equipment will be handled by rendering system
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

        // Base damage - will be enhanced by ranking system later
        float baseDamage = 4.0f; // Guards have stronger base attack than villagers

        // Show attack animation
        guard.swingHand(Hand.MAIN_HAND);

        // Deal damage
        DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        boolean damaged = target.damage(damageSource, baseDamage);

        if (damaged) {
            // Apply knockback
            double knockbackStrength = 0.5;
            target.takeKnockback(knockbackStrength,
                guard.getX() - target.getX(),
                guard.getZ() - target.getZ());

            // Play attack sound
            guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, guard.getSoundCategory(), 1.0f, 1.0f);

            // Set attack cooldown (20 ticks = 1 second)
            attackCooldown = 20;
        }
    }


    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }


    @Override
    public void stop() {
        super.stop();
        // Combat ends - rank-based equipment will be handled by rendering system
    }
}