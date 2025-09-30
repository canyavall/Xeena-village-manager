package com.xeenaa.villagermanager.threat;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central system for detecting and managing threats for guard villagers.
 *
 * <p>This system provides comprehensive threat detection capabilities including:</p>
 * <ul>
 *   <li>Proximity-based hostile mob detection</li>
 *   <li>Active attack detection when entities take damage</li>
 *   <li>Priority-based threat classification</li>
 *   <li>Memory system for tracking recent threats</li>
 *   <li>Performance optimization through caching and cooldowns</li>
 * </ul>
 *
 * <p>Threat priorities (higher number = higher priority):</p>
 * <ol>
 *   <li>Players being attacked by mobs (Priority 5)</li>
 *   <li>Villagers being attacked by mobs (Priority 4)</li>
 *   <li>Other guards being attacked (Priority 3)</li>
 *   <li>Nearby hostile mobs not actively attacking (Priority 2)</li>
 *   <li>Property damage threats if applicable (Priority 1)</li>
 * </ol>
 *
 * @since 1.0.0
 */
public class ThreatDetectionManager {
    private static final Map<String, ThreatDetectionManager> INSTANCES = new ConcurrentHashMap<>();

    // Detection configuration
    private static final int DETECTION_COOLDOWN = 20; // 1 second between full scans
    private static final int THREAT_MEMORY_DURATION = 600; // 30 seconds
    private static final double BASE_DETECTION_RANGE = 16.0;
    private static final double EXTENDED_DETECTION_RANGE = 24.0;
    private static final double CLOSE_THREAT_RANGE = 8.0;

    // Performance optimization
    private static final int MAX_THREATS_PER_SCAN = 10;
    private static final int QUICK_SCAN_INTERVAL = 5; // Quick scans every 5 ticks

    private final ServerWorld world;
    private final Map<UUID, ThreatInfo> activeThreatMemory;
    private final Map<UUID, Integer> guardCooldowns;
    private int lastFullScanTick;

    /**
     * Gets or creates a threat detection manager for the specified world
     */
    public static ThreatDetectionManager get(ServerWorld world) {
        return INSTANCES.computeIfAbsent(world.getRegistryKey().getValue().toString(),
            k -> new ThreatDetectionManager(world));
    }

    /**
     * Clears all managers (for cleanup)
     */
    public static void clearAll() {
        INSTANCES.clear();
    }

    private ThreatDetectionManager(ServerWorld world) {
        this.world = world;
        this.activeThreatMemory = new ConcurrentHashMap<>();
        this.guardCooldowns = new ConcurrentHashMap<>();
        this.lastFullScanTick = 0;
    }

    /**
     * Detects the highest priority threat for a guard villager.
     * Uses caching and cooldowns for performance optimization.
     *
     * @param guard The guard villager to detect threats for
     * @return The highest priority threat, or null if none found
     */
    public ThreatInfo detectPrimaryThreat(VillagerEntity guard) {
        if (!isGuard(guard)) {
            return null;
        }

        UUID guardId = guard.getUuid();
        int currentTick = world.getServer().getTicks();

        // Check cooldown for this specific guard
        Integer lastScan = guardCooldowns.get(guardId);
        if (lastScan != null && currentTick - lastScan < DETECTION_COOLDOWN) {
            // Return cached threat if still valid
            return getCachedThreat(guard);
        }

        // Update cooldown
        guardCooldowns.put(guardId, currentTick);

        // Get guard data for detection range calculation
        GuardData guardData = GuardDataManager.get(world).getGuardData(guardId);
        if (guardData == null) {
            return null;
        }

        // Calculate detection range based on rank
        double detectionRange = calculateDetectionRange(guardData);

        // Perform threat detection
        List<ThreatInfo> threats = detectAllThreats(guard, detectionRange);

        // Clean up old threats from memory
        cleanupThreatMemory(currentTick);

        // Find highest priority threat
        ThreatInfo primaryThreat = threats.stream()
            .max(Comparator.comparingInt(ThreatInfo::getPriorityValue))
            .orElse(null);

        // Update threat memory
        if (primaryThreat != null) {
            updateThreatMemory(primaryThreat, currentTick);
        }

        return primaryThreat;
    }

