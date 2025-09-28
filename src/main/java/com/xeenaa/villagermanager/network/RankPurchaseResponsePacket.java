package com.xeenaa.villagermanager.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Packet sent from server to client to indicate the result of a rank purchase attempt.
 * Provides feedback for successful purchases or error information for failed attempts.
 */
public record RankPurchaseResponsePacket(
    UUID villagerId,
    boolean success,
    String message
) implements CustomPayload {

    public static final CustomPayload.Id<RankPurchaseResponsePacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "rank_purchase_response"));

    public static final PacketCodec<RegistryByteBuf, RankPurchaseResponsePacket> CODEC =
        new PacketCodec<RegistryByteBuf, RankPurchaseResponsePacket>() {
            @Override
            public RankPurchaseResponsePacket decode(RegistryByteBuf buf) {
                UUID villagerId = Uuids.PACKET_CODEC.decode(buf);
                boolean success = buf.readBoolean();
                String message = buf.readString();
                return new RankPurchaseResponsePacket(villagerId, success, message);
            }

            @Override
            public void encode(RegistryByteBuf buf, RankPurchaseResponsePacket packet) {
                Uuids.PACKET_CODEC.encode(buf, packet.villagerId);
                buf.writeBoolean(packet.success);
                buf.writeString(packet.message);
            }
        };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    /**
     * Creates a success response packet.
     */
    public static RankPurchaseResponsePacket success(UUID villagerId, String message) {
        return new RankPurchaseResponsePacket(villagerId, true, message);
    }

    /**
     * Creates a failure response packet.
     */
    public static RankPurchaseResponsePacket failure(UUID villagerId, String errorMessage) {
        return new RankPurchaseResponsePacket(villagerId, false, errorMessage);
    }
}