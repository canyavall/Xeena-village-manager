package com.xeenaa.villagermanager.data.rank;

import com.xeenaa.villagermanager.data.rank.ability.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

/**
 * Enum representing guard ranks with progression costs and specialization paths.
 * Ranks must be purchased sequentially using emeralds.
 *
 * Design Specification:
 * - Melee Path: Tank focus with high HP (25→45), moderate damage (6→12)
 * - Ranged Path: Glass cannon with low HP (20→28), high damage (5→10)
 */
public enum GuardRank implements StringIdentifiable {
    // Base rank - no cost, automatically assigned
    RECRUIT("recruit", "Recruit", 0, createRecruitStats(), null),

    // Melee specialization path (Tank focus): Higher HP, moderate base damage + weapon
    MAN_AT_ARMS_I("man_at_arms_1", "Soldier I", 15, RankStats.melee(14.0f, 1.5f), null),
    MAN_AT_ARMS_II("man_at_arms_2", "Soldier II", 20, RankStats.melee(18.0f, 2.5f), null),
    MAN_AT_ARMS_III("man_at_arms_3", "Soldier III", 45, RankStats.melee(22.0f, 3.0f), null),
    KNIGHT("knight", "Knight", 75, RankStats.melee(26.0f, 4.0f), new KnockbackAbility()),

    // Ranged specialization path (Glass cannon): Lower HP, moderate base damage + bow
    MARKSMAN_I("marksman_1", "Ranger I", 15, RankStats.ranged(14.0f, 1.5f, 2.0f), null),
    MARKSMAN_II("marksman_2", "Ranger II", 20, RankStats.ranged(18.0f, 2.5f, 1.5f), null),
    MARKSMAN_III("marksman_3", "Ranger III", 45, RankStats.ranged(22.0f, 3.5f, 1.0f), null),
    SHARPSHOOTER("sharpshooter", "Sharpshooter", 75, RankStats.ranged(26.0f, 4.5f, 0.8f), new DoubleShotAbility());

    private static RankStats createRecruitStats() {
        return RankStats.melee(10.0f, 0.5f);
    }

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
    private final RankStats stats;
    private final SpecialAbility specialAbility;

    GuardRank(String id, String displayName, int emeraldCost, RankStats stats, SpecialAbility specialAbility) {
        this.id = id;
        this.displayName = displayName;
        this.emeraldCost = emeraldCost;
        this.stats = stats;
        this.specialAbility = specialAbility;
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

    public RankStats getStats() {
        return stats;
    }

    public SpecialAbility getSpecialAbility() {
        return specialAbility;
    }

    public boolean hasSpecialAbility() {
        return specialAbility != null;
    }

    /**
     * Gets the specialization path for this rank.
     */
    public GuardPath getPath() {
        if (this == RECRUIT) {
            return GuardPath.RECRUIT;
        } else if (this == MAN_AT_ARMS_I || this == MAN_AT_ARMS_II ||
                   this == MAN_AT_ARMS_III || this == KNIGHT) {
            return GuardPath.MELEE;
        } else if (this == MARKSMAN_I || this == MARKSMAN_II ||
                   this == MARKSMAN_III || this == SHARPSHOOTER) {
            return GuardPath.RANGED;
        }
        return GuardPath.RECRUIT;
    }

    /**
     * Gets the tier level (0-4) for this rank.
     */
    public int getTier() {
        return switch (this) {
            case RECRUIT -> 0;
            case MAN_AT_ARMS_I, MARKSMAN_I -> 1;
            case MAN_AT_ARMS_II, MARKSMAN_II -> 2;
            case MAN_AT_ARMS_III, MARKSMAN_III -> 3;
            case KNIGHT, SHARPSHOOTER -> 4;
        };
    }

    /**
     * Gets the next rank in the same specialization path.
     */
    public GuardRank getNextRank() {
        return switch (this) {
            case RECRUIT -> null; // Player must choose path
            case MAN_AT_ARMS_I -> MAN_AT_ARMS_II;
            case MAN_AT_ARMS_II -> MAN_AT_ARMS_III;
            case MAN_AT_ARMS_III -> KNIGHT;
            case MARKSMAN_I -> MARKSMAN_II;
            case MARKSMAN_II -> MARKSMAN_III;
            case MARKSMAN_III -> SHARPSHOOTER;
            default -> null; // Max rank reached
        };
    }

    /**
     * Gets the previous rank in the same specialization path.
     */
    public GuardRank getPreviousRank() {
        return switch (this) {
            case RECRUIT -> null; // Base rank
            case MAN_AT_ARMS_I, MARKSMAN_I -> RECRUIT;
            case MAN_AT_ARMS_II -> MAN_AT_ARMS_I;
            case MAN_AT_ARMS_III -> MAN_AT_ARMS_II;
            case KNIGHT -> MAN_AT_ARMS_III;
            case MARKSMAN_II -> MARKSMAN_I;
            case MARKSMAN_III -> MARKSMAN_II;
            case SHARPSHOOTER -> MARKSMAN_III;
        };
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
            return new GuardRank[]{MAN_AT_ARMS_I, MARKSMAN_I};
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