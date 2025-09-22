package com.xeenaa.villagermanager.util;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.GuardData;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;

/**
 * Validates equipment items for guard villagers.
 *
 * <p>This class ensures that only appropriate items can be equipped in each slot,
 * preventing invalid combinations and maintaining game balance.</p>
 *
 * @since 1.0.0
 */
public class EquipmentValidator {

    /**
     * Validates if an item can be equipped in the specified slot
     */
    public static boolean isValidForSlot(ItemStack itemStack, GuardData.EquipmentSlot slot) {
        if (itemStack == null || itemStack.isEmpty()) {
            return true; // Empty is always valid (unequipping)
        }

        Item item = itemStack.getItem();

        switch (slot) {
            case WEAPON:
                return isValidWeapon(item);
            case HELMET:
                return isValidHelmet(item);
            case CHESTPLATE:
                return isValidChestplate(item);
            case LEGGINGS:
                return isValidLeggings(item);
            case BOOTS:
                return isValidBoots(item);
            case SHIELD:
                return isValidShield(item);
            default:
                XeenaaVillagerManager.LOGGER.warn("Unknown equipment slot: {}", slot);
                return false;
        }
    }

    /**
     * Checks if an item is a valid weapon
     */
    private static boolean isValidWeapon(Item item) {
        // Accept swords
        if (item instanceof SwordItem) {
            return true;
        }

        // Accept axes (can be used as weapons)
        if (item instanceof AxeItem) {
            return true;
        }

        // Accept tridents
        if (item instanceof TridentItem) {
            return true;
        }

        // Accept crossbows and bows
        if (item instanceof RangedWeaponItem) {
            return true;
        }

        return false;
    }

    /**
     * Checks if an item is a valid helmet
     */
    private static boolean isValidHelmet(Item item) {
        if (!(item instanceof ArmorItem)) {
            return false;
        }

        ArmorItem armorItem = (ArmorItem) item;
        return armorItem.getType() == ArmorItem.Type.HELMET;
    }

    /**
     * Checks if an item is a valid chestplate
     */
    private static boolean isValidChestplate(Item item) {
        if (!(item instanceof ArmorItem)) {
            return false;
        }

        ArmorItem armorItem = (ArmorItem) item;
        return armorItem.getType() == ArmorItem.Type.CHESTPLATE;
    }

    /**
     * Checks if an item is a valid leggings
     */
    private static boolean isValidLeggings(Item item) {
        if (!(item instanceof ArmorItem)) {
            return false;
        }

        ArmorItem armorItem = (ArmorItem) item;
        return armorItem.getType() == ArmorItem.Type.LEGGINGS;
    }

    /**
     * Checks if an item is a valid boots
     */
    private static boolean isValidBoots(Item item) {
        if (!(item instanceof ArmorItem)) {
            return false;
        }

        ArmorItem armorItem = (ArmorItem) item;
        return armorItem.getType() == ArmorItem.Type.BOOTS;
    }

    /**
     * Checks if an item is a valid shield
     */
    private static boolean isValidShield(Item item) {
        return item instanceof ShieldItem;
    }

    /**
     * Gets the equipment tier/quality for sorting or restrictions
     */
    public static EquipmentTier getTier(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return EquipmentTier.NONE;
        }

        Item item = itemStack.getItem();

        // Check armor tiers
        if (item instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) item;
            ArmorMaterial material = armorItem.getMaterial().value();

            // Compare by protection values since we can't directly get material names in 1.21
            int protection = material.getProtection(armorItem.getType());

            if (protection >= 3) { // Diamond/Netherite level
                return EquipmentTier.DIAMOND;
            } else if (protection >= 2) { // Iron level
                return EquipmentTier.IRON;
            } else if (protection >= 1) { // Chain/Gold level
                return EquipmentTier.GOLD;
            } else {
                return EquipmentTier.LEATHER;
            }
        }

        // Check tool/weapon tiers
        if (item instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) item;
            ToolMaterial material = toolItem.getMaterial();

            // Check by attack damage since we can't directly access material types
            float damage = material.getAttackDamage();

            if (damage >= 4) { // Diamond/Netherite level
                return EquipmentTier.DIAMOND;
            } else if (damage >= 2) { // Iron level
                return EquipmentTier.IRON;
            } else if (damage >= 1) { // Stone/Gold level
                return EquipmentTier.STONE;
            } else {
                return EquipmentTier.WOOD;
            }
        }

        return EquipmentTier.OTHER;
    }

    /**
     * Equipment tier enum for categorizing items
     */
    public enum EquipmentTier {
        NONE(0),
        WOOD(1),
        LEATHER(1),
        STONE(2),
        GOLD(2),
        IRON(3),
        DIAMOND(4),
        NETHERITE(5),
        OTHER(1);

        private final int level;

        EquipmentTier(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public boolean isAtLeast(EquipmentTier other) {
            return this.level >= other.level;
        }
    }

    /**
     * Checks if the guard can equip this item based on restrictions
     * (e.g., village reputation, guard level, config settings)
     */
    public static boolean canGuardEquip(ItemStack itemStack, GuardData guardData) {
        // For now, allow all valid items
        // In the future, this could check:
        // - Guard level/experience
        // - Village reputation
        // - Config restrictions
        // - Item enchantments
        return true;
    }

    /**
     * Gets a reason string if an item cannot be equipped
     */
    public static String getRestrictionReason(ItemStack itemStack, GuardData.EquipmentSlot slot) {
        if (!isValidForSlot(itemStack, slot)) {
            return "Invalid item type for " + slot.name().toLowerCase() + " slot";
        }

        // Add more restriction reasons as needed
        return null;
    }
}