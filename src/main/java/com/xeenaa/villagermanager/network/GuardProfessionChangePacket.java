package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Packet sent from client to server to request profession change from guard with emerald refund.
 * This packet is used when the player confirms they want to change from guard profession
 * and receive their emerald refund.
 */
public record GuardProfessionChangePacket(
    int villagerEntityId,
    Identifier newProfessionId,
    boolean confirmed
) implements CustomPayload {

    public static final CustomPayload.Id<GuardProfessionChangePacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of(XeenaaVillagerManager.MOD_ID, "guard_profession_change"));

    public static final PacketCodec<RegistryByteBuf, GuardProfessionChangePacket> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, GuardProfessionChangePacket::villagerEntityId,
            Identifier.PACKET_CODEC, GuardProfessionChangePacket::newProfessionId,
            PacketCodecs.BOOL, GuardProfessionChangePacket::confirmed,
            GuardProfessionChangePacket::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}