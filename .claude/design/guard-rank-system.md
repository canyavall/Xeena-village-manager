# Guard Rank System Design

## Overview
The Guard Rank System replaces the previous equipment-based system with a progression-based approach where guards advance through military ranks. Each rank costs emeralds and provides increased combat effectiveness, visual upgrades, and special abilities.

## Core System Design

### Rank Progression Structure
- **Base State**: All guards start as Recruit (Rank 0)
- **Specialization**: At Recruit, players choose between Melee or Ranged paths
- **Linear Progression**: Each path has 4 additional ranks after Recruit
- **Cost Scaling**: Each rank requires payment + previous rank as prerequisite

### Payment System
- **Currency**: Emeralds (standard Minecraft trading currency)
- **Cost Structure**: Progressive scaling (10→20→30→40 emeralds)
- **Prerequisites**: Must own previous rank to upgrade
- **Refunds**: No refunds - permanent progression

## Rank Specifications

### Rank 0: Recruit (Base State)
```
Health: 25 HP (12.5 hearts)
Attack Damage: 4 DMG (Stone Sword equivalent)
Equipment: None (basic villager appearance with sword)
Cost: Free (starting rank)
Special: None
```

## Melee Path: Man-at-Arms

### Man-at-Arms I
```
Health: 30 HP (+5 from Recruit)
Attack Damage: 6 DMG (+2 from Recruit)
Equipment: Leather Tunic (cosmetic)
Cost: 10 Emeralds
Special: Unlocks melee specialization path
```

### Man-at-Arms II
```
Health: 40 HP (+10 from previous)
Attack Damage: 8 DMG (+2 from previous)
Equipment: Leather Tunic + Iron Helmet (cosmetic)
Cost: 20 Emeralds
Special: Increased durability
```

### Man-at-Arms III
```
Health: 50 HP (+10 from previous)
Attack Damage: 10 DMG (+2 from previous)
Equipment: Leather Tunic + Iron Helmet + Iron Chestplate (cosmetic)
Cost: 30 Emeralds
Special: Heavy armor appearance
```

### Knight (Final Melee Rank)
```
Health: 65 HP (+15 from previous)
Attack Damage: 12 DMG (+2 from previous)
Equipment: Full Iron Armor Set (cosmetic)
Cost: 40 Emeralds
Special: Knockback Chance - 15% chance to knock back enemies on hit
```

## Ranged Path: Marksman

### Marksman I
```
Health: 25 HP (same as Recruit)
Ranged Damage: 5 DMG (Bow)
Equipment: Bow + Leather Cap (cosmetic)
Cost: 10 Emeralds
Special: Switches from melee to ranged combat (Power I equivalent)
Firing Speed: Slow (2.0 second draw time)
```

### Marksman II
```
Health: 30 HP (+5 from previous)
Ranged Damage: 7 DMG (Bow)
Equipment: Bow + Leather Cap + Leather Tunic (cosmetic)
Cost: 20 Emeralds
Special: Normal firing speed (Power II equivalent)
Firing Speed: Normal (1.5 second draw time)
```

### Marksman III
```
Health: 35 HP (+5 from previous)
Ranged Damage: 9 DMG (Bow)
Equipment: Bow + Leather Cap + Leather Tunic + Leather Leggings (cosmetic)
Cost: 30 Emeralds
Special: Fast firing (Power III equivalent + speed boost)
Firing Speed: Fast (1.0 second draw time)
```

### Sharpshooter (Final Ranged Rank)
```
Health: 45 HP (+10 from previous)
Ranged Damage: 11 DMG (Bow)
Equipment: Bow + Full Leather Armor Set (cosmetic)
Cost: 40 Emeralds
Special: Piercing Shot - Arrows can pass through one enemy and hit a second target
Firing Speed: Very Fast (0.8 second draw time)
```

## Technical Implementation Requirements

### Data Structure
```java
public class GuardRank {
    private String rankId;
    private String displayName;
    private GuardPath path;
    private int tier;
    private int emeraldCost;
    private int health;
    private int attackDamage;
    private float attackSpeed;
    private List<ItemStack> cosmeticEquipment;
    private List<SpecialAbility> abilities;
}

public enum GuardPath {
    RECRUIT,    // Base state
    MELEE,      // Man-at-Arms path
    RANGED      // Marksman path
}
```

### GUI Requirements
- Replace Equipment Tab with Rank Tab
- Show current rank with visual progression bar
- Display next rank requirements and costs
- Path selection interface for Recruit rank
- Emerald cost display and payment confirmation
- Visual preview of rank equipment

### Persistence Requirements
- Store current rank per guard
- Store emerald payment history
- Save rank progression state
- Handle world save/load scenarios

### Combat Integration
- Dynamic health modification based on rank
- Attack damage scaling for melee ranks
- Bow damage and speed scaling for ranged ranks
- Special ability triggering system
- Visual equipment rendering per rank

## Migration Strategy

### From Equipment System
1. **Phase 1**: Deprecate equipment slots but keep functionality
2. **Phase 2**: Implement rank system alongside equipment
3. **Phase 3**: Migrate existing guards to Recruit rank
4. **Phase 4**: Remove equipment system completely
5. **Phase 5**: Clean up obsolete code and data

### Data Migration
- Convert existing guards to Recruit rank
- Clear equipment data from save files
- Update GUI references from equipment to ranks
- Maintain backward compatibility during transition

## Testing Strategy

### Unit Tests
- Rank progression logic
- Cost calculation accuracy
- Special ability triggering
- Equipment visual assignment

### Integration Tests
- GUI rank selection workflows
- Combat stat modifications
- Persistence across world saves
- Network synchronization

### User Acceptance Tests
- Intuitive rank progression experience
- Clear visual feedback for upgrades
- Balanced emerald cost scaling
- Satisfying special ability effects

## Future Enhancements

### Additional Ranks
- Veteran tiers beyond current max ranks
- Prestige system for end-game progression
- Elite specializations within paths

### Advanced Features
- Group bonuses for mixed rank squads
- Rank-based AI behavior modifications
- Custom rank naming by players
- Achievement system for rank milestones