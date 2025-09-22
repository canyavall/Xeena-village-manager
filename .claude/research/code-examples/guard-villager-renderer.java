// Complete implementation for guard villager texture replacement
// Source: Research findings on villager texture blending issue
// Compatible with: Minecraft 1.21.1 Fabric

// 1. CLIENT INITIALIZER REGISTRATION
// Add to your XeenaaVillagerManagerClient.java

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.VillagerEntityRenderer;

@Override
public void onInitializeClient() {
    // Register feature renderer for guard villager complete texture replacement
    LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
        if (entityRenderer instanceof VillagerEntityRenderer) {
            registrationHelper.register(new GuardVillagerCompleteRenderer<>((VillagerEntityRenderer) entityRenderer));
        }
    });
}

// 2. GUARD VILLAGER COMPLETE RENDERER
// Create new file: GuardVillagerCompleteRenderer.java

package com.xeenaa.villagermanager.client.renderer;

import com.xeenaa.villagermanager.registry.ModProfessions;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.VillagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

/**
 * Complete texture replacement renderer for guard villagers.
 * Bypasses Minecraft's overlay system to provide pure guard appearance.
 *
 * SOLUTION: Instead of intercepting getTexture() which only affects profession overlay,
 * this renderer completely replaces the villager appearance for guards, eliminating
 * texture blending from biome and level overlays.
 */
public class GuardVillagerCompleteRenderer<T extends VillagerEntity, M extends VillagerEntityModel<T>>
        extends FeatureRenderer<T, M> {

    // Complete guard texture - design as full villager texture, not overlay
    private static final Identifier GUARD_COMPLETE_TEXTURE = new Identifier("xeenaa_villager_manager",
        "textures/entity/villager/guard_complete.png");

    public GuardVillagerCompleteRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                      T entity, float limbAngle, float limbDistance, float tickDelta,
                      float animationProgress, float headYaw, float headPitch) {

        // Only render for guard profession villagers
        if (entity.getVillagerData().getProfession() == ModProfessions.GUARD) {

            // Get vertex consumer for our complete guard texture
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(GUARD_COMPLETE_TEXTURE)
            );

            // Render complete guard appearance, bypassing all vanilla overlays
            this.getContextModel().render(
                matrices,
                vertexConsumer,
                light,
                OverlayTexture.DEFAULT_UV,
                1.0F, 1.0F, 1.0F, 1.0F  // RGBA values
            );
        }
    }
}

// 3. ALTERNATIVE: MIXIN-BASED COMPLETE OVERRIDE
// If feature renderer approach has issues, use this mixin instead

package com.xeenaa.villagermanager.mixin.client;

import com.xeenaa.villagermanager.registry.ModProfessions;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alternative solution: Complete texture override via mixin
 * This approach replaces the base villager texture entirely for guards
 */
@Mixin(VillagerEntityRenderer.class)
public class GuardVillagerTextureMixin {

    private static final Identifier GUARD_COMPLETE_TEXTURE = new Identifier("xeenaa_villager_manager",
        "textures/entity/villager/guard_complete.png");

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void replaceGuardCompleteTexture(VillagerEntity villager, CallbackInfoReturnable<Identifier> cir) {
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Return complete guard texture instead of base villager texture
            // This should eliminate overlay blending issues
            cir.setReturnValue(GUARD_COMPLETE_TEXTURE);
        }
    }
}

// 4. TEXTURE REQUIREMENTS
// Create guard_complete.png with these specifications:

/*
TEXTURE SPECIFICATIONS:
- Size: 64x64 pixels (same as vanilla villager textures)
- Format: PNG with transparency support
- Design: Complete villager appearance (not overlay)
- Include: All villager body parts in guard style
- Path: src/main/resources/assets/xeenaa_villager_manager/textures/entity/villager/guard_complete.png

DESIGN NOTES:
- Must be a complete villager texture, not an overlay
- Should include all necessary body parts (head, body, arms, legs)
- Avoid transparent areas that might allow base texture to show through
- Consider different biome lighting conditions
- Test in various game environments (day/night, different biomes)
*/

// 5. DEBUG UTILITIES
// Add to help troubleshoot texture rendering

package com.xeenaa.villagermanager.client.debug;

import net.minecraft.entity.passive.VillagerEntity;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class VillagerTextureDebugger {

    public static void logVillagerRenderingInfo(VillagerEntity villager) {
        System.out.println("=== Guard Villager Debug ===");
        System.out.println("Profession: " + villager.getVillagerData().getProfession().id());
        System.out.println("Biome Type: " + villager.getVillagerData().getType().id());
        System.out.println("Level: " + villager.getVillagerData().getLevel());
        System.out.println("Expected overlays that might cause blending:");
        System.out.println("  - Biome: " + villager.getVillagerData().getType().id());
        System.out.println("  - Level: level_" + villager.getVillagerData().getLevel());
        System.out.println("=============================");
    }
}

// 6. TESTING CHECKLIST
/*
VERIFICATION STEPS:
1. [ ] Feature renderer registers without errors
2. [ ] Guard villagers render with pure guard texture
3. [ ] No blending visible in different biomes (desert, jungle, plains, etc.)
4. [ ] No blending visible at different profession levels
5. [ ] Performance impact minimal
6. [ ] Compatible with other villager-related mods
7. [ ] Works in both single-player and multiplayer
8. [ ] Texture loads correctly on first game launch

FAILURE INDICATORS:
- Still seeing mixed/blended textures
- Performance drops when rendering many villagers
- Texture doesn't load (missing texture purple/magenta appearance)
- Crashes related to entity rendering
- Conflicts with other mods

SUCCESS INDICATORS:
- Clean, pure guard texture appearance
- Consistent across all game conditions
- No visual artifacts or blending
- Smooth rendering performance
*/