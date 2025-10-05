package com.xeenaa.villagermanager.ai.performance;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching system for expensive pathfinding operations.
 *
 * <p>Pathfinding is one of the most expensive AI operations. This cache stores recently
 * calculated paths and patrol positions to avoid redundant calculations.</p>
 *
 * <p>Cache invalidation occurs:</p>
 * <ul>
 *   <li>After a time threshold (40 ticks / 2 seconds)</li>
 *   <li>When the guard moves significantly (>8 blocks)</li>
 *   <li>When the target moves significantly (>8 blocks)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class PathfindingCache {
    private static final int CACHE_DURATION_TICKS = 40;  // 2 seconds
    private static final double POSITION_CHANGE_THRESHOLD = 8.0;  // 8 blocks

    private final Map<UUID, CachedPath> pathCache;
    private final Map<UUID, CachedPatrolPosition> patrolCache;

    public PathfindingCache() {
        this.pathCache = new ConcurrentHashMap<>();
        this.patrolCache = new ConcurrentHashMap<>();
    }

    /**
     * Gets a cached path if valid, or returns null if cache miss/invalid.
     *
     * @param guardId The guard UUID
     * @param currentPos Current guard position
     * @param targetPos Current target position
     * @param currentTick Current server tick
     * @return Cached target position if valid, or null
     */
    public BlockPos getCachedPath(UUID guardId, BlockPos currentPos, BlockPos targetPos, int currentTick) {
        CachedPath cached = pathCache.get(guardId);

        if (cached == null) {
            return null;
        }

        // Check if cache is still valid
        if (currentTick - cached.cacheTick > CACHE_DURATION_TICKS) {
            pathCache.remove(guardId);
            return null;
        }

        // Check if positions haven't changed significantly
        if (currentPos.getSquaredDistance(cached.guardPos) > POSITION_CHANGE_THRESHOLD * POSITION_CHANGE_THRESHOLD) {
            pathCache.remove(guardId);
            return null;
        }

        if (targetPos.getSquaredDistance(cached.targetPos) > POSITION_CHANGE_THRESHOLD * POSITION_CHANGE_THRESHOLD) {
            pathCache.remove(guardId);
            return null;
        }

        return cached.cachedTarget;
    }

    /**
     * Caches a pathfinding result.
     *
     * @param guardId The guard UUID
     * @param guardPos Current guard position
     * @param targetPos Current target position
     * @param cachedTarget The calculated path target
     * @param currentTick Current server tick
     */
    public void cachePath(UUID guardId, BlockPos guardPos, BlockPos targetPos, BlockPos cachedTarget, int currentTick) {
        pathCache.put(guardId, new CachedPath(guardPos, targetPos, cachedTarget, currentTick));
    }

    /**
     * Gets a cached patrol position if valid.
     *
     * @param guardId The guard UUID
     * @param currentTick Current server tick
     * @return Cached patrol position if valid, or null
     */
    public BlockPos getCachedPatrolPosition(UUID guardId, int currentTick) {
        CachedPatrolPosition cached = patrolCache.get(guardId);

        if (cached == null) {
            return null;
        }

        // Patrol positions have a longer cache duration (100 ticks = 5 seconds)
        if (currentTick - cached.cacheTick > 100) {
            patrolCache.remove(guardId);
            return null;
        }

        // Check if guard has reached the patrol position
        if (cached.guardPos.getSquaredDistance(cached.patrolPos) < 4.0) {  // Within 2 blocks
            patrolCache.remove(guardId);
            return null;
        }

        return cached.patrolPos;
    }

    /**
     * Caches a patrol position.
     *
     * @param guardId The guard UUID
     * @param guardPos Current guard position
     * @param patrolPos Calculated patrol position
     * @param currentTick Current server tick
     */
    public void cachePatrolPosition(UUID guardId, BlockPos guardPos, BlockPos patrolPos, int currentTick) {
        patrolCache.put(guardId, new CachedPatrolPosition(guardPos, patrolPos, currentTick));
    }

    /**
     * Invalidates all cached data for a guard.
     *
     * @param guardId The guard UUID
     */
    public void invalidate(UUID guardId) {
        pathCache.remove(guardId);
        patrolCache.remove(guardId);
    }

    /**
     * Clears all cached data.
     */
    public void clear() {
        pathCache.clear();
        patrolCache.clear();
    }

    /**
     * Stores a cached pathfinding result.
     */
    private static class CachedPath {
        final BlockPos guardPos;
        final BlockPos targetPos;
        final BlockPos cachedTarget;
        final int cacheTick;

        CachedPath(BlockPos guardPos, BlockPos targetPos, BlockPos cachedTarget, int cacheTick) {
            this.guardPos = guardPos;
            this.targetPos = targetPos;
            this.cachedTarget = cachedTarget;
            this.cacheTick = cacheTick;
        }
    }

    /**
     * Stores a cached patrol position.
     */
    private static class CachedPatrolPosition {
        final BlockPos guardPos;
        final BlockPos patrolPos;
        final int cacheTick;

        CachedPatrolPosition(BlockPos guardPos, BlockPos patrolPos, int cacheTick) {
            this.guardPos = guardPos;
            this.patrolPos = patrolPos;
            this.cacheTick = cacheTick;
        }
    }
}
