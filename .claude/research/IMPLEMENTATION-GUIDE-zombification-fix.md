# Implementation Guide: Guard Zombification Attribute Preservation

**Target Agent:** minecraft-developer
**Complexity:** Medium
**Files to Create:** 1 new mixin class
**Files to Modify:** 1 mixin configuration file

---

## Overview

Guards lose their enhanced attributes (HP, damage, speed, etc.) when zombified because entity conversion creates a new entity instance with default zombie stats. We need to manually copy attributes using Fabric API's MOB_CONVERSION event.

---

## Step 1: Create VillagerZombificationMixin

**File:** `src/main/java/com/xeenaa/villagermanager/mixin/VillagerZombificationMixin.java`

```java
package com.xeenaa.villagermanager.mixin;

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
 * Preserves guard villager attributes during zombification and curing.
 *
 * <p>When guards are converted to zombie villagers, their enhanced attributes
 * (HP, damage, speed, etc.) are manually transferred to preserve progression.
 * This prevents high-tier guards from becoming standard weak zombies.</p>
 *
 * <p>Uses Fabric API's ServerLivingEntityEvents.MOB_CONVERSION event to intercept
 * entity conversion and copy attribute values.</p>
 */
@Mixin(VillagerEntity.class)
public class VillagerZombificationMixin {

    static {
        // Register event handler when mixin class is loaded
        ServerLivingEntityEvents.MOB_CONVERSION.register((original, converted, keepEquipment) -> {
            handleMobConversion(original, converted, keepEquipment);
        });
    }

    /**
     * Handles all mob conversions, delegating to specific handlers.
     */
    private static void handleMobConversion(LivingEntity original, LivingEntity converted, boolean keepEquipment) {
        // Villager → Zombie Villager (zombification)
        if (original instanceof VillagerEntity villager && converted instanceof ZombieVillagerEntity zombie) {
            handleGuardZombification(villager, zombie);
        }

        // Zombie Villager → Villager (curing)
        if (original instanceof ZombieVillagerEntity zombie && converted instanceof VillagerEntity villager) {
            handleGuardCuring(zombie, villager);
        }
    }

    /**
     * Copies guard attributes from villager to zombie during zombification.
     */
    private static void handleGuardZombification(VillagerEntity villager, ZombieVillagerEntity zombie) {
        VillagerData villagerData = villager.getVillagerData();

        // Only process guards
        if (villagerData.getProfession() != ModProfessions.GUARD) {
            return;
        }

        System.out.println("GUARD ZOMBIFICATION: Preserving attributes for guard " + villager.getUuid());

        // Copy all combat-related attributes
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_MAX_HEALTH);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_MOVEMENT_SPEED);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ARMOR);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_SPEED);

        // Preserve health as a percentage of max health
        // This handles cases where guard was damaged before conversion
        float healthPercentage = villager.getHealth() / villager.getMaxHealth();
        float newHealth = zombie.getMaxHealth() * healthPercentage;
        zombie.setHealth(newHealth);

        // Make zombie persistent so valuable guards don't despawn
        zombie.setPersistent();

        System.out.println("GUARD ZOMBIFICATION: Complete - Health: " + newHealth + "/" + zombie.getMaxHealth() +
                         ", Damage: " + zombie.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) +
                         ", Speed: " + zombie.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
    }

    /**
     * Handles guard curing (zombie → villager).
     * Most attribute restoration is handled automatically by VillagerAIMixin.applyRankBasedAttributes(),
     * but we ensure health percentage is maintained.
     */
    private static void handleGuardCuring(ZombieVillagerEntity zombie, VillagerEntity villager) {
        VillagerData zombieData = zombie.getVillagerData();

        // Only process guards
        if (zombieData.getProfession() != ModProfessions.GUARD) {
            return;
        }

        System.out.println("GUARD CURING: Processing cured guard " + villager.getUuid());

        // VillagerAIMixin.applyRankBasedAttributes() will be called automatically
        // when profession is set, restoring all attributes correctly.

        // Preserve health percentage from zombie to villager
        float healthPercentage = zombie.getHealth() / zombie.getMaxHealth();

        // Schedule health scaling for next tick to ensure attributes are applied first
        // (Using immediate setHealth may use wrong max health if attributes not yet applied)
        scheduleHealthScaling(villager, healthPercentage);
    }

    /**
     * Copies a single attribute from one entity to another.
     */
    private static void copyAttribute(LivingEntity from, LivingEntity to, RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance fromAttr = from.getAttributeInstance(attribute);
        EntityAttributeInstance toAttr = to.getAttributeInstance(attribute);

        if (fromAttr != null && toAttr != null) {
            double baseValue = fromAttr.getBaseValue();
            toAttr.setBaseValue(baseValue);

            System.out.println("  Copied attribute " + attribute.getIdAsString() + " = " + baseValue);
        }
    }

    /**
     * Schedules health scaling for the next tick.
     * This ensures attributes are applied before we scale health percentage.
     */
    private static void scheduleHealthScaling(VillagerEntity villager, float healthPercentage) {
        // Use the world's scheduler to delay health scaling by 1 tick
        villager.getWorld().getServer().execute(() -> {
            float newHealth = villager.getMaxHealth() * healthPercentage;
            villager.setHealth(newHealth);
            System.out.println("GUARD CURING: Scaled health to " + newHealth + "/" + villager.getMaxHealth());
        });
    }
}
```

