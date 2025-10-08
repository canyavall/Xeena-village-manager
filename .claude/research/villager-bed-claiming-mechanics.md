# Villager Bed Claiming Mechanics Research

## Document Information
- **Date**: 2025-10-08
- **Minecraft Version**: 1.21.1 (mechanics confirmed from 1.14+)
- **Purpose**: Understanding villager bed claiming to prevent guards from claiming beds
- **Status**: Complete

---

## Executive Summary

Villagers claim beds through a **brain memory system** that runs independently of the sleep/wake cycle. The current `VillagerSleepMixin` only prevents guards from sleeping via `sleep()` and `wakeUp()` methods, but does NOT prevent bed claiming. This is why guards still reserve beds that other villagers cannot use.

**Key Finding**: Bed claiming happens through **brain AI tasks** (`FindPointOfInterestTask`, `SleepTask`) and **memory modules** (`MemoryModuleType.HOME`), not through the sleep methods.

---

## 1. Brain Memory System

### MemoryModuleType.HOME
- **Type**: `MemoryModuleType<GlobalPos>`
- **Purpose**: Stores the location of a villager's claimed bed
- **Package**: `net.minecraft.entity.ai.brain.MemoryModuleType`
- **Data Format**: `GlobalPos` (dimension + BlockPos)

### Inspecting Villager Memory (Debug)
```bash
# View all brain memories for nearest villager
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories

# Check HOME memory specifically
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories."minecraft:home"
```

The brain stores:
- `minecraft:home` - Bed location (GlobalPos)
- `minecraft:job_site` - Workstation location (GlobalPos)
- `minecraft:meeting_point` - Bell location (GlobalPos)

---

## 2. Bed Claiming Process

### Claiming Requirements (All Must Be True)
1. **Distance**: Within 48-block sphere of bed (measured from pillow block)
2. **Pathfinding State**: Villager must be actively pathfinding
3. **Availability**: Bed not already claimed by another villager
4. **Time Independent**: Can claim at any time (day or night)
5. **Line of Sight Independent**: Does not require visual contact with bed

### Bed Location Definition
- Bed position is defined by the **pillow block** coordinates
- After claiming, TWO-WAY binding occurs:
  - Villager remembers bed coordinates (stored in `MemoryModuleType.HOME`)
  - Bed block remembers which villager claimed it

### Brain Tasks Involved in Claiming

#### FindPointOfInterestTask
- **Package**: `net.minecraft.entity.ai.brain.task`
- **Purpose**: Searches for unclaimed points of interest (beds, workstations, etc.)
- **Search Radius**: 48 blocks
- **Behavior**: When detected, villager pathfinds to claim it

#### SleepTask
- **Package**: `net.minecraft.entity.ai.brain.task`
- **Extends**: `MultiTickTask`
- **Purpose**: Manages sleep behavior AND bed claiming
- **Active Period**: Tick 12000-23999 (night time)
- **Key Point**: This task handles BOTH claiming AND sleeping

#### JumpInBedTask
- **Package**: `net.minecraft.entity.ai.brain.task`
- **Extends**: `MultiTickTask`
- **Purpose**: Handles the physical action of getting into bed
- **Parameter**: `walkSpeed` variable

---

## 3. Bed Forgetting/Unclaiming Process

Villagers forget their bed under two conditions:

### Condition 1: Bed Removal (Proximity Check)
- **Distance**: Within ~2 blocks of remembered bed location
- **Trigger**: Villager attempts to sleep
- **Check**: Bed no longer exists at pillow coordinates
- **Note**: Rotating or moving bed DOES trigger forgetting

### Condition 2: Distance-Based Forgetting
- **Distance**: 150+ blocks away (taxicab/Manhattan distance)
- **State Requirement**: Villager is pathfinding
- **Trigger**: Villager attempts to sleep
- **Formula**: `|x1 - x2| + |y1 - y2| + |z1 - z2| >= 150`

---

## 4. Related Brain Tasks

