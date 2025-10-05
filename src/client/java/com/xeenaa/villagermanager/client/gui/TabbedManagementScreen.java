package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for tabbed villager management screen providing tab navigation
 * and content switching functionality.
 *
 * <p>This screen follows Minecraft 1.21.1 GUI patterns and maintains clean
 * separation between tab logic and content rendering.</p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class TabbedManagementScreen extends Screen {
    private static final int BACKGROUND_WIDTH = 400;
    private static final int BACKGROUND_HEIGHT = 280;
    private static final int TAB_HEIGHT = 24;
    private static final int TAB_CONTENT_PADDING = 8;

    // Tab positioning constants
    private static final int TAB_WIDTH = 80;
    private static final int TAB_SPACING = 2;
    private static final int TABS_START_Y_OFFSET = -TAB_HEIGHT + 1; // Tabs overlap background by 1px

    // Color constants - Solid colors for clear rendering
    private static final int BACKGROUND_COLOR = 0xFF2C2C2C; // Solid dark gray
    private static final int BORDER_COLOR = 0xFFFFFFFF; // White
    private static final int INACTIVE_TAB_COLOR = 0xFF404040; // Medium gray
    private static final int ACTIVE_TAB_COLOR = BACKGROUND_COLOR;

    protected final VillagerEntity targetVillager;
    protected final List<Tab> tabs = new ArrayList<>();
    private final List<TabButton> tabButtons = new ArrayList<>();

    private Tab activeTab;
    private int backgroundX;
    private int backgroundY;
    private int contentAreaX;
    private int contentAreaY;
    private int contentAreaWidth;
    private int contentAreaHeight;

    /**
     * Creates a new tabbed management screen for the specified villager.
     *
     * @param villager the target villager, must not be null
     * @throws IllegalArgumentException if villager is null
     */
    public TabbedManagementScreen(VillagerEntity villager) {
        super(Text.translatable("gui.xeenaa_villager_manager.management"));
        this.targetVillager = Objects.requireNonNull(villager, "Villager cannot be null");

        XeenaaVillagerManager.LOGGER.debug("Created TabbedManagementScreen for villager {}",
            villager.getId());
    }

    @Override
    protected void init() {
        super.init();

        calculateLayout();
        initializeTabs();
        createTabButtons();

        // Set initial active tab to first tab if available
        if (!tabs.isEmpty() && activeTab == null) {
            switchToTab(tabs.get(0));
        }

        XeenaaVillagerManager.LOGGER.debug("Initialized TabbedManagementScreen with {} tabs",
            tabs.size());
    }

    /**
     * Calculates the layout positions for the screen components.
     */
    private void calculateLayout() {
        // Center the background
        backgroundX = (width - BACKGROUND_WIDTH) / 2;
        backgroundY = (height - BACKGROUND_HEIGHT) / 2;

        // Calculate content area (inside background with padding)
        contentAreaX = backgroundX + TAB_CONTENT_PADDING;
        contentAreaY = backgroundY + TAB_HEIGHT + TAB_CONTENT_PADDING;
        contentAreaWidth = BACKGROUND_WIDTH - (TAB_CONTENT_PADDING * 2);
        contentAreaHeight = BACKGROUND_HEIGHT - TAB_HEIGHT - (TAB_CONTENT_PADDING * 2);
    }

    /**
     * Initializes the available tabs based on villager state.
     * Subclasses should override to add specific tabs.
     */
    protected void initializeTabs() {
        // Default implementation - subclasses will add tabs
        tabs.clear();
    }

    /**
     * Creates tab buttons for all registered tabs.
     */
    private void createTabButtons() {
        tabButtons.clear();
        clearChildren();

        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);

            int tabX = backgroundX + (i * (TAB_WIDTH + TAB_SPACING));
            int tabY = backgroundY + TABS_START_Y_OFFSET;

            TabButton tabButton = new TabButton(
                tabX, tabY, TAB_WIDTH, TAB_HEIGHT,
                tab.getDisplayName(),
                button -> switchToTab(tab),
                tab == activeTab
            );

            tabButtons.add(tabButton);
            addDrawableChild(tabButton);
        }

        // Initialize active tab content
        if (activeTab != null) {
            activeTab.init(this, contentAreaX, contentAreaY, contentAreaWidth, contentAreaHeight);
        }
    }

    /**
     * Switches to the specified tab and updates the display.
     *
     * @param tab the tab to switch to, must not be null
     */
    protected void switchToTab(Tab tab) {
        Objects.requireNonNull(tab, "Tab cannot be null");

        if (activeTab == tab) {
            return; // Already active
        }

        XeenaaVillagerManager.LOGGER.debug("Switching to tab: {}", tab.getDisplayName().getString());

        // Clean up previous tab
        if (activeTab != null) {
            activeTab.onDeactivate();
        }

        // Set new active tab
        activeTab = tab;

        // Update tab button states
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButton button = tabButtons.get(i);
            boolean isActive = tabs.get(i) == activeTab;
            button.setActive(isActive);
        }

        // Initialize new tab
        activeTab.init(this, contentAreaX, contentAreaY, contentAreaWidth, contentAreaHeight);
        activeTab.onActivate();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Custom background rendering without blur shader
        // Create a simple dark overlay without using Minecraft's blur effect
        context.fill(0, 0, this.width, this.height, 0x66000000); // Semi-transparent dark overlay
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render our custom background (no blur)
        this.renderBackground(context, mouseX, mouseY, delta);

        // Render main background panel
        context.fill(backgroundX, backgroundY,
            backgroundX + BACKGROUND_WIDTH, backgroundY + BACKGROUND_HEIGHT,
            BACKGROUND_COLOR);

        // Render background border
        context.drawBorder(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
            BORDER_COLOR);

        // Render tab backgrounds
        renderTabBackgrounds(context);

        // Render widgets (tab buttons)
        super.render(context, mouseX, mouseY, delta);

        // Render active tab content
        if (activeTab != null) {
            activeTab.render(context, mouseX, mouseY, delta);
        }

        // Render title on top
        renderTitle(context);
    }

    /**
     * Renders the tab backgrounds to create the tabbed appearance.
     */
    private void renderTabBackgrounds(DrawContext context) {
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            boolean isActive = tab == activeTab;

            int tabX = backgroundX + (i * (TAB_WIDTH + TAB_SPACING));
            int tabY = backgroundY + TABS_START_Y_OFFSET;

            int tabColor = isActive ? ACTIVE_TAB_COLOR : INACTIVE_TAB_COLOR;

            // Fill tab background
            context.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, tabColor);

            // Draw tab borders (except bottom for active tab)
            context.drawHorizontalLine(tabX, tabX + TAB_WIDTH - 1, tabY, BORDER_COLOR); // Top
            context.drawVerticalLine(tabX, tabY, tabY + TAB_HEIGHT - 1, BORDER_COLOR); // Left
            context.drawVerticalLine(tabX + TAB_WIDTH - 1, tabY, tabY + TAB_HEIGHT - 1, BORDER_COLOR); // Right

            if (!isActive) {
                context.drawHorizontalLine(tabX, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT - 1, BORDER_COLOR); // Bottom
            }
        }
    }

    /**
     * Renders the screen title.
     */
    private void renderTitle(DrawContext context) {
        Text title = getCurrentTitle();
        int titleWidth = textRenderer.getWidth(title);
        int titleX = backgroundX + (BACKGROUND_WIDTH - titleWidth) / 2;
        int titleY = backgroundY + 8;

        context.drawText(textRenderer, title, titleX, titleY, 0xFFFFFF, true);
    }

    /**
     * Gets the current title to display, incorporating active tab information.
     *
     * @return the title text for the current state
     */
    private Text getCurrentTitle() {
        if (activeTab != null) {
            return activeTab.getTabTitle();
        }
        return getTitle(); // Fallback to default screen title
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle tab content mouse clicks first
        if (activeTab != null && activeTab.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Handle tab button clicks
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Forward mouse drag events to active tab
        if (activeTab != null && activeTab.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Forward mouse release events to active tab
        if (activeTab != null && activeTab.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false; // Keep game running for multiplayer compatibility
    }

    @Override
    public void close() {
        if (activeTab != null) {
            activeTab.onDeactivate();
        }

        XeenaaVillagerManager.LOGGER.debug("Closing TabbedManagementScreen");
        super.close();
    }

    // Getters for subclass access

    /**
     * Gets the target villager for this management screen.
     *
     * @return the target villager, never null
     */
    public VillagerEntity getTargetVillager() {
        return targetVillager;
    }

    /**
     * Gets the currently active tab.
     *
     * @return the active tab, or null if no tabs are active
     */
    public Tab getActiveTab() {
        return activeTab;
    }

    /**
     * Refreshes the currently active tab to update its content.
     * Used when underlying data changes and GUI needs to update without tab switching.
     */
    public void refreshActiveTab() {
        if (activeTab != null) {
            activeTab.refresh();
        }
    }

    /**
     * Adds a tab to the screen. Should be called during {@link #initializeTabs()}.
     *
     * @param tab the tab to add, must not be null
     */
    protected void addTab(Tab tab) {
        Objects.requireNonNull(tab, "Tab cannot be null");
        tabs.add(tab);
    }

    /**
     * Refreshes all tabs, re-evaluating availability and reinitializing the tab list.
     */
    public void refreshTabs() {
        Tab previousActiveTab = activeTab;
        String previousActiveTabName = previousActiveTab != null ?
            previousActiveTab.getClass().getSimpleName() : null;

        // Clear existing tabs
        if (activeTab != null) {
            activeTab.onDeactivate();
        }
        tabs.clear();
        tabButtons.clear();
        clearChildren(); // Clear existing UI widgets
        activeTab = null;

        // Reinitialize tabs
        initializeTabs();
        calculateLayout();
        createTabButtons(); // Create new tab buttons after refreshing tabs

        // Try to restore previous active tab or switch to a specific tab
        Tab tabToActivate = null;
        if (previousActiveTabName != null) {
            tabToActivate = tabs.stream()
                .filter(tab -> tab.getClass().getSimpleName().equals(previousActiveTabName))
                .findFirst()
                .orElse(null);
        }

        // If previous tab not found or not available, activate first tab
        if (tabToActivate == null && !tabs.isEmpty()) {
            tabToActivate = tabs.get(0);
        }

        if (tabToActivate != null) {
            switchToTab(tabToActivate);
        }
    }

    /**
     * Switches to a tab by its class type.
     */
    public boolean switchToTabByType(Class<? extends Tab> tabClass) {
        Tab targetTab = tabs.stream()
            .filter(tab -> tabClass.isInstance(tab))
            .findFirst()
            .orElse(null);

        if (targetTab != null) {
            switchToTab(targetTab);
            return true;
        }
        return false;
    }

    /**
     * Gets the content area bounds for tab content rendering.
     *
     * @return array containing [x, y, width, height] of content area
     */
    public int[] getContentBounds() {
        return new int[]{contentAreaX, contentAreaY, contentAreaWidth, contentAreaHeight};
    }
}