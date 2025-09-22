package com.xeenaa.villagermanager.mixin.client;

import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Debug mixin to track all texture calls for villagers
 */
@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerRendererDebugMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerRendererDebug");

    @Inject(method = "getTexture(Lnet/minecraft/entity/passive/VillagerEntity;)Lnet/minecraft/util/Identifier;",
            at = @At("RETURN"))
    private void logTextureReturn(VillagerEntity entity, CallbackInfoReturnable<Identifier> cir) {
        Identifier texture = cir.getReturnValue();
        LOGGER.info("[DEBUG] VillagerEntityRenderer.getTexture FINAL RETURN for villager {}: {}",
            entity.getUuid(), texture);
        LOGGER.info("[DEBUG] Profession at return: {}",
            entity.getVillagerData().getProfession().id());
    }
}