# Guard Bed Claiming Fix - Implementation Guide

## Quick Reference

### Problem
Guards are claiming/reserving beds even though they can't sleep, preventing other villagers from using those beds.

### Root Cause
The current `VillagerSleepMixin` only intercepts `sleep()` and `wakeUp()` methods, which are called AFTER bed claiming. Bed claiming happens through brain AI tasks and memory modules.

---

## Required Method Interceptions

### 1. wantsToSleep() - PRIMARY SOLUTION
**Method Signature** (Yarn mappings):
```java
public boolean wantsToSleep()
```

**Location**: `net.minecraft.entity.passive.VillagerEntity`

**Purpose**: Returns whether the villager wants to sleep. Used by `SleepTask` to determine if sleep behavior should activate.

**Mixin Implementation**:
```java
@Inject(method = "wantsToSleep", at = @At("HEAD"), cancellable = true)
private void guardsDontWantToSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}
```

---

### 2. canSleep() - BACKUP DEFENSE
**Method Signature** (Yarn mappings):
```java
protected boolean canSleep()
```

**Location**: `net.minecraft.entity.LivingEntity` (inherited by VillagerEntity)

**Purpose**: Returns whether the entity can sleep at all.

**Mixin Implementation**:
```java
@Inject(method = "canSleep", at = @At("HEAD"), cancellable = true)
private void guardsCannotSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}
```

**Note**: Since this is a `LivingEntity` method, the mixin must be on `VillagerEntity` specifically to access profession data.

---

### 3. Clear HOME Memory - RELEASE EXISTING BEDS
**Not a mixin - Call during profession change**

**Memory Type**: `MemoryModuleType.HOME`

**Location**: `net.minecraft.entity.ai.brain.MemoryModuleType`

**Implementation** (in profession change handler):
```java
import net.minecraft.entity.ai.brain.MemoryModuleType;

// When changing villager to guard profession
villager.getBrain().forget(MemoryModuleType.HOME);
```

**Purpose**: Releases any bed the villager previously claimed so other villagers can use it.

---

## Updated VillagerSleepMixin

### Complete Implementation
```java
package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to prevent guard villagers from sleeping and claiming beds.
 * Guards are 24/7 defenders and should not require rest.
 *
 * This mixin intercepts sleep-related methods to ensure guards:
 * - Never want to sleep (prevents bed claiming AND sleeping)
 * - Cannot sleep even if forced
 * - Do not claim beds (freeing them for other villagers)
 * - Remain active and alert 24/7
 * - Continue patrol and defense behaviors during night time
 */
@Mixin(VillagerEntity.class)
public class VillagerSleepMixin {

    /**
     * PRIMARY PREVENTION: Prevents guards from wanting to sleep.
     * This is checked by SleepTask before bed claiming/sleeping occurs.
     * Returning false prevents the entire sleep behavior chain.
     *
     * @param cir callback info returnable for setting return value
     */
    @Inject(method = "wantsToSleep", at = @At("HEAD"), cancellable = true)
    private void guardsDontWantToSleep(CallbackInfoReturnable<Boolean> cir) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            XeenaaVillagerManager.LOGGER.debug("Guard {} does not want to sleep (bed claiming prevented)",
                villager.getUuid());
            cir.setReturnValue(false);
        }
    }

    /**
     * BACKUP PREVENTION: Prevents guards from being able to sleep.
     * Defense-in-depth in case wantsToSleep is bypassed.
     *
     * @param cir callback info returnable for setting return value
     */
    @Inject(method = "canSleep", at = @At("HEAD"), cancellable = true)
    private void guardsCannotSleep(CallbackInfoReturnable<Boolean> cir) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            XeenaaVillagerManager.LOGGER.debug("Guard {} cannot sleep (ability check)",
                villager.getUuid());
            cir.setReturnValue(false);
        }
    }

    /**
     * TERTIARY PREVENTION: Prevents guards from physically sleeping.
     * Last line of defense if both wantsToSleep and canSleep are bypassed.
     *
     * @param pos the position of the bed the villager is attempting to sleep in
     * @param ci callback info for canceling the method
     */
    @Inject(method = "sleep", at = @At("HEAD"), cancellable = true)
    private void preventGuardSleep(BlockPos pos, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            XeenaaVillagerManager.LOGGER.debug("Preventing guard {} from sleeping at {}",
                villager.getUuid(), pos);
            ci.cancel();
        }
    }

    /**
     * Prevents guards from waking up (since they never sleep).
     * This handles edge cases where wake up might be called.
     *
     * @param ci callback info for canceling the method
     */
    @Inject(method = "wakeUp", at = @At("HEAD"), cancellable = true)
    private void preventGuardWakeUp(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity)(Object)this;

        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            // Guards never sleep, so they can't wake up
            ci.cancel();
        }
    }
}
```

---

## Profession Change Handler Update

### Where to Add Memory Clearing

Find where profession changes to GUARD happen. Add memory clearing there.

**Example locations to check**:
- `ProfessionManager.setProfession()`
- Network packet handlers for profession change
- GUI confirm buttons for profession selection

