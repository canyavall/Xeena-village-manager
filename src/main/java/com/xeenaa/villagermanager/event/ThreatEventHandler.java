package com.xeenaa.villagermanager.event;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.threat.ThreatDetectionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

/**
 * Handles damage and attack events to provide real-time threat detection
 * for the guard villager system.
 *
 * <p>This handler listens for various attack and damage events and immediately
 * notifies the {@link ThreatDetectionManager} to ensure guards can respond
 * quickly to threats against players, villagers, and other guards.</p>
 *
 * @since 1.0.0
 */
public class ThreatEventHandler {
    private static boolean initialized = false;

    /**
     * Initializes the threat event handler system
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        registerEventHandlers();
        initialized = true;

        XeenaaVillagerManager.LOGGER.info("Threat event handler system initialized");
    }

    /**
     * Registers all event handlers for threat detection
     */
    private static void registerEventHandlers() {
        // Handle player attacking entities (to detect if players are fighting back)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof LivingEntity && world instanceof ServerWorld) {
                handleAttackEvent(player, (LivingEntity) entity, (ServerWorld) world);
            }
            return ActionResult.PASS;
        });

        // Clean up threat managers on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ThreatDetectionManager.clearAll();
        });
    }

    /**
     * Handles attack events to detect threats
     *
     * @param attacker The entity performing the attack
     * @param victim The entity being attacked
     * @param world The world where the attack occurred
     */
    private static void handleAttackEvent(LivingEntity attacker, LivingEntity victim, ServerWorld world) {
        try {
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(world);

            // Register the attack for threat detection
            threatManager.registerAttackEvent(victim, attacker);

            // Special handling for high-priority victims
            if (victim instanceof PlayerEntity || victim instanceof VillagerEntity) {
                XeenaaVillagerManager.LOGGER.debug("High-priority attack detected: {} attacked {}",
                    attacker.getType().getTranslationKey(),
                    victim.getType().getTranslationKey());
            }

        } catch (Exception e) {
            XeenaaVillagerManager.LOGGER.error("Error handling attack event", e);
        }
    }

    /**
     * Handles entity damage events (called from mixin)
     *
     * @param victim The entity taking damage
     * @param source The damage source
     * @param amount The damage amount
     */
    public static void handleDamageEvent(LivingEntity victim, DamageSource source, float amount) {
        if (victim.getWorld().isClient) {
            return;
        }

        try {
            ServerWorld world = (ServerWorld) victim.getWorld();
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(world);

            // Get the attacking entity from damage source
            LivingEntity attacker = getAttackerFromDamageSource(source);
            if (attacker != null && attacker != victim) {
                threatManager.registerAttackEvent(victim, attacker);

                XeenaaVillagerManager.LOGGER.debug("Damage event detected: {} damaged {} for {} damage",
                    attacker.getType().getTranslationKey(),
                    victim.getType().getTranslationKey(),
                    amount);
            }

        } catch (Exception e) {
            XeenaaVillagerManager.LOGGER.error("Error handling damage event", e);
        }
    }

    /**
     * Extracts the attacking entity from a damage source
     *
     * @param source The damage source
     * @return The attacking entity, or null if not entity-based damage
     */
    private static LivingEntity getAttackerFromDamageSource(DamageSource source) {
        // Direct entity damage
        if (source.getAttacker() instanceof LivingEntity) {
            return (LivingEntity) source.getAttacker();
        }

        // Projectile damage
        if (source.getSource() instanceof LivingEntity) {
            return (LivingEntity) source.getSource();
        }

        // Indirect damage (like TNT placed by entity)
        if (source.getAttacker() instanceof LivingEntity && source.getSource() != source.getAttacker()) {
            return (LivingEntity) source.getAttacker();
        }

        return null;
    }

    /**
     * Handles entity death events to clean up threat tracking
     *
     * @param entity The entity that died
     */
    public static void handleEntityDeath(LivingEntity entity) {
        if (entity.getWorld().isClient) {
            return;
        }

        try {
            ServerWorld world = (ServerWorld) entity.getWorld();
            ThreatDetectionManager threatManager = ThreatDetectionManager.get(world);

            // The threat manager will automatically clean up dead entities
            // This is just for immediate cleanup
            if (threatManager.isThreat(entity)) {
                XeenaaVillagerManager.LOGGER.debug("Threat entity died: {}",
                    entity.getType().getTranslationKey());
            }

        } catch (Exception e) {
            XeenaaVillagerManager.LOGGER.error("Error handling entity death event", e);
        }
    }

    /**
     * Checks if the event handler is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}