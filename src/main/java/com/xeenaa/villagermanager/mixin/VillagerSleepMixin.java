package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to prevent guard villagers from sleeping and claiming beds.
 * Guards are 24/7 defenders and should not require rest.
 *
 * This mixin intercepts sleep-related methods to ensure guards:
 * - Never attempt to sleep in beds
 * - Do not claim beds (freeing them for other villagers)
 * - Remain active and alert 24/7
 * - Continue patrol and defense behaviors during night time
 */
@Mixin(VillagerEntity.class)
public class VillagerSleepMixin {

    /**
     * Prevents guards from sleeping.
     * Cancels the sleep action if the villager is a guard.
     *
     * @param pos the position of the bed the villager is attempting to sleep in
     * @param ci callback info for canceling the method
     */
    @Inject(method = "sleep", at = @At("HEAD"), cancellable = true)
    private void preventGuardSleep(BlockPos pos, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            XeenaaVillagerManager.LOGGER.debug("Preventing guard {} from sleeping at {}",
                villager.getUuid(), pos);
            ci.cancel();
        }
    }

    /**
     * Prevents guards from waking up (since they never sleep).
     * This handles edge cases where wake up might be called.
     *
     * @param ci callback info for canceling the method
     */
    @Inject(method = "wakeUp", at = @At("HEAD"), cancellable = true)
    private void preventGuardWakeUp(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Guards never sleep, so they can't wake up
            // Cancel to prevent any wake-up logic from running
            ci.cancel();
        }
    }
}
