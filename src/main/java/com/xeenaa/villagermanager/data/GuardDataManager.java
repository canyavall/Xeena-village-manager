package com.xeenaa.villagermanager.data;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages guard data persistence across world saves.
 *
 * <p>This class handles loading and saving guard data for all guard villagers
 * in the world, ensuring data persists through game sessions.</p>
 *
 * @since 1.0.0
 */
public class GuardDataManager extends PersistentState {
    private static final String DATA_NAME = "xeenaa_guard_data";
    private final Map<UUID, GuardData> guardDataMap = new HashMap<>();

    private static final PersistentState.Type<GuardDataManager> type = new PersistentState.Type<>(
        GuardDataManager::new,
        GuardDataManager::fromNbt,
        null
    );

    /**
     * Gets the guard data manager for a world
     */
    public static GuardDataManager get(World world) {
        if (!(world instanceof ServerWorld)) {
            throw new IllegalStateException("Guard data manager only available on server");
        }

        ServerWorld serverWorld = (ServerWorld) world;
        PersistentStateManager stateManager = serverWorld.getPersistentStateManager();

        return stateManager.getOrCreate(type, DATA_NAME);
    }

    /**
     * Creates a new guard data manager
     */
    public GuardDataManager() {
        super();
    }

    /**
     * Loads guard data manager from NBT
     */
    public static GuardDataManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        GuardDataManager manager = new GuardDataManager();

        NbtCompound guardsNbt = nbt.getCompound("Guards");
        for (String key : guardsNbt.getKeys()) {
            try {
                UUID villagerId = UUID.fromString(key);
                GuardData data = new GuardData(villagerId);
                data.deserializeNbt(guardsNbt.getCompound(key), registries);
                manager.guardDataMap.put(villagerId, data);
            } catch (IllegalArgumentException e) {
                XeenaaVillagerManager.LOGGER.error("Failed to load guard data for key: {}", key, e);
            }
        }

        XeenaaVillagerManager.LOGGER.info("Loaded {} guard data entries", manager.guardDataMap.size());
        return manager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound guardsNbt = new NbtCompound();

        for (Map.Entry<UUID, GuardData> entry : guardDataMap.entrySet()) {
            guardsNbt.put(entry.getKey().toString(), entry.getValue().serializeNbt(registries));
        }

        nbt.put("Guards", guardsNbt);
        XeenaaVillagerManager.LOGGER.debug("Saved {} guard data entries", guardDataMap.size());
        return nbt;
    }

    /**
     * Gets or creates guard data for a villager
     */
    public GuardData getOrCreateGuardData(VillagerEntity villager) {
        UUID villagerId = villager.getUuid();

        if (!guardDataMap.containsKey(villagerId)) {
            GuardData data = new GuardData(villagerId);
            guardDataMap.put(villagerId, data);
            markDirty();
            XeenaaVillagerManager.LOGGER.debug("Created new guard data for villager {}", villagerId);
        }

        return guardDataMap.get(villagerId);
    }

    /**
     * Gets guard data if it exists
     */
    public GuardData getGuardData(UUID villagerId) {
        return guardDataMap.get(villagerId);
    }

    /**
     * Removes guard data when a villager is no longer a guard
     */
    public void removeGuardData(UUID villagerId) {
        if (guardDataMap.remove(villagerId) != null) {
            markDirty();
            XeenaaVillagerManager.LOGGER.debug("Removed guard data for villager {}", villagerId);
        }
    }

    /**
     * Checks if a villager has guard data
     */
    public boolean hasGuardData(UUID villagerId) {
        return guardDataMap.containsKey(villagerId);
    }

    /**
     * Updates guard data and marks for saving
     */
    public void updateGuardData(VillagerEntity villager, GuardData data) {
        guardDataMap.put(villager.getUuid(), data);
        markDirty();
        XeenaaVillagerManager.LOGGER.debug("Updated guard data for villager {}", villager.getUuid());
    }

    /**
     * Clears equipment for a guard when changing professions
     */
    public void clearEquipmentForProfessionChange(VillagerEntity villager) {
        UUID villagerId = villager.getUuid();
        GuardData data = guardDataMap.get(villagerId);

        if (data != null && data.hasEquipment()) {
            // Drop equipment items before clearing
            dropEquipmentItems(villager, data);
            data.clearAllEquipment();
            markDirty();
            XeenaaVillagerManager.LOGGER.info("Cleared equipment for villager {} due to profession change", villagerId);
        }
    }

    /**
     * Drops equipment items when clearing
     */
    private void dropEquipmentItems(VillagerEntity villager, GuardData data) {
        for (var entry : data.getAllEquipment().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                villager.dropStack(entry.getValue());
            }
        }
    }

    /**
     * Gets total number of guards
     */
    public int getGuardCount() {
        return guardDataMap.size();
    }

    /**
     * Gets all guard data for initial synchronization
     */
    public Map<UUID, GuardData> getAllGuardData() {
        return new HashMap<>(guardDataMap);
    }

    /**
     * Cleanup method for dead or missing villagers
     */
    public void cleanupInvalidEntries(ServerWorld world) {
        int removed = 0;
        var iterator = guardDataMap.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID villagerId = entry.getKey();

            // Check if villager still exists and is alive
            boolean stillValid = false;
            for (var entity : world.iterateEntities()) {
                if (entity instanceof VillagerEntity &&
                    entity.getUuid().equals(villagerId) &&
                    entity.isAlive()) {
                    stillValid = true;
                    break;
                }
            }

            if (!stillValid) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            markDirty();
            XeenaaVillagerManager.LOGGER.info("Cleaned up {} invalid guard data entries", removed);
        }
    }
}