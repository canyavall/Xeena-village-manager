# Guard Rank System - Complete Design Specification

## Overview
The Guard Rank System replaces the previous equipment-based system with a progression-based approach where guards advance through military ranks. Each rank costs emeralds and provides increased combat effectiveness, visual upgrades, and specialized abilities based on chosen combat paths.

## Core System Design

### Rank Progression Structure
- **Base State**: All guards start as Recruit (Rank 0)
- **Specialization**: At Recruit, players choose between Melee or Ranged paths
- **Linear Progression**: Each path has 4 additional ranks after Recruit (5 total ranks)
- **Cost Scaling**: Each rank requires payment + previous rank as prerequisite
- **Currency**: Standard Minecraft emeralds only

### Payment System
- **Currency**: Emeralds (standard Minecraft trading currency)
- **Cost Structure**: Progressive scaling: 15 → 20 → 45 → 75 emeralds
- **Prerequisites**: Must own previous rank to upgrade
- **Refunds**: No refunds - permanent progression
- **Total Investment**: 155 emeralds for max rank (15+20+45+75)

## Rank Specifications

### Rank 0: Recruit (Base State)
```
Health: 25 HP (12.5 hearts)
Attack Damage: 4 DMG (Stone Sword equivalent)
Equipment: None (basic villager appearance with sword)
Cost: Free (starting rank)
Special: Can choose specialization path
```

## Melee Path: Man-at-Arms (Tanky Focus)

### Man-at-Arms I
```
Health: 35 HP (+10 from Recruit)
Attack Damage: 6 DMG (+2 from Recruit)
Equipment: Leather Tunic + Iron Sword (cosmetic)
Cost: 15 Emeralds
Special: Unlocks melee specialization path
Path Focus: Increased durability over damage
```

### Man-at-Arms II
```
Health: 50 HP (+15 from previous)
Attack Damage: 8 DMG (+2 from previous)
Equipment: Leather Tunic + Iron Helmet + Iron Sword (cosmetic)
Cost: 20 Emeralds
Special: Enhanced defensive stance
Path Focus: Significant health boost for tanking
```

### Man-at-Arms III
```
Health: 70 HP (+20 from previous)
Attack Damage: 10 DMG (+2 from previous)
Equipment: Iron Chestplate + Iron Helmet + Iron Sword (cosmetic)
Cost: 45 Emeralds
Special: Heavy armor appearance, damage reduction
Path Focus: Major health increase, moderate damage
```

### Knight (Final Melee Rank)
```
Health: 95 HP (+25 from previous)
Attack Damage: 12 DMG (+2 from previous)
Equipment: Full Iron Armor Set + Diamond Sword (cosmetic)
Cost: 75 Emeralds
Special: Knockback Strike - 20% chance to knock back enemies on hit
Path Focus: Maximum tanking potential with utility
```

## Ranged Path: Marksman (Glass Cannon Focus)

### Marksman I
```
Health: 25 HP (same as Recruit)
Ranged Damage: 6 DMG (Bow + Power I equivalent)
Equipment: Bow + Leather Cap (cosmetic)
Cost: 15 Emeralds
Special: Switches from melee to ranged combat
Firing Speed: Slow (2.0 second draw time)
Path Focus: High damage, low survivability
```

### Marksman II
```
Health: 30 HP (+5 from previous)
Ranged Damage: 9 DMG (Bow + Power II equivalent)
Equipment: Bow + Leather Cap + Leather Tunic (cosmetic)
Cost: 20 Emeralds
Special: Improved accuracy and range
Firing Speed: Normal (1.5 second draw time)
Path Focus: Damage increase prioritized over health
```

### Marksman III
```
Health: 35 HP (+5 from previous)
Ranged Damage: 12 DMG (Bow + Power III equivalent)
Equipment: Crossbow + Leather Armor Set (cosmetic)
Cost: 45 Emeralds
Special: Fast reload and improved projectile speed
Firing Speed: Fast (1.0 second draw time)
Path Focus: Significant damage boost, minimal health gain
```

