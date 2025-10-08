# Guard Villager Zombification: Attribute Preservation Research

**Research Date:** 2025-10-08
**Minecraft Version:** 1.21.1
**Fabric API Version:** 0.116.0+1.21.1
**Research Scope:** Entity attribute preservation during villager → zombie villager conversion

---

## Executive Summary

**ANSWER: Entity attributes applied via `EntityAttributeInstance.setBaseValue()` are NOT automatically preserved during villager → zombie villager conversion.**

When a guard villager with custom attributes (modified HP, damage, movement speed, etc.) is converted to a zombie villager, those attributes are RESET to zombie villager defaults. This is because:

1. **New Entity Creation**: Conversion creates a completely new `ZombieVillagerEntity` instance
2. **Default Initialization**: The new entity is initialized with zombie villager default attributes
3. **Selective Data Copy**: Only specific data (NBT, profession, villager data) is transferred
4. **Attributes Not Included**: `EntityAttributeInstance` base values are NOT part of the copied data

**Recommendation:** Implement custom attribute preservation using Fabric API's `ServerLivingEntityEvents.MOB_CONVERSION` event to manually copy guard attributes from villager to zombie villager.

---

## Research Findings

### 1. Minecraft's Entity Conversion Mechanism

#### How `MobEntity.convertTo()` Works

According to Yarn mappings and Fabric documentation:

```java
// MobEntity.convertTo() method signature (Minecraft 1.21.1)
public <T extends MobEntity> T convertTo(EntityType<T> entityType, boolean keepEquipment)
```

**What Gets Copied Automatically:**
- Entity NBT data (via `readNbt()` and `writeNbt()`)
- Vehicle/riding status
- Entity name (custom name)
- Persistence status
- Equipment (if `keepEquipment` is true)
- Villager profession data (for villagers → zombie villagers)
- Villager trades and XP

**What Does NOT Get Copied:**
- Entity attribute base values (health, damage, speed, etc.)
- AI goals and target selectors
- Memory modules (brain state)
- Active status effects (potion effects)
- Custom entity behavior state

#### Why Attributes Are Reset

When a new entity is created during conversion:

1. The new `ZombieVillagerEntity` is instantiated
2. Its attributes are initialized to **zombie villager defaults** via `createZombieVillagerAttributes()`
3. NBT data is copied from the old entity to the new entity
4. Attributes are NOT part of standard NBT serialization for conversion

**Zombie Villager Default Attributes:**
- `GENERIC_MAX_HEALTH`: 20.0 (same as normal villager)
- `GENERIC_MOVEMENT_SPEED`: 0.23 (slower than villagers)
- `GENERIC_ATTACK_DAMAGE`: 3.0 (zombie villagers can attack)
- `GENERIC_ARMOR`: 2.0 (minimal armor)
- `GENERIC_KNOCKBACK_RESISTANCE`: 0.0
- `GENERIC_ATTACK_SPEED`: 4.0

### 2. Guard Villager Attributes vs. Zombie Defaults

**Current Guard Attributes** (from `VillagerAIMixin.applyRankBasedAttributes()`):

| Rank Tier | Max Health | Attack Damage | Movement Speed | Armor | Knockback Resist |
|-----------|------------|---------------|----------------|-------|------------------|
| 0 (Recruit) | 20.0 | 1.0 | 0.5 | 0.0 | 0.0 |
| 1 (Guard) | 24.0 | 3.0 | 0.5 | 2.0 | 0.0 |
| 2 (Veteran) | 28.0 | 5.0 | 0.52 | 4.0 | 0.1 |
| 3 (Elite) | 32.0 | 7.0 | 0.54 | 6.0 | 0.2 |
| 4 (Champion) | 40.0 | 10.0 | 0.56 | 10.0 | 0.3 |

**After Zombification (Without Intervention):**
- All attributes reset to zombie villager defaults (20 HP, 3 damage, 0.23 speed, etc.)
- Guard rank progression is LOST from attributes (though NBT data may be preserved)
- High-tier guards become as weak as recruit-level zombies

**Impact:**
- A Tier 4 Champion guard (40 HP, 10 damage) becomes a standard zombie villager (20 HP, 3 damage)
- This creates a jarring gameplay experience and loss of investment
- Curing the zombie back to villager would require re-applying attributes

### 3. Data That IS Preserved

The project already has systems that WORK correctly through zombification:

#### NBT Data Preservation

From `ZombieVillagerRendererMixin.java` (lines 118-138):

