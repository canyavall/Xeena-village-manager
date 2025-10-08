package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.ai.GuardAttackGoal;
import com.xeenaa.villagermanager.ai.GuardDefendVillageGoal;
import com.xeenaa.villagermanager.ai.GuardFollowVillagerGoal;
import com.xeenaa.villagermanager.ai.GuardMeleeAttackGoal;
import com.xeenaa.villagermanager.ai.GuardPatrolGoal;
import com.xeenaa.villagermanager.ai.GuardRangedAttackGoal;
import com.xeenaa.villagermanager.ai.GuardSpecialAbilities;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.profession.ModProfessions;
import com.xeenaa.villagermanager.data.GuardDataManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add Guard AI behaviors to villagers with the Guard profession.
 */
@Mixin(VillagerEntity.class)
public abstract class VillagerAIMixin extends MerchantEntity implements com.xeenaa.villagermanager.ai.GuardAIAccessor {
    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    public abstract void setVillagerData(VillagerData villagerData);

    @Unique
    private boolean guardGoalsInitialized = false;

    @Unique
    private GuardSpecialAbilities guardAbilities = null;

    @Unique
    private int outOfCombatTicks = 0;

    @Unique
    private static final int REGENERATION_START_DELAY = 100; // 5 seconds out of combat

    @Unique
    private static final int REGENERATION_TICK_INTERVAL = 20; // Heal every 1 second

    protected VillagerAIMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Initialize guard-specific goals when profession changes to Guard.
     * Sets up AI goals based on specialization (melee/ranged) and initializes special abilities.
     */
    @Inject(method = "setVillagerData", at = @At("TAIL"))
    private void onProfessionChange(VillagerData villagerData, CallbackInfo ci) {
        VillagerEntity self = (VillagerEntity) (Object) this;

        // Only modify AI goals on the server side
        if (!this.getWorld().isClient()) {
            if (villagerData.getProfession() == ModProfessions.GUARD && !guardGoalsInitialized) {
                System.out.println("GUARD AI PROFESSION: Profession changed to Guard for " + self.getUuid());
                initializeGuardGoals();
                guardGoalsInitialized = true;

                // Initialize special abilities system
                guardAbilities = GuardSpecialAbilities.get(self);

                // Apply rank-based attribute modifications
                applyRankBasedAttributes();

                System.out.println("GUARD AI PROFESSION: Initialization complete for " + self.getUuid());
            } else if (villagerData.getProfession() != ModProfessions.GUARD && guardGoalsInitialized) {
                // Remove guard goals if profession changes away from Guard
                removeGuardGoals();
                guardGoalsInitialized = false;

                // Clean up special abilities
                if (guardAbilities != null) {
                    GuardSpecialAbilities.remove(this.getUuid());
                    guardAbilities = null;
                }

                // Reset attributes to normal
                resetVillagerAttributes();
            }
        }
    }

    @Unique
    private void initializeGuardGoals() {
        VillagerEntity self = (VillagerEntity) (Object) this;
        System.out.println("GUARD AI: Initializing guard goals for " + self.getUuid());

        // Count flee goals before removal
        long fleeGoalCount = this.goalSelector.getGoals().stream()
            .filter(goal -> goal.getGoal() instanceof FleeEntityGoal)
            .count();
        System.out.println("GUARD AI: Found " + fleeGoalCount + " flee goals to remove");

        // Remove flee from zombies goal for guards
        this.goalSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof FleeEntityGoal
        );
        System.out.println("GUARD AI: Removed flee goals");

        // Determine specialization and add appropriate combat goals
        GuardData guardData = GuardDataManager.get(self.getWorld()).getGuardData(self.getUuid());
        boolean isRangedSpecialization = false;
        int tier = 0;

        if (guardData != null) {
            GuardRankData rankData = guardData.getRankData();
            tier = rankData.getCurrentTier();
            String pathId = rankData.getChosenPath() != null ?
                rankData.getChosenPath().getId() : rankData.getCurrentRank().getPath().getId();
            // Check for ranged path
            isRangedSpecialization = pathId.equals("ranged");
            System.out.println("GUARD AI: Guard has data - Tier: " + tier + ", Path: " + pathId + ", Is Marksman (Ranged): " + isRangedSpecialization);
        } else {
            System.out.println("GUARD AI: No guard data found for " + self.getUuid() + " - using defaults");
        }

        // Auto-equip weapons based on specialization
        equipGuardWeapon(self, isRangedSpecialization, tier);

