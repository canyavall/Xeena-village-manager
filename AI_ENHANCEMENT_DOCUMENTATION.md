# Enhanced AI Pathfinding and Movement System

## Overview

The Enhanced AI Pathfinding and Movement System (P2-TASK-004) provides intelligent tactical positioning and movement for guard villagers. This system implements sophisticated combat positioning, dynamic patrol routes, formation coordination, and performance-optimized pathfinding.

## System Architecture

### Core Components

#### 1. CombatPositioningManager
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/positioning/CombatPositioningManager.java`

Central system for managing combat positioning logic. Provides tactical positioning based on guard specialization and combat situations.

**Key Features:**
- **Melee Positioning**: Flanking maneuvers, tank positioning, obstacle utilization
- **Ranged Positioning**: High ground seeking, clear line of sight, distance optimization
- **Formation Coordination**: Group coordination to prevent clustering

**Usage:**
```java
CombatPositioningManager manager = new CombatPositioningManager(world);
Optional<CombatPosition> position = manager.getOptimalCombatPosition(guard, target);
```

#### 2. TerrainAnalyzer
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/positioning/TerrainAnalyzer.java`

Analyzes terrain for tactical combat positioning advantages.

**Key Features:**
- High ground detection and evaluation
- Line of sight calculation
- Terrain safety assessment
- Escape route analysis

**Methods:**
- `findHighGroundPosition()` - Locates elevated tactical positions
- `isValidCombatPosition()` - Validates position safety
- `hasLineOfSight()` - Checks visibility between positions
- `evaluatePosition()` - Comprehensive terrain evaluation

#### 3. FormationManager
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/positioning/FormationManager.java`

Manages formation and coordination between multiple guards during combat.

**Key Features:**
- Anti-clustering algorithms
- Role-based formation positioning
- Dynamic formation adaptation
- Leadership hierarchy

**Formation Roles:**
- **LEADER**: Commands formation (tier 4+ guards)
- **TANK**: Front line defender (melee specialists)
- **SNIPER**: Elite ranged fighter (tier 3+ ranged)
- **ARCHER**: Standard ranged fighter
- **MELEE**: Standard melee fighter

#### 4. PatrolRouteManager
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/positioning/PatrolRouteManager.java`

Manages dynamic patrol routes based on village layout and threat analysis.

**Key Features:**
- **Village Analysis**: Automatic detection of buildings and gathering areas
- **Threat-Responsive Routes**: Adaptation based on recent threat activity
- **Route Optimization**: Efficient waypoint ordering
- **Zone Coverage**: Non-overlapping patrol patterns

**Patrol Point Types:**
- **PERIMETER**: Village boundary patrol
- **BUILDING**: Important structure monitoring
- **VILLAGER_AREA**: Civilian protection zones
- **THREAT_WATCH**: High-risk area monitoring

### Enhanced AI Goals

#### 1. GuardMeleeAttackGoal (Enhanced)
**Features Added:**
- Intelligent combat positioning using CombatPositioningManager
- Flanking maneuver detection and execution
- Tank positioning between threats and villagers
- Repositioning cooldowns to prevent over-movement
- Position type-based movement speeds

**Positioning Logic:**
```java
Optional<CombatPosition> optimalPosition = positioningManager.getOptimalCombatPosition(guard, target);
if (optimalPosition.isPresent()) {
    CombatPosition newPosition = optimalPosition.get();
    if (shouldMoveToNewPosition(newPosition, distance, positionDistance)) {
        moveToPosition(targetPos, newPosition.getType());
    }
}
```

#### 2. GuardRangedAttackGoal (Enhanced)
**Features Added:**
- High ground seeking behavior
- Clear line of sight positioning
- Escape route planning
- Emergency retreat improvements
- Kiting and distance management

**High Ground Priority:**
- Actively seeks elevated positions 2-4 blocks higher
- Maintains optimal 8-15 block combat range
- Considers line of sight in positioning decisions

#### 3. GuardPatrolGoal (Enhanced)
**Features Added:**
- Dynamic route generation using PatrolRouteManager
- Guard coordination to prevent overlap
- Threat-responsive patrol adaptation
- Formation-aware positioning

**Intelligent Patrolling:**
```java
Optional<BlockPos> intelligentTarget = routeManager.getNextPatrolWaypoint(guard);
if (intelligentTarget.isPresent()) {
    return intelligentTarget.get();
}
```

### Custom Pathfinding Goals

#### 1. CombatPathfindingGoal
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/pathfinding/CombatPathfindingGoal.java`

Specialized pathfinding for tactical combat movement.

**Features:**
- Tactical position validation
- Stuck detection and recovery
- Speed optimization based on position type
- Navigation failure handling

#### 2. OptimizedPathfinder
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/pathfinding/OptimizedPathfinder.java`

Performance-optimized pathfinding with caching.

**Performance Features:**
- Path caching with grid-based keys
- Rate limiting for path calculations
- Simplified A* algorithm for guard movement
- Memory-efficient cache management

