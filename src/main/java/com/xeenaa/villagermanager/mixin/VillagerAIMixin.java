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
public abstract class VillagerAIMixin extends MerchantEntity {
    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    public abstract void setVillagerData(VillagerData villagerData);

    @Unique
    private boolean guardGoalsInitialized = false;

    @Unique
    private GuardSpecialAbilities guardAbilities = null;

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
            isRangedSpecialization = pathId.equals("ranged");
            System.out.println("GUARD AI: Guard has data - Tier: " + tier + ", Path: " + pathId + ", Ranged: " + isRangedSpecialization);
        } else {
            System.out.println("GUARD AI: No guard data found for " + self.getUuid() + " - using defaults");
        }

        // Auto-equip weapons based on specialization
        equipGuardWeapon(self, isRangedSpecialization, tier);

        // Priority 0: Highest priority - direct attack goal (bypasses vanilla targeting)
        this.goalSelector.add(0, new com.xeenaa.villagermanager.ai.GuardDirectAttackGoal(self));
        System.out.println("GUARD AI: Added GuardDirectAttackGoal");

        // Priority 5: Medium-low priority - follow villagers for protection
        this.goalSelector.add(5, new GuardFollowVillagerGoal(self));
        System.out.println("GUARD AI: Added GuardFollowVillagerGoal");

        // Priority 7: Low priority - patrol when no other tasks
        this.goalSelector.add(7, new GuardPatrolGoal(self));
        System.out.println("GUARD AI: Added GuardPatrolGoal");

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
            goal.getGoal() instanceof GuardPatrolGoal
        );

        this.targetSelector.getGoals().removeIf(goal ->
            goal.getGoal() instanceof GuardAttackGoal
        );

        // Re-add normal villager flee behavior
        VillagerEntity self = (VillagerEntity) (Object) this;
        this.goalSelector.add(1, new FleeEntityGoal<>(self, ZombieEntity.class, 8.0F, 0.5, 0.5));
    }

    /**
     * Applies rank-based attribute modifications based on specialization
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
        int tier = rankData.getCurrentTier();
        String pathId = rankData.getChosenPath() != null ?
            rankData.getChosenPath().getId() : rankData.getCurrentRank().getPath().getId();

        // Base guard stats
        double baseHealth = 20.0;
        double baseSpeed = 0.5;
        double baseAttackDamage = 1.0;

        // Apply specialization and tier bonuses
        if (pathId.equals("man_at_arms")) {
            // Tank specialization: Higher health, slower speed
            baseHealth = 25.0 + (tier * 5.0); // 25-45 health
            baseSpeed = 0.45 + (tier * 0.02); // Slightly slower but improves with tier
            baseAttackDamage = 2.0 + (tier * 0.5); // 2.0-4.0 damage
        } else if (pathId.equals("marksman")) {
            // Glass cannon: Lower health, higher speed
            baseHealth = 20.0 + (tier * 2.0); // 20-28 health
            baseSpeed = 0.55 + (tier * 0.03); // Faster movement
            baseAttackDamage = 1.5 + (tier * 0.3); // 1.5-2.7 damage (for melee backup)
        } else {
            // Recruit or unknown: Default stats with minor tier scaling
            baseHealth = 20.0 + (tier * 2.0);
            baseSpeed = 0.5 + (tier * 0.01);
            baseAttackDamage = 1.0 + (tier * 0.2);
        }

        // Apply calculated attributes
        EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(baseHealth);
            this.setHealth((float) baseHealth);
        }

        EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(baseSpeed);
        }

        EntityAttributeInstance attackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(baseAttackDamage);
        }
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
        }
    }

    /**
     * Removes brain-based flee and panic activities for guards
     */
    @Unique
    private void removeBrainFleeActivities() {
        VillagerEntity self = (VillagerEntity) (Object) this;
        net.minecraft.entity.ai.brain.Brain<?> brain = self.getBrain();

        // Remove panic and hide from hostile activities
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HIDING_PLACE);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HURT_BY);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HURT_BY_ENTITY);

        System.out.println("GUARD AI: Cleared brain flee/panic memories for " + self.getUuid());
    }

    /**
     * Continuously clears brain panic state for guards
     */
    @Unique
    private void clearBrainPanic() {
        VillagerEntity self = (VillagerEntity) (Object) this;
        net.minecraft.entity.ai.brain.Brain<?> brain = self.getBrain();

        // Continuously clear panic-related memories to prevent fleeing
        if (brain.hasMemoryModule(net.minecraft.entity.ai.brain.MemoryModuleType.HIDING_PLACE)) {
            brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.HIDING_PLACE);
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

}