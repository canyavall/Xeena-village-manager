package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.ai.GuardAttackGoal;
import com.xeenaa.villagermanager.ai.GuardDefendVillageGoal;
import com.xeenaa.villagermanager.ai.GuardMeleeAttackGoal;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.profession.ModProfessions;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add Guard AI behaviors to villagers with the Guard profession.
 */
@Mixin(VillagerEntity.class)
public abstract class VillagerAIMixin extends MerchantEntity {
    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    public abstract void setVillagerData(VillagerData villagerData);

    @Unique
    private boolean guardGoalsInitialized = false;

    protected VillagerAIMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Initialize guard-specific goals when profession changes to Guard.
     */
    @Inject(method = "setVillagerData", at = @At("TAIL"))
    private void onProfessionChange(VillagerData villagerData, CallbackInfo ci) {
        if (villagerData.getProfession() == ModProfessions.GUARD && !guardGoalsInitialized) {
            initializeGuardGoals();
            guardGoalsInitialized = true;

            // Increase health for guards
            EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(30.0); // Guards have 30 health (15 hearts)
                this.setHealth(30.0f);
            }

            // Increase movement speed slightly
            EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(0.6); // Slightly faster than normal villagers
            }
        } else if (villagerData.getProfession() != ModProfessions.GUARD && guardGoalsInitialized) {
            // Remove guard goals if profession changes away from Guard
            removeGuardGoals();
            guardGoalsInitialized = false;

            // Reset health to normal
            EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(20.0);
            }

            // Reset speed to normal
            EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(0.5);
            }
        }
    }

    @Unique
    private void initializeGuardGoals() {
        VillagerEntity self = (VillagerEntity) (Object) this;

        // Remove flee from zombies goal for guards
        this.goalSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof FleeEntityGoal
        );

        // Add guard-specific goals
        this.goalSelector.add(1, new GuardDefendVillageGoal(self));
        this.goalSelector.add(2, new GuardMeleeAttackGoal(self, 1.0, false));
        this.targetSelector.add(1, new GuardAttackGoal(self));
    }

    @Unique
    private void removeGuardGoals() {
        // Remove guard-specific goals
        this.goalSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof GuardDefendVillageGoal ||
            goal.getGoal() instanceof GuardMeleeAttackGoal
        );

        this.targetSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof GuardAttackGoal
        );

        // Re-add normal villager flee behavior
        VillagerEntity self = (VillagerEntity) (Object) this;
        this.goalSelector.add(1, new FleeEntityGoal<>(self, ZombieEntity.class, 8.0F, 0.5, 0.5));
    }



}