### Sharpshooter (Final Ranged Rank)
```
Health: 45 HP (+10 from previous)
Ranged Damage: 16 DMG (Bow + Power IV equivalent)
Equipment: Crossbow + Leather Armor + Enchanted appearance (cosmetic)
Cost: 75 Emeralds
Special: Piercing Shot - Arrows pierce through first enemy and hit second target
Firing Speed: Very Fast (0.8 second draw time)
Path Focus: Maximum damage output, limited survivability
```

## Combat Balance Design

### Melee vs Ranged Philosophy
- **Melee Path**: Tank-focused with high health pools, moderate damage, close combat effectiveness
- **Ranged Path**: Glass cannon with high damage output, low health, requires tactical positioning

### Damage-to-Health Ratios
- **Melee Max (Knight)**: 95 HP / 12 DMG = 7.9:1 ratio (tanky)
- **Ranged Max (Sharpshooter)**: 45 HP / 16 DMG = 2.8:1 ratio (glass cannon)

### Strategic Implications
- **Melee guards**: Ideal for frontline defense, chokepoints, and absorbing damage
- **Ranged guards**: Best for backline support, tower defense, and high-value target elimination
- **Mixed squads**: Combining both paths creates tactical depth and balanced coverage

## Technical Implementation

### Core Data Structures

#### GuardRank.java
```java
package com.xeenaa.villagermanager.data.rank;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import java.util.List;

/**
 * Immutable data class defining all properties of a rank.
 */
public class GuardRank {
    private final String rankId;
    private final String displayName;
    private final GuardPath path;
    private final int tier;
    private final int emeraldCost;
    private final RankStats stats;
    private final List<ItemStack> cosmeticEquipment;
    private final List<SpecialAbility> abilities;
    private final String description;
    private final Identifier icon;

    // Constructor, getters, equals, hashCode, toString

    public static class Builder {
        // Builder pattern for rank creation
    }
}
```

#### GuardPath.java
```java
package com.xeenaa.villagermanager.data.rank;

/**
 * Enum representing the specialization paths for guards.
 */
public enum GuardPath {
    RECRUIT("recruit", "Recruit", "Basic guard without specialization"),
    MELEE("melee", "Man-at-Arms", "Tanky melee combat specialization"),
    RANGED("ranged", "Marksman", "High-damage ranged combat specialization");

    private final String id;
    private final String displayName;
    private final String description;

    GuardPath(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    // Getters and utility methods
}
```

#### RankStats.java
```java
package com.xeenaa.villagermanager.data.rank;

/**
 * Combat statistics for a guard rank.
 * Immutable value object.
 */
public class RankStats {
    private final int health;
    private final int attackDamage;
    private final float attackSpeed;
    private final int rangedDamage;
    private final float drawSpeed;
    private final int armor;
    private final float movementSpeed;

    public RankStats(int health, int attackDamage, float attackSpeed,
                     int rangedDamage, float drawSpeed, int armor, float movementSpeed) {
        this.health = health;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.rangedDamage = rangedDamage;
        this.drawSpeed = drawSpeed;
        this.armor = armor;
        this.movementSpeed = movementSpeed;
    }

    // Getters and utility methods

    public RankStats withModifications(RankStats modifier) {
        // Utility method to apply stat modifications
    }
}
```

### Special Abilities System

#### SpecialAbility.java
```java
package com.xeenaa.villagermanager.data.rank;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Represents a special ability that can be granted by ranks.
 */
public abstract class SpecialAbility {
    private final String abilityId;
    private final String displayName;
    private final String description;
    private final Identifier icon;
    private final float triggerChance;

    protected SpecialAbility(String abilityId, String displayName,
                           String description, Identifier icon, float triggerChance) {
        this.abilityId = abilityId;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.triggerChance = triggerChance;
    }

    /**
     * Called when the ability should trigger.
     * @param caster The guard with this ability
     * @param target The target entity (can be null for some abilities)
     * @return true if the ability was triggered successfully
     */
    public abstract boolean trigger(LivingEntity caster, LivingEntity target);

    /**
     * Called periodically to handle passive abilities.
     * @param caster The guard with this ability
     */
    public void onTick(LivingEntity caster) {
        // Default: no-op for non-passive abilities
    }

    // Getters
}
```

