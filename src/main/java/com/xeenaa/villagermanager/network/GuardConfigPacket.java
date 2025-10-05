package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.config.GuardBehaviorConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Packet sent from client to server to update guard behavior configuration.
 *
 * <p>This packet contains the villager UUID and the new behavior configuration
 * to apply. The server validates the request before applying the changes.</p>
 *
 * @param villagerId the UUID of the guard villager
 * @param config     the new behavior configuration
 * @since 1.0.0
 */
public record GuardConfigPacket(UUID villagerId, GuardBehaviorConfig config) implements CustomPayload {

    public static final CustomPayload.Id<GuardConfigPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "guard_config"));

    public static final PacketCodec<RegistryByteBuf, GuardConfigPacket> CODEC =
        new PacketCodec<RegistryByteBuf, GuardConfigPacket>() {
            @Override
            public GuardConfigPacket decode(RegistryByteBuf buf) {
                UUID villagerId = Uuids.PACKET_CODEC.decode(buf);
                GuardBehaviorConfig config = GuardBehaviorConfig.CODEC.decode(buf);
                return new GuardConfigPacket(villagerId, config);
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardConfigPacket packet) {
                Uuids.PACKET_CODEC.encode(buf, packet.villagerId);
                GuardBehaviorConfig.CODEC.encode(buf, packet.config);
            }
        };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    /**
     * Validates packet data.
     *
     * @return true if packet data is valid
     */
    public boolean isValid() {
        return villagerId != null && config != null;
    }
}
