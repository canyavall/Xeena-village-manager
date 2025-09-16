package com.xeenaa.villagepicker.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data model representing a villager profession with additional metadata for GUI display
 */
public class ProfessionData implements Comparable<ProfessionData> {
    private final Identifier id;
    private final VillagerProfession profession;
    private final String name;
    private final Text translatedName;
    private final Block workstation;
    private final ItemStack icon;
    private final boolean isVanilla;

    public ProfessionData(VillagerProfession profession) {
        this.profession = profession;
        this.id = Registries.VILLAGER_PROFESSION.getId(profession);
        this.name = id != null ? id.getPath() : "unknown";
        this.translatedName = getProfessionDisplayName(profession);
        this.workstation = getWorkstationBlock(profession);
        this.icon = createIcon();
        this.isVanilla = id != null && "minecraft".equals(id.getNamespace());
    }

    /**
     * Get the profession identifier
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Get the underlying villager profession
     */
    public VillagerProfession getProfession() {
        return profession;
    }

    /**
     * Get the profession name (path part of identifier)
     */
    public String getName() {
        return name;
    }

    /**
     * Get the translated display name for the profession
     */
    public Text getTranslatedName() {
        return translatedName;
    }

    /**
     * Get the workstation block associated with this profession
     */
    public Block getWorkstation() {
        return workstation;
    }

    /**
     * Get the icon ItemStack for GUI display
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Check if this is a vanilla profession
     */
    public boolean isVanilla() {
        return isVanilla;
    }

    /**
     * Get mod name (namespace of the identifier)
     */
    public String getModName() {
        return id != null ? id.getNamespace() : "unknown";
    }

    /**
     * Create display icon for the profession
     * Uses workstation item if available, otherwise falls back to defaults
     */
    private ItemStack createIcon() {
        if (workstation != null) {
            Item workstationItem = workstation.asItem();
            if (workstationItem != null) {
                return new ItemStack(workstationItem);
            }
        }

        // Fallback icon based on profession name
        return createFallbackIcon();
    }

    /**
     * Create fallback icon when workstation is not available
     */
    private ItemStack createFallbackIcon() {
        // Use emerald as default icon for any profession without a workstation
        Item emerald = Registries.ITEM.get(Identifier.of("minecraft", "emerald"));
        return new ItemStack(emerald);
    }

    /**
     * Get the display name for a profession with proper formatting
     */
    private Text getProfessionDisplayName(VillagerProfession profession) {
        // Try to get the translation key
        String translationKey = getTranslationKey(profession);
        Text translated = Text.translatable(translationKey);

        // If translation doesn't exist, format the name manually
        if (translated.getString().equals(translationKey)) {
            return Text.literal(formatProfessionName(name));
        }

        return translated;
    }

    /**
     * Get the workstation block for a profession
     * This is a simplified approach - in reality, professions use PointOfInterestType predicates
     */
    private Block getWorkstationBlock(VillagerProfession profession) {
        if (profession == null || id == null) {
            return null;
        }

        // For now, we'll use a simple mapping based on profession name
        // In a complete implementation, we'd need to analyze the PointOfInterestType registry
        return switch (id.getPath()) {
            case "armorer" -> Registries.BLOCK.get(Identifier.of("minecraft", "blast_furnace"));
            case "butcher" -> Registries.BLOCK.get(Identifier.of("minecraft", "smoker"));
            case "cartographer" -> Registries.BLOCK.get(Identifier.of("minecraft", "cartography_table"));
            case "cleric" -> Registries.BLOCK.get(Identifier.of("minecraft", "brewing_stand"));
            case "farmer" -> Registries.BLOCK.get(Identifier.of("minecraft", "composter"));
            case "fisherman" -> Registries.BLOCK.get(Identifier.of("minecraft", "barrel"));
            case "fletcher" -> Registries.BLOCK.get(Identifier.of("minecraft", "fletching_table"));
            case "leatherworker" -> Registries.BLOCK.get(Identifier.of("minecraft", "cauldron"));
            case "librarian" -> Registries.BLOCK.get(Identifier.of("minecraft", "lectern"));
            case "mason" -> Registries.BLOCK.get(Identifier.of("minecraft", "stonecutter"));
            case "shepherd" -> Registries.BLOCK.get(Identifier.of("minecraft", "loom"));
            case "toolsmith" -> Registries.BLOCK.get(Identifier.of("minecraft", "smithing_table"));
            case "weaponsmith" -> Registries.BLOCK.get(Identifier.of("minecraft", "grindstone"));
            default -> null; // Unknown or modded professions
        };
    }

    /**
     * Get the translation key for a profession
     */
    private String getTranslationKey(VillagerProfession profession) {
        if (id != null) {
            return "entity.minecraft.villager." + id.getPath();
        }
        return "entity.minecraft.villager.unknown";
    }

    /**
     * Format profession name for display (capitalize and replace underscores)
     */
    private String formatProfessionName(String name) {
        if (name == null || name.isEmpty()) {
            return "Unknown";
        }

        return Arrays.stream(name.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    /**
     * Natural ordering: vanilla professions first, then by name
     */
    @Override
    public int compareTo(ProfessionData other) {
        return PROFESSION_COMPARATOR.compare(this, other);
    }

    /**
     * Comparator for sorting professions: vanilla first, then alphabetical by name
     */
    public static final Comparator<ProfessionData> PROFESSION_COMPARATOR =
        Comparator.comparing((ProfessionData p) -> !p.isVanilla()) // Vanilla first (false < true)
            .thenComparing(ProfessionData::getName); // Then by name

    /**
     * Comparator for sorting by translated name
     */
    public static final Comparator<ProfessionData> BY_TRANSLATED_NAME =
        Comparator.comparing((ProfessionData p) -> !p.isVanilla())
            .thenComparing(p -> p.getTranslatedName().getString());

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProfessionData that = (ProfessionData) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ProfessionData{id=%s, name='%s', vanilla=%s, workstation=%s}",
            id, name, isVanilla, workstation);
    }
}