### VillagerTaskListProvider
- **Package**: `net.minecraft.entity.ai.brain.task`
- **Key Method**: `createRestTasks(VillagerProfession profession, float speed)`
- **Returns**: `ImmutableList` of task pairs for rest behavior
- **Purpose**: Assembles all sleep/rest-related tasks for a villager

### Additional Sleep-Related Tasks
- `WakeUpTask` - Handles waking up logic
- `WalkHomeTask` - Pathfinding to claimed bed
- `HideInHomeTask` - Taking shelter behavior
- `GoToPointOfInterestTask` - Generic POI navigation
- `GoToRememberedPositionTask` - Navigate to stored memory location

---

## 5. Point of Interest (POI) System

### PointOfInterestType.HOME
- **Block Type**: Bed blocks
- **Registry**: Beds are registered as `HOME` type POI
- **Claiming Mechanic**: Nearly identical to workstation claiming
- **Storage**: POI data stored at chunk level

### POI Claiming is NOT via sleep() method
The `VillagerEntity.sleep(BlockPos pos)` method is called AFTER claiming, not during.

**Claiming Flow**:
```
1. FindPointOfInterestTask finds unclaimed bed
2. Villager pathfinds to bed (provisional claim established)
3. SleepTask stores bed location in MemoryModuleType.HOME
4. Bed block records villager UUID as claimant
5. Later: sleep() method called to physically enter bed
```

---

## 6. Methods to Intercept for Guard Villagers

### Current Mixin (Insufficient)
```java
@Mixin(VillagerEntity.class)
public class VillagerSleepMixin {
    @Inject(method = "sleep", at = @At("HEAD"), cancellable = true)
    private void preventGuardSleep(BlockPos pos, CallbackInfo ci) {
        // This ONLY prevents sleeping, NOT claiming!
    }
}
```

### Required Interventions

#### Option 1: Prevent Brain Tasks (Recommended)
Target the task creation in `VillagerTaskListProvider`:
- Mixin: `VillagerTaskListProviderMixin`
- Method: `createRestTasks`
- Action: Return empty list for guards OR filter out sleep-related tasks

#### Option 2: Block Memory Writing
Target memory module updates:
- Class: `Brain<VillagerEntity>`
- Method: `remember(MemoryModuleType<U> type, U value)`
- Action: Cancel when type == `MemoryModuleType.HOME` for guards

#### Option 3: Task Cancellation
Create mixins for specific tasks:
- `FindPointOfInterestTaskMixin` - Cancel for guards + HOME type
- `SleepTaskMixin` - Cancel task creation for guards

#### Option 4: Override wantsToSleep() (Cleanest)
```java
@Mixin(VillagerEntity.class)
public class VillagerSleepMixin {
    @Inject(method = "wantsToSleep", at = @At("HEAD"), cancellable = true)
    private void guardsDontWantToSleep(CallbackInfoReturnable<Boolean> cir) {
        VillagerEntity villager = (VillagerEntity)(Object)this;
        if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
            cir.setReturnValue(false);
        }
    }
}
```

---

## 7. Preventing Pathfinding-Based Claiming

### Physical Prevention (Not Recommended for Guards)
You can prevent claiming by blocking pathfinding:
- Surround villager with solid blocks at foot or eye level
- Requires 6-8 blocks (glass works)
- Breaks guard mobility - NOT suitable for our use case

### AI-Based Prevention (Recommended)
Prevent pathfinding state or task execution:
- Guards should still be able to move (patrol, combat)
- Only prevent bed-claiming pathfinding tasks
- Selective task filtering in brain

---

## 8. Exact Method Signatures (Yarn 1.21.1)

### VillagerEntity Methods
```java
// Already intercepted (insufficient)
public void sleep(BlockPos pos)
public void wakeUp()

// Need to intercept (key methods)
public boolean wantsToSleep()
protected boolean canSleep()

// Brain access
public Brain<VillagerEntity> getBrain()
```

### Brain Methods (Generic)
```java
public <U> void remember(MemoryModuleType<U> type, U value)
public <U> void remember(MemoryModuleType<U> type, U value, long expiry)
public <U> void forget(MemoryModuleType<U> type)
public <U> Optional<U> getOptionalMemory(MemoryModuleType<U> type)
```

