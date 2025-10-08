# QUICK REFERENCE: Guard Zombification Attribute Preservation

**Question:** Do guard villager attributes persist through zombification?

**Answer:** NO - attributes are RESET to zombie villager defaults.

---

## The Problem

```
Guard Villager (Tier 4)         Zombification          Zombie Villager
- 40 HP                         ═══════════>           - 20 HP (LOST!)
- 10 Attack Damage                                     - 3 Attack Damage (LOST!)
- 0.56 Movement Speed                                  - 0.23 Movement Speed (LOST!)
- 10 Armor                                             - 2 Armor (LOST!)
```

**Why It Happens:**
- Conversion creates NEW entity instance
- New entity gets default zombie villager attributes
- Custom attribute base values are NOT copied automatically

**What IS Preserved:**
- NBT data (guard rank, specialization, behavior config)
- Villager profession data
- Equipment
- Custom name

**What Is NOT Preserved:**
- Entity attribute base values (HP, damage, speed, armor, etc.)

---

## The Solution

**Use Fabric API's `ServerLivingEntityEvents.MOB_CONVERSION` event:**

```java
ServerLivingEntityEvents.MOB_CONVERSION.register((original, converted, keepEquipment) -> {
    if (original instanceof VillagerEntity villager && converted instanceof ZombieVillagerEntity zombie) {
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Copy attributes from guard to zombie
            copyAttribute(villager, zombie, EntityAttributes.GENERIC_MAX_HEALTH);
            copyAttribute(villager, zombie, EntityAttributes.GENERIC_ATTACK_DAMAGE);
            // ... copy all attributes
        }
    }
});
```

---

## Implementation Required

1. **Create mixin:** `VillagerZombificationMixin`
2. **Register event:** MOB_CONVERSION handler
3. **Copy attributes:** All 6 combat attributes (HP, damage, speed, armor, knockback, attack speed)
4. **Handle curing:** Zombie → villager restoration (mostly automatic via existing system)

**File to create:** `src/main/java/com/xeenaa/villagermanager/mixin/VillagerZombificationMixin.java`

---

## Testing

```bash
# Verify zombie has guard attributes
/attribute @e[type=zombie_villager,limit=1] minecraft:generic.max_health get

# Check NBT preservation
/data get entity @e[type=zombie_villager,limit=1] XeenaaGuardData
```

---

## Full Details

See: `.claude/research/guard-zombification-attribute-preservation.md`