#### KnockbackAbility.java
```java
package com.xeenaa.villagermanager.data.rank.abilities;

import com.xeenaa.villagermanager.data.rank.SpecialAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class KnockbackAbility extends SpecialAbility {
    private final float knockbackStrength;

    public KnockbackAbility() {
        super("knockback", "Knockback Strike",
              "20% chance to knock back enemies on hit",
              new Identifier("xeenaa_villager_manager", "textures/abilities/knockback.png"),
              0.20f); // 20% chance
        this.knockbackStrength = 1.5f;
    }

    @Override
    public boolean trigger(LivingEntity caster, LivingEntity target) {
        if (target != null && caster.getRandom().nextFloat() < getTriggerChance()) {
            Vec3d direction = target.getPos().subtract(caster.getPos()).normalize();
            target.addVelocity(direction.x * knockbackStrength, 0.2, direction.z * knockbackStrength);
            target.velocityModified = true;
            return true;
        }
        return false;
    }
}
```

#### PiercingShotAbility.java
```java
package com.xeenaa.villagermanager.data.rank.abilities;

import com.xeenaa.villagermanager.data.rank.SpecialAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Identifier;

public class PiercingShotAbility extends SpecialAbility {
    public PiercingShotAbility() {
        super("piercing_shot", "Piercing Shot",
              "Arrows pierce through first enemy and hit second target",
              new Identifier("xeenaa_villager_manager", "textures/abilities/piercing.png"),
              1.0f); // Always active for arrows
    }

    @Override
    public boolean trigger(LivingEntity caster, LivingEntity target) {
        // This ability is handled in arrow entity logic
        return true;
    }

    /**
     * Called when an arrow from this guard hits a target.
     * Modifies arrow to have piercing capability.
     */
    public void onArrowHit(ArrowEntity arrow, LivingEntity target) {
        if (arrow.getPierceLevel() == 0) {
            arrow.setPierceLevel(1); // Allow piercing through one entity
        }
    }
}
```

### Progression Management

