package com.xeenaa.villagermanager.ai.performance;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intelligent AI update scheduler that adjusts update frequency based on guard activity and distance.
 *
 * <p>This system implements distance-based Level of Detail (LOD) for AI updates:</p>
 * <ul>
 *   <li>Active guards (in combat): Full AI updates every tick (1x)</li>
 *   <li>Idle guards (patrolling/following): Reduced update frequency every 5 ticks (0.2x)</li>
 *   <li>Distant guards (>64 blocks from players): Minimal updates every 20 ticks (0.05x)</li>
 *   <li>Very distant guards (>128 blocks): Suspended AI updates (0x)</li>
 * </ul>
 *
 * <p>This reduces AI overhead for large guard populations while maintaining responsiveness
 * for combat-active and nearby guards.</p>
 *
 * @since 1.0.0
 */
public class GuardAIScheduler {
    private static final Map<String, GuardAIScheduler> INSTANCES = new ConcurrentHashMap<>();

    // Distance thresholds for LOD levels
    private static final double CLOSE_DISTANCE = 32.0;      // Full update frequency
    private static final double MEDIUM_DISTANCE = 64.0;     // Reduced frequency
    private static final double FAR_DISTANCE = 128.0;       // Minimal frequency

    // Update intervals for each LOD level (in ticks)
    private static final int ACTIVE_UPDATE_INTERVAL = 1;    // Every tick
    private static final int IDLE_UPDATE_INTERVAL = 5;      // Every 5 ticks (0.25 seconds)
    private static final int DISTANT_UPDATE_INTERVAL = 20;  // Every 20 ticks (1 second)
    private static final int FAR_UPDATE_INTERVAL = 100;     // Every 100 ticks (5 seconds)

    private final ServerWorld world;
    private final Map<UUID, GuardUpdateState> guardStates;

    /**
     * Gets or creates a scheduler for the specified world.
     *
     * @param world The server world
     * @return The scheduler instance for this world
     */
    public static GuardAIScheduler get(ServerWorld world) {
        return INSTANCES.computeIfAbsent(world.getRegistryKey().getValue().toString(),
            k -> new GuardAIScheduler(world));
    }

    /**
     * Clears all schedulers (for cleanup).
     */
    public static void clearAll() {
        INSTANCES.clear();
    }

    private GuardAIScheduler(ServerWorld world) {
        this.world = world;
        this.guardStates = new ConcurrentHashMap<>();
    }

    /**
     * Determines if a guard should perform AI updates this tick.
     *
     * @param guard The guard villager
     * @return true if the guard should update AI this tick
     */
    public boolean shouldUpdateAI(VillagerEntity guard) {
        UUID guardId = guard.getUuid();
        int currentTick = world.getServer().getTicks();

        // Get or create guard state
        GuardUpdateState state = guardStates.computeIfAbsent(guardId,
            id -> new GuardUpdateState(currentTick));

        // Determine update interval based on guard status
        int updateInterval = calculateUpdateInterval(guard, state);

        // Check if enough ticks have passed since last update
        boolean shouldUpdate = (currentTick - state.lastUpdateTick) >= updateInterval;

        if (shouldUpdate) {
            state.lastUpdateTick = currentTick;
            state.updateInterval = updateInterval;
        }

        return shouldUpdate;
    }

    /**
     * Determines if a guard should perform threat detection this tick.
     * Threat detection is more expensive, so it has its own scheduling.
     *
     * @param guard The guard villager
     * @return true if the guard should detect threats this tick
     */
    public boolean shouldDetectThreats(VillagerEntity guard) {
        UUID guardId = guard.getUuid();
        int currentTick = world.getServer().getTicks();

        GuardUpdateState state = guardStates.computeIfAbsent(guardId,
            id -> new GuardUpdateState(currentTick));

        // Threat detection has different intervals than general AI
        int detectionInterval = calculateThreatDetectionInterval(guard, state);

        boolean shouldDetect = (currentTick - state.lastThreatScanTick) >= detectionInterval;

        if (shouldDetect) {
            state.lastThreatScanTick = currentTick;
        }

        return shouldDetect;
    }

    /**
     * Marks a guard as entering combat (increases update frequency).
     *
     * @param guard The guard entering combat
     */
    public void markCombatActive(VillagerEntity guard) {
        GuardUpdateState state = guardStates.get(guard.getUuid());
        if (state != null) {
            state.inCombat = true;
            state.combatStartTick = world.getServer().getTicks();
        }
    }

