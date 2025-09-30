package com.xeenaa.villagermanager.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * Simple, direct attack goal that makes guards attack nearby hostile mobs.
 * Bypasses vanilla targeting systems entirely.
 */
public class GuardDirectAttackGoal extends Goal {
    private final VillagerEntity guard;
    private LivingEntity target;
    private int attackCooldown = 0;
    private int targetSearchCooldown = 0;

    public GuardDirectAttackGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Search for targets every 10 ticks (0.5 seconds)
        if (targetSearchCooldown > 0) {
            targetSearchCooldown--;
            return this.target != null && this.target.isAlive();
        }

        targetSearchCooldown = 10;

        // Find nearest hostile entity within 16 blocks
        Box searchBox = guard.getBoundingBox().expand(16.0);
        List<HostileEntity> hostiles = guard.getWorld().getEntitiesByClass(
            HostileEntity.class,
            searchBox,
            entity -> entity.isAlive() && guard.canSee(entity)
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
                System.out.println("GUARD DIRECT ATTACK: Found target - " + this.target.getType().getName().getString() +
                                 " at distance " + Math.sqrt(guard.squaredDistanceTo(this.target)));
                return true;
            }
        }

        this.target = null;
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return this.target != null &&
               this.target.isAlive() &&
               guard.squaredDistanceTo(this.target) < 256.0; // 16 blocks
    }

    @Override
    public void start() {
        System.out.println("GUARD DIRECT ATTACK: Starting attack on " + this.target.getType().getName().getString());
        guard.setTarget(this.target);
    }

    @Override
    public void stop() {
        System.out.println("GUARD DIRECT ATTACK: Stopping attack");
        this.target = null;
        guard.setTarget(null);
        guard.getNavigation().stop();
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

        System.out.println("GUARD DIRECT ATTACK: Performing attack!");

        // Swing hand animation - must be synced to clients via EntityAnimationS2CPacket
        guard.swingHand(net.minecraft.util.Hand.MAIN_HAND);

        // Also send animation packet to all tracking clients
        if (!guard.getWorld().isClient()) {
            var packet = new net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket(guard,
                net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket.SWING_MAIN_HAND);
            ((net.minecraft.server.world.ServerWorld) guard.getWorld()).getChunkManager()
                .sendToNearbyPlayers(guard, packet);
        }

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

        System.out.println("GUARD DIRECT ATTACK: Dealing " + baseDamage + " damage");

        // Deal damage
        net.minecraft.entity.damage.DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        boolean damaged = target.damage(damageSource, baseDamage);

        if (damaged) {
            // Apply knockback
            target.takeKnockback(0.4,
                guard.getX() - target.getX(),
                guard.getZ() - target.getZ());

            // Play attack sound
            guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
                net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                guard.getSoundCategory(), 1.0f, 1.0f);
        }
    }

    private void performRangedAttack() {
        if (this.target == null) {
            return;
        }

        System.out.println("GUARD DIRECT ATTACK: Performing ranged attack!");

        // Swing hand animation for bow draw
        guard.swingHand(net.minecraft.util.Hand.MAIN_HAND);

        // Send animation packet to all tracking clients
        if (!guard.getWorld().isClient()) {
            var packet = new net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket(guard,
                net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket.SWING_MAIN_HAND);
            ((net.minecraft.server.world.ServerWorld) guard.getWorld()).getChunkManager()
                .sendToNearbyPlayers(guard, packet);
        }

        // Create and shoot arrow
        net.minecraft.entity.projectile.PersistentProjectileEntity arrow =
            new net.minecraft.entity.projectile.ArrowEntity(guard.getWorld(), guard,
                guard.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND), null);

        // Calculate trajectory
        double dx = target.getX() - guard.getX();
        double dy = target.getBodyY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        // Set velocity (similar to skeleton shooting)
        arrow.setVelocity(dx, dy + horizontalDistance * 0.20000000298023224, dz, 1.6f, 14.0f - (guard.getWorld().getDifficulty().getId() * 4.0f));

        // Set damage
        arrow.setDamage(2.0 + (guard.getWorld().getDifficulty().getId() * 0.5));

        // Play bow sound
        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
            net.minecraft.sound.SoundEvents.ENTITY_ARROW_SHOOT,
            guard.getSoundCategory(), 1.0f, 1.0f / (guard.getRandom().nextFloat() * 0.4f + 0.8f));

        // Spawn arrow
        guard.getWorld().spawnEntity(arrow);

        System.out.println("GUARD DIRECT ATTACK: Shot arrow at target");
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }
}