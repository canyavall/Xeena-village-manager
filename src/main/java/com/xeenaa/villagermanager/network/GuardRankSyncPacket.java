package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardPath;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Packet sent from server to client to synchronize guard rank data.
 */
public record GuardRankSyncPacket(UUID villagerId, GuardRank currentRank, int totalEmeraldsSpent, GuardPath chosenPath) implements CustomPayload {

    public static final CustomPayload.Id<GuardRankSyncPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "guard_rank_sync"));

    public static final PacketCodec<RegistryByteBuf, GuardRankSyncPacket> CODEC =
        new PacketCodec<RegistryByteBuf, GuardRankSyncPacket>() {
            @Override
            public GuardRankSyncPacket decode(RegistryByteBuf buf) {
                UUID villagerId = Uuids.PACKET_CODEC.decode(buf);
                GuardRank currentRank = GuardRank.CODEC.decode(buf);
                int totalEmeraldsSpent = buf.readVarInt();
                boolean hasChosenPath = buf.readBoolean();
                GuardPath chosenPath = hasChosenPath ? GuardPath.CODEC.decode(buf) : null;
                return new GuardRankSyncPacket(villagerId, currentRank, totalEmeraldsSpent, chosenPath);
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardRankSyncPacket packet) {
                Uuids.PACKET_CODEC.encode(buf, packet.villagerId);
                GuardRank.CODEC.encode(buf, packet.currentRank);
                buf.writeVarInt(packet.totalEmeraldsSpent);
                buf.writeBoolean(packet.chosenPath != null);
                if (packet.chosenPath != null) {
                    GuardPath.CODEC.encode(buf, packet.chosenPath);
                }
            }
        };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}