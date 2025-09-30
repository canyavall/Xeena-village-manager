package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.event.ThreatEventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to hook into LivingEntity damage events for threat detection.
 *
 * <p>This mixin intercepts damage events to provide real-time threat detection
 * for the guard villager system, allowing guards to respond immediately to
 * attacks against players, villagers, and other entities.</p>
 *
 * @since 1.0.0
 */
@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin {

    /**
     * Intercepts entity damage to register threat events
     *
     * @param source The damage source
     * @param amount The damage amount
     * @param cir Callback info returnable for the damage method
     */
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Cast this to LivingEntity since we're in the mixin
        LivingEntity victim = (LivingEntity) (Object) this;

        // Only process if the damage is actually going to be applied
        if (amount > 0.0f && !victim.isInvulnerableTo(source)) {
            ThreatEventHandler.handleDamageEvent(victim, source, amount);
        }
    }

    /**
     * Intercepts entity death to clean up threat tracking
     *
     * @param source The damage source that caused death
     * @param ci Callback info for the onDeath method
     */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        ThreatEventHandler.handleEntityDeath(entity);
    }
}