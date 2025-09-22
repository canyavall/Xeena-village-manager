package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.client.network.GuardDataSyncHandler;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.network.EquipGuardPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
     * Loads the current equipment state from the server data cache.
     */
    private void loadEquipmentState() {
        // Get cached guard data from server sync
        com.xeenaa.villagermanager.client.data.ClientGuardDataCache cache =
            com.xeenaa.villagermanager.client.data.ClientGuardDataCache.getInstance();
        GuardData cachedData = cache.getGuardData(targetVillager.getUuid());

        if (cachedData != null) {
            // Load equipment from cached data
            for (Map.Entry<GuardData.EquipmentSlot, ItemStack> entry : cachedData.getAllEquipment().entrySet()) {
                EquipmentSlot.SlotType slotType = convertToGuiSlotType(entry.getKey());
                if (slotType != null) {
                    equipmentSlots.get(slotType).setItem(entry.getValue());
                }
            }

            // Update role selection
            updateRoleFromServerData(cachedData.getRole());

            XeenaaVillagerManager.LOGGER.debug("Loaded equipment state from server cache for villager {}",
                targetVillager.getUuid());
        } else {
            // No cached data, request from server
            requestEquipmentDataFromServer();

            // Set empty equipment as placeholder
            for (EquipmentSlot.SlotType slotType : EquipmentSlot.SlotType.values()) {
                equipmentSlots.get(slotType).setItem(ItemStack.EMPTY);
            }

            XeenaaVillagerManager.LOGGER.debug("No cached equipment data, requesting from server for villager {}",
                targetVillager.getUuid());
        }
    }

    /**
     * Converts GuardData.EquipmentSlot to GUI EquipmentSlot.SlotType
     */
    private EquipmentSlot.SlotType convertToGuiSlotType(GuardData.EquipmentSlot dataSlot) {
        switch (dataSlot) {
            case WEAPON: return EquipmentSlot.SlotType.WEAPON;
            case HELMET: return EquipmentSlot.SlotType.HELMET;
            case CHESTPLATE: return EquipmentSlot.SlotType.CHESTPLATE;
            case LEGGINGS: return EquipmentSlot.SlotType.LEGGINGS;
            case BOOTS: return EquipmentSlot.SlotType.BOOTS;
            case SHIELD: return EquipmentSlot.SlotType.SHIELD;
            default: return null;
        }
    }

    /**
     * Converts GUI EquipmentSlot.SlotType to GuardData.EquipmentSlot
     */
    private GuardData.EquipmentSlot convertToDataSlotType(EquipmentSlot.SlotType guiSlot) {
        switch (guiSlot) {
            case WEAPON: return GuardData.EquipmentSlot.WEAPON;
            case HELMET: return GuardData.EquipmentSlot.HELMET;
            case CHESTPLATE: return GuardData.EquipmentSlot.CHESTPLATE;
            case LEGGINGS: return GuardData.EquipmentSlot.LEGGINGS;
            case BOOTS: return GuardData.EquipmentSlot.BOOTS;
            case SHIELD: return GuardData.EquipmentSlot.SHIELD;
            default: return null;
        }
    }

    /**
     * Updates role selection from server data
     */
    private void updateRoleFromServerData(GuardData.GuardRole serverRole) {
        RoleButton.GuardRole guiRole = convertToGuiRole(serverRole);
        if (guiRole != null) {
            updateRoleSelection(guiRole);
        }
    }

    /**
     * Converts GuardData.GuardRole to GUI RoleButton.GuardRole
     */
    private RoleButton.GuardRole convertToGuiRole(GuardData.GuardRole dataRole) {
        switch (dataRole) {
            case PATROL: return RoleButton.GuardRole.PATROL;
            case GUARD: return RoleButton.GuardRole.GUARD;
            case FOLLOW: return RoleButton.GuardRole.FOLLOW;
            default: return RoleButton.GuardRole.GUARD;
        }
    }

    /**
     * Converts GUI RoleButton.GuardRole to GuardData.GuardRole
     */
    private GuardData.GuardRole convertToDataRole(RoleButton.GuardRole guiRole) {
        switch (guiRole) {
            case PATROL: return GuardData.GuardRole.PATROL;
            case GUARD: return GuardData.GuardRole.GUARD;
            case FOLLOW: return GuardData.GuardRole.FOLLOW;
            default: return GuardData.GuardRole.GUARD;
        }
    }

    /**
     * Requests equipment data from server (placeholder for future implementation)
     */
    private void requestEquipmentDataFromServer() {
        // TODO: Implement equipment data request packet
        // For now, this could be part of the initial profession selection packet
        XeenaaVillagerManager.LOGGER.debug("Equipment data request from server not yet implemented");
    }

    /**
     * Handles equipment slot click events.
     */
    private void handleEquipmentSlotClick(EquipmentSlot slot) {
        XeenaaVillagerManager.LOGGER.debug("Equipment slot clicked: {}", slot.getSlotType());

        // Convert GUI slot type to data slot type
        GuardData.EquipmentSlot dataSlot = convertToDataSlotType(slot.getSlotType());
        if (dataSlot == null) {
            XeenaaVillagerManager.LOGGER.warn("Failed to convert slot type: {}", slot.getSlotType());
            return;
        }

        // For now, cycle through example items for demonstration
        // In a full implementation, this would open an inventory GUI or handle drag-and-drop
        ItemStack newEquipment = getNextExampleItem(slot);

        // Send equipment change to server
        sendEquipmentChangeToServer(dataSlot, newEquipment);

        // Update local display immediately for responsiveness (will be overridden by server response)
        slot.setItem(newEquipment);

        XeenaaVillagerManager.LOGGER.debug("Sent equipment change to server: {} -> {}",
            dataSlot, newEquipment);
    }

    /**
     * Sends equipment change packet to server
     */
    private void sendEquipmentChangeToServer(GuardData.EquipmentSlot slot, ItemStack equipment) {
        EquipGuardPacket packet = new EquipGuardPacket(
            targetVillager.getUuid(),
            slot,
            equipment
        );

        ClientPlayNetworking.send(packet);
    }

    /**
     * Gets the next example item for demonstration purposes.
     */
    private ItemStack getNextExampleItem(EquipmentSlot slot) {
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
                } else {
                    newItem = ItemStack.EMPTY; // Cycle back to empty
                }
                break;
            case HELMET:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.LEATHER_HELMET);
                } else if (currentItem.isOf(Items.LEATHER_HELMET)) {
                    newItem = new ItemStack(Items.IRON_HELMET);
                } else if (currentItem.isOf(Items.IRON_HELMET)) {
                    newItem = new ItemStack(Items.DIAMOND_HELMET);
                } else {
                    newItem = ItemStack.EMPTY; // Cycle back to empty
                }
                break;
            case CHESTPLATE:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.LEATHER_CHESTPLATE);
                } else if (currentItem.isOf(Items.LEATHER_CHESTPLATE)) {
                    newItem = new ItemStack(Items.IRON_CHESTPLATE);
                } else if (currentItem.isOf(Items.IRON_CHESTPLATE)) {
                    newItem = new ItemStack(Items.DIAMOND_CHESTPLATE);
                } else {
                    newItem = ItemStack.EMPTY;
                }
                break;
            case LEGGINGS:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.LEATHER_LEGGINGS);
                } else if (currentItem.isOf(Items.LEATHER_LEGGINGS)) {
                    newItem = new ItemStack(Items.IRON_LEGGINGS);
                } else if (currentItem.isOf(Items.IRON_LEGGINGS)) {
                    newItem = new ItemStack(Items.DIAMOND_LEGGINGS);
                } else {
                    newItem = ItemStack.EMPTY;
                }
                break;
            case BOOTS:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.LEATHER_BOOTS);
                } else if (currentItem.isOf(Items.LEATHER_BOOTS)) {
                    newItem = new ItemStack(Items.IRON_BOOTS);
                } else if (currentItem.isOf(Items.IRON_BOOTS)) {
                    newItem = new ItemStack(Items.DIAMOND_BOOTS);
                } else {
                    newItem = ItemStack.EMPTY;
                }
                break;
            case SHIELD:
                if (currentItem.isEmpty()) {
                    newItem = new ItemStack(Items.SHIELD);
                } else {
                    newItem = ItemStack.EMPTY; // Only one shield type
                }
                break;
        }

        return newItem;
    }

    /**
     * Handles role selection.
     */
    private void handleRoleSelection(RoleButton.GuardRole role) {
        if (role != currentRole) {
            updateRoleSelection(role);

            XeenaaVillagerManager.LOGGER.debug("Role changed to: {}", role);

            // Send role change to server
            // We'll use the same EquipGuardPacket for role changes by sending it with a special null equipment
            // In the future, this could be a dedicated RoleChangePacket
            sendRoleChangeToServer(role);
        }
    }

    /**
     * Sends role change to server (placeholder - would be better with dedicated packet)
     */
    private void sendRoleChangeToServer(RoleButton.GuardRole role) {
        // TODO: Implement dedicated role change packet
        // For now, log the action
        XeenaaVillagerManager.LOGGER.debug("Role change to {} would be sent to server for villager {}",
            role, targetVillager.getUuid());
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