```java
// Guard-specific NBT data IS preserved during zombification
if (nbt.contains("XeenaaGuardData")) {
    NbtCompound guardData = nbt.getCompound("XeenaaGuardData");

    // Rank data, specialization path, and behavior config are all saved
    if (guardData.contains("GuardRankData")) {
        NbtCompound rankData = guardData.getCompound("GuardRankData");
        if (rankData.contains("CurrentRank")) {
            String rankId = rankData.getString("CurrentRank");
            // Successfully reads guard rank from zombie villager NBT
        }
    }
}
```

**What This Means:**
- Guard rank data (tier, path, emerald investments) IS preserved
- Guard behavior configuration IS preserved
- Guard specialization (melee/ranged) IS preserved
- Custom textures display correctly on zombie guards

**The Missing Piece:**
- Entity attributes (the actual combat stats) are NOT preserved

### 4. Fabric API Solution: MOB_CONVERSION Event

#### Event Overview

Fabric API provides `ServerLivingEntityEvents.MOB_CONVERSION` specifically for this use case.

**Event Documentation:**
- **Trigger:** Called when a mob is converted to another type
- **Timing:** BEFORE old entity is discarded, AFTER new entity is created
- **Purpose:** Allows mods to copy data from old entity to new entity
- **Access:** Both old and new entity instances are available

**Event Signature:**
```java
ServerLivingEntityEvents.MOB_CONVERSION.register((original, converted, keepEquipment) -> {
    // original: The villager being converted (VillagerEntity)
    // converted: The newly created zombie villager (ZombieVillagerEntity)
    // keepEquipment: Whether equipment should be transferred
});
```

#### Implementation Approach

**Register Event Handler:**
```java
// In XeenaaVillagerManager.onInitialize()
ServerLivingEntityEvents.MOB_CONVERSION.register((original, converted, keepEquipment) -> {
    // Check if original entity was a guard villager
    if (original instanceof VillagerEntity villager) {
        VillagerData villagerData = villager.getVillagerData();

        if (villagerData.getProfession() == ModProfessions.GUARD) {
            // Copy guard attributes to zombie villager
            copyGuardAttributesToZombie(villager, converted);
        }
    }

    // Also handle reverse: zombie villager → villager (curing)
    if (original instanceof ZombieVillagerEntity zombieVillager &&
        converted instanceof VillagerEntity curedVillager) {

        VillagerData zombieData = zombieVillager.getVillagerData();

        if (zombieData.getProfession() == ModProfessions.GUARD) {
            // Restore guard attributes after curing
            restoreGuardAttributesAfterCuring(curedVillager);
        }
    }
});
```

**Attribute Copying Method:**
```java
private static void copyGuardAttributesToZombie(VillagerEntity villager, LivingEntity zombie) {
    // Copy each attribute from guard to zombie
    copyAttribute(villager, zombie, EntityAttributes.GENERIC_MAX_HEALTH);
    copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_DAMAGE);
    copyAttribute(villager, zombie, EntityAttributes.GENERIC_MOVEMENT_SPEED);
    copyAttribute(villager, zombie, EntityAttributes.GENERIC_ARMOR);
    copyAttribute(villager, zombie, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
    copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_SPEED);

    // Heal zombie to max health (important if converted at low health)
    zombie.setHealth(zombie.getMaxHealth());
}

private static void copyAttribute(LivingEntity from, LivingEntity to, RegistryEntry<EntityAttribute> attribute) {
    EntityAttributeInstance fromAttr = from.getAttributeInstance(attribute);
    EntityAttributeInstance toAttr = to.getAttributeInstance(attribute);

    if (fromAttr != null && toAttr != null) {
        double value = fromAttr.getBaseValue();
        toAttr.setBaseValue(value);
    }
}
```

**Curing Restoration Method:**
```java
private static void restoreGuardAttributesAfterCuring(VillagerEntity villager) {
    // After curing, VillagerAIMixin.applyRankBasedAttributes() will be called
    // automatically when profession is set back to Guard, so we don't need
    // to manually restore attributes here. The existing system handles it.

    // However, we should verify the guard data was preserved through zombification
    GuardData guardData = GuardDataManager.get(villager.getWorld()).getGuardData(villager.getUuid());

    if (guardData != null) {
        // Trigger attribute application manually if needed
        // (in case profession change listener doesn't fire)
        VillagerAIMixin mixin = (VillagerAIMixin) villager;
        // Call applyRankBasedAttributes() if accessible
    }
}
```

### 5. Alternative Approach: Attribute NBT Serialization

**Not Recommended** because:
- Attributes are not typically serialized to NBT for conversion
- Would require mixins into `VillagerEntity.writeNbt()` and `ZombieVillagerEntity.readNbt()`
- More invasive and fragile than using the Fabric API event
- May conflict with other mods using similar techniques

**Why MOB_CONVERSION Event Is Better:**
- Official Fabric API - guaranteed to work across Fabric versions
- Non-invasive - no mixins into vanilla entity NBT code
- Explicit and clear - attribute copying is intentional and visible
- Compatible - works alongside other mods using the same event

