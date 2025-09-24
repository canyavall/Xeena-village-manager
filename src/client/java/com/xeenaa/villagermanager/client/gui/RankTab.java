package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.client.data.ClientGuardDataCache;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.data.rank.GuardRankManager;
import com.xeenaa.villagermanager.data.rank.RankStats;
import com.xeenaa.villagermanager.network.PurchaseRankPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab for managing guard ranks and roles in an integrated interface.
 *
 * Features:
 * - Current rank display with stats and progress bar
 * - Path selection for recruits (Melee vs Ranged)
 * - Upgrade preview with stat comparison
 * - Emerald cost and balance display
 * - Integrated role selection (Patrol/Guard/Follow)
 *
 * @since 2.0.0
 */
public class RankTab extends Tab {
    // Layout constants following UI/UX design
    private static final int PANEL_SPACING = 8;
    private static final int SECTION_SPACING = 12;
    private static final int BUTTON_HEIGHT = 20;

    // Colors (UI/UX design specification)
    private static final int HEADER_BG_COLOR = 0xFF2A2A2A;
    private static final int PANEL_BG_COLOR = 0xFF1E1E1E;
    private static final int PROGRESS_BG_COLOR = 0xFF404040;
    private static final int PROGRESS_FILL_COLOR = 0xFF4CAF50;
    private static final int ACCENT_COLOR = 0xFF2196F3;
    private static final int SUCCESS_COLOR = 0xFF4CAF50;
    private static final int WARNING_COLOR = 0xFFFF9800;
    private static final int ERROR_COLOR = 0xFFF44336;

    // UI Elements
    private final List<ButtonWidget> pathButtons = new ArrayList<>();
    private final List<ButtonWidget> roleButtons = new ArrayList<>();
    private ButtonWidget upgradeButton;

    // Data
    private GuardRankData rankData;
    private GuardData guardData;
    private int playerEmeralds;

    public RankTab(VillagerEntity targetVillager) {
        super(targetVillager);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Rank");
    }

    @Override
    public boolean isAvailable() {
        // Only available for guard villagers
        GuardData data = ClientGuardDataCache.getInstance().getGuardData(targetVillager.getUuid());
        return data != null;
    }

    @Override
    protected void initializeContent() {
        loadGuardData();
        updatePlayerEmeralds();
        createUIElements();
    }

    @Override
    protected void updateLayout() {
        clearButtons();
        createUIElements();
    }

    @Override
    public void onActivate() {
        super.onActivate();
        refreshData();
    }

    private void loadGuardData() {
        this.guardData = ClientGuardDataCache.getInstance().getGuardData(targetVillager.getUuid());
        if (this.guardData != null) {
            this.rankData = this.guardData.getRankData();
        } else {
            // Fallback for new guards
            this.rankData = new GuardRankData(targetVillager.getUuid());
        }
    }

