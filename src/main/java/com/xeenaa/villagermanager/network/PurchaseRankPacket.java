package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.rank.GuardRank;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Packet sent from client to server to purchase a guard rank.
 */
public record PurchaseRankPacket(UUID villagerId, GuardRank targetRank) implements CustomPayload {

    public static final CustomPayload.Id<PurchaseRankPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "purchase_rank"));

    public static final PacketCodec<RegistryByteBuf, PurchaseRankPacket> CODEC =
        new PacketCodec<RegistryByteBuf, PurchaseRankPacket>() {
            @Override
            public PurchaseRankPacket decode(RegistryByteBuf buf) {
                UUID villagerId = Uuids.PACKET_CODEC.decode(buf);
                GuardRank targetRank = GuardRank.CODEC.decode(buf);
                return new PurchaseRankPacket(villagerId, targetRank);
            }

            @Override
            public void encode(RegistryByteBuf buf, PurchaseRankPacket packet) {
                Uuids.PACKET_CODEC.encode(buf, packet.villagerId);
                GuardRank.CODEC.encode(buf, packet.targetRank);
            }
        };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}