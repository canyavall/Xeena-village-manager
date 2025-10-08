# Villager Bed Claiming Fix - Executive Summary

## Problem Statement
Guard villagers are claiming/reserving beds despite being unable to sleep. This prevents other villagers from using those beds, even though guards remain active 24/7.

## Root Cause
The current `VillagerSleepMixin` only prevents **sleeping** (`sleep()` and `wakeUp()` methods), but bed **claiming** happens earlier in the AI decision chain through brain tasks that check `wantsToSleep()` and `canSleep()`.

## Solution Overview
Add two additional mixin injections to prevent guards from wanting/being able to sleep, and clear HOME memory when converting to guard profession.

---

## Implementation Checklist

### Step 1: Update VillagerSleepMixin
**File**: `C:\Users\canya\Documents\projects\Minecraft\xeena-village-manager\src\main\java\com\xeenaa\villagermanager\mixin\VillagerSleepMixin.java`

Add these two new methods:

```java
/**
 * PRIMARY PREVENTION: Prevents guards from wanting to sleep.
 */
@Inject(method = "wantsToSleep", at = @At("HEAD"), cancellable = true)
private void guardsDontWantToSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}

/**
 * BACKUP PREVENTION: Prevents guards from being able to sleep.
 */
@Inject(method = "canSleep", at = @At("HEAD"), cancellable = true)
private void guardsCannotSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}
```

### Step 2: Update ServerPacketHandler
**File**: `C:\Users\canya\Documents\projects\Minecraft\xeena-village-manager\src\main\java\com\xeenaa\villagermanager\network\ServerPacketHandler.java`

Add import at top of file:
```java
import net.minecraft.entity.ai.brain.MemoryModuleType;
```

Modify the `changeProfession` method (around line 147), add after line 167:

```java
// If the villager is now a guard, create guard data and sync to clients
if (profession == ModProfessions.GUARD) {
    createAndSyncGuardData(villager);
}
```

**Insert this new code BEFORE the above block:**
```java
// If changing FROM guard to another profession, clear HOME memory
if (originalProfession == ModProfessions.GUARD) {
    villager.getBrain().forget(MemoryModuleType.HOME);
    XeenaaVillagerManager.LOGGER.debug("Cleared HOME memory when changing from guard to {}",
        Registries.VILLAGER_PROFESSION.getId(profession));
}

// If changing TO guard profession, clear HOME memory to release any claimed bed
if (profession == ModProfessions.GUARD) {
    villager.getBrain().forget(MemoryModuleType.HOME);
    XeenaaVillagerManager.LOGGER.debug("Cleared HOME memory for new guard {}",
        villager.getUuid());
}
```

**Final code should look like:**
```java
// Set the new profession
villager.setVillagerData(villager.getVillagerData().withProfession(profession));

// Lock profession by setting experience to master level (250 XP)
villager.setExperience(250);
villager.setVillagerData(villager.getVillagerData().withLevel(5));

// Reinitialize brain for normal AI behavior
villager.reinitializeBrain((ServerWorld) villager.getWorld());

// If changing FROM guard to another profession, clear HOME memory
if (originalProfession == ModProfessions.GUARD) {
    villager.getBrain().forget(MemoryModuleType.HOME);
    XeenaaVillagerManager.LOGGER.debug("Cleared HOME memory when changing from guard to {}",
        Registries.VILLAGER_PROFESSION.getId(profession));
}

// If changing TO guard profession, clear HOME memory to release any claimed bed
if (profession == ModProfessions.GUARD) {
    villager.getBrain().forget(MemoryModuleType.HOME);
    XeenaaVillagerManager.LOGGER.debug("Cleared HOME memory for new guard {}",
        villager.getUuid());
}

// If the villager is now a guard, create guard data and sync to clients
if (profession == ModProfessions.GUARD) {
    createAndSyncGuardData(villager);
}
```

---

## How It Works

### Prevention Chain

1. **Brain Task Check (Tick 12000-23999)**
   - `SleepTask` runs during night time
   - Calls `villager.wantsToSleep()` → **We return `false` for guards**
   - Task never proceeds → No bed claiming

2. **Ability Check (If task somehow activates)**
   - Sleep logic checks `villager.canSleep()` → **We return `false` for guards**
   - Cannot physically sleep → No bed interaction

3. **Physical Sleep Prevention (Defense in depth)**
   - If somehow `sleep(BlockPos)` is called → **We cancel it for guards**
   - Already implemented in current mixin

4. **Memory Clearing (Immediate bed release)**
   - When converting TO guard → Clear HOME memory → Bed becomes available
   - When converting FROM guard → Clear HOME memory → Clean state

### Why This Solves the Problem

| Method | Current Mixin | New Mixin | Effect |
|--------|--------------|-----------|--------|
| `wantsToSleep()` | ❌ Not intercepted | ✅ Returns `false` | **Prevents bed claiming** |
| `canSleep()` | ❌ Not intercepted | ✅ Returns `false` | **Prevents sleep ability** |
| `sleep(BlockPos)` | ✅ Canceled | ✅ Canceled | Prevents physical sleep |
| `wakeUp()` | ✅ Canceled | ✅ Canceled | Prevents wake logic |
| **HOME Memory** | ❌ Not cleared | ✅ Cleared on profession change | **Releases claimed beds** |

