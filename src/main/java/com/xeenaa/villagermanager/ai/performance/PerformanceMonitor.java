package com.xeenaa.villagermanager.ai.performance;

import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance monitoring utility for guard AI systems.
 *
 * <p>Tracks and logs performance metrics including:</p>
 * <ul>
 *   <li>AI update frequency per guard</li>
 *   <li>Threat detection scan rates</li>
 *   <li>Cache hit/miss ratios</li>
 *   <li>Overall system performance</li>
 * </ul>
 *
 * <p>Use this class to identify performance bottlenecks and validate optimizations.</p>
 *
 * @since 1.0.0
 */
public class PerformanceMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final Map<String, PerformanceMonitor> INSTANCES = new ConcurrentHashMap<>();

    private final ServerWorld world;
    private final Map<String, PerformanceMetric> metrics;

    private int totalAIUpdates = 0;
    private int totalThreatScans = 0;
    private int skippedAIUpdates = 0;
    private int skippedThreatScans = 0;
    private int lastReportTick = 0;

    private static final int REPORT_INTERVAL = 6000; // Report every 5 minutes

    /**
     * Gets or creates a performance monitor for the specified world.
     *
     * @param world The server world
     * @return The performance monitor instance
     */
    public static PerformanceMonitor get(ServerWorld world) {
        return INSTANCES.computeIfAbsent(world.getRegistryKey().getValue().toString(),
            k -> new PerformanceMonitor(world));
    }

    /**
     * Clears all monitors (for cleanup).
     */
    public static void clearAll() {
        INSTANCES.clear();
    }

    private PerformanceMonitor(ServerWorld world) {
        this.world = world;
        this.metrics = new ConcurrentHashMap<>();
    }

    /**
     * Records an AI update execution.
     */
    public void recordAIUpdate() {
        totalAIUpdates++;
    }

    /**
     * Records a skipped AI update (due to scheduling).
     */
    public void recordSkippedAIUpdate() {
        skippedAIUpdates++;
    }

    /**
     * Records a threat detection scan.
     */
    public void recordThreatScan() {
        totalThreatScans++;
    }

    /**
     * Records a skipped threat detection scan.
     */
    public void recordSkippedThreatScan() {
        skippedThreatScans++;
    }

    /**
     * Records a custom performance metric.
     *
     * @param name Metric name
     * @param value Metric value (e.g., execution time in ms)
     */
    public void recordMetric(String name, long value) {
        PerformanceMetric metric = metrics.computeIfAbsent(name, k -> new PerformanceMetric());
        metric.record(value);
    }

    /**
     * Checks if it's time to report performance stats and does so if needed.
     */
    public void tick() {
        int currentTick = world.getServer().getTicks();

        if (currentTick - lastReportTick >= REPORT_INTERVAL) {
            reportPerformanceStats();
            resetCounters();
            lastReportTick = currentTick;
        }
    }

    /**
     * Generates and logs a performance report.
     */
    private void reportPerformanceStats() {
        int totalUpdates = totalAIUpdates + skippedAIUpdates;
        int totalScans = totalThreatScans + skippedThreatScans;

        double aiReductionPercent = totalUpdates > 0 ?
            (skippedAIUpdates * 100.0 / totalUpdates) : 0;
        double scanReductionPercent = totalScans > 0 ?
            (skippedThreatScans * 100.0 / totalScans) : 0;

        LOGGER.info("=== Guard AI Performance Report ===");
        LOGGER.info("AI Updates: {} executed, {} skipped ({:.1f}% reduction)",
            totalAIUpdates, skippedAIUpdates, aiReductionPercent);
        LOGGER.info("Threat Scans: {} executed, {} skipped ({:.1f}% reduction)",
            totalThreatScans, skippedThreatScans, scanReductionPercent);

        // Report custom metrics
        for (Map.Entry<String, PerformanceMetric> entry : metrics.entrySet()) {
            PerformanceMetric metric = entry.getValue();
            LOGGER.info("Metric [{}]: avg={:.2f}ms, min={}ms, max={}ms, count={}",
                entry.getKey(),
                metric.getAverage(),
                metric.getMin(),
                metric.getMax(),
                metric.getCount());
        }

        LOGGER.info("===================================");
    }

    /**
     * Resets performance counters.
     */
    private void resetCounters() {
        totalAIUpdates = 0;
        totalThreatScans = 0;
        skippedAIUpdates = 0;
        skippedThreatScans = 0;
        metrics.clear();
    }

    /**
     * Gets the current AI update reduction percentage.
     *
     * @return Percentage of AI updates skipped (0-100)
     */
    public double getAIReductionPercent() {
        int total = totalAIUpdates + skippedAIUpdates;
        return total > 0 ? (skippedAIUpdates * 100.0 / total) : 0;
    }

    /**
     * Gets the current threat scan reduction percentage.
     *
     * @return Percentage of threat scans skipped (0-100)
     */
    public double getScanReductionPercent() {
        int total = totalThreatScans + skippedThreatScans;
        return total > 0 ? (skippedThreatScans * 100.0 / total) : 0;
    }

    /**
     * Stores statistics for a performance metric.
     */
    private static class PerformanceMetric {
        private long sum = 0;
        private long min = Long.MAX_VALUE;
        private long max = Long.MIN_VALUE;
        private int count = 0;

        void record(long value) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
            count++;
        }

        double getAverage() {
            return count > 0 ? (double) sum / count : 0;
        }

        long getMin() {
            return min != Long.MAX_VALUE ? min : 0;
        }

        long getMax() {
            return max != Long.MIN_VALUE ? max : 0;
        }

        int getCount() {
            return count;
        }
    }
}
