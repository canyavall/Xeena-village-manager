package com.xeenaa.villagermanager.ai;

import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages special abilities for guard villagers based on rank and specialization.
 * Handles cooldowns, effects, and ability progression.
 */
public class GuardSpecialAbilities {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardSpecialAbilities.class);
    private static final Map<UUID, GuardSpecialAbilities> INSTANCES = new HashMap<>();

    // Ability IDs
    public enum AbilityType {
        // Melee abilities
        AREA_STRIKE,      // Tier 3+: Minor area damage
        SHIELD_BLOCK,     // Tier 4+: Damage reduction
        SWEEP_ATTACK,     // Tier 5: Area sweep with knockback
        TAUNT,           // Tier 5: Force enemies to target guard

        // Ranged abilities
        PRECISION_SHOT,   // Tier 3+: Improved accuracy and range
        MULTISHOT,       // Tier 4+: Fire multiple arrows
        SLOWING_ARROW,   // Tier 4+: Arrows apply slowness
        EXPLOSIVE_SHOT,  // Tier 5: Arrows explode on impact
        DOUBLE_SHOT      // Tier 5: Fire two arrows at different targets
    }

    private final VillagerEntity guard;
    private final Map<AbilityType, Integer> cooldowns = new HashMap<>();
    private final Map<AbilityType, Boolean> abilityStates = new HashMap<>();

    // Cooldown constants (in ticks)
    private static final Map<AbilityType, Integer> ABILITY_COOLDOWNS = Map.of(
        AbilityType.AREA_STRIKE, 80,      // 4 seconds
        AbilityType.SHIELD_BLOCK, 100,    // 5 seconds
        AbilityType.SWEEP_ATTACK, 160,    // 8 seconds
        AbilityType.TAUNT, 200,           // 10 seconds
        AbilityType.PRECISION_SHOT, 60,   // 3 seconds
        AbilityType.MULTISHOT, 120,       // 6 seconds
        AbilityType.SLOWING_ARROW, 100,   // 5 seconds
        AbilityType.EXPLOSIVE_SHOT, 240,  // 12 seconds
        AbilityType.DOUBLE_SHOT, 180      // 9 seconds
    );

    private GuardSpecialAbilities(VillagerEntity guard) {
        this.guard = guard;
        // Initialize all cooldowns to 0
        for (AbilityType ability : AbilityType.values()) {
            cooldowns.put(ability, 0);
            abilityStates.put(ability, false);
        }
    }

    /**
     * Gets or creates abilities instance for a guard
     */
    public static GuardSpecialAbilities get(VillagerEntity guard) {
        UUID guardId = guard.getUuid();
        return INSTANCES.computeIfAbsent(guardId, k -> new GuardSpecialAbilities(guard));
    }

    /**
     * Removes abilities instance when guard is removed
     */
    public static void remove(UUID guardId) {
        INSTANCES.remove(guardId);
    }

    /**
     * Updates all cooldowns (call every tick)
     */
    public void tick() {
        for (AbilityType ability : AbilityType.values()) {
            int currentCooldown = cooldowns.get(ability);
            if (currentCooldown > 0) {
                cooldowns.put(ability, currentCooldown - 1);
            }
        }
    }

    /**
     * Checks if an ability is available (not on cooldown and rank requirement met)
     */
    public boolean isAbilityAvailable(AbilityType ability) {
        if (cooldowns.get(ability) > 0) {
            return false;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return false;
        }

        GuardRankData rankData = guardData.getRankData();
        return isAbilityUnlocked(ability, rankData);
    }

    /**
     * Uses an ability if available
     */
    public boolean useAbility(AbilityType ability, LivingEntity target) {
        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        String rankName = guardData != null ? guardData.getRankData().getCurrentRank().getDisplayName() : "UNKNOWN";
        int tier = guardData != null ? guardData.getRankData().getCurrentTier() : 0;

        LOGGER.info("[ABILITY CHECK] Guard {} (Rank: {}, Tier: {}) checking ability: {}",
            guard.getUuid().toString().substring(0, 8), rankName, tier, ability);

        if (!isAbilityAvailable(ability)) {
            LOGGER.info("[ABILITY CHECK] Ability {} NOT available (on cooldown or locked)", ability);
            return false;
        }

        LOGGER.info("[ABILITY CHECK] ✓ Ability {} is available, executing...", ability);
        boolean success = executeAbility(ability, target);
        if (success) {
            cooldowns.put(ability, ABILITY_COOLDOWNS.get(ability));
            LOGGER.info("[ABILITY CHECK] ✓ Ability {} executed successfully", ability);
        } else {
            LOGGER.info("[ABILITY CHECK] ✗ Ability {} execution failed", ability);
        }

        return success;
    }

    /**
     * Gets remaining cooldown for an ability
     */
    public int getRemainingCooldown(AbilityType ability) {
        return cooldowns.get(ability);
    }

    /**
     * Checks if an ability is unlocked based on rank and specialization
     */
    private boolean isAbilityUnlocked(AbilityType ability, GuardRankData rankData) {
        int tier = rankData.getCurrentTier();
        String pathId = rankData.getChosenPath() != null ?
            rankData.getChosenPath().getId() : rankData.getCurrentRank().getPath().getId();

        switch (ability) {
            // Melee abilities (Man-at-Arms path)
            case AREA_STRIKE:
                return pathId.equals("man_at_arms") && tier >= 3;
            case SHIELD_BLOCK:
                return pathId.equals("man_at_arms") && tier >= 4;
            case SWEEP_ATTACK:
            case TAUNT:
                return pathId.equals("man_at_arms") && tier >= 5;

            // Ranged abilities (Marksman path)
            case PRECISION_SHOT:
                return pathId.equals("ranged") && tier >= 3;
            case MULTISHOT:
            case SLOWING_ARROW:
                return pathId.equals("ranged") && tier >= 4;
            case EXPLOSIVE_SHOT:
            case DOUBLE_SHOT:
                return pathId.equals("ranged") && tier >= 4;

            default:
                return false;
        }
    }

    /**
     * Executes the specified ability
     */
    private boolean executeAbility(AbilityType ability, LivingEntity target) {
        switch (ability) {
            case AREA_STRIKE:
                return executeAreaStrike(target);
            case SHIELD_BLOCK:
                return executeShieldBlock();
            case SWEEP_ATTACK:
                return executeSweepAttack(target);
            case TAUNT:
                return executeTaunt();
            case PRECISION_SHOT:
                return executePrecisionShot(target);
            case MULTISHOT:
                return executeMultishot(target);
            case SLOWING_ARROW:
                return executeSlowingArrow(target);
            case EXPLOSIVE_SHOT:
                return executeExplosiveShot(target);
            case DOUBLE_SHOT:
                return executeDoubleShot(target);
            default:
                return false;
        }
    }

    // Melee ability implementations
    private boolean executeAreaStrike(LivingEntity target) {
        float damage = (float) guard.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE) * 0.6f;

        Box searchBox = Box.of(guard.getPos(), 5, 3, 5);
        List<LivingEntity> nearbyEnemies = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard && entity != target && guard.canTarget(entity));

        for (LivingEntity enemy : nearbyEnemies) {
            if (guard.squaredDistanceTo(enemy) <= 6.25) { // 2.5 block radius
                enemy.damage(guard.getDamageSources().mobAttack(guard), damage);
                enemy.takeKnockback(0.3, guard.getX() - enemy.getX(), guard.getZ() - enemy.getZ());
            }
        }

        spawnParticles(ParticleTypes.SWEEP_ATTACK, guard.getPos(), 5);
        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            guard.getSoundCategory(), 0.8f, 1.2f);

        return true;
    }

    private boolean executeShieldBlock() {
        abilityStates.put(AbilityType.SHIELD_BLOCK, true);

        // Apply resistance effect
        guard.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 2)); // 3 seconds of Resistance III

        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK,
            guard.getSoundCategory(), 1.0f, 1.0f);

        // Schedule to turn off blocking
        guard.getWorld().getServer().execute(() -> {
            if (guard.getWorld().getTime() % 60 == 0) {
                abilityStates.put(AbilityType.SHIELD_BLOCK, false);
            }
        });

        return true;
    }

    private boolean executeSweepAttack(LivingEntity target) {
        float damage = (float) guard.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f;

        // Damage primary target
        target.damage(guard.getDamageSources().mobAttack(guard), damage);
        target.takeKnockback(1.5, guard.getX() - target.getX(), guard.getZ() - target.getZ());

        // Area damage
        Box searchBox = Box.of(guard.getPos(), 6, 3, 6);
        List<LivingEntity> nearbyEnemies = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard && entity != target && guard.canTarget(entity));

        for (LivingEntity enemy : nearbyEnemies) {
            if (guard.squaredDistanceTo(enemy) <= 9) { // 3 block radius
                enemy.damage(guard.getDamageSources().mobAttack(guard), damage * 0.8f);
                enemy.takeKnockback(1.0, guard.getX() - enemy.getX(), guard.getZ() - enemy.getZ());
            }
        }

        spawnParticles(ParticleTypes.SWEEP_ATTACK, guard.getPos(), 10);
        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            guard.getSoundCategory(), 1.0f, 0.8f);
        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
            guard.getSoundCategory(), 0.8f, 1.2f);

        return true;
    }

    private boolean executeTaunt() {
        Box searchBox = Box.of(guard.getPos(), 16, 8, 16);
        List<LivingEntity> nearbyEnemies = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard && guard.canTarget(entity));

        int taunted = 0;
        for (LivingEntity enemy : nearbyEnemies) {
            if (guard.squaredDistanceTo(enemy) <= 64 && enemy instanceof net.minecraft.entity.mob.MobEntity mobEntity) {
                mobEntity.setTarget(guard);
                taunted++;
            }
        }

        spawnParticles(ParticleTypes.ANGRY_VILLAGER, guard.getPos().add(0, 2, 0), 8);
        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_VILLAGER_CELEBRATE,
            guard.getSoundCategory(), 1.0f, 0.8f);

        return taunted > 0;
    }

    // Ranged ability implementations
    private boolean executePrecisionShot(LivingEntity target) {
        ItemStack arrow = new ItemStack(Items.ARROW);
        ArrowEntity projectile = new ArrowEntity(guard.getWorld(), guard, arrow, null);

        // Calculate precise aim
        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        projectile.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, 2.0f, 2); // Very low inaccuracy
        projectile.setDamage(projectile.getDamage() * 1.5); // 50% more damage

        guard.getWorld().spawnEntity(projectile);
        guard.swingHand(Hand.MAIN_HAND);

        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT,
            guard.getSoundCategory(), 1.0f, 1.2f);

        return true;
    }

    private boolean executeMultishot(LivingEntity target) {
        guard.swingHand(Hand.MAIN_HAND);

        // Fire 3 arrows in a spread
        for (int i = 0; i < 3; i++) {
            ItemStack arrow = new ItemStack(Items.ARROW);
            ArrowEntity projectile = new ArrowEntity(guard.getWorld(), guard, arrow, null);

            double deltaX = target.getX() - guard.getX();
            double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
            double deltaZ = target.getZ() - guard.getZ();
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            // Add spread
            double spreadAngle = (i - 1) * 0.2;
            double cos = Math.cos(spreadAngle);
            double sin = Math.sin(spreadAngle);
            double newDeltaX = deltaX * cos - deltaZ * sin;
            double newDeltaZ = deltaX * sin + deltaZ * cos;

            projectile.setVelocity(newDeltaX, deltaY + horizontalDistance * 0.20000000298023224, newDeltaZ, 1.6f, 12);
            guard.getWorld().spawnEntity(projectile);
        }

        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT,
            guard.getSoundCategory(), 1.0f, 0.8f);

        return true;
    }

    private boolean executeSlowingArrow(LivingEntity target) {
        ItemStack arrow = new ItemStack(Items.ARROW);
        ArrowEntity projectile = new ArrowEntity(guard.getWorld(), guard, arrow, null);

        // Mark arrow for slowness effect (would need projectile impact mixin)
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("SlowingArrow", true);
        projectile.writeNbt(nbt);

        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        projectile.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, 1.8f, 8);
        guard.getWorld().spawnEntity(projectile);
        guard.swingHand(Hand.MAIN_HAND);

        spawnParticles(ParticleTypes.SNOWFLAKE, guard.getPos().add(0, 1.5, 0), 5);
        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT,
            guard.getSoundCategory(), 1.0f, 0.9f);

        return true;
    }

    private boolean executeExplosiveShot(LivingEntity target) {
        ItemStack arrow = new ItemStack(Items.ARROW);
        ArrowEntity projectile = new ArrowEntity(guard.getWorld(), guard, arrow, null);

        // Mark arrow for explosive behavior
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("ExplosiveArrow", true);
        projectile.writeNbt(nbt);

        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        projectile.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, 2.2f, 6);
        guard.getWorld().spawnEntity(projectile);
        guard.swingHand(Hand.MAIN_HAND);

        spawnParticles(ParticleTypes.FLAME, guard.getPos().add(0, 1.5, 0), 8);
        guard.getWorld().playSound(null, guard.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
            guard.getSoundCategory(), 1.0f, 1.2f);

        return true;
    }

    private boolean executeDoubleShot(LivingEntity target) {
        LOGGER.info("[DOUBLE SHOT EXEC] Guard {} executing Double Shot",
            guard.getUuid().toString().substring(0, 8));

        // Find a second target nearby
        double DETECTION_RANGE = 15.0;
        Box searchBox = Box.of(guard.getPos(), DETECTION_RANGE * 2, DETECTION_RANGE * 2, DETECTION_RANGE * 2);
        List<LivingEntity> nearbyEnemies = guard.getWorld().getEntitiesByClass(
            LivingEntity.class, searchBox,
            entity -> entity != guard &&
                     entity != target &&
                     entity.isAlive() &&
                     guard.canTarget(entity) &&
                     guard.canSee(entity)
        );

        LOGGER.info("[DOUBLE SHOT EXEC] Found {} potential secondary targets", nearbyEnemies.size());

        LivingEntity secondaryTarget = null;
        double closestDistance = DETECTION_RANGE;

        // Find the closest valid secondary target
        for (LivingEntity entity : nearbyEnemies) {
            double distance = guard.squaredDistanceTo(entity);
            if (Math.sqrt(distance) < closestDistance) {
                secondaryTarget = entity;
                closestDistance = Math.sqrt(distance);
            }
        }

        // Fire second arrow if we found a target
        if (secondaryTarget != null) {
            LOGGER.info("[DOUBLE SHOT EXEC] ✓ FIRING second arrow at {} (distance: {:.2f} blocks)",
                secondaryTarget.getName().getString(), closestDistance);
            fireArrowAtTarget(secondaryTarget);

            // Play special sound effect
            guard.getWorld().playSound(null, guard.getBlockPos(),
                SoundEvents.ENTITY_ARROW_SHOOT,
                guard.getSoundCategory(), 1.0f, 1.2f);

            spawnParticles(ParticleTypes.ENCHANT, guard.getPos().add(0, 1.5, 0), 10);
            return true;
        }

        LOGGER.info("[DOUBLE SHOT EXEC] ✗ No valid secondary target found");
        return false;
    }

    /**
     * Fires an arrow at the specified target for double shot ability
     */
    private void fireArrowAtTarget(LivingEntity target) {
        // Create arrow entity
        ArrowEntity arrow = new ArrowEntity(guard.getWorld(), guard, new ItemStack(Items.ARROW), null);

        // Apply slowness effect to arrows (Slowness I for 3 seconds)
        arrow.addEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS,
            60, // 3 seconds
            0,  // Slowness I
            false,
            false
        ));

        // Calculate aim
        double deltaX = target.getX() - guard.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - arrow.getY();
        double deltaZ = target.getZ() - guard.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Set velocity with improved accuracy for double shot
        float velocity = 1.6f;
        float accuracy = 8.0f;
        arrow.setVelocity(deltaX, deltaY + horizontalDistance * 0.20000000298023224, deltaZ, velocity, accuracy);

        // Spawn arrow
        guard.getWorld().spawnEntity(arrow);
    }

    /**
     * Spawns particles for visual effects
     */
    private void spawnParticles(net.minecraft.particle.ParticleEffect particle, Vec3d center, int count) {
        if (guard.getWorld() instanceof ServerWorld serverWorld) {
            for (int i = 0; i < count; i++) {
                double offsetX = (guard.getRandom().nextDouble() - 0.5) * 2.0;
                double offsetY = guard.getRandom().nextDouble();
                double offsetZ = (guard.getRandom().nextDouble() - 0.5) * 2.0;

                serverWorld.spawnParticles(particle,
                    center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                    1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Checks if the guard is currently blocking
     */
    public boolean isBlocking() {
        return abilityStates.get(AbilityType.SHIELD_BLOCK);
    }
}