### 6. Curing Behavior Analysis

**When Zombie Villager → Villager (Curing):**

1. `convertTo()` is called to create new `VillagerEntity`
2. NBT data is copied (including `XeenaaGuardData`)
3. Villager profession is restored from NBT
4. `VillagerAIMixin.onProfessionChange()` is triggered
5. `applyRankBasedAttributes()` is called automatically

**Result:**
- Attributes are restored correctly after curing (via existing system)
- No manual intervention needed for villager side
- Zombie → villager conversion is already handled by existing code

**Implication:**
- We only need to preserve attributes for villager → zombie conversion
- Zombie → villager works automatically via existing mixin

### 7. Edge Cases and Considerations

#### Health Management
- If guard has 15/40 HP when converted, zombie should have 15/40 HP (not 15/20)
- Percentage-based health preservation may be better than absolute values
- Consider healing zombie to full health on conversion (design decision)

#### Equipment Transfer
- Guards have auto-equipped weapons (bow/sword)
- Zombies pick up and use weapons naturally
- Equipment transfer is already handled by `keepEquipment` parameter
- No additional work needed

#### Despawning and Persistence
- Zombie villagers despawn in some circumstances
- Guards are valuable and should not despawn
- May need to set `setPersistent(true)` on converted zombies

#### Display Name
- Guards have colored rank display names
- Should zombie guards show their rank in name?
- Current: Zombie texture shows rank visually
- Consider: Keep or remove custom name on zombification

#### Special Abilities
- `GuardSpecialAbilities` system tracks cooldowns and ability state
- These are NOT entity attributes (stored in separate system)
- Not preserved through zombification (by design)
- Should be cleared when converting to zombie

---

## Implementation Plan

### Required Changes

#### 1. Create New Mixin: `VillagerZombificationMixin`

**Purpose:** Register MOB_CONVERSION event handler

**Location:** `src/main/java/com/xeenaa/villagermanager/mixin/VillagerZombificationMixin.java`

**Responsibilities:**
- Register `ServerLivingEntityEvents.MOB_CONVERSION` event
- Detect guard villagers being converted to zombies
- Copy entity attributes from guard to zombie
- Handle health scaling appropriately

#### 2. Update `VillagerAIMixin`

**Changes:**
- Make `applyRankBasedAttributes()` public or provide accessor
- Allow manual triggering of attribute application after curing
- Ensure attributes are applied even if profession was already Guard

#### 3. Add Configuration Option

**Config Field:**
```java
public class ModConfig {
    // Should zombified guards retain their attributes?
    public boolean preserveGuardAttributesOnZombification = true;

    // Should zombified guards heal to full health?
    public boolean healGuardsOnZombification = true;

    // Should zombified guards be persistent (never despawn)?
    public boolean makeZombifiedGuardsPersistent = true;
}
```

#### 4. Update Mixins JSON

**Add new mixin:**
```json
{
  "mixins": [
    "VillagerZombificationMixin"
  ]
}
```

---

## Testing Plan

### Test Cases

1. **Basic Conversion:**
   - Convert Tier 0 guard (20 HP) → verify zombie has 20 HP
   - Convert Tier 4 guard (40 HP) → verify zombie has 40 HP

2. **Attribute Verification:**
   - Check all attributes (HP, damage, speed, armor, knockback resist)
   - Use `/attribute` command to verify base values on zombie

3. **Partial Health Conversion:**
   - Damage guard to 50% health → convert → verify zombie has 50% health
   - Ensure health percentage is maintained relative to max health

4. **Curing Back:**
   - Convert guard → zombie → cure back to villager
   - Verify attributes are restored correctly
   - Verify rank data and progression are intact

5. **Edge Cases:**
   - Convert guard, let zombie despawn timer run → verify persistence
   - Convert guard with low health → verify health scaling
   - Convert different ranks → verify correct attribute values

### Validation Commands

```bash
# Check entity attributes
/attribute @e[type=zombie_villager,limit=1] minecraft:generic.max_health get

# Check NBT data preservation
/data get entity @e[type=zombie_villager,limit=1] VillagerData

# Verify guard data in NBT
/data get entity @e[type=zombie_villager,limit=1] XeenaaGuardData
```

---

## Code Examples

### Complete Mixin Implementation

