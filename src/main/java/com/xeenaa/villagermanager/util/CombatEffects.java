package com.xeenaa.villagermanager.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Performance-optimized combat visual and audio effects utility.
 *
 * <p>This class provides lightweight particle and sound effects for guard combat,
 * with strict performance constraints to maintain excellent FPS even with many
 * guards in combat simultaneously.</p>
 *
 * <p><strong>Performance Guarantees:</strong></p>
 * <ul>
 *   <li>Maximum 20 particles per effect</li>
 *   <li>Client-side rendering where possible</li>
 *   <li>Distance-based culling (effects only render within 32 blocks)</li>
 *   <li>Vanilla particles only (no custom textures)</li>
 *   <li>Vanilla sounds only (no custom audio files)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class CombatEffects {

    // Performance constants
    private static final double MAX_EFFECT_DISTANCE = 32.0;
    private static final double MAX_EFFECT_DISTANCE_SQUARED = MAX_EFFECT_DISTANCE * MAX_EFFECT_DISTANCE;
    private static final int MAX_PARTICLES_PER_EFFECT = 20;

    // Particle count constants (optimized for performance)
    private static final int SWING_PARTICLES = 2;
    private static final int HIT_PARTICLES = 4;
    private static final int KNOCKBACK_RING_PARTICLES = 12;
    private static final int AREA_DAMAGE_PARTICLES = 8;
    private static final int ARROW_TRAIL_PARTICLES = 3;
    private static final int DEBUFF_PARTICLES = 5;

    /**
     * Spawns weapon swing particles (melee).
     * Very subtle effect with only 2 particles for performance.
     *
     * @param world the world to spawn particles in
     * @param attacker the entity swinging the weapon
     */
    public static void spawnMeleeSwingParticles(World world, LivingEntity attacker) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling - don't spawn particles if no players nearby
        if (!hasNearbyPlayers(serverWorld, attacker.getPos())) {
            return;
        }

        Vec3d pos = attacker.getPos().add(0, attacker.getHeight() * 0.5, 0);
        Vec3d lookDirection = attacker.getRotationVector().normalize();

        // Spawn 2 particles in front of the attacker
        for (int i = 0; i < SWING_PARTICLES; i++) {
            double offset = (i + 1) * 0.3;
            Vec3d particlePos = pos.add(lookDirection.multiply(offset));

            serverWorld.spawnParticles(
                ParticleTypes.SWEEP_ATTACK,
                particlePos.x, particlePos.y, particlePos.z,
                1, 0, 0, 0, 0
            );
        }
    }

    /**
     * Spawns arrow trail particles for ranged attacks.
     * Creates 3 particles along the arrow's initial trajectory.
     *
     * @param world the world to spawn particles in
     * @param shooter the entity shooting the arrow
     * @param target the target being shot at
     */
    public static void spawnArrowTrailParticles(World world, LivingEntity shooter, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling
        if (!hasNearbyPlayers(serverWorld, shooter.getPos())) {
            return;
        }

        Vec3d startPos = shooter.getPos().add(0, shooter.getStandingEyeHeight() - 0.1, 0);
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);
        Vec3d direction = targetPos.subtract(startPos).normalize();

        // Spawn particles along initial trajectory
        for (int i = 0; i < ARROW_TRAIL_PARTICLES; i++) {
            double offset = (i + 1) * 0.5;
            Vec3d particlePos = startPos.add(direction.multiply(offset));

            serverWorld.spawnParticles(
                ParticleTypes.CRIT,
                particlePos.x, particlePos.y, particlePos.z,
                1, 0.05, 0.05, 0.05, 0.01
            );
        }
    }

    /**
     * Spawns hit impact particles when an attack lands successfully.
     * Creates a small burst of 4 particles at the impact point.
     *
     * @param world the world to spawn particles in
     * @param target the entity that was hit
     * @param isCritical whether the hit was a critical strike
     */
    public static void spawnHitImpactParticles(World world, LivingEntity target, boolean isCritical) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling
        if (!hasNearbyPlayers(serverWorld, target.getPos())) {
            return;
        }

        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.5, 0);
        ParticleEffect particleType = isCritical ? ParticleTypes.CRIT : ParticleTypes.DAMAGE_INDICATOR;

        // Small burst of particles
        serverWorld.spawnParticles(
            particleType,
            pos.x, pos.y, pos.z,
            HIT_PARTICLES,
            0.2, 0.3, 0.2,
            0.05
        );
    }

    /**
     * Spawns knockback shockwave particles for Knight ability.
     * Creates a ring of 12 particles around the target.
     *
     * @param world the world to spawn particles in
     * @param target the entity being knocked back
     */
    public static void spawnKnockbackShockwave(World world, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling
        if (!hasNearbyPlayers(serverWorld, target.getPos())) {
            return;
        }

        Vec3d pos = target.getPos();

        // Create circular ring of particles
        for (int i = 0; i < KNOCKBACK_RING_PARTICLES; i++) {
            double angle = (i / (double) KNOCKBACK_RING_PARTICLES) * Math.PI * 2;
            double radius = 0.5;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;

            serverWorld.spawnParticles(
                ParticleTypes.CLOUD,
                x, pos.y, z,
                1, 0, 0, 0, 0.02
            );
        }
    }

    /**
     * Spawns area damage indicator particles for Knight secondary targets.
     * Creates 8 red particles to indicate splash damage.
     *
     * @param world the world to spawn particles in
     * @param target the secondary target hit by area damage
     */
    public static void spawnAreaDamageIndicators(World world, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling
        if (!hasNearbyPlayers(serverWorld, target.getPos())) {
            return;
        }

        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.5, 0);

        // Red damage particles for secondary targets
        serverWorld.spawnParticles(
            ParticleTypes.DAMAGE_INDICATOR,
            pos.x, pos.y, pos.z,
            AREA_DAMAGE_PARTICLES,
            0.3, 0.3, 0.3,
            0.1
        );
    }

    /**
     * Spawns slowness debuff particles on target.
     * Creates 5 blue-ish particles to indicate the debuff.
     *
     * @param world the world to spawn particles in
     * @param target the entity affected by slowness
     */
    public static void spawnSlownessDebuffParticles(World world, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling
        if (!hasNearbyPlayers(serverWorld, target.getPos())) {
            return;
        }

        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.7, 0);

        // Snowflake particles for slowness visual
        serverWorld.spawnParticles(
            ParticleTypes.SNOWFLAKE,
            pos.x, pos.y, pos.z,
            DEBUFF_PARTICLES,
            0.3, 0.3, 0.3,
            0.02
        );
    }

    /**
     * Spawns enhanced arrow trail for Sharpshooter Double Shot ability.
     * Creates enchantment glint particles along the arrow path.
     *
     * @param world the world to spawn particles in
     * @param shooter the guard shooting the arrow
     * @param target the target being shot at
     */
    public static void spawnDoubleShotTrail(World world, LivingEntity shooter, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Distance culling
        if (!hasNearbyPlayers(serverWorld, shooter.getPos())) {
            return;
        }

        Vec3d startPos = shooter.getPos().add(0, shooter.getStandingEyeHeight() - 0.1, 0);
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);
        Vec3d direction = targetPos.subtract(startPos).normalize();

        // Enchantment glint particles for special ability
        for (int i = 0; i < 5; i++) {
            double offset = (i + 1) * 0.4;
            Vec3d particlePos = startPos.add(direction.multiply(offset));

            serverWorld.spawnParticles(
                ParticleTypes.ENCHANT,
                particlePos.x, particlePos.y, particlePos.z,
                2, 0.1, 0.1, 0.1, 0.05
            );
        }
    }

    // ==================== SOUND EFFECTS ====================

    /**
     * Plays Knight knockback ability sound.
     * Uses vanilla BLOCK_ANVIL_LAND at reduced volume.
     *
     * @param world the world to play the sound in
     * @param pos the position to play the sound at
     * @param category the sound category
     */
    public static void playKnockbackSound(World world, Vec3d pos, SoundCategory category) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.BLOCK_ANVIL_LAND,
            category,
            0.5f, // Reduced volume
            1.2f  // Slightly higher pitch
        );
    }

    /**
     * Plays Knight area damage sweep sound.
     * Uses vanilla sweep attack sound.
     *
     * @param world the world to play the sound in
     * @param pos the position to play the sound at
     * @param category the sound category
     */
    public static void playAreaDamageSound(World world, Vec3d pos, SoundCategory category) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            category,
            0.8f,
            1.0f
        );
    }

    /**
     * Plays Sharpshooter Double Shot sound effect.
     * Uses vanilla arrow shoot sound with higher pitch.
     *
     * @param world the world to play the sound in
     * @param pos the position to play the sound at
     * @param category the sound category
     */
    public static void playDoubleShotSound(World world, Vec3d pos, SoundCategory category) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_ARROW_SHOOT,
            category,
            1.0f,
            1.3f  // Higher pitch for special ability
        );
    }

    /**
     * Plays melee weapon swing sound.
     * Uses vanilla player attack sound.
     *
     * @param world the world to play the sound in
     * @param pos the position to play the sound at
     * @param category the sound category
     */
    public static void playMeleeSwingSound(World world, Vec3d pos, SoundCategory category) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
            category,
            0.7f,
            1.0f
        );
    }

    /**
     * Plays melee hit impact sound.
     * Uses vanilla strong attack sound.
     *
     * @param world the world to play the sound in
     * @param pos the position to play the sound at
     * @param category the sound category
     * @param isCritical whether the hit was a critical strike
     */
    public static void playMeleeHitSound(World world, Vec3d pos, SoundCategory category, boolean isCritical) {
        SoundEvent sound = isCritical ?
            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT :
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG;

        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            sound,
            category,
            1.0f,
            1.0f
        );
    }

    /**
     * Plays ranged attack bow sound.
     * Uses vanilla arrow shoot sound.
     *
     * @param world the world to play the sound in
     * @param pos the position to play the sound at
     * @param category the sound category
     */
    public static void playBowShootSound(World world, Vec3d pos, SoundCategory category) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_ARROW_SHOOT,
            category,
            1.0f,
            1.0f
        );
    }

    // ==================== HELPER METHODS ====================

    /**
     * Checks if any players are nearby to render effects for.
     * Distance culling optimization - don't spawn particles if no one can see them.
     *
     * @param world the server world
     * @param pos the position to check around
     * @return true if players are nearby, false otherwise
     */
    private static boolean hasNearbyPlayers(ServerWorld world, Vec3d pos) {
        return world.getPlayers().stream()
            .anyMatch(player -> player.squaredDistanceTo(pos) <= MAX_EFFECT_DISTANCE_SQUARED);
    }
}