## Integration and Usage

### AIEnhancementIntegration
**Location**: `src/main/java/com/xeenaa/villagermanager/ai/AIEnhancementIntegration.java`

Central coordinator for all AI enhancement systems.

**Initialization:**
```java
AIEnhancementIntegration integration = AIEnhancementIntegration.get(world);
integration.initializeGuardAI(guard); // Call when guard is created/assigned
integration.updateAIEnhancements(); // Call periodically (server tick)
```

**Configuration Options:**
- Combat positioning enable/disable
- Intelligent patrolling enable/disable
- Formation coordination enable/disable
- Performance optimizations enable/disable

### Combat Position Types

```java
public enum PositionType {
    MELEE,       // Basic melee position
    RANGED,      // Ranged combat position
    FLANKING,    // Flanking maneuver
    TANK,        // Defensive tank position
    HIGH_GROUND, // Elevated position
    FORMATION,   // Formation position
    ESCAPE       // Emergency escape route
}
```

### Priority System

```java
public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4)
}
```

## Performance Optimizations

### Path Caching
- **Grid-based keys** reduce cache misses
- **Time-based expiration** prevents stale paths
- **Size limits** prevent memory bloat
- **Rate limiting** prevents excessive calculations

### Distance-Based Optimization
- **Near range**: High accuracy pathfinding
- **Medium range**: Standard pathfinding
- **Far range**: Simplified pathfinding
- **Maximum range**: Direct line approach

### Selective Recalculation
- **Position-based triggers**: Only when significantly moved
- **Time-based triggers**: Periodic recalculation
- **Event-based triggers**: Threat detection, formation changes
- **Failure-based triggers**: Navigation failure recovery

## Testing Framework

### AIEnhancementTestFramework
**Location**: `src/test/java/com/xeenaa/villagermanager/ai/AIEnhancementTestFramework.java`

Comprehensive testing system for validating AI behaviors.

**Test Categories:**
1. **Combat Positioning Tests**
   - Melee guard positioning validation
   - Ranged guard positioning validation
   - High ground seeking tests
   - Flanking maneuver tests

2. **Terrain Analysis Tests**
   - Valid position checking
   - Line of sight calculation
   - Height advantage detection

3. **Patrol Route Tests**
   - Waypoint generation
   - Route optimization
   - Threat responsiveness

4. **Formation Tests**
   - Guard coordination
   - Anti-clustering behavior
   - Role assignment

5. **Performance Tests**
   - Calculation speed benchmarks
   - Memory usage validation
   - Cache efficiency tests

**Running Tests:**
```java
AIEnhancementTestFramework framework = new AIEnhancementTestFramework(world);
TestSuite results = framework.runAllTests();
System.out.println(results.getSummary());
```

## Expected Behaviors

### Melee Guards
- **Flanking**: Move to enemy's flanks when possible (tier 3+)
- **Tank Positioning**: Position between threats and villagers (tier 2+)
- **Terrain Usage**: Use buildings and obstacles tactically
- **Formation**: Coordinate with other melee guards to avoid clustering

### Ranged Guards
- **High Ground**: Actively seek elevated positions (tier 3+)
- **Distance Management**: Maintain 8-15 block optimal range
- **Line of Sight**: Find positions with clear shooting lanes
- **Escape Routes**: Always maintain retreat paths
- **Kiting**: Move away while maintaining combat effectiveness

### Patrol Behavior
- **Dynamic Routes**: Adapt based on village layout and threats
- **Zone Coverage**: Different guards patrol different areas
- **Threat Response**: Increase patrol frequency in threatened areas
- **Coordination**: Avoid overlapping patrol routes

### Formation Behavior
- **Leadership**: Highest tier guards command formations
- **Role Specialization**: Different positioning based on guard type
- **Adaptive Spacing**: Maintain optimal distances between guards
- **Tactical Positioning**: Coordinate during combat encounters

## Configuration

The system can be configured through the AIEnhancementIntegration class:

```java
AIEnhancementIntegration integration = AIEnhancementIntegration.get(world);
integration.setCombatPositioningEnabled(true);
integration.setIntelligentPatrollingEnabled(true);
integration.setFormationCoordinationEnabled(true);
integration.setPerformanceOptimizationsEnabled(true);
```

## Debug Information

Get system status and statistics:

```java
AIEnhancementIntegration integration = AIEnhancementIntegration.get(world);
String debugInfo = integration.getDebugInfo();
AIEnhancementStats stats = integration.getStats();
```

## Integration Notes

1. **Backward Compatibility**: All enhancements are additive and don't break existing functionality
2. **Performance**: Optimized for server environments with multiple guards
3. **Modularity**: Individual systems can be enabled/disabled as needed
4. **Extensibility**: Easy to add new positioning strategies and behaviors

## Future Enhancements

Potential areas for future development:
- Weather-aware positioning
- Time-of-day behavioral changes
- Advanced threat prediction
- Player command integration
- Cross-mod compatibility hooks