    /**
     * Registers an attack event for immediate threat detection
     *
     * @param victim The entity being attacked
     * @param attacker The attacking entity
     */
    public void registerAttackEvent(LivingEntity victim, LivingEntity attacker) {
        if (!(attacker instanceof HostileEntity) || !attacker.isAlive()) {
            return;
        }

        ThreatPriority priority = calculateAttackPriority(victim);
        if (priority == ThreatPriority.NONE) {
            return;
        }

        ThreatInfo threat = new ThreatInfo(
            attacker,
            victim,
            priority,
            ThreatType.ACTIVE_ATTACK,
            attacker.squaredDistanceTo(victim)
        );

        updateThreatMemory(threat, world.getServer().getTicks());

        // Alert nearby guards immediately for high priority threats
        if (priority.getValue() >= ThreatPriority.VILLAGER_UNDER_ATTACK.getValue()) {
            alertNearbyGuards(victim, threat);
        }
    }

    /**
     * Gets all threats within range of a guard, sorted by priority
     */
    public List<ThreatInfo> getAllThreats(VillagerEntity guard, double range) {
        if (!isGuard(guard)) {
            return Collections.emptyList();
        }

        return detectAllThreats(guard, range);
    }

    /**
     * Checks if a specific entity is currently considered a threat
     */
    public boolean isThreat(LivingEntity entity) {
        return activeThreatMemory.values().stream()
            .anyMatch(threat -> threat.getThreatEntity().equals(entity));
    }

    /**
     * Gets threat information for a specific entity
     */
    public ThreatInfo getThreatInfo(LivingEntity entity) {
        return activeThreatMemory.values().stream()
            .filter(threat -> threat.getThreatEntity().equals(entity))
            .findFirst()
            .orElse(null);
    }

    private List<ThreatInfo> detectAllThreats(VillagerEntity guard, double range) {
        List<ThreatInfo> threats = new ArrayList<>();
        Box detectionBox = guard.getBoundingBox().expand(range);

        // Detect hostile entities
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class,
            detectionBox,
            hostile -> hostile.isAlive() && guard.canSee(hostile)
        );

        // Analyze each hostile for threat level
        for (HostileEntity hostile : hostiles) {
            ThreatInfo threat = analyzeThreat(guard, hostile, range);
            if (threat != null) {
                threats.add(threat);

                // Limit threats per scan for performance
                if (threats.size() >= MAX_THREATS_PER_SCAN) {
                    break;
                }
            }
        }

        // Sort by priority (highest first)
        threats.sort((a, b) -> Integer.compare(b.getPriorityValue(), a.getPriorityValue()));

