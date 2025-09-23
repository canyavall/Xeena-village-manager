package com.xeenaa.villagermanager.data.rank;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

/**
 * Enum representing guard ranks with progression costs and specialization paths.
 * Ranks must be purchased sequentially using emeralds.
 */
public enum GuardRank implements StringIdentifiable {
    // Base rank - no cost, automatically assigned
    RECRUIT("recruit", "Recruit", 0, 2.0f, 20.0f),

    // Melee specialization path
    GUARD_MELEE("guard_melee", "Guard", 15, 4.0f, 25.0f),
    SERGEANT_MELEE("sergeant_melee", "Sergeant", 20, 6.0f, 30.0f),
    LIEUTENANT_MELEE("lieutenant_melee", "Lieutenant", 45, 8.0f, 35.0f),
    CAPTAIN_MELEE("captain_melee", "Captain", 75, 10.0f, 40.0f),

    // Ranged specialization path
    GUARD_RANGED("guard_ranged", "Marksman", 15, 3.0f, 20.0f),
    SERGEANT_RANGED("sergeant_ranged", "Sharpshooter", 20, 5.0f, 22.0f),
    LIEUTENANT_RANGED("lieutenant_ranged", "Elite Archer", 45, 7.0f, 24.0f),
    CAPTAIN_RANGED("captain_ranged", "Master Archer", 75, 9.0f, 26.0f);

    public static final PacketCodec<RegistryByteBuf, GuardRank> CODEC =
        new PacketCodec<RegistryByteBuf, GuardRank>() {
            @Override
            public GuardRank decode(RegistryByteBuf buf) {
                return buf.readEnumConstant(GuardRank.class);
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardRank rank) {
                buf.writeEnumConstant(rank);
            }
        };

    private final String id;
    private final String displayName;
    private final int emeraldCost;
    private final float attackDamage;
    private final float health;

    GuardRank(String id, String displayName, int emeraldCost, float attackDamage, float health) {
        this.id = id;
        this.displayName = displayName;
        this.emeraldCost = emeraldCost;
        this.attackDamage = attackDamage;
        this.health = health;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Text getDisplayText() {
        return Text.literal(displayName);
    }

    public int getEmeraldCost() {
        return emeraldCost;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public float getHealth() {
        return health;
    }

    /**
     * Gets the specialization path for this rank.
     */
    public GuardPath getPath() {
        if (this == RECRUIT) {
            return GuardPath.RECRUIT;
        } else if (name().endsWith("_MELEE")) {
            return GuardPath.MELEE;
        } else if (name().endsWith("_RANGED")) {
            return GuardPath.RANGED;
        }
        return GuardPath.RECRUIT;
    }

    /**
     * Gets the tier level (0-4) for this rank.
     */
    public int getTier() {
        if (this == RECRUIT) return 0;
        if (name().startsWith("GUARD_")) return 1;
        if (name().startsWith("SERGEANT_")) return 2;
        if (name().startsWith("LIEUTENANT_")) return 3;
        if (name().startsWith("CAPTAIN_")) return 4;
        return 0;
    }

    /**
     * Gets the next rank in the same specialization path.
     */
    public GuardRank getNextRank() {
        GuardPath path = getPath();
        int currentTier = getTier();

        if (currentTier >= 4) return null; // Already at max rank

        switch (path) {
            case RECRUIT:
                // From recruit, can choose either path
                return null; // Player must choose path
            case MELEE:
                return switch (currentTier) {
                    case 1 -> SERGEANT_MELEE;
                    case 2 -> LIEUTENANT_MELEE;
                    case 3 -> CAPTAIN_MELEE;
                    default -> null;
                };
            case RANGED:
                return switch (currentTier) {
                    case 1 -> SERGEANT_RANGED;
                    case 2 -> LIEUTENANT_RANGED;
                    case 3 -> CAPTAIN_RANGED;
                    default -> null;
                };
        }
        return null;
    }

    /**
     * Gets the previous rank in the same specialization path.
     */
    public GuardRank getPreviousRank() {
        GuardPath path = getPath();
        int currentTier = getTier();

        if (currentTier <= 0) return null; // Already at base rank

        switch (path) {
            case MELEE:
                return switch (currentTier) {
                    case 1 -> RECRUIT;
                    case 2 -> GUARD_MELEE;
                    case 3 -> SERGEANT_MELEE;
                    case 4 -> LIEUTENANT_MELEE;
                    default -> null;
                };
            case RANGED:
                return switch (currentTier) {
                    case 1 -> RECRUIT;
                    case 2 -> GUARD_RANGED;
                    case 3 -> SERGEANT_RANGED;
                    case 4 -> LIEUTENANT_RANGED;
                    default -> null;
                };
        }
        return RECRUIT;
    }

    /**
     * Checks if this rank can be purchased (previous rank requirement met).
     */
    public boolean canPurchase(GuardRank currentRank) {
        if (this == RECRUIT) return true; // Base rank is free

        GuardRank requiredPrevious = getPreviousRank();
        return requiredPrevious != null && currentRank == requiredPrevious;
    }

    /**
     * Gets all available next ranks from the given current rank.
     */
    public static GuardRank[] getAvailableUpgrades(GuardRank currentRank) {
        if (currentRank == RECRUIT) {
            // From recruit, can choose either path
            return new GuardRank[]{GUARD_MELEE, GUARD_RANGED};
        }

        GuardRank nextRank = currentRank.getNextRank();
        return nextRank != null ? new GuardRank[]{nextRank} : new GuardRank[0];
    }

    public static GuardRank fromId(String id) {
        for (GuardRank rank : values()) {
            if (rank.id.equals(id)) {
                return rank;
            }
        }
        return RECRUIT;
    }

    public static GuardRank fromOrdinal(int ordinal) {
        GuardRank[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return RECRUIT;
    }
}