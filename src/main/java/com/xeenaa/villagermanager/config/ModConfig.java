package com.xeenaa.villagermanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple configuration system for Villager Manager mod
 */
public class ModConfig {
    private static final String CONFIG_FILE_NAME = "xeenaa-villager-manager.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ModConfig INSTANCE;

    // Configuration fields
    public List<String> blacklisted_professions = new ArrayList<>();

    // Guard profession settings
    public GuardSettings guard_settings = new GuardSettings();

    /**
     * Guard profession configuration
     */
    public static class GuardSettings {
        public boolean enabled = true;
        public int max_guards_per_village = 10;
        public int guard_range = 16;
        public int patrol_radius = 48;
        public double experience_gain_rate = 1.0;
        // Equipment system removed - replaced with ranking system

        // Default guard behavior settings
        public GuardBehaviorSettings default_behavior = new GuardBehaviorSettings();
    }

    /**
     * Default guard behavior settings for new guards.
     * These values are used as defaults for newly created guards.
     */
    public static class GuardBehaviorSettings {
        public double detection_range = 20.0;
        public String aggression_level = "balanced"; // defensive, balanced, aggressive
        public boolean patrol_enabled = true;
        public String combat_mode = "defensive"; // defensive, aggressive
        public boolean rest_enabled = true;
    }

    // Default configuration
    private ModConfig() {
        // Default blacklist - exclude nitwit by default
        blacklisted_professions.add("minecraft:nitwit");
        // Guard settings initialized with defaults in the field declaration
    }

    /**
     * Get the singleton config instance
     */
    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = loadConfig();
        }
        return INSTANCE;
    }

    /**
     * Check if a profession is blacklisted
     */
    public boolean isProfessionBlacklisted(Identifier professionId) {
        return blacklisted_professions.contains(professionId.toString());
    }

    /**
     * Get all blacklisted profession identifiers
     */
    public List<String> getBlacklistedProfessions() {
        return new ArrayList<>(blacklisted_professions);
    }

    /**
     * Add a profession to the blacklist
     */
    public void addBlacklistedProfession(String professionId) {
        if (!blacklisted_professions.contains(professionId)) {
            blacklisted_professions.add(professionId);
            saveConfig();
        }
    }

    /**
     * Remove a profession from the blacklist
     */
    public void removeBlacklistedProfession(String professionId) {
        if (blacklisted_professions.remove(professionId)) {
            saveConfig();
        }
    }

    /**
     * Load configuration from file
     */
    private static ModConfig loadConfig() {
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            XeenaaVillagerManager.LOGGER.info("Config file not found, creating default configuration");
            ModConfig defaultConfig = new ModConfig();
            defaultConfig.saveConfig();
            return defaultConfig;
        }

        try {
            String json = Files.readString(configPath);
            ModConfig config = GSON.fromJson(json, ModConfig.class);
            XeenaaVillagerManager.LOGGER.info("Loaded configuration with {} blacklisted professions",
                config.blacklisted_professions.size());
            return config;
        } catch (IOException e) {
            XeenaaVillagerManager.LOGGER.error("Failed to load config file, using defaults", e);
            return new ModConfig();
        }
    }

    /**
     * Save configuration to file
     */
    public void saveConfig() {
        Path configPath = getConfigPath();

        try {
            // Ensure config directory exists
            Files.createDirectories(configPath.getParent());

            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            XeenaaVillagerManager.LOGGER.info("Saved configuration to {}", configPath);
        } catch (IOException e) {
            XeenaaVillagerManager.LOGGER.error("Failed to save config file", e);
        }
    }

    /**
     * Get the path to the config file
     */
    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    /**
     * Reload configuration from file
     */
    public static void reload() {
        INSTANCE = loadConfig();
        XeenaaVillagerManager.LOGGER.info("Configuration reloaded");
    }

    /**
     * Get Guard profession settings
     */
    public GuardSettings getGuardSettings() {
        return guard_settings;
    }

    /**
     * Check if Guard profession is enabled
     */
    public boolean isGuardProfessionEnabled() {
        return guard_settings != null && guard_settings.enabled;
    }

    // Equipment check method removed - replaced with ranking system
}