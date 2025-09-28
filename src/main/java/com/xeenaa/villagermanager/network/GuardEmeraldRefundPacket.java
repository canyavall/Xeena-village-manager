package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Packet sent from server to client to show the guard profession change confirmation dialog.
 * Contains information about emeralds that will be permanently lost if the profession change is confirmed.
 * This serves as a warning to players about the penalty for changing from guard profession.
 */
public record GuardEmeraldRefundPacket(
    int villagerEntityId,
    UUID villagerId,
    Identifier newProfessionId,
    int totalEmeraldsToRefund, // Note: Despite the name, this represents emeralds to LOSE, not refund
    String currentRankName
) implements CustomPayload {

    public static final CustomPayload.Id<GuardEmeraldRefundPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of(XeenaaVillagerManager.MOD_ID, "guard_emerald_refund"));

    public static final PacketCodec<RegistryByteBuf, GuardEmeraldRefundPacket> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, GuardEmeraldRefundPacket::villagerEntityId,
            Uuids.PACKET_CODEC, GuardEmeraldRefundPacket::villagerId,
            Identifier.PACKET_CODEC, GuardEmeraldRefundPacket::newProfessionId,
            PacketCodecs.VAR_INT, GuardEmeraldRefundPacket::totalEmeraldsToRefund,
            PacketCodecs.STRING, GuardEmeraldRefundPacket::currentRankName,
            GuardEmeraldRefundPacket::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}