### Task-Related Classes
```java
// Package: net.minecraft.entity.ai.brain.task
public class FindPointOfInterestTask extends Task<VillagerEntity>
public class SleepTask extends MultiTickTask<VillagerEntity>
public class JumpInBedTask extends MultiTickTask<VillagerEntity>
public class WakeUpTask extends Task<VillagerEntity>

// Provider
public class VillagerTaskListProvider {
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>
        createRestTasks(VillagerProfession profession, float speed)
}
```

---

## 9. Recommended Solution

### Approach: Override wantsToSleep() + Clear Existing HOME Memory

**Step 1**: Prevent future claiming via `wantsToSleep()`
```java
@Inject(method = "wantsToSleep", at = @At("HEAD"), cancellable = true)
private void guardsDontWantToSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}
```

**Step 2**: Clear HOME memory when villager becomes a guard
```java
// In profession change handler
villager.getBrain().forget(MemoryModuleType.HOME);
```

**Step 3**: Prevent canSleep() as backup
```java
@Inject(method = "canSleep", at = @At("HEAD"), cancellable = true)
private void guardsCannotSleep(CallbackInfoReturnable<Boolean> cir) {
    VillagerEntity villager = (VillagerEntity)(Object)this;
    if (villager.getVillagerData().getProfession() == ModProfessions.GUARD) {
        cir.setReturnValue(false);
    }
}
```

### Why This Works
1. `wantsToSleep()` is checked by `SleepTask` before attempting to claim/sleep
2. Returning `false` prevents the entire sleep task chain from executing
3. No claiming = bed stays available for other villagers
4. Clearing existing HOME memory releases already-claimed beds
5. `canSleep()` provides defense-in-depth

---

## 10. Testing & Verification

### Test Cases
1. **New Guard Creation**: Convert villager to guard, verify bed is released
2. **Existing Beds**: Guards near beds should not claim them
3. **Other Villagers**: Non-guards should successfully claim beds previously unavailable
4. **Night Behavior**: Guards should remain active during night (tick 12000-23999)
5. **Memory Check**: `/data get` should show no HOME memory for guards

### Debug Commands
```bash
# Check guard's brain memories (should have no HOME)
/data get entity @e[type=minecraft:villager,limit=1,sort=nearest] Brain.memories."minecraft:home"

# Check bed claim status (bed NBT data)
/data get block <x> <y> <z>
```

---

## 11. References

### Primary Sources
- Yarn Mappings (FabricMC): https://maven.fabricmc.net/docs/
- Villager Mechanics Analysis: https://gist.github.com/orlp/db1ca6dbb82727c4a939c95694a52b81
- Brain System Documentation: `net.minecraft.entity.ai.brain` package

### Key Findings
- Bed claiming is independent of sleep action
- Brain tasks run on schedule (tick-based)
- Memory modules persist across sessions
- Pathfinding is requirement for claiming (can be exploited)

### Version Compatibility
- Mechanics confirmed for Minecraft 1.14 through 1.21.1
- Brain system introduced in 1.14 (Village & Pillage update)
- Yarn mappings available for all modern versions

---

## 12. Implementation Checklist

- [ ] Add `wantsToSleep()` mixin injection
- [ ] Add `canSleep()` mixin injection
- [ ] Clear HOME memory on profession change to guard
- [ ] Test bed release when converting to guard
- [ ] Test guards do not claim new beds
- [ ] Verify other villagers can use freed beds
- [ ] Test across server restart (persistence)
- [ ] Add logging for bed claiming prevention (debug mode)

---

## Conclusion

The villager bed claiming system operates through Minecraft's brain AI framework, specifically through memory modules and scheduled tasks. The `sleep()` method is called AFTER claiming, making it ineffective for preventing bed reservation.

To prevent guards from claiming beds, we must intercept the brain's desire/ability to sleep (`wantsToSleep()` and `canSleep()`) and clear any existing HOME memories when a villager becomes a guard.

This research provides the exact method names, signatures, and implementation strategy needed to solve the guard bed claiming issue.
