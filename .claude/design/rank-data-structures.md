# Guard Rank System - Data Structures

## Core Data Models

### GuardRank.java
```java
package com.xeenaa.villagermanager.data.rank;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Map;

/**
 * Represents a single rank in the guard progression system.
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

### GuardPath.java
```java
package com.xeenaa.villagermanager.data.rank;

/**
 * Enum representing the specialization paths for guards.
 */
public enum GuardPath {
    RECRUIT("recruit", "Recruit", "Basic guard without specialization"),
    MELEE("melee", "Man-at-Arms", "Melee combat specialization path"),
    RANGED("ranged", "Marksman", "Ranged combat specialization path");

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

### RankStats.java
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

### SpecialAbility.java
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

### Concrete Special Abilities

#### KnockbackAbility.java
```java
package com.xeenaa.villagermanager.data.rank.abilities;

import com.xeenaa.villagermanager.data.rank.SpecialAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class KnockbackAbility extends SpecialAbility {
    private final float knockbackStrength;

    public KnockbackAbility(float knockbackStrength) {
        super("knockback", "Knockback Strike",
              "Small chance to knock back enemies on hit",
              new Identifier("xeenaa_villager_manager", "textures/abilities/knockback.png"),
              0.15f); // 15% chance
        this.knockbackStrength = knockbackStrength;
    }

    @Override
    public boolean trigger(LivingEntity caster, LivingEntity target) {
        if (target != null && caster.getRandom().nextFloat() < getTriggerChance()) {
            Vec3d direction = target.getPos().subtract(caster.getPos()).normalize();
            target.addVelocity(direction.x * knockbackStrength, 0.1, direction.z * knockbackStrength);
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
              "Arrows can pass through one enemy and hit a second target",
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

## Progression Management

### GuardRankManager.java
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

    // Rank creation methods...
}
```

### GuardRankData.java
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

## Integration Points

### GuardData.java (Modified)
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
```

### Network Packets

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

## Configuration System

### rank_definitions.json
```json
{
  "ranks": {
    "recruit": {
      "displayName": "Recruit",
      "path": "RECRUIT",
      "tier": 0,
      "emeraldCost": 0,
      "stats": {
        "health": 25,
        "attackDamage": 4,
        "attackSpeed": 1.0,
        "rangedDamage": 0,
        "drawSpeed": 0.0,
        "armor": 0,
        "movementSpeed": 1.0
      },
      "equipment": [],
      "abilities": [],
      "description": "Basic guard without specialization",
      "icon": "xeenaa_villager_manager:textures/ranks/recruit.png"
    }
    // ... other ranks
  },
  "paths": {
    "melee": {
      "displayName": "Man-at-Arms",
      "description": "Melee combat specialization",
      "ranks": ["recruit", "man_at_arms_1", "man_at_arms_2", "man_at_arms_3", "knight"]
    },
    "ranged": {
      "displayName": "Marksman",
      "description": "Ranged combat specialization",
      "ranks": ["recruit", "marksman_1", "marksman_2", "marksman_3", "sharpshooter"]
    }
  }
}
```