---

## Expected Behavior After Fix

### Scenario 1: Converting Villager to Guard
1. Villager has claimed bed
2. Player changes profession to Guard
3. **HOME memory cleared** → Bed is immediately released
4. Other villagers can now claim that bed
5. Guard never claims another bed

### Scenario 2: New Guard Near Beds
1. Guard villager spawned/created
2. Beds nearby available
3. Night arrives (tick 12000)
4. Guard's brain runs `SleepTask`
5. Task calls `wantsToSleep()` → Returns `false`
6. Task never activates → **No bed claiming**
7. Other villagers successfully claim beds

### Scenario 3: Converting Guard to Villager
1. Guard villager exists (no bed claimed)
2. Player changes profession to Farmer
3. HOME memory cleared (clean state)
4. Farmer now free to claim bed normally
5. No lingering guard data affecting behavior

---

## Testing Procedure

### Test 1: Existing Bed Release
```
1. Spawn villager
2. Place bed nearby, let villager claim it
3. Verify villager has bed (particles, pathfinding to bed at night)
4. Convert villager to Guard
5. ✅ PASS: Bed particles disappear, other villagers can claim it
6. ❌ FAIL: Bed still shows claimed particles, other villagers can't claim
```

### Test 2: Guards Don't Claim New Beds
```
1. Create guard villager
2. Place 3 beds nearby
3. Spawn 2 normal villagers
4. Wait for night cycle (10-15 minutes)
5. ✅ PASS: 2 normal villagers claim 2 beds, 1 bed remains unclaimed, guard never approaches beds
6. ❌ FAIL: Guard claims a bed, or all beds claimed preventing other villagers
```

### Test 3: Multiple Guards and Villagers
```
1. Create area with 5 beds
2. Spawn 3 guards, 5 normal villagers
3. Wait for night cycle
4. ✅ PASS: All 5 normal villagers claim beds, guards never claim
5. ❌ FAIL: Guards claim some beds, normal villagers can't get beds
```

### Test 4: Memory Verification (Debug)
```bash
# Before conversion (normal villager with bed)
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories."minecraft:home"
# Should show: {pos: {X: 123, Y: 64, Z: 456}, dimension: "minecraft:overworld"}

# After conversion to guard
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories."minecraft:home"
# Should show: "No data found" or empty

# ✅ PASS: HOME memory is cleared
# ❌ FAIL: HOME memory still contains bed location
```

---

## Technical Details

### Method Signatures (Yarn Mappings 1.21.1)

```java
// VillagerEntity class
public boolean wantsToSleep()           // Returns if villager wants to sleep
protected boolean canSleep()             // Returns if villager can sleep
public void sleep(BlockPos pos)         // Physically sleeps at position
public void wakeUp()                     // Wakes up from sleep

// Brain class (accessed via villager.getBrain())
public <U> void forget(MemoryModuleType<U> type)  // Clears specific memory

// MemoryModuleType constants
public static final MemoryModuleType<GlobalPos> HOME  // Bed location memory
```

### AI Task Flow
```
Night Time (tick 12000-23999)
    ↓
SleepTask.shouldRun()
    ↓
villager.wantsToSleep() ──→ FALSE (guards) → Task skipped ✅
    ↓ TRUE (normal villagers)
FindPointOfInterestTask (find unclaimed bed)
    ↓
villager.canSleep() ──→ FALSE (guards) → Cannot sleep ✅
    ↓ TRUE (normal villagers)
Store bed in MemoryModuleType.HOME (BED CLAIMED)
    ↓
villager.sleep(bedPos) ──→ CANCELED (guards) → No sleep ✅
    ↓ SUCCESS (normal villagers)
Physical sleep occurs
```

---

## Files Modified

### 1. VillagerSleepMixin.java
**Location**: `src/main/java/com/xeenaa/villagermanager/mixin/VillagerSleepMixin.java`
- Added `wantsToSleep()` injection
- Added `canSleep()` injection
- Kept existing `sleep()` and `wakeUp()` injections

### 2. ServerPacketHandler.java
**Location**: `src/main/java/com/xeenaa/villagermanager/network/ServerPacketHandler.java`
- Added `MemoryModuleType` import
- Modified `changeProfession()` method to clear HOME memory

---

## Detailed Documentation

For complete technical details, see:
- **`villager-bed-claiming-mechanics.md`** - Full research on Minecraft's bed claiming system
- **`guard-bed-fix-implementation.md`** - Detailed implementation guide with code examples

---

## Summary

**Two simple additions solve the problem:**

1. **Mixin additions**: `wantsToSleep()` and `canSleep()` return `false` for guards
2. **Memory clearing**: Call `getBrain().forget(MemoryModuleType.HOME)` when changing profession

This prevents guards from ever claiming beds while ensuring any previously claimed beds are immediately released for other villagers to use.