    /**
     * Marks a guard as leaving combat (reduces update frequency).
     *
     * @param guard The guard leaving combat
     */
    public void markCombatInactive(VillagerEntity guard) {
        GuardUpdateState state = guardStates.get(guard.getUuid());
        if (state != null) {
            state.inCombat = false;
        }
    }

    /**
     * Cleans up state for a guard that no longer exists.
     *
     * @param guardId The guard UUID
     */
    public void removeGuard(UUID guardId) {
        guardStates.remove(guardId);
    }

    /**
     * Calculates the appropriate AI update interval for a guard.
     */
    private int calculateUpdateInterval(VillagerEntity guard, GuardUpdateState state) {
        // Combat-active guards always update every tick
        if (state.inCombat || guard.getTarget() != null) {
            state.inCombat = true;
            return ACTIVE_UPDATE_INTERVAL;
        }

        // Check distance to nearest player
        double distanceToPlayer = getDistanceToNearestPlayer(guard);

        // LOD-based update intervals
        if (distanceToPlayer < CLOSE_DISTANCE) {
            return IDLE_UPDATE_INTERVAL;  // Close: Update every 5 ticks
        } else if (distanceToPlayer < MEDIUM_DISTANCE) {
            return DISTANT_UPDATE_INTERVAL;  // Medium: Update every 20 ticks
        } else if (distanceToPlayer < FAR_DISTANCE) {
            return FAR_UPDATE_INTERVAL;  // Far: Update every 100 ticks
        } else {
            return Integer.MAX_VALUE;  // Very far: Suspend updates (chunk likely unloaded)
        }
    }

    /**
     * Calculates the appropriate threat detection interval for a guard.
     * Threat detection is more expensive than general AI updates.
     */
    private int calculateThreatDetectionInterval(VillagerEntity guard, GuardUpdateState state) {
        // Combat-active guards scan frequently
        if (state.inCombat || guard.getTarget() != null) {
            return 10;  // Every 0.5 seconds during combat
        }

        // Distance-based detection intervals
        double distanceToPlayer = getDistanceToNearestPlayer(guard);

        if (distanceToPlayer < CLOSE_DISTANCE) {
            return 20;  // Every 1 second when close to players
        } else if (distanceToPlayer < MEDIUM_DISTANCE) {
            return 40;  // Every 2 seconds at medium distance
        } else if (distanceToPlayer < FAR_DISTANCE) {
            return 100;  // Every 5 seconds when far
        } else {
            return Integer.MAX_VALUE;  // Very far: Suspend detection
        }
    }

    /**
     * Gets the distance from a guard to the nearest player.
     * Uses squared distance for performance.
     */
    private double getDistanceToNearestPlayer(VillagerEntity guard) {
        // Use a cached value if available and recent
        GuardUpdateState state = guardStates.get(guard.getUuid());
        int currentTick = world.getServer().getTicks();

        if (state != null && state.cachedPlayerDistance >= 0 &&
            (currentTick - state.playerDistanceCacheTick) < 20) {  // Cache for 1 second
            return state.cachedPlayerDistance;
        }

        // Find nearest player efficiently
        Box searchBox = guard.getBoundingBox().expand(FAR_DISTANCE);
        List<PlayerEntity> players = world.getEntitiesByClass(
            PlayerEntity.class,
            searchBox,
            player -> !player.isSpectator()
        );

        double minDistance = Double.MAX_VALUE;
        for (PlayerEntity player : players) {
            double distance = guard.squaredDistanceTo(player);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        // Cache the result
        if (state != null) {
            state.cachedPlayerDistance = minDistance;
            state.playerDistanceCacheTick = currentTick;
        }

        return minDistance;
    }

    /**
     * Gets the current update interval for a guard (for debugging/monitoring).
     *
     * @param guard The guard villager
     * @return The current update interval in ticks
     */
    public int getCurrentUpdateInterval(VillagerEntity guard) {
        GuardUpdateState state = guardStates.get(guard.getUuid());
        return state != null ? state.updateInterval : IDLE_UPDATE_INTERVAL;
    }

    /**
     * Tracks the update state for individual guards.
     */
    private static class GuardUpdateState {
        int lastUpdateTick;
        int lastThreatScanTick;
        int updateInterval;
        boolean inCombat;
        int combatStartTick;
        double cachedPlayerDistance = -1;
        int playerDistanceCacheTick;

        GuardUpdateState(int currentTick) {
            this.lastUpdateTick = currentTick;
            this.lastThreatScanTick = currentTick;
            this.updateInterval = IDLE_UPDATE_INTERVAL;
            this.inCombat = false;
        }
    }
}
