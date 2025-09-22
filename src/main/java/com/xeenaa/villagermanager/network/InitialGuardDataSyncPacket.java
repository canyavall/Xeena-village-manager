package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.data.GuardData;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Packet for synchronizing all guard data from server to client on initial connection.
 * This ensures the client cache is populated with existing guard data for proper rendering.
 */
public record InitialGuardDataSyncPacket(Map<UUID, GuardData> guardDataMap) implements CustomPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger("InitialGuardDataSyncPacket");

    public static final CustomPayload.Id<InitialGuardDataSyncPacket> PACKET_ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "initial_guard_sync"));

    public static final PacketCodec<RegistryByteBuf, InitialGuardDataSyncPacket> CODEC =
        new PacketCodec<RegistryByteBuf, InitialGuardDataSyncPacket>() {
            @Override
            public InitialGuardDataSyncPacket decode(RegistryByteBuf buf) {
                LOGGER.info("=== DECODING INITIAL GUARD SYNC PACKET ===");

                Map<UUID, GuardData> guardDataMap = new HashMap<>();
                int count = buf.readVarInt();
                LOGGER.info("Reading {} guard data entries", count);

                for (int i = 0; i < count; i++) {
                    try {
                        UUID villagerId = Uuids.PACKET_CODEC.decode(buf);
                        GuardData.GuardRole role = buf.readEnumConstant(GuardData.GuardRole.class);

                        // Read equipment map
                        int equipmentCount = buf.readVarInt();
                        GuardData guardData = new GuardData(villagerId);
                        guardData.setRole(role);

                        for (int j = 0; j < equipmentCount; j++) {
                            GuardData.EquipmentSlot slot = buf.readEnumConstant(GuardData.EquipmentSlot.class);
                            ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
                            guardData.setEquipment(slot, itemStack);
                        }

                        guardDataMap.put(villagerId, guardData);
                        LOGGER.info("Decoded guard data for villager {}: role={}, has equipment = {}",
                                   villagerId, role, guardData.hasEquipment());
                    } catch (Exception e) {
                        LOGGER.error("Failed to decode guard data entry {}: {}", i, e.getMessage(), e);
                    }
                }

                LOGGER.info("Successfully decoded initial guard sync with {} entries", guardDataMap.size());
                return new InitialGuardDataSyncPacket(guardDataMap);
            }

            @Override
            public void encode(RegistryByteBuf buf, InitialGuardDataSyncPacket packet) {
                LOGGER.info("=== ENCODING INITIAL GUARD SYNC PACKET ===");
                LOGGER.info("Encoding {} guard data entries", packet.guardDataMap.size());

                buf.writeVarInt(packet.guardDataMap.size());

                for (Map.Entry<UUID, GuardData> entry : packet.guardDataMap.entrySet()) {
                    try {
                        Uuids.PACKET_CODEC.encode(buf, entry.getKey());
                        GuardData guardData = entry.getValue();
                        buf.writeEnumConstant(guardData.getRole());

                        // Write equipment map - only non-empty items
                        Map<GuardData.EquipmentSlot, ItemStack> nonEmptyEquipment = new HashMap<>();
                        for (Map.Entry<GuardData.EquipmentSlot, ItemStack> equipEntry : guardData.getAllEquipment().entrySet()) {
                            if (!equipEntry.getValue().isEmpty()) {
                                nonEmptyEquipment.put(equipEntry.getKey(), equipEntry.getValue());
                            }
                        }

                        buf.writeVarInt(nonEmptyEquipment.size());
                        for (Map.Entry<GuardData.EquipmentSlot, ItemStack> equipEntry : nonEmptyEquipment.entrySet()) {
                            buf.writeEnumConstant(equipEntry.getKey());
                            ItemStack.PACKET_CODEC.encode(buf, equipEntry.getValue());
                        }

                        LOGGER.info("Encoded guard data for villager {}: role={}, equipment items = {}",
                                   entry.getKey(), guardData.getRole(), nonEmptyEquipment.size());
                    } catch (Exception e) {
                        LOGGER.error("Failed to encode guard data for villager {}: {}",
                                   entry.getKey(), e.getMessage(), e);
                    }
                }

                LOGGER.info("Successfully encoded initial guard sync packet");
            }
        };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}