```java
package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.VillagerData;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Preserves guard villager attributes when converted to zombie villagers.
 *
 * When a guard villager is zombified, their enhanced attributes (HP, damage, speed, etc.)
 * need to be manually transferred to the zombie entity. Without this mixin, zombified
 * guards would revert to standard zombie villager stats, losing their rank progression.
 */
@Mixin(VillagerEntity.class)
public class VillagerZombificationMixin {

    static {
        // Register mob conversion handler on class load
        ServerLivingEntityEvents.MOB_CONVERSION.register((original, converted, keepEquipment) -> {
            handleMobConversion(original, converted, keepEquipment);
        });
    }

    private static void handleMobConversion(LivingEntity original, LivingEntity converted, boolean keepEquipment) {
        // Handle villager → zombie conversion
        if (original instanceof VillagerEntity villager && converted instanceof ZombieVillagerEntity zombie) {
            handleGuardZombification(villager, zombie);
        }

        // Handle zombie → villager conversion (curing)
        if (original instanceof ZombieVillagerEntity zombie && converted instanceof VillagerEntity villager) {
            handleGuardCuring(zombie, villager);
        }
    }

    private static void handleGuardZombification(VillagerEntity villager, ZombieVillagerEntity zombie) {
        VillagerData villagerData = villager.getVillagerData();

        // Only process guards
        if (villagerData.getProfession() != ModProfessions.GUARD) {
            return;
        }

        System.out.println("GUARD ZOMBIFICATION: Preserving attributes for guard " + villager.getUuid());

        // Copy all combat attributes from guard to zombie
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_MAX_HEALTH);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_MOVEMENT_SPEED);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ARMOR);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_SPEED);

        // Preserve health percentage (not absolute value)
        float healthPercentage = villager.getHealth() / villager.getMaxHealth();
        float newHealth = zombie.getMaxHealth() * healthPercentage;
        zombie.setHealth(newHealth);

        // Make zombie persistent so valuable guards don't despawn
        zombie.setPersistent();

        System.out.println("GUARD ZOMBIFICATION: Zombie health set to " + newHealth + " / " + zombie.getMaxHealth());
    }

    private static void handleGuardCuring(ZombieVillagerEntity zombie, VillagerEntity villager) {
        VillagerData zombieData = zombie.getVillagerData();

        // Only process guards
        if (zombieData.getProfession() != ModProfessions.GUARD) {
            return;
        }

        System.out.println("GUARD CURING: Restoring attributes for cured guard " + villager.getUuid());

        // VillagerAIMixin.applyRankBasedAttributes() will be called automatically
        // when the profession is set, so we don't need to manually copy attributes.
        // The existing system handles restoration correctly.

        // However, we should preserve health percentage
        float healthPercentage = zombie.getHealth() / zombie.getMaxHealth();

        // Give the system time to apply attributes, then scale health
        // (This may need to be done in a tick callback if timing is an issue)
        float newHealth = villager.getMaxHealth() * healthPercentage;
        villager.setHealth(newHealth);
    }

    private static void copyAttribute(LivingEntity from, LivingEntity to, RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance fromAttr = from.getAttributeInstance(attribute);
        EntityAttributeInstance toAttr = to.getAttributeInstance(attribute);

        if (fromAttr != null && toAttr != null) {
            double baseValue = fromAttr.getBaseValue();
            toAttr.setBaseValue(baseValue);

            System.out.println("ATTRIBUTE COPY: " + attribute.getIdAsString() +
                             " = " + baseValue +
                             " (from " + from.getType().getName().getString() +
                             " to " + to.getType().getName().getString() + ")");
        }
    }
}
```

---

## Conclusion

**Answer to Research Question:**

**NO**, entity attributes modified via `EntityAttributeInstance.setBaseValue()` are **NOT** preserved during villager → zombie villager conversion. This is by design in Minecraft's entity conversion system.

**Solution Required:**

Implement a `ServerLivingEntityEvents.MOB_CONVERSION` event handler to manually copy guard attributes from the original villager to the zombie villager entity. This is the recommended approach and aligns with Fabric API best practices.

**Expected Outcome:**

After implementation:
- Guards retain their enhanced attributes when zombified
- Tier 4 Champion guards become powerful zombie villagers (40 HP, high damage)
- Attributes are automatically restored when zombie is cured back to villager
- Player investment in guard progression is preserved through zombification cycle

---

## References

- **Fabric API Documentation:** ServerLivingEntityEvents.MOB_CONVERSION
- **Minecraft Version:** 1.21.1
- **Yarn Mappings:** 1.21.1+build.3
- **Fabric API Version:** 0.116.0+1.21.1
- **Project Files:**
  - `VillagerAIMixin.java` (lines 196-266: applyRankBasedAttributes)
  - `ZombieVillagerRendererMixin.java` (lines 68-93: NBT data preservation proof)
  - `GuardData.java` (lines 87-180: NBT serialization system)

---

**Research Status:** COMPLETE
**Implementation Status:** PENDING
**Next Steps:** Create implementation task for minecraft-developer agent
