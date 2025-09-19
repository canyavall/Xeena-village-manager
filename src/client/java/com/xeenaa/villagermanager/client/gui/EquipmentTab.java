package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tab for managing Guard villager equipment and gear configuration.
 *
 * <p>This tab is only available for villagers with the Guard profession and
 * provides comprehensive functionality to manage their equipment and role settings
 * in a clean, streamlined two-section layout.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Six equipment slots: weapon, helmet, chestplate, leggings, boots, shield</li>
 *   <li>Role selection buttons: Patrol, Guard (stationary), Follow</li>
 *   <li>Clean two-section layout focused on core functionality</li>
 *   <li>Drag-and-drop equipment management</li>
 *   <li>Equipment validation and visual feedback</li>
 * </ul>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class EquipmentTab extends Tab {
    private static final Identifier GUARD_PROFESSION_ID =
        Identifier.of("xeenaa_villager_manager", "guard");

    // Layout constants - optimized two-section design for proper GUI fit
    private static final int EQUIPMENT_SECTION_WIDTH = 160; // Optimized to fit within main GUI
    private static final int ROLE_SECTION_WIDTH = 168; // Balanced to use remaining space
    private static final int SECTION_SPACING = 24; // Clean spacing between sections
    private static final int SLOT_SPACING = 32; // Better spacing for equipment slots
    private static final int ROLE_BUTTON_SPACING = 8; // Improved button spacing
    private static final int SECTION_PADDING = 16; // More generous padding
    private static final int TITLE_HEIGHT = 24; // Better title spacing
    private static final int SECTION_MARGIN = 10; // Clean internal margins

    // Equipment slots
    private final Map<EquipmentSlot.SlotType, EquipmentSlot> equipmentSlots = new EnumMap<>(EquipmentSlot.SlotType.class);

    // Role selection
    private final Map<RoleButton.GuardRole, RoleButton> roleButtons = new EnumMap<>(RoleButton.GuardRole.class);
    private RoleButton.GuardRole currentRole = RoleButton.GuardRole.GUARD; // Default role

    /**
     * Creates a new equipment tab for the specified villager.
     *
     * @param targetVillager the villager to manage equipment for
     */
    public EquipmentTab(VillagerEntity targetVillager) {
        super(targetVillager);
    }

    public Text getDisplayName() {
        return Text.translatable("gui.xeenaa_villager_manager.tab.equipment");
    }

    public boolean isAvailable() {
        // Only available for Guard villagers
        if (!targetVillager.isAlive() || targetVillager.isBaby()) {
            return false;
        }

        // Check if villager has Guard profession
        String professionString = targetVillager.getVillagerData().getProfession().id();
        Identifier currentProfession = Identifier.of(professionString);
        boolean isGuard = GUARD_PROFESSION_ID.equals(currentProfession);

        XeenaaVillagerManager.LOGGER.debug("Equipment tab availability check: villager={}, profession={}, isGuard={}",
            targetVillager.getId(), currentProfession, isGuard);

        return isGuard;
    }

    @Override
    protected void initializeContent() {
        XeenaaVillagerManager.LOGGER.debug("Initialized EquipmentTab for Guard villager {}",
            targetVillager.getId());

        // Initialize equipment slots
        initializeEquipmentSlots();

        // Initialize role selection buttons
        initializeRoleButtons();

        // Load current equipment state (placeholder for now)
        loadEquipmentState();

        // Initial layout update
        if (contentWidth > 0 && contentHeight > 0) {
            updateLayout();
        }
    }

    @Override
    protected void updateLayout() {
        XeenaaVillagerManager.LOGGER.debug("Updated EquipmentTab layout - contentArea: {}x{} at {},{}",
            contentWidth, contentHeight, contentX, contentY);

        // Ensure we have valid content dimensions with minimum size requirements
        if (contentWidth <= 0 || contentHeight <= 0) {
            XeenaaVillagerManager.LOGGER.warn("Invalid content dimensions for EquipmentTab layout");
            return;
        }

        // Validate minimum size requirements for two-section layout
        int minRequiredWidth = EQUIPMENT_SECTION_WIDTH + ROLE_SECTION_WIDTH + SECTION_SPACING + (SECTION_PADDING * 2);
        int minRequiredHeight = TITLE_HEIGHT + 160 + (SECTION_PADDING * 2); // Minimum height for balanced sections

        if (contentWidth < minRequiredWidth || contentHeight < minRequiredHeight) {
            XeenaaVillagerManager.LOGGER.warn("Content area too small for proper layout: {}x{} (min: {}x{})",
                contentWidth, contentHeight, minRequiredWidth, minRequiredHeight);
        }

        // Update equipment slot positions
        updateEquipmentSlotPositions();

        // Update role button positions
        updateRoleButtonPositions();

        // Validate all elements are within bounds
        validateElementBounds();
    }

    public void onActivate() {
        super.onActivate();

        XeenaaVillagerManager.LOGGER.debug("EquipmentTab activated for villager {}",
            targetVillager.getId());

        // Force layout update when tab becomes active to ensure proper positioning
        if (contentWidth > 0 && contentHeight > 0) {
            updateLayout();
        }

        // TODO: Refresh equipment state
        // - Sync current equipment from server
        // - Update patrol status
        // - Refresh combat settings
    }

    public void onDeactivate() {
        super.onDeactivate();

        XeenaaVillagerManager.LOGGER.debug("EquipmentTab deactivated for villager {}",
            targetVillager.getId());

        // TODO: Save any pending changes
        // - Send equipment updates to server
        // - Save patrol preferences
        // - Apply combat configuration changes
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render section backgrounds
        renderSectionBackgrounds(context);

        // Render section titles
        renderSectionTitles(context);

        // Render equipment slots
        for (EquipmentSlot slot : equipmentSlots.values()) {
            slot.render(context, mouseX, mouseY, delta);
        }

        // Render role buttons
        for (RoleButton button : roleButtons.values()) {
            button.render(context, mouseX, mouseY, delta);
        }

        // Render additional UI elements
        renderAdditionalElements(context, mouseX, mouseY);
    }

    /**
     * Initializes all equipment slots with their positions and click handlers.
     */
    private void initializeEquipmentSlots() {
        // Create slots for each equipment type
        EquipmentSlot.SlotType[] slotTypes = {
            EquipmentSlot.SlotType.WEAPON,
            EquipmentSlot.SlotType.HELMET,
            EquipmentSlot.SlotType.CHESTPLATE,
            EquipmentSlot.SlotType.LEGGINGS,
            EquipmentSlot.SlotType.BOOTS,
            EquipmentSlot.SlotType.SHIELD
        };

        for (EquipmentSlot.SlotType slotType : slotTypes) {
            EquipmentSlot slot = new EquipmentSlot(0, 0, slotType, button -> {
                handleEquipmentSlotClick((EquipmentSlot) button);
            });
            equipmentSlots.put(slotType, slot);
        }
    }

    /**
     * Initializes role selection buttons.
     */
    private void initializeRoleButtons() {
        for (RoleButton.GuardRole role : RoleButton.GuardRole.values()) {
            RoleButton button = new RoleButton(0, 0, role, btn -> {
                handleRoleSelection(((RoleButton) btn).getRole());
            });
            roleButtons.put(role, button);
        }

        // Set initial selection
        updateRoleSelection(currentRole);
    }

    /**
     * Updates equipment slot positions in expanded layout with better organization.
     */
    private void updateEquipmentSlotPositions() {
        int sectionX = contentX + SECTION_PADDING + SECTION_MARGIN;
        int sectionY = contentY + TITLE_HEIGHT + SECTION_PADDING + SECTION_MARGIN;

        // 3x2 grid layout optimized for reduced section width
        // Tighter spacing to fit in 160px width
        int slotSize = 24;
        int slotSpacing = 20; // Reduced from 32 to fit better

        // Top row: Weapon, Helmet, Shield
        int topRowY = sectionY + 10;
        equipmentSlots.get(EquipmentSlot.SlotType.WEAPON).setPosition(sectionX + 12, topRowY);
        equipmentSlots.get(EquipmentSlot.SlotType.HELMET).setPosition(sectionX + 12 + slotSize + slotSpacing, topRowY);
        equipmentSlots.get(EquipmentSlot.SlotType.SHIELD).setPosition(sectionX + 12 + (slotSize + slotSpacing) * 2, topRowY);

        // Bottom row: Chestplate, Leggings, Boots
        int bottomRowY = topRowY + slotSize + slotSpacing;
        equipmentSlots.get(EquipmentSlot.SlotType.CHESTPLATE).setPosition(sectionX + 12, bottomRowY);
        equipmentSlots.get(EquipmentSlot.SlotType.LEGGINGS).setPosition(sectionX + 12 + slotSize + slotSpacing, bottomRowY);
        equipmentSlots.get(EquipmentSlot.SlotType.BOOTS).setPosition(sectionX + 12 + (slotSize + slotSpacing) * 2, bottomRowY);
    }

    /**
     * Updates role button positions with enhanced spacing and layout.
     */
    private void updateRoleButtonPositions() {
        int sectionX = contentX + EQUIPMENT_SECTION_WIDTH + SECTION_SPACING + SECTION_PADDING;
        int sectionY = contentY + TITLE_HEIGHT + SECTION_PADDING + SECTION_MARGIN;

        // Center buttons horizontally within the expanded role section
        int buttonX = sectionX + 20; // More centered in 168px width

        int index = 0;
        for (RoleButton button : roleButtons.values()) {
            button.setPosition(buttonX, sectionY + 20 + index * (26 + ROLE_BUTTON_SPACING));
            index++;
        }
    }

    /**
     * Renders section backgrounds for clean two-section visual organization.
     */
    private void renderSectionBackgrounds(DrawContext context) {
        // Calculate common section height for visual balance
        int sectionHeight = Math.min(160, contentHeight - TITLE_HEIGHT - SECTION_PADDING);

        // Equipment section background - optimized width
        int equipX = contentX + SECTION_PADDING;
        int equipY = contentY + TITLE_HEIGHT;
        context.fill(equipX, equipY, equipX + EQUIPMENT_SECTION_WIDTH, equipY + sectionHeight, 0x30000000);
        context.drawBorder(equipX, equipY, EQUIPMENT_SECTION_WIDTH, sectionHeight, 0x60FFFFFF);

        // Role section background - balanced width and equal height
        int roleX = contentX + EQUIPMENT_SECTION_WIDTH + SECTION_SPACING + SECTION_PADDING;
        int roleY = contentY + TITLE_HEIGHT;
        context.fill(roleX, roleY, roleX + ROLE_SECTION_WIDTH, roleY + sectionHeight, 0x30000000);
        context.drawBorder(roleX, roleY, ROLE_SECTION_WIDTH, sectionHeight, 0x60FFFFFF);
    }

    /**
     * Renders section titles for the two-section layout.
     */
    private void renderSectionTitles(DrawContext context) {
        // Equipment section title
        Text equipTitle = Text.translatable("gui.xeenaa_villager_manager.equipment.slots");
        context.drawText(getTextRenderer(), equipTitle,
            contentX + SECTION_PADDING + 8, contentY + 6, 0xFFFFFF, true);

        // Role section title
        Text roleTitle = Text.translatable("gui.xeenaa_villager_manager.equipment.role");
        int roleTitleX = contentX + EQUIPMENT_SECTION_WIDTH + SECTION_SPACING + SECTION_PADDING + 8;
        context.drawText(getTextRenderer(), roleTitle,
            roleTitleX, contentY + 6, 0xFFFFFF, true);
    }

    /**
     * Renders additional UI elements like status indicators.
     */
    private void renderAdditionalElements(DrawContext context, int mouseX, int mouseY) {
        // Render current role indicator in role section
        Text roleStatus = Text.translatable("gui.xeenaa_villager_manager.equipment.current_role",
            currentRole.getName());
        int statusX = contentX + EQUIPMENT_SECTION_WIDTH + SECTION_SPACING + SECTION_PADDING + 8;
        int statusY = contentY + contentHeight - 25;
        context.drawText(getTextRenderer(), roleStatus,
            statusX, statusY, 0xFFCCCC, false);
    }

    /**
     * Loads the current equipment state from the villager or storage.
     */
    private void loadEquipmentState() {
        // For now, set some example equipment for demonstration
        // In a full implementation, this would load from NBT data
        equipmentSlots.get(EquipmentSlot.SlotType.WEAPON).setItem(new ItemStack(Items.IRON_SWORD));
        equipmentSlots.get(EquipmentSlot.SlotType.HELMET).setItem(new ItemStack(Items.CHAINMAIL_HELMET));
        equipmentSlots.get(EquipmentSlot.SlotType.CHESTPLATE).setItem(new ItemStack(Items.LEATHER_CHESTPLATE));
        equipmentSlots.get(EquipmentSlot.SlotType.SHIELD).setItem(new ItemStack(Items.SHIELD));
        // Leggings and boots remain empty for demonstration
    }

    /**
     * Handles equipment slot click events.
     */
    private void handleEquipmentSlotClick(EquipmentSlot slot) {
        XeenaaVillagerManager.LOGGER.debug("Equipment slot clicked: {}", slot.getSlotType());

        // TODO: Implement equipment change logic
        // This would typically:
        // 1. Check player inventory for valid items
        // 2. Show item selection GUI or handle drag-and-drop
        // 3. Send equipment change packet to server
        // 4. Update local display

        // For now, cycle through example items for demonstration
        cycleExampleItem(slot);
    }

    /**
     * Cycles through example items for demonstration purposes.
     */
    private void cycleExampleItem(EquipmentSlot slot) {
        ItemStack currentItem = slot.getItem();
        ItemStack newItem = ItemStack.EMPTY;

        switch (slot.getSlotType()) {
            case WEAPON:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.WOODEN_SWORD);
                } else if (currentItem.isOf(Items.WOODEN_SWORD)) {
                    newItem = new ItemStack(Items.IRON_SWORD);
                } else if (currentItem.isOf(Items.IRON_SWORD)) {
                    newItem = new ItemStack(Items.DIAMOND_SWORD);
                }
                break;
            case HELMET:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.LEATHER_HELMET);
                } else if (currentItem.isOf(Items.LEATHER_HELMET)) {
                    newItem = new ItemStack(Items.IRON_HELMET);
                } else if (currentItem.isOf(Items.IRON_HELMET)) {
                    newItem = new ItemStack(Items.DIAMOND_HELMET);
                }
                break;
            // Add similar logic for other slots as needed
        }

        slot.setItem(newItem);
    }

    /**
     * Handles role selection.
     */
    private void handleRoleSelection(RoleButton.GuardRole role) {
        if (role != currentRole) {
            updateRoleSelection(role);

            XeenaaVillagerManager.LOGGER.debug("Role changed to: {}", role);

            // TODO: Send role change packet to server
            // TODO: Update villager AI behavior
        }
    }

    /**
     * Updates role selection state.
     */
    private void updateRoleSelection(RoleButton.GuardRole newRole) {
        // Update button states
        for (Map.Entry<RoleButton.GuardRole, RoleButton> entry : roleButtons.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == newRole);
        }

        currentRole = newRole;
    }

    /**
     * Validates that all UI elements are within their designated bounds.
     * Logs warnings if elements exceed their section boundaries.
     */
    private void validateElementBounds() {
        // Validate equipment slots stay within equipment section
        int equipSectionMaxX = contentX + SECTION_PADDING + EQUIPMENT_SECTION_WIDTH;
        int equipSectionMaxY = contentY + TITLE_HEIGHT + Math.min(160, contentHeight - TITLE_HEIGHT - SECTION_PADDING);

        for (EquipmentSlot slot : equipmentSlots.values()) {
            if (slot.getX() + 24 > equipSectionMaxX || slot.getY() + 24 > equipSectionMaxY) {
                XeenaaVillagerManager.LOGGER.warn("Equipment slot {} exceeds section bounds: {}+24 > {} or {}+24 > {}",
                    slot.getSlotType(), slot.getX(), equipSectionMaxX, slot.getY(), equipSectionMaxY);
            }
        }

        // Validate role buttons stay within role section
        int roleSectionX = contentX + EQUIPMENT_SECTION_WIDTH + SECTION_SPACING + SECTION_PADDING;
        int roleSectionMaxX = roleSectionX + ROLE_SECTION_WIDTH;

        for (RoleButton button : roleButtons.values()) {
            if (button.getX() + button.getWidth() > roleSectionMaxX) {
                XeenaaVillagerManager.LOGGER.warn("Role button exceeds section bounds: {}+{} > {}",
                    button.getX(), button.getWidth(), roleSectionMaxX);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check equipment slot clicks
        for (EquipmentSlot slot : equipmentSlots.values()) {
            if (slot.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Check role button clicks
        for (RoleButton roleButton : roleButtons.values()) {
            if (roleButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}