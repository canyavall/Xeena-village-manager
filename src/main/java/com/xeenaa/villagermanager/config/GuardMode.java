package com.xeenaa.villagermanager.config;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

/**
 * Defines guard movement behavior modes.
 *
 * <p>Guard modes control how guards move and position themselves:</p>
 * <ul>
 *   <li>FOLLOW: Guard follows the player who set this mode</li>
 *   <li>PATROL: Guard patrols around their guard post workstation</li>
 *   <li>STAND: Guard stands still at current location, only rotates for combat</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum GuardMode implements StringIdentifiable {
    FOLLOW("follow", "Follow"),
    PATROL("patrol", "Patrol"),
    STAND("stand", "Stand");

    private final String id;
    private final String displayName;

    GuardMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String asString() {
        return id;
    }

    /**
     * Gets the display name for this guard mode.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets a guard mode by its string identifier.
     *
     * @param id the identifier
     * @return the matching guard mode, or PATROL as default
     */
    public static GuardMode fromString(String id) {
        for (GuardMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return PATROL; // Default
    }

    /**
     * PacketCodec for network serialization.
     */
    public static final PacketCodec<RegistryByteBuf, GuardMode> CODEC =
        new PacketCodec<RegistryByteBuf, GuardMode>() {
            @Override
            public GuardMode decode(RegistryByteBuf buf) {
                return values()[buf.readVarInt()];
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardMode mode) {
                buf.writeVarInt(mode.ordinal());
            }
        };
}