---

## Step 2: Register Mixin

**File:** `src/main/resources/xeenaa_villager_manager.mixins.json`

Add to the `"mixins"` array:

```json
{
  "required": true,
  "package": "com.xeenaa.villagermanager.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "VillagerAIMixin",
    "VillagerBreedingMixin",
    "VillagerSleepMixin",
    "VillagerZombificationMixin"  <-- ADD THIS LINE
  ],
  "client": [
    "client.GuardVillagerRendererMixin",
    "client.ZombieVillagerRendererMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

---

## Step 3: Testing Checklist

### Test Case 1: Basic Zombification

```
1. Create guard villager (any rank)
2. Spawn zombie
3. Let zombie attack guard
4. Verify conversion happens
5. Check zombie attributes: /attribute @e[type=zombie_villager,limit=1] minecraft:generic.max_health get
6. Verify attributes match original guard's attributes
```

### Test Case 2: Tier 4 Guard

```
1. Create Tier 4 guard (40 HP, 10 damage)
2. Zombify
3. Verify zombie has:
   - Max Health: 40.0
   - Attack Damage: 10.0
   - Movement Speed: 0.56
   - Armor: 10.0
4. Verify zombie texture shows correct rank
```

### Test Case 3: Partial Health

```
1. Create guard
2. Damage to 50% health
3. Zombify
4. Verify zombie has 50% of its max health (not 50% of 20)
```

### Test Case 4: Curing

```
1. Zombify guard
2. Apply weakness potion
3. Feed golden apple
4. Wait for curing
5. Verify villager has guard profession
6. Verify attributes restored to correct rank values
7. Verify health percentage maintained
```

### Test Case 5: Despawning

```
1. Zombify guard
2. Move far away (> 128 blocks)
3. Return
4. Verify zombie is still present (persistence check)
```

---

## Step 4: Debug Commands

### Check Zombie Attributes

```bash
/attribute @e[type=zombie_villager,limit=1] minecraft:generic.max_health get
/attribute @e[type=zombie_villager,limit=1] minecraft:generic.attack_damage get
/attribute @e[type=zombie_villager,limit=1] minecraft:generic.movement_speed get
/attribute @e[type=zombie_villager,limit=1] minecraft:generic.armor get
```

### Check NBT Data

```bash
# Verify guard data preserved
/data get entity @e[type=zombie_villager,limit=1] XeenaaGuardData

# Check profession
/data get entity @e[type=zombie_villager,limit=1] VillagerData.profession

# Check persistence
/data get entity @e[type=zombie_villager,limit=1] PersistenceRequired
```

### Force Conversion (Testing)

```bash
# Summon zombie near villager
/summon zombie ~ ~ ~ {IsBaby:0b}

# Or convert manually (requires datapack/command block)
/execute as @e[type=villager,limit=1] run data modify entity @s ConversionTime set value 0
```

---

## Step 5: Verify Console Output

Expected log messages during zombification:

```
GUARD ZOMBIFICATION: Preserving attributes for guard <uuid>
  Copied attribute minecraft:generic.max_health = 40.0
  Copied attribute minecraft:generic.attack_damage = 10.0
  Copied attribute minecraft:generic.movement_speed = 0.56
  Copied attribute minecraft:generic.armor = 10.0
  Copied attribute minecraft:generic.knockback_resistance = 0.3
  Copied attribute minecraft:generic.attack_speed = 4.0
