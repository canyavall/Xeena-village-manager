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
     * Loads all available professions based on configuration and mod state.
     */
    private void loadAvailableProfessions() {
        availableProfessions.clear();

        ProfessionManager manager = ProfessionManager.getInstance();
        ModConfig config = ModConfig.getInstance();

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
        String currentProfessionKey = targetVillager.getVillagerData().getProfession().toString();
        Text currentProfessionText = Text.translatable("gui.xeenaa_villager_manager.current_profession",
            Text.translatable("entity.minecraft.villager." + currentProfessionKey.replace(":", ".")));

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
            XeenaaVillagerManager.LOGGER.info("Guard profession selected - initiating workflow transition to rank management");

            // Close profession screen and schedule rank screen opening
            if (parentScreen != null) {
                parentScreen.close();
                scheduleGuardRankScreenOpening();
            }
        } else {
            // For non-guard professions, just close the screen normally
            if (parentScreen != null) {
                parentScreen.close();
            }
        }
    }

    /**
     * Schedules the opening of the GuardRankScreen after a brief delay to allow server processing
     */
    private void scheduleGuardRankScreenOpening() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Schedule the rank screen opening after server has time to process and sync guard data
        client.execute(() -> {
            // Wait a few ticks for server processing
            new Thread(() -> {
                try {
                    Thread.sleep(500); // 500ms delay for server processing

                    client.execute(() -> {
                        openGuardRankScreen();
                    });
                } catch (InterruptedException e) {
                    XeenaaVillagerManager.LOGGER.warn("Guard rank screen opening interrupted", e);
                }
            }).start();
        });
    }

    /**
     * Opens the GuardRankScreen for the newly created guard
     */
    private void openGuardRankScreen() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();

            // Try to get guard data from cache
            GuardData guardData = cache.getGuardData(targetVillager);

            if (guardData != null) {
                GuardRankScreen rankScreen = new GuardRankScreen(targetVillager, guardData.getRankData());
                client.setScreen(rankScreen);
                XeenaaVillagerManager.LOGGER.info("Successfully opened GuardRankScreen for new guard via workflow transition");
            } else {
                XeenaaVillagerManager.LOGGER.warn("Guard data not yet available for workflow transition - guard data sync may be delayed");
                // Could retry here or show a temporary message
            }
        } catch (Exception e) {
            XeenaaVillagerManager.LOGGER.error("Failed to open GuardRankScreen in workflow transition", e);
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