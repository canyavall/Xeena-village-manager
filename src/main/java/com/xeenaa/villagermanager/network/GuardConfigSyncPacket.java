package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.config.GuardBehaviorConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Packet sent from server to client to synchronize guard behavior configuration.
 *
 * <p>This packet updates the client's cached guard configuration data when
 * a configuration change occurs on the server.</p>
 *
 * @param villagerId the UUID of the guard villager
 * @param config     the updated behavior configuration
 * @since 1.0.0
 */
public record GuardConfigSyncPacket(UUID villagerId, GuardBehaviorConfig config) implements CustomPayload {

    public static final CustomPayload.Id<GuardConfigSyncPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "guard_config_sync"));

    public static final PacketCodec<RegistryByteBuf, GuardConfigSyncPacket> CODEC =
        new PacketCodec<RegistryByteBuf, GuardConfigSyncPacket>() {
            @Override
            public GuardConfigSyncPacket decode(RegistryByteBuf buf) {
                UUID villagerId = Uuids.PACKET_CODEC.decode(buf);
                GuardBehaviorConfig config = GuardBehaviorConfig.CODEC.decode(buf);
                return new GuardConfigSyncPacket(villagerId, config);
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardConfigSyncPacket packet) {
                Uuids.PACKET_CODEC.encode(buf, packet.villagerId);
                GuardBehaviorConfig.CODEC.encode(buf, packet.config);
            }
        };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