        // Priority 0: Target and attack enemies (with proper cooldowns)
        this.goalSelector.add(0, new com.xeenaa.villagermanager.ai.GuardDirectAttackGoal(self));
        System.out.println("GUARD AI: Added GuardDirectAttackGoal");

        // Priority 1: High priority - defend villagers from threats
        this.goalSelector.add(1, new GuardDefendVillageGoal(self));
        System.out.println("GUARD AI: Added GuardDefendVillageGoal");

        // Priority 2: High priority - retreat when health is low
        this.goalSelector.add(2, new com.xeenaa.villagermanager.ai.GuardRetreatGoal(self));
        System.out.println("GUARD AI: Added GuardRetreatGoal");

        // NOTE: GuardMeleeAttackGoal and GuardRangedAttackGoal removed - all combat handled by GuardDirectAttackGoal
        // GuardDirectAttackGoal now includes Tier 4 special abilities (Knight Knockback, Sharpshooter Double Shot)

        // Priority 5: Medium-low priority - follow villagers for protection
        this.goalSelector.add(5, new GuardFollowVillagerGoal(self));
        System.out.println("GUARD AI: Added GuardFollowVillagerGoal");

        // Priority 7: Low priority - patrol when no other tasks
        this.goalSelector.add(7, new GuardPatrolGoal(self));
        System.out.println("GUARD AI: Added GuardPatrolGoal");

        // Priority 8: STAND mode - prevent all movement (lowest priority so FOLLOW/PATROL can override)
        this.goalSelector.add(8, new com.xeenaa.villagermanager.ai.GuardStandGoal(self));
        System.out.println("GUARD AI: Added GuardStandGoal");

