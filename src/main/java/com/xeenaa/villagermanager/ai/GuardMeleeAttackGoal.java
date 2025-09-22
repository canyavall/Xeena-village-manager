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
        if (guardData == null || !hasWeapon(guardData)) {
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

        // Update weapon in hand for visual feedback during combat
        if (guard.getTarget() != null) {
            GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
            if (guardData != null) {
                ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
                if (!weapon.isEmpty() && guard.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
                    guard.setStackInHand(Hand.MAIN_HAND, weapon.copy());
                }
            }
        }
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

        ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
        float baseDamage = 1.0f; // Base villager damage

        // Calculate weapon damage
        if (!weapon.isEmpty()) {
            if (weapon.getItem() instanceof SwordItem swordItem) {
                // Get attack damage from item attributes
                baseDamage += 3.0f; // Base sword damage - should be calculated properly
            } else if (weapon.getItem() instanceof ToolItem toolItem) {
                baseDamage += 2.0f; // Base tool damage - should be calculated properly
            }

            // Apply enchantments (simplified for now)
            // baseDamage += EnchantmentHelper.getAttackDamage(weapon, target.getType());

            // Show weapon swing animation
            guard.swingHand(Hand.MAIN_HAND);
        }

        // Apply armor protection bonus based on equipped armor
        float protectionBonus = calculateProtectionBonus(guardData);

        // Deal damage
        DamageSource damageSource = guard.getDamageSources().mobAttack(guard);
        boolean damaged = target.damage(damageSource, baseDamage);

        if (damaged) {
            // Apply knockback
            double knockbackStrength = 0.5;
            // Simplified knockback calculation
            // if (!weapon.isEmpty()) {
            //     knockbackStrength += EnchantmentHelper.getKnockback(guard) * 0.5;
            // }

            target.takeKnockback(knockbackStrength,
                guard.getX() - target.getX(),
                guard.getZ() - target.getZ());

            // Play attack sound
            guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, guard.getSoundCategory(), 1.0f, 1.0f);

            // Set attack cooldown (20 ticks = 1 second)
            attackCooldown = 20;

            // Apply weapon durability damage
            if (!weapon.isEmpty() && weapon.isDamageable()) {
                // Simplified durability handling - reduce durability directly
                if (guard.getRandom().nextFloat() < 0.1f) { // 10% chance to damage
                    weapon.setDamage(weapon.getDamage() + 1);
                    // Update the stored weapon if it breaks
                    if (weapon.getDamage() >= weapon.getMaxDamage()) {
                        guardData.setEquipment(GuardData.EquipmentSlot.WEAPON, ItemStack.EMPTY);
                        guard.getWorld().playSound(null, guard.getX(), guard.getY(), guard.getZ(),
                            SoundEvents.ENTITY_ITEM_BREAK, guard.getSoundCategory(), 1.0f, 1.0f);
                    }
                }
            }
        }
    }

    private float calculateProtectionBonus(GuardData guardData) {
        float bonus = 0.0f;

        // Check each armor piece
        if (!guardData.getEquipment(GuardData.EquipmentSlot.HELMET).isEmpty()) {
            bonus += 0.5f;
        }
        if (!guardData.getEquipment(GuardData.EquipmentSlot.CHESTPLATE).isEmpty()) {
            bonus += 1.0f;
        }
        if (!guardData.getEquipment(GuardData.EquipmentSlot.LEGGINGS).isEmpty()) {
            bonus += 0.75f;
        }
        if (!guardData.getEquipment(GuardData.EquipmentSlot.BOOTS).isEmpty()) {
            bonus += 0.25f;
        }
        if (!guardData.getEquipment(GuardData.EquipmentSlot.SHIELD).isEmpty()) {
            bonus += 1.0f;
        }

        return bonus;
    }

    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }

    private boolean hasWeapon(GuardData guardData) {
        ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
        return !weapon.isEmpty();
    }

    @Override
    public void stop() {
        super.stop();
        // Clear weapon from hand when combat ends
        guard.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    }
}