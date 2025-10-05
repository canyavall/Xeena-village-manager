package com.xeenaa.villagermanager.config;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

/**
 * Defines guard orders for behavior control.
 *
 * <p>Guard orders provide player control over guard movement:</p>
 * <ul>
 *   <li>STAY: Remain in current position</li>
 *   <li>FOLLOW: Follow the player who issued the order</li>
 *   <li>PATROL: Patrol the surrounding area</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum GuardOrder implements StringIdentifiable {
    STAY("stay", "Stay"),
    FOLLOW("follow", "Follow"),
    PATROL("patrol", "Patrol");

    private final String id;
    private final String displayName;

    GuardOrder(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String asString() {
        return id;
    }

    /**
     * Gets the display name for this guard order.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets a guard order by its string identifier.
     *
     * @param id the identifier
     * @return the matching guard order, or PATROL as default
     */
    public static GuardOrder fromString(String id) {
        for (GuardOrder order : values()) {
            if (order.id.equals(id)) {
                return order;
            }
        }
        return PATROL; // Default
    }

    /**
     * PacketCodec for network serialization.
     */
    public static final PacketCodec<RegistryByteBuf, GuardOrder> CODEC =
        new PacketCodec<RegistryByteBuf, GuardOrder>() {
            @Override
            public GuardOrder decode(RegistryByteBuf buf) {
                return values()[buf.readVarInt()];
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardOrder order) {
                buf.writeVarInt(order.ordinal());
            }
        };
}