    private void updatePlayerEmeralds() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            this.playerEmeralds = GuardRankManager.getInstance().countPlayerEmeralds(client.player);
        }
    }

    private void refreshData() {
        loadGuardData();
        updatePlayerEmeralds();
        updateButtonStates();
    }

    private void createUIElements() {
        int currentY = contentY + 10;

        // Current rank info is rendered, not buttons
        // Expanded header area to include stat comparison text
        currentY += 120; // Increased space for larger header + progress bar + tier text

        // Path selection for recruits or upgrade section for others
        if (rankData.getCurrentRank() == GuardRank.RECRUIT) {
            currentY = createPathSelectionButtons(currentY);
        } else {
            currentY = createUpgradeSection(currentY);
        }

        currentY += SECTION_SPACING;

        // Role selection section (removed - not needed per user feedback)
        // createRoleButtons(currentY);
    }

    private int createPathSelectionButtons(int startY) {
        // Clear existing path buttons
        pathButtons.clear();

        int buttonWidth = (contentWidth - PANEL_SPACING * 4) / 2; // More spacing for margins
        int leftX = contentX + PANEL_SPACING * 2;
        int rightX = contentX + PANEL_SPACING * 2 + buttonWidth + PANEL_SPACING;

        // Melee Path Button
        ButtonWidget meleeButton = ButtonWidget.builder(
            Text.literal("⚔ Melee Path").styled(style -> style.withColor(0xFFE57373)),
            button -> purchaseRank(GuardRank.MAN_AT_ARMS_I)
        )
        .dimensions(leftX, startY, buttonWidth, BUTTON_HEIGHT + 10)
        .build();

        // Ranged Path Button
        ButtonWidget rangedButton = ButtonWidget.builder(
            Text.literal("🏹 Ranged Path").styled(style -> style.withColor(0xFF81C784)),
            button -> purchaseRank(GuardRank.MARKSMAN_I)
        )
        .dimensions(rightX, startY, buttonWidth, BUTTON_HEIGHT + 10)
        .build();

        pathButtons.add(meleeButton);
        pathButtons.add(rangedButton);

        // Buttons are managed by the tab itself

        return startY + BUTTON_HEIGHT + 20;
    }

    private int createUpgradeSection(int startY) {
        GuardRank currentRank = rankData.getCurrentRank();
        GuardRank nextRank = currentRank.getNextRank();

        if (nextRank != null) {
            int cost = nextRank.getEmeraldCost();
            boolean canAfford = playerEmeralds >= cost;

            Text buttonText = Text.literal("Upgrade to " + nextRank.getDisplayName())
                .append(Text.literal(" (").styled(style -> style.withColor(0xFFBDBDBD)))
                .append(Text.literal(cost + " emeralds").styled(style ->
                    style.withColor(canAfford ? SUCCESS_COLOR : ERROR_COLOR)))
                .append(Text.literal(")").styled(style -> style.withColor(0xFFBDBDBD)));

            upgradeButton = ButtonWidget.builder(buttonText, button -> purchaseRank(nextRank))
                .dimensions(contentX + PANEL_SPACING * 2, startY, contentWidth - (PANEL_SPACING * 4), BUTTON_HEIGHT + 5)
                .build();

            // Button managed by tab itself

            return startY + BUTTON_HEIGHT + 15;
        }

        return startY;
    }

    private void createRoleButtons(int startY) {
        roleButtons.clear();

        GuardData.GuardRole[] roles = GuardData.GuardRole.values();
        int buttonWidth = (contentWidth - (PANEL_SPACING * 4)) / 3;

        for (int i = 0; i < roles.length; i++) {
            GuardData.GuardRole role = roles[i];
            int buttonX = contentX + PANEL_SPACING + (i * (buttonWidth + PANEL_SPACING));

            ButtonWidget roleButton = ButtonWidget.builder(
                Text.literal(role.getName()),
                button -> setGuardRole(role)
            )
            .dimensions(buttonX, startY, buttonWidth, BUTTON_HEIGHT)
            .build();

            roleButtons.add(roleButton);
        }
    }

    private void updateButtonStates() {
        // Update path buttons
        for (ButtonWidget button : pathButtons) {
            button.active = true; // Recruits can always choose a path
        }

        // Update upgrade button
        if (upgradeButton != null) {
            GuardRank nextRank = rankData.getCurrentRank().getNextRank();
            upgradeButton.active = nextRank != null && playerEmeralds >= nextRank.getEmeraldCost();
        }

        // Update role buttons - highlight current role
        GuardData.GuardRole currentRole = guardData != null ? guardData.getRole() : GuardData.GuardRole.GUARD;
        // Role button highlighting would be handled in custom button rendering
    }

    private void purchaseRank(GuardRank targetRank) {
        XeenaaVillagerManager.LOGGER.info("Purchasing rank {} for villager {}",
            targetRank.getDisplayName(), targetVillager.getId());

        PurchaseRankPacket packet = new PurchaseRankPacket(targetVillager.getUuid(), targetRank);
        ClientPlayNetworking.send(packet);
    }

    private void setGuardRole(GuardData.GuardRole role) {
        XeenaaVillagerManager.LOGGER.info("Setting guard role to {} for villager {}",
            role.getName(), targetVillager.getId());

        // TODO: Create SetGuardRolePacket when role management is implemented
        XeenaaVillagerManager.LOGGER.warn("Role setting not yet implemented - packet needed");
    }

    private void clearButtons() {
        pathButtons.clear();
        roleButtons.clear();
        upgradeButton = null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (rankData == null) return;

        renderBackground(context);
        renderRankHeader(context);
        renderProgressBar(context);
        renderEmeraldBalance(context);
        renderButtons(context, mouseX, mouseY, delta);
    }

    private void renderBackground(DrawContext context) {
        // Main panel background
        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, PANEL_BG_COLOR);

        // Border
        context.drawBorder(contentX, contentY, contentWidth, contentHeight, 0xFF555555);
    }

    private void renderRankHeader(DrawContext context) {
        GuardRank currentRank = rankData.getCurrentRank();
        RankStats stats = currentRank.getStats();

        // Header background - expanded to include stat comparison
        context.fill(contentX + 4, contentY + 4, contentX + contentWidth - 4, contentY + 90, HEADER_BG_COLOR);

        // Rank name and tier
        String headerText = String.format("%s (Tier %d/%d)",
            currentRank.getDisplayName(), currentRank.getTier(), 4);
        context.drawText(getTextRenderer(), Text.literal(headerText).styled(style -> style.withBold(true)),
            contentX + 10, contentY + 10, 0xFFFFFF, true);

        // Stats display
        String statsText = String.format("Health: %.0f HP | Damage: %.0f",
            stats.getMaxHealth(), stats.getAttackDamage());
        context.drawText(getTextRenderer(), Text.literal(statsText),
            contentX + 10, contentY + 25, 0xFFBDBDBD, false);

        // Special ability
        if (currentRank.hasSpecialAbility()) {
            String abilityText = "Special: " + currentRank.getSpecialAbility().getName();
            context.drawText(getTextRenderer(), Text.literal(abilityText),
                contentX + 10, contentY + 35, 0xFFAB47BC, false);
        }

        // Next rank preview (moved from renderStatComparison to header area)
        GuardRank nextRank = currentRank.getNextRank();
        if (nextRank != null && currentRank != GuardRank.RECRUIT) {
            int nextRankY = contentY + 48; // Position after special ability, before progress bar

            context.drawText(getTextRenderer(), Text.literal("Next Rank: " + nextRank.getDisplayName()),
                contentX + 10, nextRankY, 0xFF81C784, false);

            RankStats currentStats = currentRank.getStats();
            RankStats nextStats = nextRank.getStats();

            // Health comparison - positioned to the right of "Next Rank" text
            String healthText = String.format("Health: %.0f → %.0f HP",
                currentStats.getMaxHealth(), nextStats.getMaxHealth());
            context.drawText(getTextRenderer(), Text.literal(healthText),
                contentX + 180, nextRankY, 0xFFE57373, false);
        }
    }

    private void renderProgressBar(DrawContext context) {
        int barY = contentY + 95; // Moved down to accommodate larger header
        int barWidth = contentWidth - 20;
        int barHeight = 8;
        int barX = contentX + 10;

        // Background
        context.fill(barX, barY, barX + barWidth, barY + barHeight, PROGRESS_BG_COLOR);

        // Progress fill
        GuardRank currentRank = rankData.getCurrentRank();
        int tier = currentRank.getTier();
        int fillWidth = (barWidth * tier) / 4; // 4 is max tier

        if (fillWidth > 0) {
            context.fill(barX, barY, barX + fillWidth, barY + barHeight, PROGRESS_FILL_COLOR);
        }

        // Progress text
        String progressText = tier == 4 ? "Max Rank Achieved" : String.format("Tier %d/4", tier);
        int textWidth = getTextRenderer().getWidth(progressText);
        context.drawText(getTextRenderer(), Text.literal(progressText),
            barX + (barWidth - textWidth) / 2, barY + barHeight + 3, 0xFFFFFF, true);
    }

    private void renderStatComparison(DrawContext context) {
        GuardRank currentRank = rankData.getCurrentRank();
        GuardRank nextRank = currentRank.getNextRank();

        if (nextRank == null || currentRank == GuardRank.RECRUIT) return;

        int startY = contentY + 90;

        context.drawText(getTextRenderer(), Text.literal("Next Rank: " + nextRank.getDisplayName()),
            contentX + 10, startY, 0xFF81C784, false);

        RankStats currentStats = currentRank.getStats();
        RankStats nextStats = nextRank.getStats();

        // Health comparison
        String healthText = String.format("Health: %.0f → %.0f HP",
            currentStats.getMaxHealth(), nextStats.getMaxHealth());
        context.drawText(getTextRenderer(), Text.literal(healthText),
            contentX + 10, startY + 12, 0xFFE57373, false);

        // Damage comparison
        String damageText = String.format("Damage: %.0f → %.0f",
            currentStats.getAttackDamage(), nextStats.getAttackDamage());
        context.drawText(getTextRenderer(), Text.literal(damageText),
            contentX + 10, startY + 24, 0xFFFFB74D, false);
    }

    private void renderEmeraldBalance(DrawContext context) {
        int balanceY = contentY + contentHeight - 40;

        String balanceText = String.format("Emeralds: %d", playerEmeralds);
        context.drawText(getTextRenderer(), Text.literal(balanceText),
            contentX + 10, balanceY, 0xFF4CAF50, false);

        // Show cost for next upgrade
        GuardRank currentRank = rankData.getCurrentRank();
        GuardRank nextRank = currentRank.getNextRank();

        if (nextRank != null) {
            int cost = nextRank.getEmeraldCost();
            String costText = String.format("Next upgrade: %d emeralds", cost);
            int textWidth = getTextRenderer().getWidth(costText);
            int color = playerEmeralds >= cost ? SUCCESS_COLOR : ERROR_COLOR;
            context.drawText(getTextRenderer(), Text.literal(costText),
                contentX + contentWidth - textWidth - 10, balanceY, color, false);
        }
    }

    private void renderRoleSection(DrawContext context) {
        int roleY = contentY + contentHeight - 70;

        context.drawText(getTextRenderer(), Text.literal("Guard Role:"),
            contentX + 10, roleY, 0xFFFFFF, false);

        if (guardData != null) {
            GuardData.GuardRole currentRole = guardData.getRole();
            context.drawText(getTextRenderer(), Text.literal("Current: " + currentRole.getName()),
                contentX + 10, roleY + 12, ACCENT_COLOR, false);
        }
    }

    private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render path selection buttons
        for (ButtonWidget button : pathButtons) {
            button.render(context, mouseX, mouseY, delta);
        }

        // Render upgrade button
        if (upgradeButton != null) {
            upgradeButton.render(context, mouseX, mouseY, delta);
        }

        // Render role buttons
        for (ButtonWidget button : roleButtons) {
            button.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle path button clicks
        for (ButtonWidget pathButton : pathButtons) {
            if (pathButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Handle upgrade button click
        if (upgradeButton != null && upgradeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Handle role button clicks
        for (ButtonWidget roleButton : roleButtons) {
            if (roleButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        clearButtons();
    }
}