        return threats;
    }

    private ThreatInfo analyzeThreat(VillagerEntity guard, HostileEntity hostile, double range) {
        double distance = guard.squaredDistanceTo(hostile);

        // Check if hostile is actively attacking someone
        LivingEntity victim = findAttackVictim(hostile, range);
        if (victim != null) {
            ThreatPriority priority = calculateAttackPriority(victim);
            return new ThreatInfo(
                hostile,
                victim,
                priority,
                ThreatType.ACTIVE_ATTACK,
                distance
            );
        }

        // Check for proximity threats
        ThreatPriority proximityPriority = calculateProximityPriority(guard, hostile, distance);
        if (proximityPriority != ThreatPriority.NONE) {
            return new ThreatInfo(
                hostile,
                null,
                proximityPriority,
                ThreatType.PROXIMITY_THREAT,
                distance
            );
        }

        return null;
    }

    private LivingEntity findAttackVictim(HostileEntity hostile, double range) {
        // Check current target
        LivingEntity target = hostile.getTarget();
        if (target != null && target.isAlive() &&
            hostile.squaredDistanceTo(target) <= range * range) {
            return target;
        }

        // Check recent damage
        LivingEntity attacker = hostile.getAttacker();
        if (attacker != null && attacker.isAlive() &&
            hostile.squaredDistanceTo(attacker) <= CLOSE_THREAT_RANGE * CLOSE_THREAT_RANGE) {
            return attacker;
        }

        return null;
    }

    private ThreatPriority calculateAttackPriority(LivingEntity victim) {
        if (victim instanceof PlayerEntity) {
            return ThreatPriority.PLAYER_UNDER_ATTACK;
        } else if (victim instanceof VillagerEntity) {
            VillagerEntity villager = (VillagerEntity) victim;
            if (isGuard(villager)) {
                return ThreatPriority.GUARD_UNDER_ATTACK;
            } else {
                return ThreatPriority.VILLAGER_UNDER_ATTACK;
            }
        }
        return ThreatPriority.NONE;
    }

    private ThreatPriority calculateProximityPriority(VillagerEntity guard, HostileEntity hostile, double distance) {
        // High priority for very close threats
        if (distance <= CLOSE_THREAT_RANGE * CLOSE_THREAT_RANGE) {
            return ThreatPriority.PROXIMITY_THREAT_HIGH;
        }

        // Check if hostile is near other villagers
        List<VillagerEntity> nearbyVillagers = world.getTargets(
            VillagerEntity.class,
            TargetPredicate.createNonAttackable().setBaseMaxDistance(CLOSE_THREAT_RANGE),
            hostile,
            hostile.getBoundingBox().expand(CLOSE_THREAT_RANGE)
        );

        if (!nearbyVillagers.isEmpty()) {
            return ThreatPriority.PROXIMITY_THREAT_MEDIUM;
        }

        // General proximity threat
        return ThreatPriority.PROXIMITY_THREAT_LOW;
    }

    private double calculateDetectionRange(GuardData guardData) {
        // Use tier-based detection range: 8 + (tier * 2) blocks
        // Tier 1 = 8 blocks, Tier 2 = 10 blocks, Tier 3 = 12 blocks, Tier 4 = 14 blocks, Tier 5 = 16 blocks
        int tier = guardData.getRankData().getCurrentTier();
        double baseRange = 8.0 + (tier * 2.0);

        // Role-specific modifications
        switch (guardData.getRole()) {
            case PATROL:
                return baseRange * 1.25; // Patrol guards have extended range (+25%)
            case GUARD:
                return baseRange;
            case FOLLOW:
                return baseRange * 0.9; // Follow guards focus on close protection (-10%)
            default:
                return baseRange;
        }
    }

    private ThreatInfo getCachedThreat(VillagerEntity guard) {
        // Find the most relevant cached threat for this guard
        double guardX = guard.getX();
        double guardZ = guard.getZ();

        return activeThreatMemory.values().stream()
            .filter(threat -> threat.getThreatEntity().isAlive())
            .filter(threat -> {
                double dx = threat.getThreatEntity().getX() - guardX;
                double dz = threat.getThreatEntity().getZ() - guardZ;
                return (dx * dx + dz * dz) <= BASE_DETECTION_RANGE * BASE_DETECTION_RANGE;
            })
            .max(Comparator.comparingInt(ThreatInfo::getPriorityValue))
            .orElse(null);
    }

    private void updateThreatMemory(ThreatInfo threat, int currentTick) {
        UUID threatId = threat.getThreatEntity().getUuid();
        threat.setLastSeenTick(currentTick);
        activeThreatMemory.put(threatId, threat);
    }

    private void cleanupThreatMemory(int currentTick) {
        activeThreatMemory.entrySet().removeIf(entry -> {
            ThreatInfo threat = entry.getValue();
            return !threat.getThreatEntity().isAlive() ||
                   (currentTick - threat.getLastSeenTick()) > THREAT_MEMORY_DURATION;
        });
    }

    private void alertNearbyGuards(LivingEntity victim, ThreatInfo threat) {
        double alertRange = BASE_DETECTION_RANGE * 2; // 32 blocks

        List<VillagerEntity> nearbyGuards = world.getEntitiesByClass(
            VillagerEntity.class,
            victim.getBoundingBox().expand(alertRange),
            villager -> isGuard(villager) && villager.getTarget() == null
        );

        for (VillagerEntity guard : nearbyGuards) {
            // Set target directly for immediate response
            guard.setTarget(threat.getThreatEntity());

            XeenaaVillagerManager.LOGGER.debug("Alerted guard {} of threat: {}",
                guard.getUuid(), threat.getThreatEntity().getType().getTranslationKey());
        }
    }

    private boolean isGuard(VillagerEntity villager) {
        return villager.getVillagerData().getProfession().id().equals("guard");
    }
}