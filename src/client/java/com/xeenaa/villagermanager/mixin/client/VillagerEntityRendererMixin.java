package com.xeenaa.villagermanager.mixin.client;

import com.xeenaa.villagermanager.profession.ModProfessions;
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
 * Mixin to fix guard villager texture blending issue.
 *
 * This mixin intercepts the getTexture method before Minecraft's texture layering
 * system can apply profession overlays, completely replacing the texture resolution
 * for guard villagers.
 */
@Mixin(VillagerEntityRenderer.class)
public class VillagerEntityRendererMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerEntityRendererMixin");

    // Guard texture that should be used without any blending
    private static final Identifier GUARD_TEXTURE =
        Identifier.of("xeenaa_villager_manager", "textures/entity/villager/profession/guard.png");

    /**
     * Intercepts texture resolution for villagers.
     * For guard villagers, cancels the default texture resolution and returns
     * the guard texture directly, preventing any texture layering/blending.
     */
    @Inject(method = "getTexture(Lnet/minecraft/entity/passive/VillagerEntity;)Lnet/minecraft/util/Identifier;",
            at = @At("HEAD"),
            cancellable = true)
    private void interceptGuardTexture(VillagerEntity entity, CallbackInfoReturnable<Identifier> cir) {
        LOGGER.info("=== VillagerEntityRendererMixin.interceptGuardTexture called ===");

        // Check if this is a guard villager
        if (entity != null && entity.getVillagerData() != null) {
            var profession = entity.getVillagerData().getProfession();
            var professionId = profession.id();

            LOGGER.info("Villager UUID: {}", entity.getUuid());
            LOGGER.info("Profession: {} (ID: {})", profession, professionId);
            LOGGER.info("Is Guard: {}", profession == ModProfessions.GUARD);
            LOGGER.info("ModProfessions.GUARD: {}", ModProfessions.GUARD);
            LOGGER.info("Profession equals check: {}", profession.equals(ModProfessions.GUARD));

            if (profession == ModProfessions.GUARD) {
                // Cancel default texture resolution and return guard texture
                LOGGER.info(">>> INTERCEPTING GUARD TEXTURE! Setting to: {}", GUARD_TEXTURE);
                LOGGER.info(">>> Cancelling default texture resolution");
                cir.setReturnValue(GUARD_TEXTURE);
                LOGGER.info(">>> Guard texture set successfully");
            } else {
                LOGGER.info("Not a guard villager, passing through normal texture resolution");
            }
        } else {
            LOGGER.warn("Entity or VillagerData is null!");
            if (entity == null) {
                LOGGER.warn("Entity is null");
            } else {
                LOGGER.warn("VillagerData is null");
            }
        }
    }
}