package com.xeenaa.villagermanager.data.rank;

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

    public static final PacketCodec<PacketCodecs.PacketBuf, GuardPath> CODEC =
        PacketCodecs.fromEnum(GuardPath.class);

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
}