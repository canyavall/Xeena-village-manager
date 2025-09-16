package com.xeenaa.villagepicker.network;

import com.xeenaa.villagepicker.XeenaaVillagePicker;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Packet sent from client to server when a player selects a profession for a villager
 */
public record SelectProfessionPacket(
    int villagerEntityId,
    Identifier professionId
) implements CustomPayload {

    public static final CustomPayload.Id<SelectProfessionPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of(XeenaaVillagePicker.MOD_ID, "select_profession"));

    public static final PacketCodec<RegistryByteBuf, SelectProfessionPacket> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, SelectProfessionPacket::villagerEntityId,
            Identifier.PACKET_CODEC, SelectProfessionPacket::professionId,
            SelectProfessionPacket::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}