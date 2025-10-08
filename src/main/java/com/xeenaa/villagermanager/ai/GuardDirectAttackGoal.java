package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.ai.performance.GuardAIScheduler;
import com.xeenaa.villagermanager.config.GuardBehaviorConfig;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.util.CombatEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * Simple, direct attack goal that makes guards attack nearby hostile mobs.
 * Bypasses vanilla targeting systems entirely.
 *
 * <p>This goal respects guard behavior configuration including detection range.
 * Guards attack all visible hostile mobs within their detection range.</p>
 */
public class GuardDirectAttackGoal extends Goal {
    private final VillagerEntity guard;
    private LivingEntity target;
    private int attackCooldown = 0;
    private int targetSearchCooldown = 0;

    // Cached configuration values
    private GuardBehaviorConfig cachedConfig;
    private int configRefreshCounter = 0;
    private static final int CONFIG_REFRESH_INTERVAL = 100; // Refresh every 5 seconds

    public GuardDirectAttackGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        refreshConfiguration();
    }

    /**
     * Refreshes the cached configuration from guard data.
     * Only works on server side - goals only execute on server anyway.
     */
    private void refreshConfiguration() {
        // Goals only execute on server side, but Goal objects exist on both sides
        // Only access GuardDataManager on server to avoid crashes
        if (guard.getWorld() != null && !guard.getWorld().isClient() && guard.getWorld() instanceof ServerWorld world) {
            GuardDataManager manager = GuardDataManager.get(world);
            if (manager != null) {
                GuardData guardData = manager.getGuardData(guard.getUuid());
                if (guardData != null) {
                    GuardBehaviorConfig newConfig = guardData.getBehaviorConfig();
                    // Log if configuration changed
                    if (cachedConfig == null || !cachedConfig.equals(newConfig)) {
                        XeenaaVillagerManager.LOGGER.info("Guard {} configuration updated - Detection: {}, GuardMode: {}",
                            guard.getUuid(),
                            newConfig.detectionRange(),
                            newConfig.guardMode().getDisplayName());
                    }
                    cachedConfig = newConfig;
                    return;
                }
            }
        }
        // Fallback to default configuration
        if (cachedConfig == null || !cachedConfig.equals(GuardBehaviorConfig.DEFAULT)) {
            XeenaaVillagerManager.LOGGER.debug("Guard {} using default configuration", guard.getUuid());
        }
        cachedConfig = GuardBehaviorConfig.DEFAULT;
    }

    /**
     * Gets the current detection range from configuration.
     */
    private double getDetectionRange() {
        // Periodically refresh configuration to pick up changes
        if (++configRefreshCounter >= CONFIG_REFRESH_INTERVAL) {
            configRefreshCounter = 0;
            refreshConfiguration();
        }

        if (cachedConfig != null) {
            return cachedConfig.detectionRange();
        }
        return 20.0; // Default fallback
    }

    /**
     * Checks if the guard should engage a hostile.
     * Guards always attack visible hostile entities within their detection range.
     */
    private boolean shouldEngageHostile(HostileEntity hostile) {
        // Attack all visible hostile mobs within detection range
        return guard.canSee(hostile);
    }

    @Override
    public boolean canStart() {
        // Search for targets every 10 ticks (0.5 seconds)
        if (targetSearchCooldown > 0) {
            targetSearchCooldown--;
            return this.target != null && this.target.isAlive();
        }

        targetSearchCooldown = 10;

        // Get detection range from configuration
        double detectionRange = getDetectionRange();

        // Find nearest hostile entity within configured range
        Box searchBox = guard.getBoundingBox().expand(detectionRange);

        // Find hostiles based on configuration
        List<HostileEntity> hostiles = guard.getWorld().getEntitiesByClass(
            HostileEntity.class,
            searchBox,
            entity -> entity.isAlive() && shouldEngageHostile(entity)
        );

        if (!hostiles.isEmpty()) {
            // Target the closest hostile
            this.target = hostiles.stream()
                .min((e1, e2) -> Double.compare(
                    guard.squaredDistanceTo(e1),
                    guard.squaredDistanceTo(e2)
                ))
                .orElse(null);

            if (this.target != null) {
                return true;
            }
        }

        this.target = null;
        return false;
    }

    @Override
    public boolean shouldContinue() {
        double detectionRange = getDetectionRange();
        double maxDistanceSquared = detectionRange * detectionRange;

        return this.target != null &&
               this.target.isAlive() &&
               guard.squaredDistanceTo(this.target) < maxDistanceSquared;
    }

    @Override
    public void start() {
        guard.setTarget(this.target);

        // Notify scheduler that guard entered combat for increased update frequency
        if (guard.getWorld() instanceof ServerWorld serverWorld) {
            GuardAIScheduler scheduler = GuardAIScheduler.get(serverWorld);
            scheduler.markCombatActive(guard);
        }
    }

    @Override
    public void stop() {
        this.target = null;
        guard.setTarget(null);
        guard.getNavigation().stop();

        // Notify scheduler that guard left combat for reduced update frequency
        if (guard.getWorld() instanceof ServerWorld serverWorld) {
            GuardAIScheduler scheduler = GuardAIScheduler.get(serverWorld);
            scheduler.markCombatInactive(guard);
        }
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        // Update cooldowns
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Look at target
        guard.getLookControl().lookAt(target, 30.0f, 30.0f);

        double distanceToTarget = guard.squaredDistanceTo(target);
        double actualDistance = Math.sqrt(distanceToTarget);

        // Check if guard has a bow (ranged guard)
        net.minecraft.item.ItemStack weapon = guard.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
        boolean isRanged = weapon.getItem() instanceof net.minecraft.item.BowItem;

        if (isRanged) {
            // Ranged combat - keep distance and shoot
            if (actualDistance < 8.0) {
                // Too close, back away
                net.minecraft.util.math.Vec3d guardPos = guard.getPos();
                net.minecraft.util.math.Vec3d targetPos = target.getPos();
                net.minecraft.util.math.Vec3d awayDirection = guardPos.subtract(targetPos).normalize();
                net.minecraft.util.math.Vec3d backupTarget = guardPos.add(awayDirection.multiply(2.0));
                guard.getNavigation().startMovingTo(backupTarget.x, backupTarget.y, backupTarget.z, 0.8);
            } else if (actualDistance > 12.0) {
                // Too far, move closer
                guard.getNavigation().startMovingTo(target, 0.8);
            } else {
                // Good distance, stop and shoot
                guard.getNavigation().stop();
            }

            // Draw bow when aiming (within shooting range)
            if (actualDistance >= 4.0 && actualDistance <= 16.0) {
                if (!guard.isUsingItem() && attackCooldown > 20) {
                    // Start drawing bow (show draw animation while in cooldown)
                    guard.setCurrentHand(net.minecraft.util.Hand.MAIN_HAND);
                }
            }

            // Shoot if in range and cooldown ready
            if (actualDistance >= 4.0 && actualDistance <= 16.0 && attackCooldown <= 0) {
                performRangedAttack();
                attackCooldown = 30; // 1.5 second cooldown for bows
            }
        } else {
            // Melee combat - close distance and attack
            if (actualDistance > 2.0) {
                guard.getNavigation().startMovingTo(target, 1.0);
            } else {
                guard.getNavigation().stop();
            }

            // Attack if in range and cooldown is ready
            if (actualDistance <= 3.0 && attackCooldown <= 0) {
                performMeleeAttack();
                attackCooldown = 20; // 1 second cooldown
            }
        }
    }

    private void performMeleeAttack() {
        if (this.target == null) {
            return;
        }

        // Swing hand animation - must be synced to clients via EntityAnimationS2CPacket
        guard.swingHand(net.minecraft.util.Hand.MAIN_HAND);

        // Also send animation packet to all tracking clients
        if (!guard.getWorld().isClient()) {
            var packet = new net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket(guard,
                net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket.SWING_MAIN_HAND);
            ((net.minecraft.server.world.ServerWorld) guard.getWorld()).getChunkManager()
                .sendToNearbyPlayers(guard, packet);
        }

        // Visual effect: Weapon swing particles (2 particles for performance)
        CombatEffects.spawnMeleeSwingParticles(guard.getWorld(), guard);

        // Audio effect: Weapon swing sound
        CombatEffects.playMeleeSwingSound(guard.getWorld(), guard.getPos(), guard.getSoundCategory());

        // Calculate damage - villagers don't have attack damage attribute by default, so start with 1.0
        float baseDamage = 1.0f;
        if (guard.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
            baseDamage = (float) guard.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }

        // Add weapon damage
        net.minecraft.item.ItemStack weapon = guard.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
        if (weapon.getItem() instanceof net.minecraft.item.SwordItem || weapon.getItem() instanceof net.minecraft.item.ToolItem) {
            double weaponDamage = weapon.getOrDefault(
                net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS,
                net.minecraft.component.type.AttributeModifiersComponent.DEFAULT
            ).modifiers().stream()
                .filter(entry -> entry.attribute().equals(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE))
                .mapToDouble(entry -> entry.modifier().value())
                .sum();
            baseDamage += (float) weaponDamage;
        }

        // Deal damage
        net.minecraft.entity.damage.DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        boolean damaged = target.damage(damageSource, baseDamage);

        if (damaged) {
            // Apply knockback
            target.takeKnockback(0.4,
                guard.getX() - target.getX(),
                guard.getZ() - target.getZ());

            // Visual effect: Hit impact particles (4 particles)
            CombatEffects.spawnHitImpactParticles(guard.getWorld(), target, false);

            // Audio effect: Hit impact sound
            CombatEffects.playMeleeHitSound(guard.getWorld(), target.getPos(), guard.getSoundCategory(), false);
        }
    }

    private void performRangedAttack() {
        if (this.target == null) {
            return;
        }

        // Set guard to "using item" state for bow draw animation
        guard.setCurrentHand(net.minecraft.util.Hand.MAIN_HAND);

        // Visual effect: Arrow trail particles (3 particles for performance)
        CombatEffects.spawnArrowTrailParticles(guard.getWorld(), guard, target);

        // Calculate arrow spawn position (from guard's eye level, offset for hand position)
        // Villagers are 1.95 blocks tall, eye height is at ~1.62 blocks
        double arrowX = guard.getX();
        double arrowY = guard.getEyeY() - 0.1; // Slightly below eye level (bow hand height)
        double arrowZ = guard.getZ();

        // Offset arrow spawn to the side (right hand) based on guard's yaw
        float yaw = guard.getYaw() * ((float)Math.PI / 180f);
        double offsetX = -Math.sin(yaw) * 0.3; // 0.3 blocks to the right
        double offsetZ = Math.cos(yaw) * 0.3;

        arrowX += offsetX;
        arrowZ += offsetZ;

        // Create arrow at proper position
        net.minecraft.entity.projectile.ArrowEntity arrow =
            new net.minecraft.entity.projectile.ArrowEntity(guard.getWorld(), arrowX, arrowY, arrowZ,
                guard.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND).copy(), null);

        arrow.setOwner(guard);

        // Calculate trajectory from arrow's spawn position to target
        double dx = target.getX() - arrowX;
        double dy = target.getBodyY(0.3333333333333333) - arrowY;
        double dz = target.getZ() - arrowZ;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        // Set velocity (similar to skeleton shooting)
        arrow.setVelocity(dx, dy + horizontalDistance * 0.20000000298023224, dz, 1.6f, 14.0f - (guard.getWorld().getDifficulty().getId() * 4.0f));

        // Set damage
        arrow.setDamage(2.0 + (guard.getWorld().getDifficulty().getId() * 0.5));

        // Audio effect: Bow shoot sound
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            net.minecraft.sound.SoundEvents.ENTITY_ARROW_SHOOT,
            guard.getSoundCategory(), 1.0f, 1.0f / (guard.getRandom().nextFloat() * 0.4f + 0.8f));

        // Spawn arrow
        guard.getWorld().spawnEntity(arrow);

        // Clear "using item" state after a short delay (bow release animation)
        // This happens automatically when the item use finishes, but we clear it manually
        guard.clearActiveItem();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }
}