package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to prevent guard villagers from breeding.
 * Guards are specialized combat units and should not reproduce.
 */
@Mixin(VillagerEntity.class)
public class VillagerBreedingMixin {

    /**
     * Prevents guards from being able to breed.
     * Returns false if the villager is a guard, otherwise uses vanilla logic.
     */
    @Inject(method = "canBreed", at = @At("HEAD"), cancellable = true)
    private void preventGuardBreeding(CallbackInfoReturnable<Boolean> cir) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        // Check if this villager is a guard
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            XeenaaVillagerManager.LOGGER.debug("Preventing guard villager {} from breeding", villager.getUuid());
            cir.setReturnValue(false);
        }
    }

    /**
     * Prevents guard babies from being created.
     * If either parent is a guard, prevent baby creation.
     */
    @Inject(method = "createChild", at = @At("HEAD"), cancellable = true)
    private void preventGuardBabyCreation(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<VillagerEntity> cir) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        // Check if this villager (parent) is a guard
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            XeenaaVillagerManager.LOGGER.info("Blocked baby creation - parent {} is a guard", villager.getUuid());
            cir.setReturnValue(null);
            return;
        }

        // Check if the other parent is a guard
        if (entity instanceof VillagerEntity otherParent) {
            if (otherParent.getVillagerData().getProfession() == ModProfessions.GUARD) {
                XeenaaVillagerManager.LOGGER.info("Blocked baby creation - other parent {} is a guard", otherParent.getUuid());
                cir.setReturnValue(null);
            }
        }
    }
}
