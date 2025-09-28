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
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    // Enhanced color scheme for affordability indicators
    private static final int AFFORDABLE_COLOR = 0xFF4CAF50;     // Green for affordable
    private static final int UNAFFORDABLE_COLOR = 0xFFF44336;   // Red for unaffordable
    private static final int EMERALD_COLOR = 0xFF00C851;        // Bright green for emerald count
    private static final int COST_LABEL_COLOR = 0xFFBDBDBD;     // Gray for labels
    private static final int DISABLED_COLOR = 0xFF757575;       // Gray for disabled elements

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
    public Text getTabTitle() {
        if (rankData != null) {
            GuardRank currentRank = rankData.getCurrentRank();
            if (currentRank == GuardRank.RECRUIT) {
                return Text.literal("Guard Ranking - Choose Path");
            } else {
                return Text.literal("Guard Ranking - " + currentRank.getDisplayName());
            }
        }
        return Text.literal("Guard Ranking");
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
        // Ensure title is set properly on first activation
        loadGuardData();
    }

    @Override
    public void refresh() {
        // Called when underlying data changes (e.g., rank purchase complete)
        refreshData();

        // CRITICAL FIX: Recreate UI elements to show updated rank progression
        // This ensures buttons display the correct next rank after purchase
        updateLayout();
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

        // Role selection UI integrated with Guard data - roles managed server-side
    }

    private int createPathSelectionButtons(int startY) {
        // Clear existing path buttons
        pathButtons.clear();

        int buttonWidth = (contentWidth - PANEL_SPACING * 4) / 2; // More spacing for margins
        int leftX = contentX + PANEL_SPACING * 2;
        int rightX = contentX + PANEL_SPACING * 2 + buttonWidth + PANEL_SPACING;

        // Check path availability for initial button styling
        boolean meleePathAvailable = rankData.isPathAvailable(GuardPath.MELEE);
        boolean rangedPathAvailable = rankData.isPathAvailable(GuardPath.RANGED);

        // Melee Path Button - adjust color based on availability
        Text meleeText = Text.literal("⚔ Melee Path").styled(style ->
            style.withColor(meleePathAvailable ? 0xFFE57373 : DISABLED_COLOR));

        ButtonWidget meleeButton = ButtonWidget.builder(meleeText,
            button -> purchaseRank(GuardRank.MAN_AT_ARMS_I))
        .dimensions(leftX, startY, buttonWidth, BUTTON_HEIGHT + 10)
        .build();

        // Ranged Path Button - adjust color based on availability
        Text rangedText = Text.literal("🏹 Ranged Path").styled(style ->
            style.withColor(rangedPathAvailable ? 0xFF81C784 : DISABLED_COLOR));

        ButtonWidget rangedButton = ButtonWidget.builder(rangedText,
            button -> purchaseRank(GuardRank.MARKSMAN_I))
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
                    style.withColor(canAfford ? AFFORDABLE_COLOR : UNAFFORDABLE_COLOR)))
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
        // Update path buttons with proper path locking logic
        if (pathButtons.size() >= 2) {
            ButtonWidget meleeButton = pathButtons.get(0);
            ButtonWidget rangedButton = pathButtons.get(1);

            // Check path availability and affordability
            boolean meleePathAvailable = rankData.isPathAvailable(GuardPath.MELEE);
            boolean rangedPathAvailable = rankData.isPathAvailable(GuardPath.RANGED);
            int meleeCost = GuardRank.MAN_AT_ARMS_I.getEmeraldCost();
            int rangedCost = GuardRank.MARKSMAN_I.getEmeraldCost();
            boolean canAffordMelee = playerEmeralds >= meleeCost;
            boolean canAffordRanged = playerEmeralds >= rangedCost;

            // Set button states and tooltips
            meleeButton.active = meleePathAvailable && canAffordMelee;
            rangedButton.active = rangedPathAvailable && canAffordRanged;

            // Update tooltips with path lock information
            updatePathButtonTooltips(meleeButton, rangedButton,
                meleePathAvailable, rangedPathAvailable,
                canAffordMelee, canAffordRanged,
                meleeCost, rangedCost);
        }

        // Update upgrade button
        if (upgradeButton != null) {
            GuardRank nextRank = rankData.getCurrentRank().getNextRank();
            boolean canAfford = nextRank != null && playerEmeralds >= nextRank.getEmeraldCost();
            upgradeButton.active = canAfford;

            // Update upgrade button tooltip
            if (nextRank != null) {
                updateUpgradeButtonTooltip(upgradeButton, nextRank, canAfford);
            }
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

    /**
     * Updates tooltips for path selection buttons with detailed information.
     */
    private void updatePathButtonTooltips(ButtonWidget meleeButton, ButtonWidget rangedButton,
            boolean meleePathAvailable, boolean rangedPathAvailable,
            boolean canAffordMelee, boolean canAffordRanged,
            int meleeCost, int rangedCost) {

        // Melee button tooltip
        if (!meleePathAvailable && rankData.hasChosenPath()) {
            meleeButton.setTooltip(Tooltip.of(Text.literal(
                "Path locked: Guard specialized as " + rankData.getChosenPath().getDisplayName())
                .formatted(Formatting.RED)));
        } else {
            String meleeTooltip = String.format("Cost: %d emeralds\nYou have: %d emeralds",
                meleeCost, playerEmeralds);
            if (!canAffordMelee) {
                meleeTooltip += String.format("\nNeed %d more emeralds", meleeCost - playerEmeralds);
            }
            meleeButton.setTooltip(Tooltip.of(Text.literal(meleeTooltip)
                .formatted(canAffordMelee ? Formatting.GREEN : Formatting.RED)));
        }

        // Ranged button tooltip
        if (!rangedPathAvailable && rankData.hasChosenPath()) {
            rangedButton.setTooltip(Tooltip.of(Text.literal(
                "Path locked: Guard specialized as " + rankData.getChosenPath().getDisplayName())
                .formatted(Formatting.RED)));
        } else {
            String rangedTooltip = String.format("Cost: %d emeralds\nYou have: %d emeralds",
                rangedCost, playerEmeralds);
            if (!canAffordRanged) {
                rangedTooltip += String.format("\nNeed %d more emeralds", rangedCost - playerEmeralds);
            }
            rangedButton.setTooltip(Tooltip.of(Text.literal(rangedTooltip)
                .formatted(canAffordRanged ? Formatting.GREEN : Formatting.RED)));
        }
    }

    /**
     * Updates tooltip for upgrade button with detailed cost information.
     */
    private void updateUpgradeButtonTooltip(ButtonWidget upgradeButton, GuardRank nextRank, boolean canAfford) {
        int cost = nextRank.getEmeraldCost();
        String tooltipText = String.format("Cost: %d emeralds\nYou have: %d emeralds", cost, playerEmeralds);

        if (!canAfford) {
            tooltipText += String.format("\nNeed %d more emeralds", cost - playerEmeralds);
        } else {
            tooltipText += String.format("\n%d emeralds remaining after purchase", playerEmeralds - cost);
        }

        upgradeButton.setTooltip(Tooltip.of(Text.literal(tooltipText)
            .formatted(canAfford ? Formatting.GREEN : Formatting.RED)));
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
        int balanceY = contentY + contentHeight - 55; // Moved up to make room for cost section

        // Enhanced emerald balance display
        renderCostInformationSection(context, balanceY);
    }

    /**
     * Renders comprehensive cost information section with clear affordability indicators.
     * Always shows costs regardless of player emerald count.
     */
    private void renderCostInformationSection(DrawContext context, int startY) {
        // Background panel for cost information
        int panelHeight = 45;
        context.fill(contentX + 4, startY - 5, contentX + contentWidth - 4, startY + panelHeight, 0xFF2A2A2A);
        context.drawBorder(contentX + 4, startY - 5, contentWidth - 8, panelHeight, 0xFF404040);

        // Player emerald count - always visible
        String emeraldText = String.format("You have: %d emeralds", playerEmeralds);
        context.drawText(getTextRenderer(), Text.literal(emeraldText),
            contentX + 10, startY, EMERALD_COLOR, false);

        // Cost information - always shown regardless of rank state
        GuardRank currentRank = rankData.getCurrentRank();

        if (currentRank == GuardRank.RECRUIT) {
            // Show both path costs for recruits
            renderRecruitPathCosts(context, startY + 12);
        } else {
            // Show next upgrade cost or max rank status
            renderUpgradeCosts(context, startY + 12, currentRank);
        }
    }

    /**
     * Renders cost information for recruit path selection.
     */
    private void renderRecruitPathCosts(DrawContext context, int y) {
        int meleePathCost = GuardRank.MAN_AT_ARMS_I.getEmeraldCost();
        int rangedPathCost = GuardRank.MARKSMAN_I.getEmeraldCost();

        // Melee path cost
        boolean canAffordMelee = playerEmeralds >= meleePathCost;
        int meleeColor = canAffordMelee ? AFFORDABLE_COLOR : UNAFFORDABLE_COLOR;
        String meleeText = String.format("⚔ Melee Path: %d emeralds", meleePathCost);
        context.drawText(getTextRenderer(), Text.literal(meleeText),
            contentX + 10, y, meleeColor, false);

        // Ranged path cost
        boolean canAffordRanged = playerEmeralds >= rangedPathCost;
        int rangedColor = canAffordRanged ? AFFORDABLE_COLOR : UNAFFORDABLE_COLOR;
        String rangedText = String.format("🏹 Ranged Path: %d emeralds", rangedPathCost);
        context.drawText(getTextRenderer(), Text.literal(rangedText),
            contentX + 10, y + 12, rangedColor, false);

        // Affordability status indicator
        if (!canAffordMelee && !canAffordRanged) {
            String statusText = "❌ Cannot afford any path upgrades";
            context.drawText(getTextRenderer(), Text.literal(statusText),
                contentX + contentWidth - getTextRenderer().getWidth(statusText) - 10, y + 6, UNAFFORDABLE_COLOR, false);
        } else if (canAffordMelee && canAffordRanged) {
            String statusText = "✓ Can afford both paths";
            context.drawText(getTextRenderer(), Text.literal(statusText),
                contentX + contentWidth - getTextRenderer().getWidth(statusText) - 10, y + 6, AFFORDABLE_COLOR, false);
        } else {
            String statusText = String.format("⚠ Can afford %s path only", canAffordMelee ? "melee" : "ranged");
            context.drawText(getTextRenderer(), Text.literal(statusText),
                contentX + contentWidth - getTextRenderer().getWidth(statusText) - 10, y + 6, WARNING_COLOR, false);
        }
    }

    /**
     * Renders cost information for rank upgrades.
     */
    private void renderUpgradeCosts(DrawContext context, int y, GuardRank currentRank) {
        GuardRank nextRank = currentRank.getNextRank();

        if (nextRank != null) {
            int cost = nextRank.getEmeraldCost();
            boolean canAfford = playerEmeralds >= cost;

            // Next rank cost - always visible
            String costLabel = "Next rank cost:";
            context.drawText(getTextRenderer(), Text.literal(costLabel),
                contentX + 10, y, COST_LABEL_COLOR, false);

            String costText = String.format("%d emeralds", cost);
            int costColor = canAfford ? AFFORDABLE_COLOR : UNAFFORDABLE_COLOR;
            context.drawText(getTextRenderer(), Text.literal(costText),
                contentX + 110, y, costColor, false);

            // Affordability status with emerald difference
            String statusText;
            int statusColor;
            if (canAfford) {
                int remaining = playerEmeralds - cost;
                statusText = String.format("✓ Affordable (%d emeralds remaining)", remaining);
                statusColor = AFFORDABLE_COLOR;
            } else {
                int needed = cost - playerEmeralds;
                statusText = String.format("❌ Need %d more emeralds", needed);
                statusColor = UNAFFORDABLE_COLOR;
            }

            int statusWidth = getTextRenderer().getWidth(statusText);
            context.drawText(getTextRenderer(), Text.literal(statusText),
                contentX + contentWidth - statusWidth - 10, y, statusColor, false);

            // Next rank name
            String nextRankText = String.format("→ %s", nextRank.getDisplayName());
            context.drawText(getTextRenderer(), Text.literal(nextRankText),
                contentX + 10, y + 12, ACCENT_COLOR, false);
        } else {
            // Maximum rank achieved
            String maxRankText = "🏆 Maximum rank achieved";
            int textWidth = getTextRenderer().getWidth(maxRankText);
            int centerX = contentX + (contentWidth - textWidth) / 2;
            context.drawText(getTextRenderer(), Text.literal(maxRankText),
                centerX, y + 6, 0xFFFFD700, false); // Gold color
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