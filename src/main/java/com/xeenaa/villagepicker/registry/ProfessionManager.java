package com.xeenaa.villagepicker.registry;

import com.xeenaa.villagepicker.XeenaaVillagePicker;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;

import java.util.*;
import java.util.stream.Collectors;

public class ProfessionManager {
    private static ProfessionManager instance;
    private final Map<Identifier, VillagerProfession> professions = new LinkedHashMap<>();
    private final Set<Identifier> vanillaProfessions = new HashSet<>();
    private boolean initialized = false;

    private ProfessionManager() {
        // Private constructor for singleton
    }

    public static ProfessionManager getInstance() {
        if (instance == null) {
            instance = new ProfessionManager();
        }
        return instance;
    }

    /**
     * Initialize the profession manager by collecting all available professions
     */
    public void initialize() {
        if (initialized) {
            XeenaaVillagePicker.LOGGER.warn("ProfessionManager already initialized");
            return;
        }

        XeenaaVillagePicker.LOGGER.info("Initializing ProfessionManager - collecting all professions");
        collectProfessions();
        identifyVanillaProfessions();
        initialized = true;

        XeenaaVillagePicker.LOGGER.info("ProfessionManager initialized with {} total professions ({} vanilla, {} modded)",
            professions.size(), vanillaProfessions.size(), professions.size() - vanillaProfessions.size());
    }

    /**
     * Collect all registered villager professions from the registry
     */
    private void collectProfessions() {
        professions.clear();

        for (VillagerProfession profession : Registries.VILLAGER_PROFESSION) {
            Identifier id = Registries.VILLAGER_PROFESSION.getId(profession);
            if (id != null) {
                professions.put(id, profession);
                XeenaaVillagePicker.LOGGER.debug("Found profession: {} ({})", id, profession);
            }
        }

        XeenaaVillagePicker.LOGGER.info("Collected {} professions from registry", professions.size());
    }

    /**
     * Identify vanilla professions vs modded professions
     */
    private void identifyVanillaProfessions() {
        vanillaProfessions.clear();

        for (Identifier id : professions.keySet()) {
            if ("minecraft".equals(id.getNamespace())) {
                vanillaProfessions.add(id);
            }
        }

        XeenaaVillagePicker.LOGGER.info("Identified {} vanilla professions", vanillaProfessions.size());
    }

    /**
     * Get all available professions as an ordered collection
     */
    public Collection<VillagerProfession> getAllProfessions() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableCollection(professions.values());
    }

    /**
     * Get all profession IDs as an ordered collection
     */
    public Collection<Identifier> getAllProfessionIds() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableCollection(professions.keySet());
    }

    /**
     * Get a profession by its identifier
     */
    public VillagerProfession getProfession(Identifier id) {
        if (!initialized) {
            initialize();
        }
        return professions.get(id);
    }

    /**
     * Check if a profession is vanilla (Minecraft namespace)
     */
    public boolean isVanillaProfession(Identifier id) {
        if (!initialized) {
            initialize();
        }
        return vanillaProfessions.contains(id);
    }

    /**
     * Get only vanilla professions
     */
    public Collection<VillagerProfession> getVanillaProfessions() {
        if (!initialized) {
            initialize();
        }
        return vanillaProfessions.stream()
            .map(professions::get)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Get only modded professions
     */
    public Collection<VillagerProfession> getModdedProfessions() {
        if (!initialized) {
            initialize();
        }
        return professions.entrySet().stream()
            .filter(entry -> !vanillaProfessions.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .toList();
    }

    /**
     * Get profession count information
     */
    public ProfessionStats getStats() {
        if (!initialized) {
            initialize();
        }
        return new ProfessionStats(professions.size(), vanillaProfessions.size(),
            professions.size() - vanillaProfessions.size());
    }

    /**
     * Get all professions as ProfessionData objects, sorted appropriately
     */
    public List<ProfessionData> getAllProfessionData() {
        if (!initialized) {
            initialize();
        }
        return professions.values().stream()
            .map(ProfessionData::new)
            .sorted(ProfessionData.PROFESSION_COMPARATOR)
            .collect(Collectors.toList());
    }

    /**
     * Get vanilla profession data only
     */
    public List<ProfessionData> getVanillaProfessionData() {
        return getAllProfessionData().stream()
            .filter(ProfessionData::isVanilla)
            .collect(Collectors.toList());
    }

    /**
     * Get modded profession data only
     */
    public List<ProfessionData> getModdedProfessionData() {
        return getAllProfessionData().stream()
            .filter(data -> !data.isVanilla())
            .collect(Collectors.toList());
    }

    /**
     * Log detailed information about all detected professions for testing
     */
    public void logAllProfessions() {
        if (!initialized) {
            initialize();
        }

        XeenaaVillagePicker.LOGGER.info("=== PROFESSION REGISTRY DETAILS ===");

        List<ProfessionData> allData = getAllProfessionData();
        for (ProfessionData data : allData) {
            String type = data.isVanilla() ? "VANILLA" : "MODDED ";
            String workstation = data.getWorkstation() != null ? data.getWorkstation().toString() : "none";

            XeenaaVillagePicker.LOGGER.info("[{}] {} ({}) - Workstation: {} - Display: '{}'",
                type, data.getId(), data.getName(), workstation, data.getTranslatedName().getString());
        }

        XeenaaVillagePicker.LOGGER.info("=== END PROFESSION REGISTRY ===");
    }

    /**
     * Force refresh the profession registry (for use when mods are loaded dynamically)
     */
    public void refresh() {
        XeenaaVillagePicker.LOGGER.info("Refreshing ProfessionManager");
        initialized = false;
        initialize();
    }

    public record ProfessionStats(int total, int vanilla, int modded) {}
}