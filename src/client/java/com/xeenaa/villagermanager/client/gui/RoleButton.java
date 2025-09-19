package com.xeenaa.villagermanager.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * A specialized button widget for selecting Guard villager roles.
 *
 * <p>This widget represents one of the three available roles for Guard villagers:
 * Patrol, Guard (stationary), or Follow. Only one role can be selected at a time,
 * and the button provides visual feedback for the selected state.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Toggle-style button with selected/unselected states</li>
 *   <li>Role-specific icons and descriptions</li>
 *   <li>Tooltip showing role details and requirements</li>
 *   <li>Visual distinction for active/inactive states</li>
 * </ul>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class RoleButton extends ButtonWidget {

    // Button styling constants
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 26;
    private static final int SELECTED_BORDER_COLOR = 0xFF00CC00;
    private static final int UNSELECTED_BORDER_COLOR = 0xFF666666;
    private static final int SELECTED_BACKGROUND_COLOR = 0xFF0A5A0A;
    private static final int UNSELECTED_BACKGROUND_COLOR = 0xFF2A2A2A;
    private static final int HOVER_BACKGROUND_COLOR = 0xFF404040;
    private static final int HOVER_SELECTED_BACKGROUND_COLOR = 0xFF0F6F0F;
    private static final int SELECTED_GLOW_COLOR = 0x4000FF00;
    private static final int ICON_SIZE = 14;

    /**
     * Enumeration of available Guard roles.
     */
    public enum GuardRole {
        PATROL("patrol", "gui.xeenaa_villager_manager.role.patrol.name",
               "gui.xeenaa_villager_manager.role.patrol.description"),
        GUARD("guard", "gui.xeenaa_villager_manager.role.guard.name",
              "gui.xeenaa_villager_manager.role.guard.description"),
        FOLLOW("follow", "gui.xeenaa_villager_manager.role.follow.name",
               "gui.xeenaa_villager_manager.role.follow.description");

        private final String id;
        private final String nameKey;
        private final String descriptionKey;

        GuardRole(String id, String nameKey, String descriptionKey) {
            this.id = id;
            this.nameKey = nameKey;
            this.descriptionKey = descriptionKey;
        }

        public String getId() {
            return id;
        }

        public Text getName() {
            return Text.translatable(nameKey);
        }

        public Text getDescription() {
            return Text.translatable(descriptionKey);
        }
    }

    private final GuardRole role;
    private boolean selected;

    /**
     * Creates a new role selection button.
     *
     * @param x the x position of the button
     * @param y the y position of the button
     * @param role the role this button represents
     * @param onPress the action to perform when clicked
     */
    public RoleButton(int x, int y, GuardRole role, PressAction onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, role.getName(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.role = java.util.Objects.requireNonNull(role, "Role cannot be null");
        this.selected = false;

        // Set tooltip with role description
        setTooltip(Tooltip.of(role.getDescription()));
    }

    /**
     * Sets whether this role button is selected.
     *
     * @param selected true if this role is currently selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Checks if this role button is currently selected.
     *
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Gets the role represented by this button.
     *
     * @return the guard role
     */
    public GuardRole getRole() {
        return role;
    }

    /**
     * Sets the position of this role button.
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
        // Determine colors based on selection and hover state
        int backgroundColor;
        int borderColor;
        int textColor;
        int iconColor;

        if (selected) {
            backgroundColor = this.isHovered() ? HOVER_SELECTED_BACKGROUND_COLOR : SELECTED_BACKGROUND_COLOR;
            borderColor = SELECTED_BORDER_COLOR;
            textColor = 0xFFFFFF;
            iconColor = 0xFF00FF00;
        } else {
            backgroundColor = this.isHovered() ? HOVER_BACKGROUND_COLOR : UNSELECTED_BACKGROUND_COLOR;
            borderColor = this.isHovered() ? 0xFF888888 : UNSELECTED_BORDER_COLOR;
            textColor = this.active ? 0xFFFFFF : 0xFFA0A0A0;
            iconColor = this.isHovered() ? 0xFFBBBBBB : 0xFF888888;
        }

        // Render button background with gradient effect
        context.fill(getX(), getY(), getX() + width, getY() + height, backgroundColor);

        // Add selected state glow effect
        if (selected) {
            context.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), SELECTED_GLOW_COLOR);
            context.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, SELECTED_GLOW_COLOR);
            context.fill(getX() - 1, getY(), getX(), getY() + height, SELECTED_GLOW_COLOR);
            context.fill(getX() + width, getY(), getX() + width + 1, getY() + height, SELECTED_GLOW_COLOR);
        }

        // Render button border with double border for selected state
        context.drawBorder(getX(), getY(), width, height, borderColor);
        if (selected) {
            context.drawBorder(getX() + 1, getY() + 1, width - 2, height - 2, 0x60FFFFFF);
        } else if (this.isHovered()) {
            context.drawBorder(getX() + 1, getY() + 1, width - 2, height - 2, 0x30FFFFFF);
        }

        // Add subtle depth effect
        if (!selected) {
            context.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + 2, 0x20FFFFFF);
            context.fill(getX() + 1, getY() + 1, getX() + 2, getY() + height - 1, 0x20FFFFFF);
        }

        // Render role icon
        renderRoleIcon(context, iconColor);

        // Render role name text
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text roleName = role.getName();

        // Calculate text position (centered, with space for icon)
        int textX = getX() + 22; // Leave space for larger icon
        int textY = getY() + (height - textRenderer.fontHeight) / 2;
        int maxTextWidth = width - 27; // Account for icon and padding

        // Truncate text if necessary
        String displayText = roleName.getString();
        if (textRenderer.getWidth(displayText) > maxTextWidth) {
            displayText = textRenderer.trimToWidth(displayText, maxTextWidth - textRenderer.getWidth("...")) + "...";
        }

        // Render text with shadow for better readability
        context.drawText(textRenderer, displayText, textX, textY, textColor, true);
    }

    /**
     * Renders the role-specific icon.
     */
    private void renderRoleIcon(DrawContext context, int iconColor) {
        int iconX = getX() + 5;
        int iconY = getY() + (height - ICON_SIZE) / 2;

        // Draw role-specific icon with enhanced design
        switch (role) {
            case PATROL:
                // Draw patrol icon (circular arrow)
                renderPatrolIcon(context, iconX, iconY, iconColor);
                break;
            case GUARD:
                // Draw guard icon (stationary post)
                renderGuardIcon(context, iconX, iconY, iconColor);
                break;
            case FOLLOW:
                // Draw follow icon (arrow pointing right)
                renderFollowIcon(context, iconX, iconY, iconColor);
                break;
        }
    }

    /**
     * Renders the patrol icon (circular movement pattern).
     */
    private void renderPatrolIcon(DrawContext context, int x, int y, int color) {
        // Draw a circular path with enhanced arrows
        // Outer circle
        context.drawBorder(x + 1, y + 1, 12, 12, color);
        context.drawBorder(x + 2, y + 2, 10, 10, 0x40FFFFFF);

        // Direction arrows showing movement
        context.fill(x + 10, y + 3, x + 12, y + 4, color); // Right arrow tip
        context.fill(x + 9, y + 4, x + 11, y + 5, color); // Right arrow body
        context.fill(x + 6, y + 10, x + 7, y + 12, color); // Down arrow tip
        context.fill(x + 5, y + 9, x + 8, y + 10, color); // Down arrow body
    }

    /**
     * Renders the guard icon (stationary position marker).
     */
    private void renderGuardIcon(DrawContext context, int x, int y, int color) {
        // Draw an enhanced guard post with flag
        // Vertical post
        context.fill(x + 6, y + 2, x + 8, y + 11, color);
        context.fill(x + 7, y + 3, x + 8, y + 10, 0x40FFFFFF); // Highlight

        // Base platform
        context.fill(x + 3, y + 10, x + 11, y + 12, color);
        context.fill(x + 4, y + 11, x + 10, y + 12, 0x40FFFFFF); // Base highlight

        // Flag at top
        context.fill(x + 8, y + 2, x + 12, y + 5, color);
        context.fill(x + 9, y + 3, x + 11, y + 4, 0x40FFFFFF); // Flag highlight
    }

    /**
     * Renders the follow icon (directional arrow).
     */
    private void renderFollowIcon(DrawContext context, int x, int y, int color) {
        // Draw an enhanced follow arrow with movement indication
        // Arrow shaft
        context.fill(x + 2, y + 6, x + 9, y + 8, color);
        context.fill(x + 3, y + 7, x + 8, y + 8, 0x40FFFFFF); // Shaft highlight

        // Arrow head
        context.fill(x + 8, y + 4, x + 10, y + 6, color);
        context.fill(x + 8, y + 8, x + 10, y + 10, color);
        context.fill(x + 10, y + 6, x + 12, y + 8, color); // Arrow tip

        // Movement trails
        context.fill(x + 1, y + 5, x + 2, y + 6, 0x60000000 | (color & 0x00FFFFFF));
        context.fill(x + 1, y + 9, x + 2, y + 10, 0x60000000 | (color & 0x00FFFFFF));
    }

    @Override
    public void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        // Add accessibility narration
        Text narrationText = Text.translatable("gui.xeenaa_villager_manager.role_button_narration",
            role.getName(), selected ? Text.translatable("gui.xeenaa_villager_manager.selected") :
                                     Text.translatable("gui.xeenaa_villager_manager.unselected"));
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, narrationText);
    }
}