package com.xeenaa.villagermanager.data;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages persistent equipment and role data for Guard villagers.
 *
 * <p>This class handles NBT serialization/deserialization of guard-specific data
 * including equipment loadout, guard role, and other persistent state.</p>
 *
 * @since 1.0.0
 */
public class GuardData {
    private static final String NBT_KEY_EQUIPMENT = "GuardEquipment";
    private static final String NBT_KEY_ROLE = "GuardRole";
    private static final String NBT_KEY_GUARD_DATA = "XeenaaGuardData";
    private static final String NBT_KEY_VERSION = "DataVersion";
    private static final int CURRENT_VERSION = 1;

    private final UUID villagerId;
    private final Map<EquipmentSlot, ItemStack> equipment;
    private GuardRole currentRole;

    /**
     * Equipment slot types matching the GUI slots
     */
    public enum EquipmentSlot {
        WEAPON("Weapon"),
        HELMET("Helmet"),
        CHESTPLATE("Chestplate"),
        LEGGINGS("Leggings"),
        BOOTS("Boots"),
        SHIELD("Shield");

        private final String nbtKey;

        EquipmentSlot(String nbtKey) {
            this.nbtKey = nbtKey;
        }

        public String getNbtKey() {
            return nbtKey;
        }
    }

    /**
     * Guard role types for AI behavior
     */
    public enum GuardRole {
        PATROL("Patrol"),
        GUARD("Guard"),
        FOLLOW("Follow");

        private final String name;

        GuardRole(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static GuardRole fromString(String name) {
            for (GuardRole role : values()) {
                if (role.name.equalsIgnoreCase(name)) {
                    return role;
                }
            }
            return GUARD; // Default
        }
    }

    /**
     * Creates new guard data for a villager
     */
    public GuardData(UUID villagerId) {
        this.villagerId = villagerId;
        this.equipment = new EnumMap<>(EquipmentSlot.class);
        this.currentRole = GuardRole.GUARD; // Default role

        // Initialize with empty equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, ItemStack.EMPTY);
        }
    }

    /**
     * Loads guard data from a villager's NBT
     */
    public static GuardData fromNbt(VillagerEntity villager, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        UUID villagerId = villager.getUuid();
        GuardData data = new GuardData(villagerId);

        NbtCompound villagerNbt = new NbtCompound();
        villager.writeCustomDataToNbt(villagerNbt);

        if (villagerNbt.contains(NBT_KEY_GUARD_DATA)) {
            NbtCompound guardNbt = villagerNbt.getCompound(NBT_KEY_GUARD_DATA);
            data.deserializeNbt(guardNbt, registries);
        }

        return data;
    }

    /**
     * Saves guard data to a villager's NBT
     */
    public void saveToVillager(VillagerEntity villager, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        NbtCompound villagerNbt = new NbtCompound();
        villager.writeCustomDataToNbt(villagerNbt);

        NbtCompound guardNbt = serializeNbt(registries);
        villagerNbt.put(NBT_KEY_GUARD_DATA, guardNbt);

        villager.readCustomDataFromNbt(villagerNbt);

        XeenaaVillagerManager.LOGGER.debug("Saved guard data for villager {}", villagerId);
    }

    /**
     * Serializes guard data to NBT
     */
    public NbtCompound serializeNbt(net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(NBT_KEY_VERSION, CURRENT_VERSION);
        nbt.putString(NBT_KEY_ROLE, currentRole.getName());

        // Serialize equipment
        NbtCompound equipmentNbt = new NbtCompound();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt = (NbtCompound) entry.getValue().encode(registries);
                equipmentNbt.put(entry.getKey().getNbtKey(), itemNbt);
            }
        }
        nbt.put(NBT_KEY_EQUIPMENT, equipmentNbt);

        return nbt;
    }

    /**
     * Deserializes guard data from NBT
     */
    public void deserializeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        // Check version for future compatibility
        int version = nbt.getInt(NBT_KEY_VERSION);
        if (version > CURRENT_VERSION) {
            XeenaaVillagerManager.LOGGER.warn("Loading guard data from newer version: {} > {}",
                version, CURRENT_VERSION);
        }

        // Load role
        if (nbt.contains(NBT_KEY_ROLE)) {
            currentRole = GuardRole.fromString(nbt.getString(NBT_KEY_ROLE));
        }

        // Load equipment
        if (nbt.contains(NBT_KEY_EQUIPMENT)) {
            NbtCompound equipmentNbt = nbt.getCompound(NBT_KEY_EQUIPMENT);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (equipmentNbt.contains(slot.getNbtKey())) {
                    NbtCompound itemNbt = equipmentNbt.getCompound(slot.getNbtKey());
                    ItemStack item = ItemStack.fromNbt(registries, itemNbt).orElse(ItemStack.EMPTY);
                    equipment.put(slot, item);
                }
            }
        }

        XeenaaVillagerManager.LOGGER.debug("Loaded guard data for villager {}: role={}, equipment={}",
            villagerId, currentRole, getEquipmentSummary());
    }

    /**
     * Sets equipment in a specific slot
     */
    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        if (slot == null) {
            XeenaaVillagerManager.LOGGER.warn("Attempted to set equipment with null slot");
            return;
        }

        if (item == null) {
            equipment.put(slot, ItemStack.EMPTY);
        } else {
            equipment.put(slot, item.copy());
        }

        XeenaaVillagerManager.LOGGER.debug("Set {} equipment to {}", slot, item);
    }

    /**
     * Gets equipment from a specific slot
     */
    public ItemStack getEquipment(EquipmentSlot slot) {
        return equipment.getOrDefault(slot, ItemStack.EMPTY).copy();
    }

    /**
     * Gets all equipment
     */
    public Map<EquipmentSlot, ItemStack> getAllEquipment() {
        Map<EquipmentSlot, ItemStack> copy = new EnumMap<>(EquipmentSlot.class);
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    /**
     * Checks if the guard has any equipment
     */
    public boolean hasEquipment() {
        for (ItemStack item : equipment.values()) {
            if (!item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all equipment
     */
    public void clearAllEquipment() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, ItemStack.EMPTY);
        }
        XeenaaVillagerManager.LOGGER.debug("Cleared all equipment for villager {}", villagerId);
    }

    /**
     * Gets the current guard role
     */
    public GuardRole getRole() {
        return currentRole;
    }

    /**
     * Sets the guard role
     */
    public void setRole(GuardRole role) {
        if (role == null) {
            XeenaaVillagerManager.LOGGER.warn("Attempted to set null role");
            return;
        }

        this.currentRole = role;
        XeenaaVillagerManager.LOGGER.debug("Set guard role to {} for villager {}", role, villagerId);
    }

    /**
     * Gets a summary of equipped items for logging
     */
    private String getEquipmentSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("{");
        boolean first = true;
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                if (!first) summary.append(", ");
                summary.append(entry.getKey()).append("=")
                       .append(Registries.ITEM.getId(entry.getValue().getItem()));
                first = false;
            }
        }
        summary.append("}");
        return summary.toString();
    }

    /**
     * Gets the villager ID
     */
    public UUID getVillagerId() {
        return villagerId;
    }
}