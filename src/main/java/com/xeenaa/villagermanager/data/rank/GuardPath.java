package com.xeenaa.villagermanager.data.rank;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;

/**
 * Enum representing the specialization paths for guards.
 */
public enum GuardPath implements StringIdentifiable {
    RECRUIT("recruit", "Recruit", "Basic guard without specialization"),
    MELEE("melee", "Man-at-Arms", "Melee combat specialization path"),
    RANGED("ranged", "Marksman", "Ranged combat specialization path");

    public static final PacketCodec<RegistryByteBuf, GuardPath> CODEC =
        new PacketCodec<RegistryByteBuf, GuardPath>() {
            @Override
            public GuardPath decode(RegistryByteBuf buf) {
                return buf.readEnumConstant(GuardPath.class);
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardPath path) {
                buf.writeEnumConstant(path);
            }
        };

    private final String id;
    private final String displayName;
    private final String description;

    GuardPath(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public static GuardPath fromId(String id) {
        for (GuardPath path : values()) {
            if (path.id.equals(id)) {
                return path;
            }
        }
        return RECRUIT;
    }

    public static GuardPath fromOrdinal(int ordinal) {
        GuardPath[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return RECRUIT;
    }

    /**
     * Checks if this path can be selected from the current path
     * @param currentPath The guard's current path
     * @return true if the transition is valid
     */
    public boolean canTransitionFrom(GuardPath currentPath) {
        // Can only specialize from RECRUIT to MELEE or RANGED
        if (this == RECRUIT) {
            return false; // Cannot go back to recruit
        }

        // Can specialize from RECRUIT to any path
        if (currentPath == RECRUIT) {
            return this != RECRUIT;
        }

        // Cannot switch between specializations
        return this == currentPath;
    }
}