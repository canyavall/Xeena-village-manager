package com.xeenaa.villagermanager.mixin.client;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to ZombieVillagerEntityRenderer to provide custom textures for zombified guard villagers.
 * <p>
 * This mixin intercepts texture lookups for zombie villagers and returns appropriate
 * zombie guard textures when the zombie villager's profession is Guard. The texture
 * selection is based on the guard's specialization path (Recruit, Melee, or Ranged),
 * which is stored in the zombie villager's NBT data and persists through zombification.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> This mixin runs on the render thread only and is
 * not designed for concurrent access.
 * </p>
 * <p>
 * <strong>Performance:</strong> Texture selection is optimized to avoid repeated NBT
 * lookups by checking profession first and only parsing guard data if needed.
 * </p>
 *
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
@Mixin(net.minecraft.client.render.entity.ZombieVillagerEntityRenderer.class)
public class ZombieVillagerRendererMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("ZombieGuardRenderer");

    // Zombie guard texture identifiers for each specialization path
    @Unique
    private static final Identifier ZOMBIE_GUARD_RECRUIT_TEXTURE =
        Identifier.of(XeenaaVillagerManager.MOD_ID, "textures/entity/zombie_villager/profession/guard_recruit.png");
    @Unique
    private static final Identifier ZOMBIE_GUARD_MELEE_TEXTURE =
        Identifier.of(XeenaaVillagerManager.MOD_ID, "textures/entity/zombie_villager/profession/guard_arms.png");
    @Unique
    private static final Identifier ZOMBIE_GUARD_RANGED_TEXTURE =
        Identifier.of(XeenaaVillagerManager.MOD_ID, "textures/entity/zombie_villager/profession/guard_marksman.png");

    /**
     * Injects into the getTexture method to provide custom textures for zombie guard villagers.
     * <p>
     * This injection runs at the HEAD of the getTexture method with cancellable=true, allowing
     * us to override the texture for guard zombie villagers while letting vanilla handle all
     * other zombie villager types.
     * </p>
     *
     * @param entity the zombie villager entity being rendered
     * @param cir callback info for returning the custom texture identifier
     */
    @Inject(
        method = "getTexture(Lnet/minecraft/entity/mob/ZombieVillagerEntity;)Lnet/minecraft/util/Identifier;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onGetTexture(ZombieVillagerEntity entity, CallbackInfoReturnable<Identifier> cir) {
        try {
            // Check if this zombie villager was previously a guard
            VillagerData villagerData = entity.getVillagerData();
            VillagerProfession profession = villagerData.getProfession();

            if (profession == ModProfessions.GUARD) {
                // Get the appropriate zombie guard texture based on guard type
                Identifier guardTexture = getZombieGuardTexture(entity);
                cir.setReturnValue(guardTexture);

                LOGGER.trace("Applied zombie guard texture for entity {}: {}",
                    entity.getUuid(), guardTexture);
            }
            // If not a guard, fall through to vanilla behavior

        } catch (Exception e) {
            // Log error but don't crash - fall back to vanilla rendering
            LOGGER.error("Error determining zombie guard texture for entity {}", entity.getUuid(), e);
        }
    }

    /**
     * Determines the appropriate zombie guard texture based on the guard's specialization path.
     * <p>
     * This method reads guard-specific NBT data to determine which specialization path
     * (Recruit, Melee, or Ranged) the guard was on before zombification. The guard path
     * information is stored in the entity's NBT data and persists through the zombification
     * process.
     * </p>
     * <p>
     * If guard data cannot be found or parsed, defaults to the recruit texture as a safe
     * fallback.
     * </p>
     *
     * @param entity the zombie villager entity
     * @return the appropriate texture identifier for the zombie guard type
     */
    @Unique
    private Identifier getZombieGuardTexture(ZombieVillagerEntity entity) {
        try {
            // Read NBT data to determine guard type
            NbtCompound nbt = new NbtCompound();
            entity.writeNbt(nbt);

            // Check if guard data exists in NBT
            if (nbt.contains("XeenaaGuardData")) {
                NbtCompound guardData = nbt.getCompound("XeenaaGuardData");

                // Read rank data to determine specialization path
                if (guardData.contains("GuardRankData")) {
                    NbtCompound rankData = guardData.getCompound("GuardRankData");

                    if (rankData.contains("CurrentRank")) {
                        String rankId = rankData.getString("CurrentRank");
                        GuardPath path = getPathFromRankId(rankId);

                        // Return texture based on guard path
                        return switch (path) {
                            case RECRUIT -> ZOMBIE_GUARD_RECRUIT_TEXTURE;
                            case MELEE -> ZOMBIE_GUARD_MELEE_TEXTURE;
                            case RANGED -> ZOMBIE_GUARD_RANGED_TEXTURE;
                        };
                    }
                }
            }

            // If no guard data found, check legacy role-based data (backward compatibility)
            if (nbt.contains("XeenaaGuardData")) {
                NbtCompound guardData = nbt.getCompound("XeenaaGuardData");
                if (guardData.contains("GuardRole")) {
                    String role = guardData.getString("GuardRole");
                    LOGGER.debug("Found legacy role data for zombie guard: {}", role);
                }
            }

            // Default to recruit texture if no specific path data found
            LOGGER.debug("No guard path data found for zombie villager {}, using recruit texture",
                entity.getUuid());
            return ZOMBIE_GUARD_RECRUIT_TEXTURE;

        } catch (Exception e) {
            // If anything goes wrong, fail safely with recruit texture
            LOGGER.error("Error reading zombie guard NBT data for entity {}",
                entity.getUuid(), e);
            return ZOMBIE_GUARD_RECRUIT_TEXTURE;
        }
    }

    /**
     * Determines the guard specialization path from a rank ID string.
     * <p>
     * This method parses rank identifiers to determine which specialization path
     * the rank belongs to, enabling proper texture selection for zombie guards.
     * </p>
     *
     * @param rankId the rank identifier string (e.g., "recruit", "man_at_arms_1", "marksman_2")
     * @return the guard specialization path
     */
    @Unique
    private GuardPath getPathFromRankId(String rankId) {
        if (rankId == null || rankId.isEmpty()) {
            return GuardPath.RECRUIT;
        }

        // Determine path based on rank ID prefix
        if (rankId.equals("recruit")) {
            return GuardPath.RECRUIT;
        } else if (rankId.startsWith("man_at_arms") || rankId.equals("knight")) {
            return GuardPath.MELEE;
        } else if (rankId.startsWith("marksman") || rankId.equals("sharpshooter")) {
            return GuardPath.RANGED;
        }

        // Default to recruit for unknown rank IDs
        LOGGER.warn("Unknown rank ID: {}, defaulting to RECRUIT path", rankId);
        return GuardPath.RECRUIT;
    }
}
