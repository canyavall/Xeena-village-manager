package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AI goal that makes guard villagers target and attack hostile mobs.
 */
public class GuardAttackGoal extends ActiveTargetGoal<HostileEntity> {
    private final VillagerEntity guard;

    public GuardAttackGoal(VillagerEntity guard) {
        super(guard, HostileEntity.class, 10, true, false,
            entity -> entity instanceof HostileEntity && entity.canSee(guard));
        this.guard = guard;
    }

    @Override
    public boolean canStart() {
        // Only guards can use this goal
        if (!isGuard()) {
            return false;
        }

        // Check if guard has a weapon equipped
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null || !hasWeapon(guardData)) {
            return false;
        }

        // Check role-specific conditions
        GuardData.GuardRole role = guardData.getRole();
        switch (role) {
            case PATROL:
                // Patrol guards always look for threats
                return super.canStart();
            case GUARD:
                // Stationary guards defend their area (16 block radius)
                return super.canStart() && isNearPost();
            case FOLLOW:
                // Follow guards protect their assigned player
                return super.canStart() && isNearPlayer();
            default:
                return false;
        }
    }

    @Override
    public void start() {
        super.start();

        // Equip weapon in hand for visual feedback
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData != null) {
            ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
            if (!weapon.isEmpty()) {
                guard.setStackInHand(Hand.MAIN_HAND, weapon.copy());
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        // Clear weapon from hand when not in combat
        guard.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    }

    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }

    private boolean hasWeapon(GuardData guardData) {
        ItemStack weapon = guardData.getEquipment(GuardData.EquipmentSlot.WEAPON);
        return !weapon.isEmpty();
    }

    private boolean isNearPost() {
        // Check if guard is within 16 blocks of their assigned post
        // For now, use their current position as "post"
        return true; // TODO: Implement actual post tracking
    }

    private boolean isNearPlayer() {
        // Check if guard is following a player and within range
        return guard.getWorld().getClosestPlayer(guard, 32) != null;
    }
}