package com.xeenaa.villagermanager.data.rank.ability;

import com.xeenaa.villagermanager.util.CombatEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Double Shot ability for Sharpshooter rank guards.
 * Fires two arrows at different targets simultaneously.
 *
 * @since 2.0.0
 */
public class DoubleShotAbility extends SpecialAbility {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleShotAbility.class);
    private static final double DETECTION_RANGE = 15.0;

    public DoubleShotAbility() {
        super("Double Shot",
              "Fires two arrows at different targets",
              1.0f); // Always active for Sharpshooters
    }

    @Override
    public void execute(VillagerEntity guard, LivingEntity primaryTarget) {
        LOGGER.info("[DOUBLE SHOT] Guard {} attempting Double Shot ability",
            guard.getUuid().toString().substring(0, 8));

        if (primaryTarget == null || !primaryTarget.isAlive()) {
            LOGGER.info("[DOUBLE SHOT] Primary target null or dead, aborting");
            return;
        }

        // Find a second target nearby
        Box searchBox = Box.of(guard.getPos(), DETECTION_RANGE * 2, DETECTION_RANGE * 2, DETECTION_RANGE * 2);
        List<LivingEntity> nearbyEnemies = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard &&
                     entity != primaryTarget &&
                     entity.isAlive() &&
                     guard.canTarget(entity) &&
                     guard.canSee(entity)
        );

        LOGGER.info("[DOUBLE SHOT] Found {} potential secondary targets in range", nearbyEnemies.size());

        LivingEntity secondaryTarget = null;
        double closestDistance = DETECTION_RANGE;

        // Find the closest valid secondary target
        for (LivingEntity entity : nearbyEnemies) {
            double distance = guard.squaredDistanceTo(entity);
            if (Math.sqrt(distance) < closestDistance) {
                secondaryTarget = entity;
                closestDistance = Math.sqrt(distance);
            }
        }

        // Fire second arrow if we found a target
        if (secondaryTarget != null) {
            LOGGER.info("[DOUBLE SHOT] ✓ FIRING second arrow at {} (distance: {:.2f} blocks)",
                secondaryTarget.getName().getString(), closestDistance);

            // Visual effect: Enhanced arrow trail with enchantment particles
            CombatEffects.spawnDoubleShotTrail(guard.getWorld(), guard, secondaryTarget);

            fireArrowAt(guard, secondaryTarget);

            // Audio effect: Double Shot special sound (higher pitch arrow sound)
            CombatEffects.playDoubleShotSound(guard.getWorld(), guard.getPos(), guard.getSoundCategory());
        } else {
            LOGGER.info("[DOUBLE SHOT] ✗ No valid secondary target found");
        }
    }

    /**
     * Fires an arrow at the specified target
     */
    private void fireArrowAt(VillagerEntity guard, LivingEntity target) {
        // Create arrow entity
        ArrowEntity arrow = new ArrowEntity(guard.getWorld(), guard, new ItemStack(Items.ARROW), null);

        // Apply slowness effect to arrows (Slowness I for 3 seconds)
        arrow.addEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SLOWNESS,
            60, // 3 seconds
            0,  // Slowness I
            false,
            false
        ));

        // Calculate aim
        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - arrow.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Set velocity with improved accuracy for double shot
        float velocity = 1.6f;
        float accuracy = 8.0f; // Better accuracy for double shot
        arrow.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, velocity, accuracy);

        // Spawn arrow
        guard.getWorld().spawnEntity(arrow);
    }
}
