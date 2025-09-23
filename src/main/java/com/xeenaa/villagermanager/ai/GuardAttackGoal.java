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

        // Check if guard data exists (guards always can fight with basic combat)
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
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
        // Guard AI starts - rank-based equipment will be handled by rendering system
    }

    @Override
    public void stop() {
        super.stop();
        // Guard AI stops - rank-based equipment will be handled by rendering system
    }

    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
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