#### GuardRankManager.java
```java
package com.xeenaa.villagermanager.data.rank;

import net.minecraft.util.Identifier;
import java.util.*;

/**
 * Singleton manager for guard rank definitions and progression logic.
 */
public class GuardRankManager {
    private static GuardRankManager instance;

    private final Map<String, GuardRank> ranks = new LinkedHashMap<>();
    private final Map<GuardPath, List<GuardRank>> pathRanks = new EnumMap<>(GuardPath.class);
    private final int[] RANK_COSTS = {0, 15, 20, 45, 75}; // Recruit + 4 tiers

    private GuardRankManager() {
        initializeRanks();
    }

    public static GuardRankManager getInstance() {
        if (instance == null) {
            instance = new GuardRankManager();
        }
        return instance;
    }

    private void initializeRanks() {
        // Initialize all ranks as defined in the specification
        registerRank(createRecruitRank());
        registerMeleeRanks();
        registerRangedRanks();
    }

    private void registerRank(GuardRank rank) {
        ranks.put(rank.getRankId(), rank);
        pathRanks.computeIfAbsent(rank.getPath(), k -> new ArrayList<>()).add(rank);
    }

    private GuardRank createRecruitRank() {
        return new GuardRank.Builder()
            .rankId("recruit")
            .displayName("Recruit")
            .path(GuardPath.RECRUIT)
            .tier(0)
            .emeraldCost(0)
            .stats(new RankStats(25, 4, 1.0f, 0, 0.0f, 0, 1.0f))
            .cosmeticEquipment(Collections.emptyList())
            .abilities(Collections.emptyList())
            .description("Basic guard without specialization")
            .icon(new Identifier("xeenaa_villager_manager", "textures/ranks/recruit.png"))
            .build();
    }

    private void registerMeleeRanks() {
        // Man-at-Arms I
        registerRank(new GuardRank.Builder()
            .rankId("man_at_arms_1")
            .displayName("Man-at-Arms I")
            .path(GuardPath.MELEE)
            .tier(1)
            .emeraldCost(15)
            .stats(new RankStats(35, 6, 1.0f, 0, 0.0f, 1, 1.0f))
            .build());

        // Man-at-Arms II
        registerRank(new GuardRank.Builder()
            .rankId("man_at_arms_2")
            .displayName("Man-at-Arms II")
            .path(GuardPath.MELEE)
            .tier(2)
            .emeraldCost(20)
            .stats(new RankStats(50, 8, 1.0f, 0, 0.0f, 2, 1.0f))
            .build());

        // Man-at-Arms III
        registerRank(new GuardRank.Builder()
            .rankId("man_at_arms_3")
            .displayName("Man-at-Arms III")
            .path(GuardPath.MELEE)
            .tier(3)
            .emeraldCost(45)
            .stats(new RankStats(70, 10, 1.0f, 0, 0.0f, 3, 1.0f))
            .build());

        // Knight
        registerRank(new GuardRank.Builder()
            .rankId("knight")
            .displayName("Knight")
            .path(GuardPath.MELEE)
            .tier(4)
            .emeraldCost(75)
            .stats(new RankStats(95, 12, 1.0f, 0, 0.0f, 4, 1.0f))
            .abilities(Arrays.asList(new KnockbackAbility()))
            .build());
    }

    private void registerRangedRanks() {
        // Marksman I
        registerRank(new GuardRank.Builder()
            .rankId("marksman_1")
            .displayName("Marksman I")
            .path(GuardPath.RANGED)
            .tier(1)
            .emeraldCost(15)
            .stats(new RankStats(25, 0, 0.0f, 6, 2.0f, 0, 1.0f))
            .build());

        // Marksman II
        registerRank(new GuardRank.Builder()
            .rankId("marksman_2")
            .displayName("Marksman II")
            .path(GuardPath.RANGED)
            .tier(2)
            .emeraldCost(20)
            .stats(new RankStats(30, 0, 0.0f, 9, 1.5f, 0, 1.0f))
            .build());

        // Marksman III
        registerRank(new GuardRank.Builder()
            .rankId("marksman_3")
            .displayName("Marksman III")
            .path(GuardPath.RANGED)
            .tier(3)
            .emeraldCost(45)
            .stats(new RankStats(35, 0, 0.0f, 12, 1.0f, 0, 1.0f))
            .build());

        // Sharpshooter
        registerRank(new GuardRank.Builder()
            .rankId("sharpshooter")
            .displayName("Sharpshooter")
            .path(GuardPath.RANGED)
            .tier(4)
            .emeraldCost(75)
            .stats(new RankStats(45, 0, 0.0f, 16, 0.8f, 0, 1.0f))
            .abilities(Arrays.asList(new PiercingShotAbility()))
            .build());
    }

    public GuardRank getRank(String rankId) {
        return ranks.get(rankId);
    }

    public List<GuardRank> getRanksForPath(GuardPath path) {
        return Collections.unmodifiableList(pathRanks.getOrDefault(path, Collections.emptyList()));
    }

    public GuardRank getNextRank(GuardRank currentRank) {
        List<GuardRank> pathRanks = getRanksForPath(currentRank.getPath());
        int currentIndex = pathRanks.indexOf(currentRank);
        if (currentIndex >= 0 && currentIndex < pathRanks.size() - 1) {
            return pathRanks.get(currentIndex + 1);
        }
        return null; // Already at max rank
    }

    public boolean canUpgrade(GuardRank currentRank, int availableEmeralds) {
        GuardRank nextRank = getNextRank(currentRank);
        return nextRank != null && availableEmeralds >= nextRank.getEmeraldCost();
    }

    public int[] getRankCosts() {
        return RANK_COSTS.clone();
    }
}
```

