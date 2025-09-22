package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * AI goal that makes guards defend other villagers and players from threats.
 */
public class GuardDefendVillageGoal extends Goal {
    private final VillagerEntity guard;
    private LivingEntity targetEntity;
    private VillagerEntity villagerToDefend;
    private int cooldown;

    private static final TargetPredicate VILLAGER_PREDICATE = TargetPredicate.createNonAttackable()
        .setBaseMaxDistance(16.0)
        .ignoreVisibility();

    public GuardDefendVillageGoal(VillagerEntity guard) {
        this.guard = guard;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!isGuard()) {
            return false;
        }

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        // Find villagers under attack
        List<VillagerEntity> nearbyVillagers = guard.getWorld().getTargets(
            VillagerEntity.class,
            VILLAGER_PREDICATE,
            guard,
            guard.getBoundingBox().expand(16.0)
        );

        for (VillagerEntity villager : nearbyVillagers) {
            if (villager == guard) continue;

            LivingEntity attacker = villager.getAttacker();
            if (attacker != null && attacker instanceof HostileEntity && attacker.isAlive()) {
                // Found a villager being attacked
                this.villagerToDefend = villager;
                this.targetEntity = attacker;
                return true;
            }
        }

        // Also check for hostile mobs near villagers
        List<HostileEntity> nearbyHostiles = guard.getWorld().getEntitiesByClass(
            HostileEntity.class,
            guard.getBoundingBox().expand(12.0),
            hostile -> hostile.isAlive() && guard.canSee(hostile)
        );

        if (!nearbyHostiles.isEmpty() && !nearbyVillagers.isEmpty()) {
            // Find the closest hostile to any villager
            double closestDistance = Double.MAX_VALUE;
            HostileEntity closestHostile = null;
            VillagerEntity closestVillager = null;

            for (HostileEntity hostile : nearbyHostiles) {
                for (VillagerEntity villager : nearbyVillagers) {
                    if (villager == guard) continue;

                    double distance = hostile.squaredDistanceTo(villager);
                    if (distance < closestDistance && distance < 64.0) { // Within 8 blocks
                        closestDistance = distance;
                        closestHostile = hostile;
                        closestVillager = villager;
                    }
                }
            }

            if (closestHostile != null) {
                this.targetEntity = closestHostile;
                this.villagerToDefend = closestVillager;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (targetEntity == null || !targetEntity.isAlive()) {
            return false;
        }

        if (villagerToDefend != null && !villagerToDefend.isAlive()) {
            return false;
        }

        // Continue defending until threat is eliminated or too far away
        return guard.squaredDistanceTo(targetEntity) < 256.0; // 16 blocks
    }

    @Override
    public void start() {
        guard.setTarget(targetEntity);
        cooldown = 0;

        // Alert nearby guards
        alertNearbyGuards();
    }

    @Override
    public void stop() {
        guard.setTarget(null);
        targetEntity = null;
        villagerToDefend = null;
        cooldown = 40; // 2 second cooldown
    }

    private void alertNearbyGuards() {
        // Find other guards within 32 blocks and alert them
        List<VillagerEntity> nearbyVillagers = guard.getWorld().getEntitiesByClass(
            VillagerEntity.class,
            guard.getBoundingBox().expand(32.0),
            villager -> villager != guard && isGuard(villager)
        );

        for (VillagerEntity otherGuard : nearbyVillagers) {
            if (otherGuard.getTarget() == null) {
                otherGuard.setTarget(targetEntity);
            }
        }
    }

    private boolean isGuard() {
        return guard.getVillagerData().getProfession().id().equals("guard");
    }

    private boolean isGuard(VillagerEntity villager) {
        return villager.getVillagerData().getProfession().id().equals("guard");
    }
}