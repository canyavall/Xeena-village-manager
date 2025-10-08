package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preserves guard villager attributes during zombification and curing.
 * <p>
 * When guards are converted to zombie villagers, their enhanced attributes
 * (HP, damage, speed, armor, knockback resistance, attack speed) are manually
 * transferred to preserve progression. This prevents high-tier guards from
 * becoming standard weak zombies with default stats.
 * </p>
 * <p>
 * Uses Fabric API's ServerLivingEntityEvents.MOB_CONVERSION event to intercept
 * entity conversion and copy attribute values. The event fires after the new
 * entity is created but before the old entity is removed, allowing safe
 * attribute transfer.
 * </p>
 * <p>
 * <strong>Special Abilities:</strong> Special abilities (Knight knockback/area damage,
 * Sharpshooter double shot) are NOT preserved. These are handled by the
 * GuardSpecialAbilities class which only works on living guard villagers.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> Event registration happens once during mixin class
 * loading. Event handlers execute on the server thread, ensuring thread-safe
 * entity modification.
 * </p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager
 */
@Mixin(VillagerEntity.class)
public class VillagerZombificationMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(VillagerZombificationMixin.class);

    static {
        // Register event handler when mixin class is loaded
        // This ensures registration happens exactly once during mod initialization
        ServerLivingEntityEvents.MOB_CONVERSION.register((original, converted, keepEquipment) -> {
            handleMobConversion(original, converted, keepEquipment);
        });

        LOGGER.info("Guard zombification attribute preservation system initialized");
    }

    /**
     * Prevents guards from dying to zombie damage and forces early zombification.
     * <p>
     * Without this, guards are killed (health=0) then converted, resulting in a
     * zombie with 0 HP that dies immediately. This intercepts lethal damage from
     * zombies and triggers conversion while the guard still has health.
     * </p>
     */
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onGuardDeath(DamageSource damageSource, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        VillagerData data = villager.getVillagerData();

        // Only process guards on the server
        if (data.getProfession() != ModProfessions.GUARD || villager.getWorld().isClient()) {
            return;
        }

        // Check if damage is from a zombie
        if (damageSource.getAttacker() instanceof ZombieEntity zombie) {
            LOGGER.info("GUARD ZOMBIFICATION: Preventing death, forcing conversion for guard {} at {}% health",
                villager.getUuid(), (int)((villager.getHealth() / villager.getMaxHealth()) * 100));

            // Cancel the death
            ci.cancel();

            // Set health to at least 80% to ensure zombie survives initial conversion with substantial health
            float minHealth = villager.getMaxHealth() * 0.8f;
            if (villager.getHealth() < minHealth) {
                villager.setHealth(minHealth);
                LOGGER.info("GUARD ZOMBIFICATION: Boosted health to 80% ({}) for conversion", minHealth);
            }

            // Force conversion immediately
            ServerWorld serverWorld = (ServerWorld) villager.getWorld();
            ZombieVillagerEntity zombieVillager = villager.convertTo(
                net.minecraft.entity.EntityType.ZOMBIE_VILLAGER,
                false // Don't keep equipment (guards don't have equipment items)
            );

            if (zombieVillager != null) {
                LOGGER.info("GUARD ZOMBIFICATION: Successfully converted guard {} to zombie",
                    villager.getUuid());
            } else {
                LOGGER.error("GUARD ZOMBIFICATION: Failed to convert guard {}", villager.getUuid());
            }
        }
    }

    /**
     * Handles all mob conversions, delegating to specific handlers.
     * <p>
     * This method is called by Fabric API whenever any entity conversion occurs.
     * It checks the entity types and delegates to appropriate specialized handlers.
     * </p>
     *
     * @param original the original entity before conversion
     * @param converted the new entity after conversion
     * @param keepEquipment whether equipment should be preserved (vanilla parameter)
     */
    private static void handleMobConversion(LivingEntity original, LivingEntity converted, boolean keepEquipment) {
        try {
            // Villager → Zombie Villager (zombification)
            if (original instanceof VillagerEntity villager && converted instanceof ZombieVillagerEntity zombie) {
                handleGuardZombification(villager, zombie);
            }

            // Zombie Villager → Villager (curing)
            if (original instanceof ZombieVillagerEntity zombie && converted instanceof VillagerEntity villager) {
                handleGuardCuring(zombie, villager);
            }
        } catch (Exception e) {
            // Never let conversion errors crash the server - fail gracefully
            LOGGER.error("Error handling mob conversion", e);
        }
    }

    /**
     * Copies guard attributes from villager to zombie during zombification.
     * <p>
     * When a guard villager is zombified, this method preserves all combat-related
     * attributes so the zombie retains the guard's combat effectiveness. Health is
     * preserved as a percentage to handle max health changes correctly.
     * </p>
     * <p>
     * Only processes guards - other villager professions use vanilla zombification.
     * </p>
     *
     * @param villager the original guard villager being zombified
     * @param zombie the newly created zombie villager
     */
    private static void handleGuardZombification(VillagerEntity villager, ZombieVillagerEntity zombie) {
        VillagerData villagerData = villager.getVillagerData();

        // Only process guards - early exit for performance
        if (villagerData.getProfession() != ModProfessions.GUARD) {
            return;
        }

        LOGGER.info("GUARD ZOMBIFICATION: Preserving attributes for guard {}", villager.getUuid());

        // Copy all combat-related attributes
        // These are the 6 attributes modified by VillagerAIMixin.applyRankBasedAttributes()
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_MAX_HEALTH);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_MOVEMENT_SPEED);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ARMOR);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_SPEED);

        // Preserve health as a percentage of max health
        // This handles cases where guard was damaged before conversion
        // Example: Guard with 20/40 HP becomes zombie with 20/40 HP (50%), not 20/20 HP
        float healthPercentage = villager.getHealth() / villager.getMaxHealth();
        float newHealth = zombie.getMaxHealth() * healthPercentage;
        zombie.setHealth(newHealth);

        // Make zombie persistent so valuable guards don't despawn
        // Guards represent player investment (emeralds, progression time)
        zombie.setPersistent();

        LOGGER.info("GUARD ZOMBIFICATION: Complete - Health: {}/{}, Damage: {}, Speed: {}",
            newHealth,
            zombie.getMaxHealth(),
            zombie.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
            zombie.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
    }

    /**
     * Handles guard curing (zombie → villager).
     * <p>
     * Most attribute restoration is handled automatically by VillagerAIMixin.applyRankBasedAttributes()
     * when the profession is restored. This method ensures health percentage is maintained
     * during the curing process.
     * </p>
     * <p>
     * Only processes guards - other zombie villagers use vanilla curing.
     * </p>
     *
     * @param zombie the zombie villager being cured
     * @param villager the newly cured villager
     */
    private static void handleGuardCuring(ZombieVillagerEntity zombie, VillagerEntity villager) {
        VillagerData zombieData = zombie.getVillagerData();

        // Only process guards - early exit for performance
        if (zombieData.getProfession() != ModProfessions.GUARD) {
            return;
        }

        LOGGER.info("GUARD CURING: Processing cured guard {}", villager.getUuid());

        // VillagerAIMixin.applyRankBasedAttributes() will be called automatically
        // when profession is set, restoring all attributes correctly.

        // Preserve health percentage from zombie to villager
        float healthPercentage = zombie.getHealth() / zombie.getMaxHealth();

        // Schedule health scaling for next tick to ensure attributes are applied first
        // (Using immediate setHealth may use wrong max health if attributes not yet applied)
        scheduleHealthScaling(villager, healthPercentage);
    }

    /**
     * Copies a single attribute from one entity to another.
     * <p>
     * This method safely copies attribute base values, handling null attribute
     * instances gracefully. Uses setBaseValue() to modify the entity's base
     * attribute, which persists through saves and is networked to clients.
     * </p>
     *
     * @param from the source entity to copy from
     * @param to the target entity to copy to
     * @param attribute the attribute to copy
     */
    private static void copyAttribute(LivingEntity from, LivingEntity to, RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance fromAttr = from.getAttributeInstance(attribute);
        EntityAttributeInstance toAttr = to.getAttributeInstance(attribute);

        // Null checks prevent crashes if entity doesn't have the attribute
        if (fromAttr != null && toAttr != null) {
            double baseValue = fromAttr.getBaseValue();
            toAttr.setBaseValue(baseValue);

            LOGGER.debug("  Copied attribute {} = {}", attribute.getIdAsString(), baseValue);
        } else {
            if (fromAttr == null) {
                LOGGER.warn("  Source entity missing attribute: {}", attribute.getIdAsString());
            }
            if (toAttr == null) {
                LOGGER.warn("  Target entity missing attribute: {}", attribute.getIdAsString());
            }
        }
    }

    /**
     * Schedules health scaling for the next tick.
     * <p>
     * This ensures attributes are applied before we scale health percentage.
     * During curing, the villager's max health attribute is restored by
     * VillagerAIMixin.applyRankBasedAttributes(), but this happens after
     * the MOB_CONVERSION event. Delaying health scaling by 1 tick ensures
     * we use the correct max health value.
     * </p>
     *
     * @param villager the villager to scale health for
     * @param healthPercentage the percentage of max health to restore (0.0 to 1.0)
     */
    private static void scheduleHealthScaling(VillagerEntity villager, float healthPercentage) {
        // Validate health percentage to prevent invalid values
        if (healthPercentage < 0.0f || healthPercentage > 1.0f) {
            LOGGER.warn("Invalid health percentage: {}, clamping to valid range", healthPercentage);
            healthPercentage = Math.max(0.0f, Math.min(1.0f, healthPercentage));
        }

        // Capture percentage in final variable for lambda
        final float finalHealthPercentage = healthPercentage;

        // Use the server's scheduler to delay health scaling by 1 tick
        // This ensures VillagerAIMixin.applyRankBasedAttributes() runs first
        villager.getWorld().getServer().execute(() -> {
            float newHealth = villager.getMaxHealth() * finalHealthPercentage;
            villager.setHealth(newHealth);
            LOGGER.info("GUARD CURING: Scaled health to {}/{} ({}%)",
                newHealth,
                villager.getMaxHealth(),
                (int)(finalHealthPercentage * 100));
        });
    }
}
