package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.GuardData;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Packet for synchronizing guard equipment data from server to client.
 *
 * <p>This packet ensures that all clients see the current equipment state
 * of guard villagers, keeping the UI in sync with server data.</p>
 *
 * @since 1.0.0
 */
public record GuardDataSyncPacket(
    UUID villagerId,
    Map<GuardData.EquipmentSlot, ItemStack> equipment,
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
        return new GuardDataSyncPacket(villagerId, guardData.getAllEquipment(), guardData.getRole());
    }

    /**
     * Writes packet data to buffer
     */
    public static void write(GuardDataSyncPacket packet, RegistryByteBuf buf) {
        buf.writeUuid(packet.villagerId);
        buf.writeEnumConstant(packet.role);

        // Write equipment map - only non-empty items
        Map<GuardData.EquipmentSlot, ItemStack> nonEmptyEquipment = new EnumMap<>(GuardData.EquipmentSlot.class);
        for (Map.Entry<GuardData.EquipmentSlot, ItemStack> entry : packet.equipment.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                nonEmptyEquipment.put(entry.getKey(), entry.getValue());
            }
        }

        buf.writeInt(nonEmptyEquipment.size());
        for (Map.Entry<GuardData.EquipmentSlot, ItemStack> entry : nonEmptyEquipment.entrySet()) {
            buf.writeEnumConstant(entry.getKey());
            ItemStack.PACKET_CODEC.encode(buf, entry.getValue());
        }
    }

    /**
     * Reads packet data from buffer
     */
    public static GuardDataSyncPacket read(RegistryByteBuf buf) {
        UUID villagerId = buf.readUuid();
        GuardData.GuardRole role = buf.readEnumConstant(GuardData.GuardRole.class);

        // Read equipment map
        int equipmentCount = buf.readInt();
        Map<GuardData.EquipmentSlot, ItemStack> equipment = new EnumMap<>(GuardData.EquipmentSlot.class);

        for (int i = 0; i < equipmentCount; i++) {
            GuardData.EquipmentSlot slot = buf.readEnumConstant(GuardData.EquipmentSlot.class);
            ItemStack item = ItemStack.PACKET_CODEC.decode(buf);
            equipment.put(slot, item);
        }

        return new GuardDataSyncPacket(villagerId, equipment, role);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}