GUARD ZOMBIFICATION: Complete - Health: 40.0/40.0, Damage: 10.0, Speed: 0.56
```

Expected log messages during curing:

```
GUARD CURING: Processing cured guard <uuid>
GUARD CURING: Scaled health to 20.0/40.0
GUARD AI PROFESSION: Profession changed to Guard for <uuid>
GUARD RANK: Applied attributes for <uuid> - HP: 40.0, DMG: 10.0, SPD: 0.56, Armor: 10.0
```

---

## Implementation Notes

### Why Static Initializer?

```java
static {
    ServerLivingEntityEvents.MOB_CONVERSION.register(...);
}
```

- Event registration must happen once when class is loaded
- Cannot register in instance methods (mixin has no constructor)
- Static block ensures registration happens exactly once
- Alternative: Register in mod initializer (less direct association with mixin)

### Why Not Mixin Into convertTo()?

We could mixin into `MobEntity.convertTo()`, but:
- More invasive (modifies vanilla method)
- Less compatible with other mods
- Fabric API event is designed for this use case
- Event approach is cleaner and more maintainable

### Health Scaling Strategy

We preserve health as a percentage, not absolute value:

```java
float healthPercentage = villager.getHealth() / villager.getMaxHealth();
float newHealth = zombie.getMaxHealth() * healthPercentage;
```

**Why?**
- Guard with 20/40 HP should become zombie with 10/40 HP (50%), not 20/40 HP (overhealed)
- Handles edge cases where guard has more health than zombie max health
- Maintains damage state through conversion

### Persistence Flag

```java
zombie.setPersistent();
```

**Purpose:**
- Prevents valuable zombified guards from despawning
- Without this, zombies may despawn if player moves away
- Guards represent player investment (emeralds, time)
- Should be treated as important entities

---

## Edge Cases Handled

1. **Partial Health:** Health percentage preserved
2. **Health Overflow:** Clamped to max health
3. **Non-Guard Villagers:** Ignored, vanilla behavior preserved
4. **Curing Timing:** Health scaling delayed 1 tick to ensure attributes applied
5. **Despawning:** Zombies made persistent
6. **Missing Attributes:** Null checks prevent crashes

---

## Compatibility Considerations

### Other Mods

- Uses official Fabric API event (high compatibility)
- Only affects guard profession villagers
- Other mods can use same event without conflict
- NBT data preservation unaffected

### Vanilla Behavior

- Non-guard villagers: unchanged
- Other zombification mechanics: unchanged
- Curing mechanics: enhanced but compatible
- Server performance: minimal impact (event fires only on conversion)

---

## Performance Impact

**Minimal:**
- Event fires only during entity conversion (rare)
- 6 attribute copy operations per conversion
- Simple arithmetic for health scaling
- No continuous tick overhead
- No database lookups or file I/O

**Estimated overhead:** < 1ms per conversion event

---

## Rollback Plan

If issues arise:

1. Remove `"VillagerZombificationMixin"` from mixins.json
2. Delete `VillagerZombificationMixin.java`
3. Rebuild mod

**Result:** Guards will lose attributes on zombification (original behavior)

---

## Success Criteria

- [ ] Tier 0 guard zombifies with 20 HP
- [ ] Tier 4 guard zombifies with 40 HP
- [ ] All 6 attributes copied correctly
- [ ] Health percentage maintained through conversion
- [ ] Zombie guards don't despawn
- [ ] Cured guards retain rank progression
- [ ] No console errors or warnings
- [ ] No performance degradation
- [ ] Works on dedicated server
- [ ] Works on single-player

---

## Documentation Updates

After implementation, update:

1. **Changelog:**
   - "Fixed: Guards now retain their attributes when zombified"
   - "Added: Zombified guards are persistent and won't despawn"

2. **README:**
   - Mention zombification behavior in features list
   - Note that guard progression is preserved through zombie conversion

3. **Code Comments:**
   - Ensure VillagerZombificationMixin is well-documented
   - Add javadoc explaining the preservation mechanism

---

## Related Files

**Modified:**
- `src/main/resources/xeenaa_villager_manager.mixins.json` (add mixin registration)

**Created:**
- `src/main/java/com/xeenaa/villagermanager/mixin/VillagerZombificationMixin.java`

**Referenced (no changes needed):**
- `src/main/java/com/xeenaa/villagermanager/mixin/VillagerAIMixin.java` (attribute application)
- `src/client/java/com/xeenaa/villagermanager/mixin/client/ZombieVillagerRendererMixin.java` (NBT preservation proof)
- `src/main/java/com/xeenaa/villagermanager/data/GuardData.java` (NBT serialization)

---

## Conclusion

This implementation ensures guard villagers maintain their combat effectiveness through zombification, preserving player investment in guard progression. The solution uses official Fabric APIs, is minimally invasive, and handles all edge cases appropriately.

**Estimated Implementation Time:** 30-60 minutes
**Testing Time:** 30-45 minutes
**Total Time:** 1-2 hours

---

**Implementation Guide Complete** ✓

For questions or clarifications, refer to:
- Full research document: `guard-zombification-attribute-preservation.md`
- Quick reference: `QUICK-REFERENCE-guard-zombification.md`