        System.out.println("GUARD AI: Initialization complete for " + self.getUuid());
    }

    @Unique
    private void removeGuardGoals() {
        // Remove guard-specific goals
        this.goalSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof GuardDefendVillageGoal ||
            goal.getGoal() instanceof GuardMeleeAttackGoal ||
            goal.getGoal() instanceof GuardRangedAttackGoal ||
            goal.getGoal() instanceof GuardFollowVillagerGoal ||
            goal.getGoal() instanceof GuardPatrolGoal ||
            goal.getGoal() instanceof com.xeenaa.villagermanager.ai.GuardRetreatGoal ||
            goal.getGoal() instanceof com.xeenaa.villagermanager.ai.GuardStandGoal
        );

        this.targetSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof GuardAttackGoal
        );

        // Re-add normal villager flee behavior
        VillagerEntity self = (VillagerEntity) (Object) this;
        this.goalSelector.add(1, new FleeEntityGoal<>(self, ZombieEntity.class, 8.0F, 0.5, 0.5));
    }

    /**
     * Applies rank-based attribute modifications using RankStats from the rank system.
     * This ensures combat stats scale consistently with rank progression.
     */
    @Unique
    private void applyRankBasedAttributes() {
        VillagerEntity self = (VillagerEntity) (Object) this;
        GuardData guardData = GuardDataManager.get(self.getWorld()).getGuardData(self.getUuid());

        if (guardData == null) {
            // Apply default guard attributes
            setDefaultGuardAttributes();
            return;
        }

        GuardRankData rankData = guardData.getRankData();
        com.xeenaa.villagermanager.data.rank.RankStats stats = rankData.getCurrentRank().getStats();

        // Apply health from RankStats
        EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(stats.getMaxHealth());

            // Heal to max health on rank change to prevent death from stat changes
            float currentHealth = this.getHealth();
            float maxHealth = (float) stats.getMaxHealth();
            if (currentHealth > maxHealth) {
                this.setHealth(maxHealth);
            } else {
                // Scale current health proportionally if gaining health
                float healthRatio = currentHealth / this.getMaxHealth();
                this.setHealth(maxHealth * healthRatio);
            }
        }

        // Apply movement speed from RankStats
        EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(stats.getMovementSpeed());
        }

        // Apply attack damage from RankStats
        EntityAttributeInstance attackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(stats.getAttackDamage());
        }

        // Apply knockback resistance from RankStats
        EntityAttributeInstance knockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            knockbackAttribute.setBaseValue(stats.getKnockbackResistance());
        }

        // Apply armor value from RankStats
        EntityAttributeInstance armorAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armorAttribute != null) {
            armorAttribute.setBaseValue(stats.getArmorValue());
        }

        // Apply attack speed from RankStats
        EntityAttributeInstance attackSpeedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttribute != null) {
            attackSpeedAttribute.setBaseValue(stats.getAttackSpeed());
        }

        System.out.println("GUARD RANK: Applied attributes for " + self.getUuid() +
                         " - HP: " + stats.getMaxHealth() +
                         ", DMG: " + stats.getAttackDamage() +
                         ", SPD: " + stats.getMovementSpeed() +
                         ", Armor: " + stats.getArmorValue());
    }

    /**
     * Sets default guard attributes for new guards
     */
    @Unique
    private void setDefaultGuardAttributes() {
        EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(20.0);
            this.setHealth(20.0f);
        }

        EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(0.5);
        }

        EntityAttributeInstance attackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(1.0);
        }
    }

    /**
     * Resets villager attributes to normal values
     */
    @Unique
    private void resetVillagerAttributes() {
        EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(20.0);
        }

        EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(0.5);
        }

        EntityAttributeInstance attackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(1.0);
        }
    }

    /**
     * Skip villager Brain AI for guards, use GoalSelector instead.
     * Cancels VillagerEntity.mobTick() but still needs MobEntity.mobTick() for movement.
     */
    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void guardMobTick(CallbackInfo ci) {
        VillagerEntity self = (VillagerEntity) (Object) this;

        // Only for guards on server side
        if (!this.getWorld().isClient() &&
            this.getVillagerData().getProfession() == ModProfessions.GUARD &&
            guardGoalsInitialized) {

            // Debug: Log goal selector tick every 100 ticks (5 seconds)
            if (self.age % 100 == 0) {
                System.out.println("GUARD MOB TICK: Ticking goal selector for guard " + self.getUuid());
                System.out.println("GUARD MOB TICK: Goal count: " + this.goalSelector.getGoals().size());
                // Print each goal and its status
                this.goalSelector.getGoals().forEach(goal -> {
                    System.out.println("GUARD MOB TICK: - " + goal.getGoal().getClass().getSimpleName() +
                        " (priority: " + goal.getPriority() + ", running: " + goal.isRunning() + ")");
                });
            }

            // Call parent MobEntity.mobTick() for basic mob functionality (movement, AI tick)
            // This skips VillagerEntity's Brain-based logic
            super.mobTick();

            // Manually tick goal selector (replaces Brain AI)
            this.getWorld().getProfiler().push("guardGoalSelector");
            this.goalSelector.tick();
            this.getWorld().getProfiler().pop();

            this.getWorld().getProfiler().push("guardTargetSelector");
            this.targetSelector.tick();
            this.getWorld().getProfiler().pop();

            // Cancel VillagerEntity.mobTick() (Brain AI)
            ci.cancel();
        }
    }

    /**
     * Tick special abilities system for guards and initialize goals on load
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void tickGuardAbilities(CallbackInfo ci) {
        VillagerEntity self = (VillagerEntity) (Object) this;

        // Initialize guard goals on load if not already initialized (SERVER ONLY)
        if (!this.getWorld().isClient() &&
            this.getVillagerData().getProfession() == ModProfessions.GUARD &&
            !guardGoalsInitialized) {

            System.out.println("GUARD AI TICK: Attempting to initialize guard goals for " + self.getUuid());
            System.out.println("GUARD AI TICK: World is client: " + this.getWorld().isClient());
            System.out.println("GUARD AI TICK: Profession is Guard: " + (this.getVillagerData().getProfession() == ModProfessions.GUARD));
            System.out.println("GUARD AI TICK: Goals initialized: " + guardGoalsInitialized);

            initializeGuardGoals();
            guardGoalsInitialized = true;

            // Initialize special abilities system
            guardAbilities = GuardSpecialAbilities.get(self);

            // Apply rank-based attribute modifications
            applyRankBasedAttributes();

            // Remove brain panic/flee activities for guards
            removeBrainFleeActivities();

            System.out.println("GUARD AI TICK: Initialization complete for " + self.getUuid());
        }

        // Continuously remove flee goals that villagers try to add (GUARDS ONLY, SERVER ONLY)
        if (!this.getWorld().isClient() &&
            this.getVillagerData().getProfession() == ModProfessions.GUARD &&
            guardGoalsInitialized) {

            // Remove any flee goals that vanilla behavior might have added
            boolean removedFlee = this.goalSelector.getGoals().removeIf(goal ->
                goal.getGoal() instanceof FleeEntityGoal
            );

            if (removedFlee) {
                System.out.println("GUARD AI: Removed dynamically-added flee goal from " + self.getUuid());
            }

            // Also continuously clear brain panic activities
            clearBrainPanic();

            // Re-equip weapons if missing (every 100 ticks = 5 seconds - less frequent to avoid flicker)
            if (self.age % 100 == 0) {
                ItemStack mainHand = self.getEquippedStack(EquipmentSlot.MAINHAND);

                // Only re-equip if mainhand is completely empty
                if (mainHand.isEmpty()) {
                    GuardData guardData = GuardDataManager.get(self.getWorld()).getGuardData(self.getUuid());
                    if (guardData != null) {
                        GuardRankData rankData = guardData.getRankData();
                        int tier = rankData.getCurrentTier();
                        String pathId = rankData.getChosenPath() != null ?
                            rankData.getChosenPath().getId() : rankData.getCurrentRank().getPath().getId();
                        boolean isRanged = pathId.equals("ranged");

                        equipGuardWeapon(self, isRanged, tier);
                        System.out.println("GUARD AI: Re-equipped weapon for " + self.getUuid());
                    }
                }
            }
        }

        // Tick special abilities if guard is active
        if (guardAbilities != null && this.getVillagerData().getProfession() == ModProfessions.GUARD) {
            guardAbilities.tick();

            // Handle regeneration and passive effects for guards
            handleGuardPassiveEffects(self);
        }
    }

    /**
     * Handles passive effects for guards including regeneration and damage resistance
     */
    @Unique
    private void handleGuardPassiveEffects(VillagerEntity guard) {
        // Skip on client side
        if (guard.getWorld().isClient()) {
            return;
        }

        GuardData guardData = GuardDataManager.get(guard.getWorld()).getGuardData(guard.getUuid());
        if (guardData == null) {
            return;
        }

        GuardRankData rankData = guardData.getRankData();
        int tier = rankData.getCurrentTier();
        boolean hasTarget = guard.getTarget() != null && guard.getTarget().isAlive();

        // Track combat status
        if (hasTarget) {
            outOfCombatTicks = 0;
        } else {
            outOfCombatTicks++;
        }

        // Regeneration for Tier 3+ guards when out of combat
        if (tier >= 3 && outOfCombatTicks >= REGENERATION_START_DELAY) {
            if (outOfCombatTicks % REGENERATION_TICK_INTERVAL == 0) {
                float currentHealth = guard.getHealth();
                float maxHealth = guard.getMaxHealth();

                if (currentHealth < maxHealth) {
                    // Regeneration rate scales with tier
                    // Tier 3: 0.5 HP/sec | Tier 4: 1.0 HP/sec
                    float healAmount = tier >= 4 ? 1.0f : 0.5f;
                    guard.heal(healAmount);
                }
            }
        }

        // Damage resistance for top-tier guards (Knight/Sharpshooter)
        if (tier >= 4) {
            // Apply permanent resistance effect for max-tier guards
            net.minecraft.entity.effect.StatusEffectInstance currentResistance =
                guard.getStatusEffect(net.minecraft.entity.effect.StatusEffects.RESISTANCE);

            // Only refresh if not present or about to expire
            if (currentResistance == null || currentResistance.getDuration() < 20) {
                // Resistance I (20% damage reduction) for 6 seconds, refreshed continuously
                guard.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.RESISTANCE,
                    120, // 6 seconds
                    0,   // Resistance I
                    true,  // ambient
                    false, // show particles
                    false  // show icon
                ));
            }
        }
    }

    /**
     * Removes brain-based flee, panic, and sleep activities for guards
     */
    @Unique
    private void removeBrainFleeActivities() {
        VillagerEntity self = (VillagerEntity) (Object) this;
        net.minecraft.entity.ai.brain.Brain<?> brain = self.getBrain();

        // Remove panic and hide from hostile activities
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HIDING_PLACE);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HURT_BY);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HURT_BY_ENTITY);

        // Remove sleep-related memories to prevent guards from sleeping
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HOME);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.LAST_SLEPT);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.LAST_WOKEN);

        System.out.println("GUARD AI: Cleared brain flee/panic/sleep memories for " + self.getUuid());
    }

    /**
     * Continuously clears brain panic and sleep state for guards
     */
    @Unique
    private void clearBrainPanic() {
        VillagerEntity self = (VillagerEntity) (Object) this;
        net.minecraft.entity.ai.brain.Brain<?> brain = self.getBrain();

        // Continuously clear panic-related memories to prevent fleeing
        if (brain.hasMemoryModule(net.minecraft.entity.ai.brain.MemoryModuleType.HIDING_PLACE)) {
            brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HIDING_PLACE);
        }

        // Continuously clear sleep-related memories to prevent guards from trying to sleep
        if (brain.hasMemoryModule(net.minecraft.entity.ai.brain.MemoryModuleType.HOME)) {
            brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HOME);
        }
    }

    /**
     * Auto-equips appropriate weapons for guards based on specialization and tier
     */
    @Unique
    private void equipGuardWeapon(VillagerEntity guard, boolean isRangedSpecialization, int tier) {
        System.out.println("GUARD AI: Equipping weapon for guard " + guard.getUuid() +
                           ", ranged=" + isRangedSpecialization + ", tier=" + tier);

        ItemStack currentMainHand = guard.getEquippedStack(EquipmentSlot.MAINHAND);
        System.out.println("GUARD AI: Current mainhand item: " + currentMainHand.getItem());

        if (isRangedSpecialization) {
            // Ranged guards need bows and arrows
            if (!(currentMainHand.getItem() instanceof BowItem)) {
                System.out.println("GUARD AI: Equipping bow and arrows");
                ItemStack bow = new ItemStack(Items.BOW);
                // Make weapon unbreakable so guards don't lose it
                bow.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
                guard.equipStack(EquipmentSlot.MAINHAND, bow);
                // Set equipment drop chance to 0 so villagers keep weapons
                guard.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);

                // Give arrows in offhand
                ItemStack arrows = new ItemStack(Items.ARROW, 64);
                guard.equipStack(EquipmentSlot.OFFHAND, arrows);
                guard.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0f);
                System.out.println("GUARD AI: Bow equipped successfully");
            } else {
                System.out.println("GUARD AI: Guard already has bow equipped");
            }
        } else {
            // Melee guards need swords based on tier
            if (!(currentMainHand.getItem() instanceof SwordItem)) {
                ItemStack sword = getSwordForTier(tier);
                System.out.println("GUARD AI: Equipping " + sword.getItem() + " for tier " + tier);
                // Make weapon unbreakable
                sword.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
                guard.equipStack(EquipmentSlot.MAINHAND, sword);
                // Set equipment drop chance to 0 so villagers keep weapons
                guard.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
                System.out.println("GUARD AI: Sword equipped successfully");
            } else {
                System.out.println("GUARD AI: Guard already has sword equipped");
            }
        }
    }

    /**
     * Gets the appropriate sword item based on guard tier
     */
    @Unique
    private ItemStack getSwordForTier(int tier) {
        return switch (tier) {
            case 0, 1 -> new ItemStack(Items.IRON_SWORD);      // Recruit, Tier 1
            case 2 -> new ItemStack(Items.DIAMOND_SWORD);      // Tier 2
            case 3 -> new ItemStack(Items.DIAMOND_SWORD);      // Tier 3
            case 4 -> new ItemStack(Items.NETHERITE_SWORD);    // Tier 4 (max rank)
            default -> new ItemStack(Items.IRON_SWORD);
        };
    }

    /**
     * Public method to re-initialize combat AI goals when rank/specialization changes.
     * Can be called from ServerPacketHandler after rank purchase.
     */
    public void xeenaa$reinitializeCombatGoals(boolean isRangedSpecialization) {
        VillagerEntity self = (VillagerEntity) (Object) this;
        System.out.println("GUARD AI: Re-initializing combat goals for guard " + self.getUuid() +
            ", isRanged=" + isRangedSpecialization);

        // Remove existing combat goals
        this.goalSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof GuardMeleeAttackGoal ||
            goal.getGoal() instanceof GuardRangedAttackGoal
        );

        // Add appropriate combat goal based on specialization
        if (isRangedSpecialization) {
            // RANGED: Add ranged attack goal for marksman path
            this.goalSelector.add(3, new GuardRangedAttackGoal(self, 1.0, 20, 15.0f));
            System.out.println("GUARD AI: Added GuardRangedAttackGoal (Marksman specialization)");
        } else {
            // MELEE: Add melee attack goal for man-at-arms path
            this.goalSelector.add(3, new GuardMeleeAttackGoal(self, 1.0, true));
            System.out.println("GUARD AI: Added GuardMeleeAttackGoal (Melee specialization)");
        }
    }

}