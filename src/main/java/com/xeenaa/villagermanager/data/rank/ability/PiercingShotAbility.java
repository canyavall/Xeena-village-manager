package com.xeenaa.villagermanager.data.rank.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.List;

/**
 * Piercing Shot ability for Sharpshooter rank guards.
 * Arrows can pierce through multiple enemies.
 *
 * @since 2.0.0
 */
public class PiercingShotAbility extends SpecialAbility {
    private static final int MAX_PIERCING_TARGETS = 3;
    private static final double PIERCING_RANGE = 10.0;

    public PiercingShotAbility() {
        super("Piercing Shot",
              "Arrows pierce through multiple enemies",
              1.0f); // Always active for Sharpshooters
    }

    @Override
    public void execute(VillagerEntity guard, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        // This ability is passive and modifies projectile behavior
        // The actual piercing effect should be handled in the projectile logic
        // Here we just add a marker or effect

        // Play special sound effect when firing
        guard.getWorld().playSound(null, guard.getBlockPos(),
                                  SoundEvents.ENTITY_ARROW_SHOOT,
                                  guard.getSoundCategory(), 1.0f, 0.8f);
    }

    /**
     * Special method to handle piercing damage to multiple targets
     * This would be called from the projectile hit logic
     */
    public void handlePiercing(VillagerEntity guard, Vec3d hitPos, Vec3d direction, float damage) {
        // Find all entities in a line from the hit position
        Box searchBox = new Box(hitPos.subtract(PIERCING_RANGE, PIERCING_RANGE, PIERCING_RANGE),
                                hitPos.add(PIERCING_RANGE, PIERCING_RANGE, PIERCING_RANGE));

        List<LivingEntity> targets = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard && entity.isAlive() && !entity.isSpectator()
        );

        int hitCount = 0;
        for (LivingEntity entity : targets) {
            if (hitCount >= MAX_PIERCING_TARGETS) break;

            // Check if entity is roughly in line with the shot direction
            Vec3d toEntity = entity.getPos().subtract(hitPos).normalize();
            double dotProduct = toEntity.dotProduct(direction);

            if (dotProduct > 0.7) { // Entity is roughly in the direction of the shot
                // Use mob damage source for simplicity (arrow damage needs actual projectile)
                entity.damage(guard.getDamageSources().mobAttack(guard),
                            damage * (1.0f - (hitCount * 0.2f))); // Reduce damage for each pierce
                hitCount++;
            }
        }
    }
}