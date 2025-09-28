package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.config.ModConfig;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.network.SelectProfessionPacket;
import com.xeenaa.villagermanager.profession.ModProfessions;
import com.xeenaa.villagermanager.registry.ProfessionData;
import com.xeenaa.villagermanager.registry.ProfessionManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab for managing villager professions, providing a grid-based selection interface.
 *
 * <p>This tab migrates functionality from the original ProfessionSelectionScreen
 * and maintains the same user experience while fitting within the tabbed interface.</p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class ProfessionTab extends Tab {
    private static final int BUTTON_WIDTH = 115;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_SPACING = 5;
    private static final int COLUMNS = 3;
    private static final int MAX_VISIBLE_PROFESSIONS = 15; // 5 rows x 3 columns

    private final List<ProfessionButton> professionButtons = new ArrayList<>();
    private final List<ProfessionData> availableProfessions = new ArrayList<>();

    private ButtonWidget closeButton;

    /**
     * Creates a new profession tab for the specified villager.
     *
     * @param targetVillager the villager to manage professions for
     */
    public ProfessionTab(VillagerEntity targetVillager) {
        super(targetVillager);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("gui.xeenaa_villager_manager.tab.professions");
    }

    @Override
    public Text getTabTitle() {
        // Get current profession for context
        VillagerProfession currentProfession = targetVillager.getVillagerData().getProfession();
        Identifier professionId = net.minecraft.registry.Registries.VILLAGER_PROFESSION.getId(currentProfession);

        if (professionId != null) {
            // Create a contextual title with current profession
            String professionName;
            if ("minecraft".equals(professionId.getNamespace())) {
                // Vanilla profession - use simple name
                professionName = professionId.getPath();
            } else {
                // Modded profession - use display name
                professionName = professionId.getPath();
            }
            return Text.literal("Profession - " + capitalize(professionName));
        }
        return Text.literal("Profession Selection");
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean isAvailable() {
        // Profession tab is always available for valid villagers
        return targetVillager.isAlive() && !targetVillager.isBaby();
    }

    @Override
    protected void initializeContent() {
        loadAvailableProfessions();
        XeenaaVillagerManager.LOGGER.debug("Initialized ProfessionTab with {} professions",
            availableProfessions.size());
    }

    @Override
    protected void updateLayout() {
        // Clear existing buttons
        professionButtons.clear();

        // Recreate profession buttons with new layout
        createProfessionButtons();
        createCloseButton();

        XeenaaVillagerManager.LOGGER.debug("Updated ProfessionTab layout with {} buttons",
            professionButtons.size());
    }

    /**
     * Loads all available professions based on configuration and mod state, excluding the currently selected profession.
     */
    private void loadAvailableProfessions() {
        availableProfessions.clear();

        ProfessionManager manager = ProfessionManager.getInstance();
        ModConfig config = ModConfig.getInstance();

        // Get current profession of the villager
        VillagerProfession currentProfession = targetVillager.getVillagerData().getProfession();
        Identifier currentProfessionId = Registries.VILLAGER_PROFESSION.getId(currentProfession);

        for (ProfessionData professionData : manager.getAllProfessionData()) {
            // Skip blacklisted professions
            if (config.isProfessionBlacklisted(professionData.getId())) {
                continue;
            }

            // Skip Guard profession if disabled in config
            if (professionData.getId().toString().equals("xeenaa_villager_manager:guard")
                && !config.isGuardProfessionEnabled()) {
                continue;
            }

            // Skip currently selected profession
            if (professionData.getId().equals(currentProfessionId)) {
                continue;
            }

            availableProfessions.add(professionData);

            // Limit number of visible professions to fit UI
            if (availableProfessions.size() >= MAX_VISIBLE_PROFESSIONS) {
                break;
            }
        }

        // Log profession statistics
        ProfessionManager.ProfessionStats stats = manager.getStats();
        XeenaaVillagerManager.LOGGER.debug("Loaded {} available professions ({} total, {} vanilla, {} modded)",
            availableProfessions.size(), stats.total(), stats.vanilla(), stats.modded());
    }

    /**
     * Creates profession selection buttons in a grid layout.
     */
    private void createProfessionButtons() {
        int startX = contentX + 10; // Left margin
        int startY = contentY + 20; // Top margin for instructions
        int buttonCount = 0;
        int row = 0;
        int col = 0;

        for (ProfessionData professionData : availableProfessions) {
            // Calculate button position in grid
            int buttonX = startX + col * (BUTTON_WIDTH + BUTTON_SPACING);
            int buttonY = startY + row * (BUTTON_HEIGHT + BUTTON_SPACING);

            // Ensure button fits within content area
            if (buttonX + BUTTON_WIDTH > contentX + contentWidth ||
                buttonY + BUTTON_HEIGHT > contentY + contentHeight - 40) { // Reserve space for close button
                break;
            }

            ProfessionButton professionButton = new ProfessionButton(
                buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                professionData,
                button -> selectProfession(((ProfessionButton) button).getProfessionData())
            );

            professionButtons.add(professionButton);

            buttonCount++;
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Creates the close button at the bottom of the tab.
     */
    private void createCloseButton() {
        int closeButtonWidth = 70;
        int closeButtonHeight = 20;
        int closeButtonX = contentX + contentWidth - closeButtonWidth - 10;
        int closeButtonY = contentY + contentHeight - closeButtonHeight - 10;

        closeButton = ButtonWidget.builder(
            Text.translatable("gui.done"),
            button -> closeTab()
        ).dimensions(closeButtonX, closeButtonY, closeButtonWidth, closeButtonHeight)
         .build();
    }

    @Override
    public void onActivate() {
        super.onActivate();

        // Refresh professions in case configuration changed
        loadAvailableProfessions();
        updateLayout();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render instructions
        Text instructions = Text.translatable("gui.xeenaa_villager_manager.instructions.select_profession");
        context.drawText(getTextRenderer(), instructions,
            contentX + 10, contentY + 5, 0xFFAAAAA, true);

        // Render profession buttons
        for (ProfessionButton button : professionButtons) {
            button.render(context, mouseX, mouseY, delta);
        }

        // Render close button
        if (closeButton != null) {
            closeButton.render(context, mouseX, mouseY, delta);
        }

        // Render current villager profession info
        renderCurrentProfessionInfo(context);
    }

    /**
     * Renders information about the villager's current profession.
     */
    private void renderCurrentProfessionInfo(DrawContext context) {
        // Get current profession info
        VillagerProfession currentProfession = targetVillager.getVillagerData().getProfession();
        Identifier professionId = Registries.VILLAGER_PROFESSION.getId(currentProfession);

        // Get translated profession name using proper key generation
        Text professionName;
        if (professionId != null) {
            String translationKey;
            if ("minecraft".equals(professionId.getNamespace())) {
                // Vanilla profession
                translationKey = "entity.minecraft.villager." + professionId.getPath();
            } else {
                // Modded profession - use full namespaced identifier
                translationKey = "entity.minecraft.villager." + professionId.getNamespace() + "." + professionId.getPath();
            }
            Text translated = Text.translatable(translationKey);
            String translatedString = translated.getString();

            // If translation fails (shows raw key), use fallback
            if (translatedString.equals(translationKey) || translatedString.startsWith("entity.minecraft.villager.")) {
                // For Guard profession specifically, ensure we show "Guard" not raw key
                if ("xeenaa_villager_manager".equals(professionId.getNamespace()) && "guard".equals(professionId.getPath())) {
                    professionName = Text.literal("Guard");
                } else {
                    // Format the profession name nicely
                    String formattedName = capitalize(professionId.getPath().replace("_", " "));
                    professionName = Text.literal(formattedName);
                }
            } else {
                professionName = translated;
            }
        } else {
            professionName = Text.literal("Unknown");
        }

        Text currentProfessionText = Text.translatable("gui.xeenaa_villager_manager.current_profession", professionName);

        // Render at bottom left of content area
        int textY = contentY + contentHeight - 35;
        context.drawText(getTextRenderer(), currentProfessionText,
            contentX + 10, textY, 0xFFCCCC, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle profession button clicks
        for (ProfessionButton professionButton : professionButtons) {
            if (professionButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Handle close button click
        if (closeButton != null && closeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }

    /**
     * Handles profession selection when a button is clicked.
     *
     * @param professionData the selected profession data
     */
    private void selectProfession(ProfessionData professionData) {
        XeenaaVillagerManager.LOGGER.info("Player selected profession: {} for villager {}",
            professionData.getTranslatedName().getString(), targetVillager.getId());

        // Create and send the packet to the server
        SelectProfessionPacket packet = new SelectProfessionPacket(
            targetVillager.getId(),
            professionData.getId()
        );
        ClientPlayNetworking.send(packet);

        // Check if Guard profession was selected for workflow transition
        Identifier guardProfessionId = Registries.VILLAGER_PROFESSION.getId(ModProfessions.GUARD);
        if (professionData.getId().equals(guardProfessionId)) {
            XeenaaVillagerManager.LOGGER.info("Guard profession selected - refreshing tabs and switching to rank management");

            // Stay in the VillagerManagementScreen and refresh tabs to show RankTab
            if (parentScreen instanceof TabbedManagementScreen tabbedScreen) {
                // Schedule refresh after server sync delay
                scheduleTabRefreshAndSwitch(tabbedScreen);
            } else {
                // Fallback: close screen normally
                if (parentScreen != null) {
                    parentScreen.close();
                }
            }
        } else {
            // For non-guard professions, just close the screen normally
            if (parentScreen != null) {
                parentScreen.close();
            }
        }
    }


    /**
     * Schedules tab refresh and switch to RankTab after a brief delay to allow server processing
     */
    private void scheduleTabRefreshAndSwitch(TabbedManagementScreen tabbedScreen) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Schedule the tab refresh after server has time to process and sync guard data
        client.execute(() -> {
            // Wait a few ticks for server processing
            new Thread(() -> {
                try {
                    Thread.sleep(500); // 500ms delay for server processing

                    client.execute(() -> {
                        refreshTabsAndSwitchToRank(tabbedScreen);
                    });
                } catch (InterruptedException e) {
                    XeenaaVillagerManager.LOGGER.warn("Tab refresh and switch interrupted", e);
                }
            }).start();
        });
    }

    /**
     * Refreshes tabs and switches to RankTab for the newly created guard
     */
    private void refreshTabsAndSwitchToRank(TabbedManagementScreen tabbedScreen) {
        try {
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();

            // Check if guard data is available
            GuardData guardData = cache.getGuardData(targetVillager);

            if (guardData != null) {
                // Refresh tabs to include the new RankTab
                tabbedScreen.refreshTabs();

                // Switch to RankTab
                boolean switched = tabbedScreen.switchToTabByType(RankTab.class);
                if (switched) {
                    XeenaaVillagerManager.LOGGER.info("Successfully switched to RankTab for new guard via integrated workflow");
                } else {
                    XeenaaVillagerManager.LOGGER.warn("RankTab not available after refresh - may need to retry");
                }
            } else {
                XeenaaVillagerManager.LOGGER.warn("Guard data not yet available for workflow transition - guard data sync may be delayed");
                // Could retry here or show a temporary message
            }
        } catch (Exception e) {
            XeenaaVillagerManager.LOGGER.error("Failed to refresh tabs and switch to RankTab in workflow transition", e);
        }
    }

    /**
     * Handles the close button click.
     */
    private void closeTab() {
        XeenaaVillagerManager.LOGGER.debug("Close button clicked in ProfessionTab");

        if (parentScreen != null) {
            parentScreen.close();
        }
    }

    /**
     * Gets the list of profession buttons for testing purposes.
     *
     * @return immutable list of profession buttons
     */
    public List<ProfessionButton> getProfessionButtons() {
        return List.copyOf(professionButtons);
    }

    /**
     * Gets the list of available professions for testing purposes.
     *
     * @return immutable list of available profession data
     */
    public List<ProfessionData> getAvailableProfessions() {
        return List.copyOf(availableProfessions);
    }
}