package com.xeenaa.villagermanager.network;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.util.EquipmentValidator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Packet for sending equipment changes from client to server.
 *
 * <p>This packet is sent when a player modifies guard equipment through the GUI,
 * ensuring the changes are validated and applied on the server side.</p>
 *
 * @since 1.0.0
 */
public record EquipGuardPacket(
    UUID villagerId,
    GuardData.EquipmentSlot slot,
    ItemStack equipment
) implements CustomPayload {

    public static final CustomPayload.Id<EquipGuardPacket> ID =
        new CustomPayload.Id<>(Identifier.of("xeenaa_villager_manager", "equip_guard"));

    public static final PacketCodec<RegistryByteBuf, EquipGuardPacket> CODEC =
        PacketCodec.of(EquipGuardPacket::write, EquipGuardPacket::read);

    /**
     * Writes packet data to buffer
     */
    public static void write(EquipGuardPacket packet, RegistryByteBuf buf) {
        buf.writeUuid(packet.villagerId);
        buf.writeEnumConstant(packet.slot);

        // Handle empty ItemStack by writing a boolean flag first
        boolean isEmpty = packet.equipment.isEmpty();
        buf.writeBoolean(isEmpty);

        if (!isEmpty) {
            ItemStack.PACKET_CODEC.encode(buf, packet.equipment);
        }
    }

    /**
     * Reads packet data from buffer
     */
    public static EquipGuardPacket read(RegistryByteBuf buf) {
        UUID villagerId = buf.readUuid();
        GuardData.EquipmentSlot slot = buf.readEnumConstant(GuardData.EquipmentSlot.class);

        // Read the empty flag first
        boolean isEmpty = buf.readBoolean();
        ItemStack equipment;

        if (isEmpty) {
            equipment = ItemStack.EMPTY;
        } else {
            equipment = ItemStack.PACKET_CODEC.decode(buf);
        }

        return new EquipGuardPacket(villagerId, slot, equipment);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Registers the packet handler
     */
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, EquipGuardPacket::handle);
        XeenaaVillagerManager.LOGGER.info("Registered EquipGuardPacket handler");
    }

    /**
     * Handles the packet on the server side
     */
    public static void handle(EquipGuardPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();

        // Execute on server thread
        world.getServer().execute(() -> {
            try {
                processEquipmentChange(packet, player, world);
            } catch (Exception e) {
                XeenaaVillagerManager.LOGGER.error("Error processing equipment change for villager {}: {}",
                    packet.villagerId, e.getMessage(), e);
            }
        });
    }

    /**
     * Processes the equipment change on the server
     */
    private static void processEquipmentChange(EquipGuardPacket packet, ServerPlayerEntity player, ServerWorld world) {
        // Find the villager
        VillagerEntity villager = findVillager(world, packet.villagerId);
        if (villager == null) {
            XeenaaVillagerManager.LOGGER.warn("Villager {} not found for equipment change", packet.villagerId);
            return;
        }

        // Validate the player has permission to modify this villager
        if (!isPlayerAuthorized(player, villager)) {
            XeenaaVillagerManager.LOGGER.warn("Player {} not authorized to modify villager {}",
                player.getName().getString(), packet.villagerId);
            return;
        }

        // Validate the equipment is suitable for the slot
        if (!EquipmentValidator.isValidForSlot(packet.equipment, packet.slot)) {
            XeenaaVillagerManager.LOGGER.warn("Invalid equipment {} for slot {}",
                packet.equipment, packet.slot);
            return;
        }

        // Check if villager is still a guard
        String professionString = villager.getVillagerData().getProfession().id();
        Identifier currentProfession = Identifier.of(professionString);
        Identifier guardProfession = Identifier.of("xeenaa_villager_manager", "guard");

        if (!guardProfession.equals(currentProfession)) {
            XeenaaVillagerManager.LOGGER.warn("Villager {} is no longer a guard, cannot equip items", packet.villagerId);
            return;
        }

        // Apply the equipment change
        GuardDataManager manager = GuardDataManager.get(world);
        GuardData guardData = manager.getOrCreateGuardData(villager);

        // Get the current equipment to potentially return to player
        ItemStack currentEquipment = guardData.getEquipment(packet.slot);

        // Set the new equipment
        guardData.setEquipment(packet.slot, packet.equipment);
        manager.updateGuardData(villager, guardData);

        // If there was existing equipment, drop it or return it to player
        if (!currentEquipment.isEmpty()) {
            // For now, drop the item near the villager
            villager.dropStack(currentEquipment);
        }

        XeenaaVillagerManager.LOGGER.info("Player {} equipped {} in slot {} for guard villager {}",
            player.getName().getString(), packet.equipment, packet.slot, packet.villagerId);

        // Send sync packet back to all nearby clients
        sendSyncPacketToNearbyPlayers(world, villager, guardData);
    }

    /**
     * Finds a villager by UUID in the world
     */
    private static VillagerEntity findVillager(ServerWorld world, UUID villagerId) {
        for (var entity : world.iterateEntities()) {
            if (entity instanceof VillagerEntity && entity.getUuid().equals(villagerId)) {
                return (VillagerEntity) entity;
            }
        }
        return null;
    }

    /**
     * Checks if a player is authorized to modify the villager's equipment
     */
    private static boolean isPlayerAuthorized(ServerPlayerEntity player, VillagerEntity villager) {
        // Basic distance check - player must be within 10 blocks
        double distance = player.squaredDistanceTo(villager);
        if (distance > 100.0) { // 10 blocks squared
            return false;
        }

        // For now, all players can modify any guard
        // In the future, this could check:
        // - Village ownership
        // - Player permissions
        // - Guard assignment to specific players
        return true;
    }

    /**
     * Sends sync packet to all nearby players to update guard equipment
     */
    private static void sendSyncPacketToNearbyPlayers(ServerWorld world, VillagerEntity villager, GuardData guardData) {
        // Create sync packet
        GuardDataSyncPacket syncPacket = GuardDataSyncPacket.fromGuardData(villager.getUuid(), guardData);

        // Send to all players within range
        double syncRange = 64.0; // 64 block range for sync
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(villager) <= syncRange * syncRange) {
                ServerPlayNetworking.send(player, syncPacket);
            }
        }

        XeenaaVillagerManager.LOGGER.debug("Sent guard data sync packet for villager {} to nearby players",
            villager.getUuid());
    }
}