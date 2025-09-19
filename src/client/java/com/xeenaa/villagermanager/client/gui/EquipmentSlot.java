package com.xeenaa.villagermanager.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A specialized button widget representing an equipment slot for Guard villagers.
 *
 * <p>This widget displays an equipment slot that can hold an item and provides
 * visual feedback for valid/invalid items. It supports tooltips, drag-and-drop
 * interactions, and different slot types (weapon, armor pieces, shield).</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Item rendering with proper scaling and positioning</li>
 *   <li>Visual feedback for valid/invalid equipment</li>
 *   <li>Tooltip support showing item stats and requirements</li>
 *   <li>Slot type validation for appropriate equipment</li>
 *   <li>Empty slot placeholder display</li>
 * </ul>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class EquipmentSlot extends ButtonWidget {

    // Slot styling constants
    private static final int SLOT_SIZE = 24; // Increased slot size for better visibility
    private static final int SLOT_BORDER_COLOR = 0xFF8B8B8B;
    private static final int SLOT_BACKGROUND_COLOR = 0xFF373737;
    private static final int SLOT_BACKGROUND_HOVER_COLOR = 0xFF4B4B4B;
    private static final int SLOT_BACKGROUND_EMPTY_COLOR = 0xFF2D2D2D;
    private static final int SLOT_VALID_BORDER_COLOR = 0xFF00AA00;
    private static final int SLOT_INVALID_BORDER_COLOR = 0xFFCC0000;
    private static final int SLOT_HIGHLIGHT_COLOR = 0xFF5555FF;
    private static final int PLACEHOLDER_COLOR = 0xFF555555;

    // Slot type for validation
    public enum SlotType {
        WEAPON("weapon", "gui.xeenaa_villager_manager.equipment.slot.weapon"),
        HELMET("helmet", "gui.xeenaa_villager_manager.equipment.slot.helmet"),
        CHESTPLATE("chestplate", "gui.xeenaa_villager_manager.equipment.slot.chestplate"),
        LEGGINGS("leggings", "gui.xeenaa_villager_manager.equipment.slot.leggings"),
        BOOTS("boots", "gui.xeenaa_villager_manager.equipment.slot.boots"),
        SHIELD("shield", "gui.xeenaa_villager_manager.equipment.slot.shield");

        private final String id;
        private final String translationKey;

        SlotType(String id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public String getId() {
            return id;
        }

        public Text getDisplayName() {
            return Text.translatable(translationKey);
        }
    }

    private final SlotType slotType;
    private ItemStack currentItem = ItemStack.EMPTY;
    private boolean validItem = true;
    private boolean showValidationBorder = false;

    /**
     * Creates a new equipment slot widget.
     *
     * @param x the x position of the slot
     * @param y the y position of the slot
     * @param slotType the type of equipment this slot accepts
     * @param onPress the action to perform when the slot is clicked
     */
    public EquipmentSlot(int x, int y, SlotType slotType, PressAction onPress) {
        super(x, y, SLOT_SIZE, SLOT_SIZE, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.slotType = java.util.Objects.requireNonNull(slotType, "Slot type cannot be null");

        // Set initial tooltip
        updateTooltip();
    }

    /**
     * Sets the item in this equipment slot.
     *
     * @param item the item to place in the slot, or ItemStack.EMPTY to clear
     */
    public void setItem(ItemStack item) {
        this.currentItem = item != null ? item : ItemStack.EMPTY;
        this.validItem = validateItem(currentItem);
        updateTooltip();
    }

    /**
     * Gets the item currently in this slot.
     *
     * @return the current item, or ItemStack.EMPTY if slot is empty
     */
    public ItemStack getItem() {
        return currentItem;
    }

    /**
     * Gets the slot type.
     *
     * @return the slot type
     */
    public SlotType getSlotType() {
        return slotType;
    }

    /**
     * Checks if the current item is valid for this slot.
     *
     * @return true if the item is valid or slot is empty
     */
    public boolean isValidItem() {
        return validItem;
    }

    /**
     * Sets whether to show validation border colors.
     *
     * @param show true to show validation borders, false for normal borders
     */
    public void setShowValidationBorder(boolean show) {
        this.showValidationBorder = show;
    }

    /**
     * Sets the position of this equipment slot.
     *
     * @param x the new x position
     * @param y the new y position
     */
    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Determine background and border colors
        int backgroundColor;
        int borderColor = SLOT_BORDER_COLOR;

        if (currentItem.isEmpty()) {
            backgroundColor = this.isHovered() ? SLOT_BACKGROUND_HOVER_COLOR : SLOT_BACKGROUND_EMPTY_COLOR;
        } else {
            backgroundColor = this.isHovered() ? SLOT_BACKGROUND_HOVER_COLOR : SLOT_BACKGROUND_COLOR;
        }

        // Apply validation colors if needed
        if (showValidationBorder && !currentItem.isEmpty()) {
            borderColor = validItem ? SLOT_VALID_BORDER_COLOR : SLOT_INVALID_BORDER_COLOR;
        } else if (this.isHovered()) {
            borderColor = SLOT_HIGHLIGHT_COLOR;
        }

        // Render slot background with slight inset for depth
        context.fill(getX(), getY(), getX() + width, getY() + height, backgroundColor);

        // Render inner shadow for depth effect
        if (currentItem.isEmpty()) {
            context.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + 2, 0x40000000);
            context.fill(getX() + 1, getY() + 1, getX() + 2, getY() + height - 1, 0x40000000);
        }

        // Render slot border with double border for better definition
        context.drawBorder(getX(), getY(), width, height, borderColor);
        if (this.isHovered() || !currentItem.isEmpty()) {
            context.drawBorder(getX() + 1, getY() + 1, width - 2, height - 2, 0x30FFFFFF);
        }

        // Render item if present with enhanced visibility
        if (!currentItem.isEmpty()) {
            // Center the item within the slot with proper scaling
            int itemSize = 18; // Larger item rendering size
            int itemX = getX() + (width - itemSize) / 2;
            int itemY = getY() + (height - itemSize) / 2;

            // Scale the item for better visibility
            context.getMatrices().push();
            context.getMatrices().translate(itemX + itemSize / 2.0f, itemY + itemSize / 2.0f, 0);
            context.getMatrices().scale(1.125f, 1.125f, 1.0f); // 12.5% larger
            context.getMatrices().translate(-itemSize / 2.0f, -itemSize / 2.0f, 0);

            context.drawItem(currentItem, 0, 0);

            // Render item overlay (durability bar, stack count, etc.) at original position
            context.getMatrices().pop();
            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, currentItem, itemX, itemY);

            // Add subtle glow for equipped items
            if (validItem) {
                context.fill(getX(), getY(), getX() + width, getY() + 1, 0x2000FF00);
                context.fill(getX(), getY(), getX() + 1, getY() + height, 0x2000FF00);
            }
        } else {
            // Render slot type icon/placeholder when empty
            renderEmptySlotPlaceholder(context);
        }
    }

    /**
     * Renders a placeholder icon when the slot is empty.
     */
    private void renderEmptySlotPlaceholder(DrawContext context) {
        int centerX = getX() + width / 2;
        int centerY = getY() + height / 2;

        // Use different colors for hovered vs normal state
        int color = this.isHovered() ? 0xFF888888 : PLACEHOLDER_COLOR;
        int highlightColor = 0xFF999999;

        // Draw enhanced icons with better visual definition
        switch (slotType) {
            case WEAPON:
                // Enhanced sword icon with blade and handle detail
                context.fill(centerX, centerY - 6, centerX + 1, centerY + 3, color);
                context.fill(centerX - 2, centerY - 3, centerX + 3, centerY - 2, color);
                context.fill(centerX - 1, centerY + 3, centerX + 2, centerY + 5, highlightColor);
                break;
            case HELMET:
                // Enhanced helmet with dome shape and face opening
                context.fill(centerX - 3, centerY - 3, centerX + 4, centerY - 2, color);
                context.fill(centerX - 4, centerY - 2, centerX + 5, centerY + 2, color);
                context.fill(centerX - 2, centerY - 1, centerX + 3, centerY + 1, 0x00000000); // Face opening
                break;
            case CHESTPLATE:
                // Enhanced chestplate with shoulder pads
                context.fill(centerX - 4, centerY - 3, centerX + 5, centerY + 4, color);
                context.fill(centerX - 5, centerY - 2, centerX - 3, centerY + 1, color); // Left shoulder
                context.fill(centerX + 4, centerY - 2, centerX + 6, centerY + 1, color); // Right shoulder
                break;
            case LEGGINGS:
                // Enhanced leggings with belt detail
                context.fill(centerX - 3, centerY - 2, centerX + 4, centerY, color); // Belt
                context.fill(centerX - 2, centerY, centerX + 1, centerY + 5, color); // Left leg
                context.fill(centerX + 1, centerY, centerX + 4, centerY + 5, color); // Right leg
                break;
            case BOOTS:
                // Enhanced boots with sole detail
                context.fill(centerX - 3, centerY + 1, centerX + 4, centerY + 4, color);
                context.fill(centerX - 4, centerY + 3, centerX + 5, centerY + 5, highlightColor); // Sole
                break;
            case SHIELD:
                // Enhanced shield with central boss
                context.fill(centerX - 3, centerY - 4, centerX + 4, centerY + 4, color);
                context.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, highlightColor); // Boss
                break;
        }
    }

    /**
     * Validates if an item is appropriate for this slot type.
     *
     * @param item the item to validate
     * @return true if the item is valid for this slot type
     */
    private boolean validateItem(ItemStack item) {
        if (item.isEmpty()) {
            return true; // Empty slots are always valid
        }

        // Perform item validation based on slot type
        return switch (slotType) {
            case WEAPON -> isValidWeapon(item);
            case HELMET -> isValidHelmet(item);
            case CHESTPLATE -> isValidChestplate(item);
            case LEGGINGS -> isValidLeggings(item);
            case BOOTS -> isValidBoots(item);
            case SHIELD -> isValidShield(item);
        };
    }

    /**
     * Checks if an item is a valid weapon.
     */
    private boolean isValidWeapon(ItemStack item) {
        // Check if item has attack damage attribute or is in weapon tag
        return item.getItem().getComponents().contains(net.minecraft.component.DataComponentTypes.TOOL) ||
               item.getItem().toString().contains("sword") ||
               item.getItem().toString().contains("axe") ||
               item.getItem().toString().contains("bow") ||
               item.getItem().toString().contains("crossbow");
    }

    /**
     * Checks if an item is a valid helmet.
     */
    private boolean isValidHelmet(ItemStack item) {
        return item.getItem().toString().contains("helmet") ||
               item.getItem().toString().contains("_cap") ||
               item.getItem() instanceof net.minecraft.item.ArmorItem armorItem &&
               armorItem.getType() == net.minecraft.item.ArmorItem.Type.HELMET;
    }

    /**
     * Checks if an item is a valid chestplate.
     */
    private boolean isValidChestplate(ItemStack item) {
        return item.getItem().toString().contains("chestplate") ||
               item.getItem().toString().contains("tunic") ||
               item.getItem() instanceof net.minecraft.item.ArmorItem armorItem &&
               armorItem.getType() == net.minecraft.item.ArmorItem.Type.CHESTPLATE;
    }

    /**
     * Checks if an item is a valid leggings.
     */
    private boolean isValidLeggings(ItemStack item) {
        return item.getItem().toString().contains("leggings") ||
               item.getItem().toString().contains("pants") ||
               item.getItem() instanceof net.minecraft.item.ArmorItem armorItem &&
               armorItem.getType() == net.minecraft.item.ArmorItem.Type.LEGGINGS;
    }

    /**
     * Checks if an item is a valid boots.
     */
    private boolean isValidBoots(ItemStack item) {
        return item.getItem().toString().contains("boots") ||
               item.getItem() instanceof net.minecraft.item.ArmorItem armorItem &&
               armorItem.getType() == net.minecraft.item.ArmorItem.Type.BOOTS;
    }

    /**
     * Checks if an item is a valid shield.
     */
    private boolean isValidShield(ItemStack item) {
        return item.getItem().toString().contains("shield") ||
               item.getItem() instanceof net.minecraft.item.ShieldItem;
    }

    /**
     * Updates the tooltip based on current slot state.
     */
    private void updateTooltip() {
        if (currentItem.isEmpty()) {
            // Show slot type description when empty
            Text tooltipText = Text.translatable("gui.xeenaa_villager_manager.equipment.slot_empty",
                slotType.getDisplayName());
            setTooltip(Tooltip.of(tooltipText));
        } else {
            // Show item tooltip when equipped
            if (validItem) {
                setTooltip(null); // Let item handle its own tooltip
            } else {
                Text invalidText = Text.translatable("gui.xeenaa_villager_manager.equipment.slot_invalid",
                    currentItem.getName(), slotType.getDisplayName());
                setTooltip(Tooltip.of(invalidText));
            }
        }
    }

    @Override
    public void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        // Add accessibility narration
        if (currentItem.isEmpty()) {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE,
                Text.translatable("gui.xeenaa_villager_manager.equipment.slot_narration_empty",
                    slotType.getDisplayName()));
        } else {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE,
                Text.translatable("gui.xeenaa_villager_manager.equipment.slot_narration_equipped",
                    slotType.getDisplayName(), currentItem.getName()));
        }
    }
}