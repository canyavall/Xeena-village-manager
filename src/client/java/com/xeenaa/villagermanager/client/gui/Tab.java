package com.xeenaa.villagermanager.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;

/**
 * Abstract base class for tab content in the villager management interface.
 *
 * <p>Each tab represents a specific aspect of villager management (professions,
 * equipment, etc.) and handles its own content rendering and interaction logic.</p>
 *
 * <p><strong>Lifecycle:</strong></p>
 * <ol>
 *   <li>{@link #init(Screen, int, int, int, int)} - Initialize tab with content area bounds</li>
 *   <li>{@link #onActivate()} - Called when tab becomes active</li>
 *   <li>{@link #render(DrawContext, int, int, float)} - Render tab content each frame</li>
 *   <li>{@link #onDeactivate()} - Called when tab becomes inactive</li>
 * </ol>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public abstract class Tab {

    protected Screen parentScreen;
    protected VillagerEntity targetVillager;

    // Content area bounds
    protected int contentX;
    protected int contentY;
    protected int contentWidth;
    protected int contentHeight;

    private boolean initialized = false;
    private boolean active = false;

    /**
     * Creates a new tab for the specified villager.
     *
     * @param targetVillager the villager this tab manages, must not be null
     */
    protected Tab(VillagerEntity targetVillager) {
        this.targetVillager = java.util.Objects.requireNonNull(targetVillager, "Target villager cannot be null");
    }

    /**
     * Gets the display name for this tab.
     *
     * @return the text to display on the tab button
     */
    public abstract Text getDisplayName();

    /**
     * Checks if this tab is available for the current villager.
     * For example, equipment tabs might only be available for Guard villagers.
     *
     * @return true if this tab should be shown, false otherwise
     */
    public abstract boolean isAvailable();

    /**
     * Initializes the tab with the content area bounds.
     * This is called when the tab is first created or when the screen is resized.
     *
     * @param parentScreen the parent screen containing this tab
     * @param contentX the x position of the content area
     * @param contentY the y position of the content area
     * @param contentWidth the width of the content area
     * @param contentHeight the height of the content area
     */
    public final void init(Screen parentScreen, int contentX, int contentY, int contentWidth, int contentHeight) {
        this.parentScreen = parentScreen;
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;

        if (!initialized) {
            initializeContent();
            initialized = true;
        } else {
            updateLayout();
        }
    }

    /**
     * Called once to initialize tab-specific content and widgets.
     * Subclasses should override this to set up their initial state.
     */
    protected abstract void initializeContent();

    /**
     * Called when the content area bounds change (e.g., screen resize).
     * Subclasses should override this to reposition their widgets.
     */
    protected abstract void updateLayout();

    /**
     * Called when this tab becomes the active tab.
     * Subclasses can override to perform activation logic.
     */
    public void onActivate() {
        this.active = true;
    }

    /**
     * Called when this tab is no longer the active tab.
     * Subclasses can override to perform cleanup logic.
     */
    public void onDeactivate() {
        this.active = false;
    }

    /**
     * Renders the tab content.
     *
     * @param context the drawing context
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param delta the frame delta time
     */
    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     * Handles mouse click events within the tab content area.
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param button the mouse button that was clicked
     * @return true if the click was handled, false otherwise
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Default implementation - subclasses can override
        return false;
    }

    /**
     * Handles mouse scroll events within the tab content area.
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param horizontalAmount the horizontal scroll amount
     * @param verticalAmount the vertical scroll amount
     * @return true if the scroll was handled, false otherwise
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Default implementation - subclasses can override
        return false;
    }

    /**
     * Handles key press events when this tab is active.
     *
     * @param keyCode the key code
     * @param scanCode the scan code
     * @param modifiers the key modifiers
     * @return true if the key press was handled, false otherwise
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Default implementation - subclasses can override
        return false;
    }

    /**
     * Handles character typed events when this tab is active.
     *
     * @param chr the character that was typed
     * @param modifiers the key modifiers
     * @return true if the character input was handled, false otherwise
     */
    public boolean charTyped(char chr, int modifiers) {
        // Default implementation - subclasses can override
        return false;
    }

    // Getters for subclass access

    /**
     * Gets the target villager for this tab.
     *
     * @return the target villager, never null
     */
    public final VillagerEntity getTargetVillager() {
        return targetVillager;
    }

    /**
     * Gets the parent screen containing this tab.
     *
     * @return the parent screen, or null if not yet initialized
     */
    public final Screen getParentScreen() {
        return parentScreen;
    }

    /**
     * Checks if this tab has been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if this tab is currently active.
     *
     * @return true if active, false otherwise
     */
    public final boolean isActive() {
        return active;
    }

    /**
     * Gets the content area bounds.
     *
     * @return array containing [x, y, width, height] of content area
     */
    public final int[] getContentBounds() {
        return new int[]{contentX, contentY, contentWidth, contentHeight};
    }

    /**
     * Checks if the given coordinates are within the content area.
     *
     * @param x the x coordinate to check
     * @param y the y coordinate to check
     * @return true if the coordinates are within the content area
     */
    protected final boolean isWithinContentArea(double x, double y) {
        return x >= contentX && x < contentX + contentWidth &&
               y >= contentY && y < contentY + contentHeight;
    }

    /**
     * Gets the text renderer instance for text rendering.
     *
     * @return the text renderer
     */
    protected final TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }
}