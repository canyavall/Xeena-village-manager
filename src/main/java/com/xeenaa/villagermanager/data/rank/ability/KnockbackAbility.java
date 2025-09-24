package com.xeenaa.villagermanager.data.rank.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * Knockback Strike ability for Knight rank guards.
 * 20% chance to knock back enemies on hit.
 *
 * @since 2.0.0
 */
public class KnockbackAbility extends SpecialAbility {
    private static final float KNOCKBACK_STRENGTH = 1.5f;

    public KnockbackAbility() {
        super("Knockback Strike",
              "Powerful strikes that knock enemies back",
              0.2f); // 20% chance
    }

    @Override
    public void execute(VillagerEntity guard, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        // Calculate knockback direction
        Vec3d knockbackDirection = target.getPos().subtract(guard.getPos()).normalize();

        // Apply knockback
        target.addVelocity(
            knockbackDirection.x * KNOCKBACK_STRENGTH,
            0.3, // Slight upward knockback
            knockbackDirection.z * KNOCKBACK_STRENGTH
        );

        // Force velocity update
        target.velocityModified = true;

        // Play sound effect
        guard.getWorld().playSound(null, guard.getBlockPos(),
                                  SoundEvents.ENTITY_IRON_GOLEM_ATTACK,
                                  guard.getSoundCategory(), 0.5f, 1.0f);
    }
}