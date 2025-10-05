package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.config.GuardMode;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.EnumSet;

/**
 * AI goal that makes guards stand still at their current position.
 * Guards in STAND mode will not move except to rotate for combat.
 */
public class GuardStandGoal extends Goal {
    private final VillagerEntity guard;

    public GuardStandGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Only activate in STAND mode
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        return guardData.getBehaviorConfig().guardMode() == GuardMode.STAND;
    }

    @Override
    public boolean shouldContinue() {
        // Always continue while in STAND mode
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        return guardData.getBehaviorConfig().guardMode() == GuardMode.STAND;
    }

    @Override
    public void start() {
        // Stop all movement
        guard.getNavigation().stop();
    }

    @Override
    public void tick() {
        // Continuously cancel movement to enforce standing still
        if (guard.getNavigation().isFollowingPath()) {
            guard.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        // No cleanup needed
    }
}
