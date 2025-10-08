# Quick Reference: Bed Claiming Methods

## The Answer You Need

### Methods to Intercept in VillagerSleepMixin

```java
// 1. PRIMARY - Prevents bed claiming
@Inject(method = "wantsToSleep", at = @At("HEAD"), cancellable = true)
private void guardsDontWantToSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}

// 2. BACKUP - Defense in depth
@Inject(method = "canSleep", at = @At("HEAD"), cancellable = true)
private void guardsCannotSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}
```

### Memory to Clear in ServerPacketHandler

```java
// Add import
import net.minecraft.entity.ai.brain.MemoryModuleType;

// In changeProfession() method, after reinitializeBrain():
if (profession == ModProfessions.GUARD) {
    villager.getBrain().forget(MemoryModuleType.HOME);
}
```

---

## Brain Memory Modules (Reference)

```java
// Key memory types for villagers
MemoryModuleType.HOME          // GlobalPos - Bed location
MemoryModuleType.JOB_SITE      // GlobalPos - Workstation location
MemoryModuleType.MEETING_POINT // GlobalPos - Bell location
```

---

## Brain Tasks Involved in Sleep (Reference)

```java
// Package: net.minecraft.entity.ai.brain.task

SleepTask              // Manages sleep behavior + bed claiming
FindPointOfInterestTask // Searches for unclaimed POIs (beds/workstations)
JumpInBedTask          // Physical action of entering bed
WakeUpTask             // Handles waking up
WalkHomeTask           // Pathfinding to claimed bed
```

---

## Debug Commands

```bash
# Check if villager has bed claimed
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories."minecraft:home"

# Expected for guards: "No data found"
# Expected for normal villagers with bed: {pos: {...}, dimension: "..."}
```

---

## Files to Modify

1. **VillagerSleepMixin.java**
   - Path: `src/main/java/com/xeenaa/villagermanager/mixin/VillagerSleepMixin.java`
   - Add: `wantsToSleep()` and `canSleep()` injections

2. **ServerPacketHandler.java**
   - Path: `src/main/java/com/xeenaa/villagermanager/network/ServerPacketHandler.java`
   - Add: `MemoryModuleType` import + `forget(MemoryModuleType.HOME)` call

---

## Why Current Mixin Doesn't Work

```
Current Mixin: sleep() and wakeUp()
Problem: These are called AFTER bed claiming

Bed Claiming Flow:
1. wantsToSleep() ← NOT INTERCEPTED ❌
2. canSleep()     ← NOT INTERCEPTED ❌
3. Brain stores bed in HOME memory ← BED CLAIMED ❌
4. sleep(BlockPos) ← INTERCEPTED ✅ (too late, bed already claimed)
```

---

## Complete Research Documents

- **SUMMARY-bed-claiming-fix.md** - Executive summary with implementation checklist
- **guard-bed-fix-implementation.md** - Detailed implementation guide
- **villager-bed-claiming-mechanics.md** - Full technical research on Minecraft bed system

---

## TL;DR

**Add 2 mixin injections + 1 memory clear = Problem solved**

- `wantsToSleep()` → return false for guards
- `canSleep()` → return false for guards
- `forget(MemoryModuleType.HOME)` → when becoming guard

Guards will never claim beds, and any existing claimed beds are released.