### Code to Add
```java
import net.minecraft.entity.ai.brain.MemoryModuleType;

// After setting profession to GUARD
if (newProfession == ModProfessions.GUARD) {
    // Release claimed bed so other villagers can use it
    villager.getBrain().forget(MemoryModuleType.HOME);

    XeenaaVillagerManager.LOGGER.debug(
        "Cleared HOME memory for guard {} (released bed)",
        villager.getUuid()
    );
}
```

---

## Method Details Reference

### wantsToSleep()
- **Returns**: boolean
- **When Called**: Every tick by SleepTask during night (tick 12000-23999)
- **Default Behavior**: Returns true during night time for normal villagers
- **Our Override**: Return false for guards → prevents bed claiming AND sleeping
- **Impact**: SleepTask never activates for guards

### canSleep()
- **Returns**: boolean
- **When Called**: Before sleep() is called, as validation check
- **Default Behavior**: Checks if entity type can sleep
- **Our Override**: Return false for guards → prevents sleeping
- **Impact**: Even if sleep tasks activate, guards cannot physically sleep

### sleep(BlockPos pos)
- **Returns**: void
- **When Called**: When entity enters bed
- **Default Behavior**: Sets entity sleeping state, plays animation
- **Our Override**: Cancel method execution for guards
- **Impact**: Guards never enter sleeping state

### wakeUp()
- **Returns**: void
- **When Called**: When entity should exit sleeping state
- **Default Behavior**: Clears sleeping state, resets position
- **Our Override**: Cancel method execution for guards
- **Impact**: Prevents edge case wake-up calls

---

## Testing Checklist

### Test 1: New Guard Does Not Claim Bed
1. Place beds near villager
2. Change villager to guard profession
3. Wait through night cycle
4. Verify guard does not approach or claim bed
5. Verify other villagers CAN claim the bed

### Test 2: Existing Bed is Released
1. Let villager claim a bed
2. Change villager to guard profession
3. Verify bed is immediately available
4. Another villager should be able to claim it

### Test 3: Guards Stay Active at Night
1. Create guard villager
2. Wait for night time (tick 12000-23999)
3. Verify guard continues patrol/combat behavior
4. Verify guard does not seek beds

### Test 4: Memory Verification
```bash
# Check guard has no HOME memory
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories."minecraft:home"
# Should return: No data found
```

### Test 5: Multiple Guards and Villagers
1. Create area with 3 beds, 2 guards, 3 normal villagers
2. All 3 normal villagers should claim beds
3. Guards should never claim beds
4. No bed shortage for normal villagers

---

## Debug Logging

### Add to fabric.mod.json (if not present)
```json
{
  "custom": {
    "modmenu": {
      "logging": {
        "level": "DEBUG"
      }
    }
  }
}
```

### Expected Log Output
```
[DEBUG] Guard 12345678-1234-1234-1234-123456789abc does not want to sleep (bed claiming prevented)
[DEBUG] Cleared HOME memory for guard 12345678-1234-1234-1234-123456789abc (released bed)
```

---

## Common Issues & Solutions

### Issue 1: Guards Still Claiming Beds
**Cause**: Memory not cleared on profession change
**Solution**: Add `getBrain().forget(MemoryModuleType.HOME)` to profession change handler

### Issue 2: Other Villagers Won't Claim Released Beds
**Cause**: Bed block still has guard UUID stored
**Solution**: Break and replace bed, or wait for villager pathfinding refresh (~150 blocks away)

### Issue 3: Mixin Not Activating
**Cause**: Mixin not registered or wrong target
**Solution**: Verify `villagermanager.mixins.json` includes `VillagerSleepMixin`

### Issue 4: Guards Pathfinding to Beds
**Cause**: `wantsToSleep()` not being called before pathfinding task
**Solution**: Verify mixin injection point is `@At("HEAD")` to run before task logic

---

## File Locations

### Files to Modify
1. **VillagerSleepMixin.java**
   - Path: `src/main/java/com/xeenaa/villagermanager/mixin/VillagerSleepMixin.java`
   - Add: `wantsToSleep()` and `canSleep()` injections

2. **Profession Change Handler**
   - Find where profession is set to GUARD
   - Add: `villager.getBrain().forget(MemoryModuleType.HOME)`

3. **villagermanager.mixins.json** (verify)
   - Path: `src/main/resources/villagermanager.mixins.json`
   - Ensure: `"VillagerSleepMixin"` is listed

---

## Performance Considerations

### Low Impact
- `wantsToSleep()` is already called every tick for all villagers
- Our injection adds only profession check (O(1))
- `forget()` is called once per profession change (rare)

### No Pathfinding Impact
- Guards can still pathfind normally
- Only sleep-related pathfinding is blocked
- Combat and patrol behaviors unaffected

---

## Summary

**Three Changes Needed**:

1. Add `wantsToSleep()` mixin → Prevents bed claiming
2. Add `canSleep()` mixin → Backup prevention
3. Call `forget(MemoryModuleType.HOME)` → Release existing beds

**Expected Result**:
- Guards never claim beds
- Guards never sleep
- Guards active 24/7
- Beds available for other villagers