#### GuardRankData.java
```java
package com.xeenaa.villagermanager.data.rank;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

/**
 * Per-guard rank data stored in GuardData.
 * Mutable data representing current rank state.
 */
public class GuardRankData {
    private String currentRankId = "recruit";
    private GuardPath chosenPath = GuardPath.RECRUIT;
    private int totalEmeraldsSpent = 0;
    private long lastRankChangeTime = 0;

    public GuardRankData() {}

    public GuardRankData(String rankId, GuardPath path) {
        this.currentRankId = rankId;
        this.chosenPath = path;
    }

    // Getters and setters

    public GuardRank getCurrentRank() {
        return GuardRankManager.getInstance().getRank(currentRankId);
    }

    public boolean canChoosePath() {
        return chosenPath == GuardPath.RECRUIT && "recruit".equals(currentRankId);
    }

    public boolean canUpgrade(int availableEmeralds) {
        return GuardRankManager.getInstance().canUpgrade(getCurrentRank(), availableEmeralds);
    }

    public void upgrade(String newRankId) {
        GuardRank newRank = GuardRankManager.getInstance().getRank(newRankId);
        if (newRank != null) {
            this.currentRankId = newRankId;
            this.chosenPath = newRank.getPath();
            this.totalEmeraldsSpent += newRank.getEmeraldCost();
            this.lastRankChangeTime = System.currentTimeMillis();
        }
    }

    // NBT serialization
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("currentRank", currentRankId);
        nbt.putString("chosenPath", chosenPath.name());
        nbt.putInt("emeraldsSpent", totalEmeraldsSpent);
        nbt.putLong("lastRankChange", lastRankChangeTime);
    }

    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.currentRankId = nbt.getString("currentRank");
        this.chosenPath = GuardPath.valueOf(nbt.getString("chosenPath"));
        this.totalEmeraldsSpent = nbt.getInt("emeraldsSpent");
        this.lastRankChangeTime = nbt.getLong("lastRankChange");
    }
}
```

### Network Communication

#### RankUpgradePacket.java
```java
package com.xeenaa.villagermanager.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

public record RankUpgradePacket(UUID guardId, String newRankId) implements CustomPayload {
    public static final CustomPayload.Id<RankUpgradePacket> ID =
        new CustomPayload.Id<>(new Identifier("xeenaa_villager_manager", "rank_upgrade"));

    public static final PacketCodec<PacketByteBuf, RankUpgradePacket> CODEC =
        PacketCodec.tuple(
            PacketCodecs.UUID, RankUpgradePacket::guardId,
            PacketCodecs.STRING, RankUpgradePacket::newRankId,
            RankUpgradePacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
```

#### PathSelectionPacket.java
```java
package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.rank.GuardPath;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

public record PathSelectionPacket(UUID guardId, GuardPath path) implements CustomPayload {
    public static final CustomPayload.Id<PathSelectionPacket> ID =
        new CustomPayload.Id<>(new Identifier("xeenaa_villager_manager", "path_selection"));

    public static final PacketCodec<PacketByteBuf, PathSelectionPacket> CODEC =
        PacketCodec.tuple(
            PacketCodecs.UUID, PathSelectionPacket::guardId,
            PacketCodecs.fromCodec(GuardPath.CODEC), PathSelectionPacket::path,
            PathSelectionPacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
```

## GUI Requirements

### Rank Tab Interface
- **Replace Equipment Tab**: Remove existing equipment system
- **Current Rank Display**: Show current rank with progress bar
- **Path Selection**: Choose between Melee/Ranged at Recruit level
- **Upgrade Interface**: Display next rank, cost, and benefits
- **Emerald Cost Display**: Clear cost breakdown and payment confirmation
- **Visual Preview**: Show equipment and stat changes for next rank
- **Stat Comparison**: Before/after stats with highlighted improvements

