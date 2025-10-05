package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.config.GuardBehaviorConfig;
import com.xeenaa.villagermanager.config.GuardMode;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.network.GuardConfigPacket;
import com.xeenaa.villagermanager.profession.ModProfessions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;

/**
 * Configuration tab for guard behavior settings.
 *
 * <p>Allows players to configure individual guard behavior including detection range,
 * guard mode, and profession locking.</p>
 *
 * @since 1.0.0
 */
public class ConfigTab extends Tab {

    private GuardBehaviorConfig currentConfig;
    private GuardBehaviorConfig originalConfig;
    private boolean hasLoadedFromCache = false;

    // Widgets
    private DetectionRangeSlider detectionSlider;
    private CyclingButtonWidget<GuardMode> guardModeButton;
    private CyclingButtonWidget<Boolean> professionLockButton;
    private ButtonWidget saveButton;
    private ButtonWidget resetButton;

    public ConfigTab(VillagerEntity targetVillager) {
        super(targetVillager);
        loadCurrentConfig();
    }

    /**
     * Loads the current configuration from CLIENT-SIDE cached guard data.
     * The cache is synchronized from the server, so we never access GuardDataManager on client.
     */
    private void loadCurrentConfig() {
        // Use client-side cache instead of accessing server-only GuardDataManager
        ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
        GuardData guardData = cache.getGuardData(targetVillager);

        if (guardData != null) {
            this.originalConfig = guardData.getBehaviorConfig();
            this.currentConfig = guardData.getBehaviorConfig();
        } else {
            // Fallback to default if not yet synced from server
            this.originalConfig = GuardBehaviorConfig.DEFAULT;
            this.currentConfig = GuardBehaviorConfig.DEFAULT;
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Config");
    }

    @Override
    public Text getTabTitle() {
        return Text.literal("Guard Configuration");
    }

    @Override
    public boolean isAvailable() {
        return targetVillager != null &&
               !targetVillager.isBaby() &&
               targetVillager.getVillagerData().getProfession() == ModProfessions.GUARD;
    }

    @Override
    protected void initializeContent() {
        // Create widgets during initial tab setup
        // This ensures widgets exist even if server data hasn't arrived yet
        updateLayout();
    }

    @Override
    public void onActivate() {
        super.onActivate();
        // Refresh config from cache when tab is shown (handles race condition with server sync)
        GuardBehaviorConfig newConfig = loadConfigFromCache();
        if (!newConfig.equals(currentConfig)) {
            // Config has changed since tab was created - update widgets
            currentConfig = newConfig;
            originalConfig = newConfig;
            updateWidgetValues();
        }
    }

    /**
     * Loads configuration from cache without modifying current state.
     * Returns the config that would be loaded.
     */
    private GuardBehaviorConfig loadConfigFromCache() {
        ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
        GuardData guardData = cache.getGuardData(targetVillager);
        if (guardData != null) {
            return guardData.getBehaviorConfig();
        }
        return GuardBehaviorConfig.DEFAULT;
    }

    /**
     * Updates all widget values to match current config.
     */
    private void updateWidgetValues() {
        if (detectionSlider != null) {
            detectionSlider.setValue(currentConfig.detectionRange());
        }
        if (guardModeButton != null) {
            guardModeButton.setValue(currentConfig.guardMode());
        }
        if (professionLockButton != null) {
            professionLockButton.setValue(currentConfig.professionLocked());
        }
    }

    @Override
    protected void updateLayout() {
        // Clear existing widgets if any
        if (detectionSlider != null) {
            detectionSlider = null;
        }

        int centerX = contentX + contentWidth / 2;
        int startY = contentY + 20;
        int widgetWidth = 200;
        int widgetHeight = 20;
        int spacing = 30;

        // Detection Range Slider
        detectionSlider = new DetectionRangeSlider(
            centerX - widgetWidth / 2,
            startY,
            widgetWidth,
            widgetHeight,
            currentConfig.detectionRange()
        );

        // Guard Mode Button
        guardModeButton = CyclingButtonWidget.builder((GuardMode mode) -> Text.literal(mode.getDisplayName()))
            .values(GuardMode.values())
            .initially(currentConfig.guardMode())
            .build(
                centerX - widgetWidth / 2,
                startY + spacing,
                widgetWidth,
                widgetHeight,
                Text.literal("Guard Mode"),
                (button, value) -> updateGuardMode(value)
            );

        // Profession Lock Button
        professionLockButton = CyclingButtonWidget.onOffBuilder()
            .initially(currentConfig.professionLocked())
            .build(
                centerX - widgetWidth / 2,
                startY + spacing * 2,
                widgetWidth,
                widgetHeight,
                Text.literal("Lock Profession"),
                (button, value) -> updateProfessionLock(value)
            );

        // Save Button
        saveButton = ButtonWidget.builder(
            Text.literal("Save Configuration"),
            button -> saveConfiguration()
        ).dimensions(
            centerX - widgetWidth / 2,
            startY + spacing * 3 + 10,
            widgetWidth / 2 - 5,
            widgetHeight
        ).build();

        // Reset Button
        resetButton = ButtonWidget.builder(
            Text.literal("Reset to Default"),
            button -> resetToDefault()
        ).dimensions(
            centerX + 5,
            startY + spacing * 3 + 10,
            widgetWidth / 2 - 5,
            widgetHeight
        ).build();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Keep checking cache until we load guard data from server
        // This handles the race condition where tab is created before server sync
        if (!hasLoadedFromCache) {
            ClientGuardDataCache cache = ClientGuardDataCache.getInstance();
            GuardData guardData = cache.getGuardData(targetVillager);
            if (guardData != null && guardData.getBehaviorConfig() != null) {
                // We have data from server now! (even if it's default values)
                hasLoadedFromCache = true;
                GuardBehaviorConfig newConfig = guardData.getBehaviorConfig();
                if (!newConfig.equals(currentConfig)) {
                    currentConfig = newConfig;
                    originalConfig = newConfig;
                    // CRITICAL FIX: Recreate widgets with correct values instead of just updating them
                    // This ensures widgets display the correct values on first render
                    updateLayout();
                }
            }
        }

        int centerX = contentX + contentWidth / 2;
        int startY = contentY + 20;
        int spacing = 30;

        // Render detection range slider
        if (detectionSlider != null) {
            detectionSlider.render(context, mouseX, mouseY, delta);
        }

        // Render other buttons
        if (guardModeButton != null) {
            guardModeButton.render(context, mouseX, mouseY, delta);
        }
        if (professionLockButton != null) {
            professionLockButton.render(context, mouseX, mouseY, delta);
        }
        if (saveButton != null) {
            saveButton.render(context, mouseX, mouseY, delta);
        }
        if (resetButton != null) {
            resetButton.render(context, mouseX, mouseY, delta);
        }

        // Render configuration summary at bottom
        renderConfigSummary(context, centerX, startY + spacing * 4 + 20);
    }

    /**
     * Renders a summary of current configuration values.
     */
    private void renderConfigSummary(DrawContext context, int centerX, int y) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        String summary = String.format("Range: %.1f | Mode: %s | Locked: %s",
            currentConfig.detectionRange(),
            currentConfig.guardMode().getDisplayName(),
            currentConfig.professionLocked() ? "Yes" : "No");

        int textWidth = textRenderer.getWidth(summary);
        context.drawText(textRenderer, summary, centerX - textWidth / 2, y, 0xAAAAAA, false);

        // Show "unsaved changes" indicator
        if (!currentConfig.equals(originalConfig)) {
            String unsavedText = "Unsaved changes";
            int unsavedWidth = textRenderer.getWidth(unsavedText);
            context.drawText(textRenderer, unsavedText, centerX - unsavedWidth / 2, y + 12, 0xFFFF55, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        XeenaaVillagerManager.LOGGER.debug("ConfigTab mouseClicked at ({}, {}), button {}", mouseX, mouseY, button);

        if (detectionSlider != null && detectionSlider.mouseClicked(mouseX, mouseY, button)) {
            XeenaaVillagerManager.LOGGER.debug("Detection slider clicked - new value: {}", currentConfig.detectionRange());
            return true;
        }
        if (guardModeButton != null && guardModeButton.mouseClicked(mouseX, mouseY, button)) {
            XeenaaVillagerManager.LOGGER.debug("Guard mode button clicked - new value: {}", currentConfig.guardMode());
            return true;
        }
        if (professionLockButton != null && professionLockButton.mouseClicked(mouseX, mouseY, button)) {
            XeenaaVillagerManager.LOGGER.debug("Profession lock button clicked - new value: {}", currentConfig.professionLocked());
            return true;
        }
        if (saveButton != null && saveButton.mouseClicked(mouseX, mouseY, button)) {
            XeenaaVillagerManager.LOGGER.debug("Save button clicked");
            return true;
        }
        if (resetButton != null && resetButton.mouseClicked(mouseX, mouseY, button)) {
            XeenaaVillagerManager.LOGGER.debug("Reset button clicked");
            return true;
        }

        XeenaaVillagerManager.LOGGER.debug("ConfigTab mouseClicked - no widget handled the click");
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (detectionSlider != null && detectionSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            XeenaaVillagerManager.LOGGER.debug("Detection slider dragged - new value: {}", currentConfig.detectionRange());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (detectionSlider != null && detectionSlider.mouseReleased(mouseX, mouseY, button)) {
            XeenaaVillagerManager.LOGGER.debug("Detection slider released - final value: {}", currentConfig.detectionRange());
            return true;
        }
        return false;
    }

    /**
     * Updates guard mode in current config.
     * If switching to FOLLOW mode, sets the follow target to the current player.
     */
    private void updateGuardMode(GuardMode mode) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Set follow target to current player if switching to FOLLOW mode
        java.util.UUID followTarget = null;
        if (mode == GuardMode.FOLLOW && client.player != null) {
            followTarget = client.player.getUuid();
        }

        currentConfig = new GuardBehaviorConfig(
            currentConfig.detectionRange(),
            mode,
            currentConfig.professionLocked(),
            followTarget
        );
    }

    /**
     * Updates profession lock in current config.
     */
    private void updateProfessionLock(boolean locked) {
        currentConfig = new GuardBehaviorConfig(
            currentConfig.detectionRange(),
            currentConfig.guardMode(),
            locked,
            currentConfig.followTargetPlayerId()
        );
    }

    /**
     * Saves the current configuration to the server.
     */
    private void saveConfiguration() {
        XeenaaVillagerManager.LOGGER.info("Saving guard configuration - Villager: {}, Config: Detection={}, GuardMode={}, ProfessionLocked={}, FollowTarget={}",
            targetVillager.getUuid(),
            currentConfig.detectionRange(),
            currentConfig.guardMode().getDisplayName(),
            currentConfig.professionLocked(),
            currentConfig.followTargetPlayerId());

        GuardConfigPacket packet = new GuardConfigPacket(targetVillager.getUuid(), currentConfig);
        ClientPlayNetworking.send(packet);

        XeenaaVillagerManager.LOGGER.info("GuardConfigPacket sent to server for villager {}", targetVillager.getUuid());

        // Update original config to mark as saved
        originalConfig = currentConfig;

        // Provide feedback
        MinecraftClient.getInstance().player.sendMessage(
            Text.literal("Guard configuration saved!"), true
        );
    }

    /**
     * Resets configuration to default values.
     */
    private void resetToDefault() {
        XeenaaVillagerManager.LOGGER.info("Resetting guard configuration to default for villager {}", targetVillager.getUuid());
        currentConfig = GuardBehaviorConfig.DEFAULT;
        updateWidgetValues();
    }

    /**
     * Custom slider widget for detection range.
     */
    private class DetectionRangeSlider extends SliderWidget {
        private double range;

        public DetectionRangeSlider(int x, int y, int width, int height, double initialRange) {
            super(x, y, width, height, Text.literal("Detection Range: " + (int) initialRange),
                  (initialRange - GuardBehaviorConfig.MIN_DETECTION_RANGE) /
                  (GuardBehaviorConfig.MAX_DETECTION_RANGE - GuardBehaviorConfig.MIN_DETECTION_RANGE));
            this.range = initialRange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Detection Range: " + (int) range + " blocks"));
        }

        @Override
        protected void applyValue() {
            range = GuardBehaviorConfig.MIN_DETECTION_RANGE +
                   value * (GuardBehaviorConfig.MAX_DETECTION_RANGE - GuardBehaviorConfig.MIN_DETECTION_RANGE);

            currentConfig = new GuardBehaviorConfig(
                range,
                currentConfig.guardMode(),
                currentConfig.professionLocked(),
                currentConfig.followTargetPlayerId()
            );
        }

        public void setValue(double newRange) {
            this.range = newRange;
            this.value = (newRange - GuardBehaviorConfig.MIN_DETECTION_RANGE) /
                        (GuardBehaviorConfig.MAX_DETECTION_RANGE - GuardBehaviorConfig.MIN_DETECTION_RANGE);
            updateMessage();
        }
    }
}
