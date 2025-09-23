package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.GuardData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * Packet for synchronizing guard role data from server to client.
 *
 * <p>This packet ensures that all clients see the current role state
 * of guard villagers, keeping the UI in sync with server data.</p>
 *
 * @since 1.0.0
 */
public record GuardDataSyncPacket(
    UUID villagerId,
    GuardData.GuardRole role
) implements CustomPayload {

    public static final CustomPayload.Id<GuardDataSyncPacket> ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "guard_data_sync"));

    public static final PacketCodec<RegistryByteBuf, GuardDataSyncPacket> CODEC =
        PacketCodec.of(GuardDataSyncPacket::write, GuardDataSyncPacket::read);

    /**
     * Creates a sync packet from guard data
     */
    public static GuardDataSyncPacket fromGuardData(UUID villagerId, GuardData guardData) {
        return new GuardDataSyncPacket(villagerId, guardData.getRole());
    }

    /**
     * Writes packet data to buffer
     */
    public static void write(GuardDataSyncPacket packet, RegistryByteBuf buf) {
        buf.writeUuid(packet.villagerId);
        buf.writeEnumConstant(packet.role);
    }

    /**
     * Reads packet data from buffer
     */
    public static GuardDataSyncPacket read(RegistryByteBuf buf) {
        UUID villagerId = buf.readUuid();
        GuardData.GuardRole role = buf.readEnumConstant(GuardData.GuardRole.class);

        return new GuardDataSyncPacket(villagerId, role);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}