### GUI Layout Structure
```
[Guard Info Header]
[Current Rank: Knight | Health: 95 HP | Damage: 12]
[Progress Bar: Rank 4/4 - Max Rank Achieved]

[Path Selection] (Only for Recruits)
[ Melee Path ]  [ Ranged Path ]
[Tank Focus]    [Damage Focus]

[Upgrade Section] (If not max rank)
[Next Rank: Sharpshooter]
[Cost: 75 Emeralds] [You have: 120 Emeralds]
[Health: 35 → 45 HP] [Damage: 12 → 16]
[Special: Piercing Shot ability]
[Upgrade Button]
```

## Integration Points

### GuardData.java Integration
```java
// Add to existing GuardData class
private GuardRankData rankData = new GuardRankData();

public GuardRankData getRankData() {
    return rankData;
}

public void setRankData(GuardRankData rankData) {
    this.rankData = rankData;
}

// Update NBT methods to include rank data
@Override
public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    NbtCompound rankNbt = new NbtCompound();
    rankData.writeNbt(rankNbt, registryLookup);
    nbt.put("rankData", rankNbt);
}

@Override
public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.readFromNbt(nbt, registryLookup);
    if (nbt.contains("rankData")) {
        rankData.readFromNbt(nbt.getCompound("rankData"), registryLookup);
    }
}
```

### Combat System Integration
- **Health Modification**: Apply rank health bonuses to guard entities
- **Damage Scaling**: Modify attack damage based on current rank
- **Special Abilities**: Trigger rank-specific abilities during combat
- **Visual Equipment**: Render cosmetic equipment based on current rank
- **AI Behavior**: Adjust guard behavior based on melee vs ranged specialization

## Migration Strategy

### From Equipment System
1. **Phase 1**: Implement rank system alongside existing equipment
2. **Phase 2**: Deprecate equipment GUI while maintaining functionality
3. **Phase 3**: Convert all existing guards to Recruit rank
4. **Phase 4**: Remove equipment system and update GUI references
5. **Phase 5**: Clean up obsolete code and data structures

### Data Migration Process
- **Convert Existing Guards**: Set all guards to Recruit rank with no path selected
- **Preserve Guard Data**: Maintain all other guard properties (name, profession, etc.)
- **Clear Equipment Data**: Remove equipment references from save files
- **Update Save Format**: Increment save version for compatibility tracking

## Balance Testing Framework

### Key Metrics to Monitor
- **Emerald Economy**: Track emerald earning vs spending rates
- **Combat Effectiveness**: Measure guard survival and kill rates by rank
- **Player Engagement**: Monitor upgrade frequency and path distribution
- **Cost Scaling**: Evaluate if progression feels rewarding vs grindy

### Testing Scenarios
- **Early Game**: 1-2 guards, limited emeralds (0-50)
- **Mid Game**: 3-5 guards, moderate emeralds (50-200)
- **Late Game**: 6+ guards, abundant emeralds (200+)
- **Path Balance**: Equal viability of melee vs ranged strategies

### Success Criteria
- **Progression Feels Rewarding**: Each upgrade provides meaningful improvement
- **Balanced Paths**: Both melee and ranged are viable strategies
- **Economic Balance**: Emerald costs feel fair for benefits gained
- **Strategic Depth**: Players make meaningful decisions about guard composition

## Future Enhancement Opportunities

### Additional Content
- **Prestige System**: Reset ranks for permanent bonuses
- **Elite Specializations**: Branching paths beyond current max ranks
- **Group Bonuses**: Synergies between different rank combinations
- **Custom Equipment**: Player-crafted cosmetic upgrades

### Advanced Features
- **Achievement System**: Milestones for rank progression
- **Rank-Based AI**: Different behavior patterns by specialization
- **Formation System**: Tactical positioning based on guard types
- **Combat Analytics**: Detailed statistics on guard performance

This comprehensive design provides the foundation for a balanced, engaging, and technically sound guard rank progression system that enhances the core gameplay experience while maintaining clear progression paths and meaningful player choices.