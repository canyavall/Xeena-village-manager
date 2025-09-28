package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.data.rank.GuardPath;
import com.xeenaa.villagermanager.network.PurchaseRankPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI screen for managing guard ranks and purchases.
 * Displays current rank, available upgrades, and purchase options.
 */
public class GuardRankScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardRankScreen.class);

    private static final Identifier BACKGROUND_TEXTURE =
        Identifier.of("xeenaa_villager_manager", "textures/gui/guard_rank_background.png");
    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 220; // Increased height for cost section

    // Enhanced color scheme for affordability indicators
    private static final int AFFORDABLE_COLOR = 0xFF4CAF50;     // Green for affordable
    private static final int UNAFFORDABLE_COLOR = 0xFFF44336;   // Red for unaffordable
    private static final int EMERALD_COLOR = 0xFF00C851;        // Bright green for emerald count
    private static final int COST_LABEL_COLOR = 0xFFBDBDBD;     // Gray for labels
    private static final int WARNING_COLOR = 0xFFFF9800;        // Orange for warnings

    private final VillagerEntity targetVillager;
    private final GuardRankData rankData;
    private int backgroundX;
    private int backgroundY;

    // GUI components
    private ButtonWidget meleePathButton;
    private ButtonWidget rangedPathButton;
    private ButtonWidget upgradeButton;
    private ButtonWidget closeButton;

    public GuardRankScreen(VillagerEntity villager, GuardRankData rankData) {
        super(Text.literal("Guard Ranks - " + villager.getName().getString()));
        this.targetVillager = villager;
        this.rankData = rankData;
    }

    @Override
    protected void init() {
        super.init();

        // Center the background
        this.backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
        this.backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;

        initializeButtons();
        updateButtonStates();

        LOGGER.debug("Initialized GuardRankScreen for villager {} with rank {}",
                    targetVillager.getId(), rankData.getCurrentRank().getDisplayName());
    }

    private void initializeButtons() {
        // Close button (top right)
        closeButton = ButtonWidget.builder(Text.literal("X"), button -> close())
                .dimensions(backgroundX + BACKGROUND_WIDTH - 25, backgroundY + 5, 20, 20)
                .build();
        addDrawableChild(closeButton);

        // Path selection buttons (only show for recruit rank)
        if (rankData.getCurrentRank() == GuardRank.RECRUIT) {
            int meleePathCost = GuardRank.MAN_AT_ARMS_I.getEmeraldCost();
            int rangedPathCost = GuardRank.MARKSMAN_I.getEmeraldCost();

            // Create melee path button with simplified text
            Text meleeText = Text.literal("⚔ Melee Path");
            meleePathButton = ButtonWidget.builder(meleeText, button -> purchaseRank(GuardRank.MAN_AT_ARMS_I))
                    .dimensions(backgroundX + 20, backgroundY + 145, 100, 20)
                    .build();
            addDrawableChild(meleePathButton);

            // Create ranged path button with simplified text
            Text rangedText = Text.literal("🏹 Ranged Path");
            rangedPathButton = ButtonWidget.builder(rangedText, button -> purchaseRank(GuardRank.MARKSMAN_I))
                    .dimensions(backgroundX + 136, backgroundY + 145, 100, 20)
                    .build();
            addDrawableChild(rangedPathButton);
        } else {
            // Upgrade button for existing ranks
            GuardRank[] upgrades = rankData.getAvailableUpgrades();
            if (upgrades.length > 0) {
                GuardRank nextRank = upgrades[0];
                Text buttonText = Text.literal("Upgrade to " + nextRank.getDisplayName());

                upgradeButton = ButtonWidget.builder(buttonText, button -> purchaseRank(nextRank))
                        .dimensions(backgroundX + 20, backgroundY + 145, 216, 20)
                        .build();
                addDrawableChild(upgradeButton);
            }
        }
    }

    private void updateButtonStates() {
        int playerEmeralds = getPlayerEmeraldCount();

        if (meleePathButton != null) {
            int cost = GuardRank.MAN_AT_ARMS_I.getEmeraldCost();
            boolean canAfford = playerEmeralds >= cost;
            boolean pathAvailable = rankData.isPathAvailable(GuardPath.MELEE);
            meleePathButton.active = canAfford && pathAvailable;

            // Enhanced tooltip with affordability information
            if (!pathAvailable && rankData.hasChosenPath()) {
                meleePathButton.setTooltip(Tooltip.of(Text.literal(
                    "Path locked: Guard specialized as " + rankData.getChosenPath().getDisplayName())
                    .formatted(Formatting.RED)));
            } else {
                String tooltipText = String.format("Cost: %d emeralds\nYou have: %d emeralds", cost, playerEmeralds);
                if (!canAfford) {
                    tooltipText += String.format("\nNeed %d more emeralds", cost - playerEmeralds);
                }
                meleePathButton.setTooltip(Tooltip.of(Text.literal(tooltipText)
                    .formatted(canAfford ? Formatting.GREEN : Formatting.RED)));
            }
        }

        if (rangedPathButton != null) {
            int cost = GuardRank.MARKSMAN_I.getEmeraldCost();
            boolean canAfford = playerEmeralds >= cost;
            boolean pathAvailable = rankData.isPathAvailable(GuardPath.RANGED);
            rangedPathButton.active = canAfford && pathAvailable;

            // Enhanced tooltip with affordability information
            if (!pathAvailable && rankData.hasChosenPath()) {
                rangedPathButton.setTooltip(Tooltip.of(Text.literal(
                    "Path locked: Guard specialized as " + rankData.getChosenPath().getDisplayName())
                    .formatted(Formatting.RED)));
            } else {
                String tooltipText = String.format("Cost: %d emeralds\nYou have: %d emeralds", cost, playerEmeralds);
                if (!canAfford) {
                    tooltipText += String.format("\nNeed %d more emeralds", cost - playerEmeralds);
                }
                rangedPathButton.setTooltip(Tooltip.of(Text.literal(tooltipText)
                    .formatted(canAfford ? Formatting.GREEN : Formatting.RED)));
            }
        }

        if (upgradeButton != null) {
            GuardRank[] upgrades = rankData.getAvailableUpgrades();
            if (upgrades.length > 0) {
                int cost = upgrades[0].getEmeraldCost();
                boolean canAfford = playerEmeralds >= cost;
                upgradeButton.active = canAfford;

                // Enhanced tooltip with detailed cost information
                String tooltipText = String.format("Cost: %d emeralds\nYou have: %d emeralds", cost, playerEmeralds);
                if (!canAfford) {
                    tooltipText += String.format("\nNeed %d more emeralds", cost - playerEmeralds);
                } else {
                    tooltipText += String.format("\n%d emeralds remaining after purchase", playerEmeralds - cost);
                }
                upgradeButton.setTooltip(Tooltip.of(Text.literal(tooltipText)
                    .formatted(canAfford ? Formatting.GREEN : Formatting.RED)));
            }
        }
    }

    private void purchaseRank(GuardRank targetRank) {
        int playerEmeralds = getPlayerEmeraldCount();

        LOGGER.info("Attempting to purchase rank {} for villager {} (player has {} emeralds)",
                   targetRank.getDisplayName(), targetVillager.getId(), playerEmeralds);

        // Validate purchase on client side first
        if (!rankData.canPurchaseRank(targetRank)) {
            LOGGER.debug("Cannot purchase rank {} - requirements not met", targetRank.getDisplayName());
            return;
        }

        if (playerEmeralds < targetRank.getEmeraldCost()) {
            LOGGER.debug("Cannot purchase rank {} - insufficient emeralds ({} < {})",
                targetRank.getDisplayName(), playerEmeralds, targetRank.getEmeraldCost());
            return;
        }

        // Send purchase packet to server
        PurchaseRankPacket packet = new PurchaseRankPacket(targetVillager.getUuid(), targetRank);
        ClientPlayNetworking.send(packet);

        LOGGER.info("Sent rank purchase packet to server: {} -> {}",
                   targetVillager.getId(), targetRank.getDisplayName());

        // Close screen - server will handle the purchase and sync back to client
        close();
    }

    private int getPlayerEmeraldCount() {
        if (client == null || client.player == null) return 0;

        return client.player.getInventory().count(Items.EMERALD);
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

        // Render background texture (will be created as simple colored background for now)
        context.fill(backgroundX, backgroundY, backgroundX + BACKGROUND_WIDTH, backgroundY + BACKGROUND_HEIGHT, 0xFF2D2D30);
        context.drawBorder(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0xFF4A4A4A);

        // Render title
        Text title = Text.literal("Guard Ranks").formatted(Formatting.BOLD);
        int titleX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(title)) / 2;
        context.drawText(client.textRenderer, title, titleX, backgroundY + 10, 0xFFFFFF, false);

        // Render villager name
        Text villagerName = targetVillager.getName().copy().formatted(Formatting.YELLOW);
        int nameX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(villagerName)) / 2;
        context.drawText(client.textRenderer, villagerName, nameX, backgroundY + 25, 0xFFFFFF, false);

        // Render current rank information
        renderCurrentRankInfo(context);

        // Render available upgrades
        renderUpgradeOptions(context);

        // Render comprehensive cost information
        renderCostInformation(context);

        // Render buttons
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCurrentRankInfo(DrawContext context) {
        int startY = backgroundY + 45;

        // Current rank
        Text currentRankLabel = Text.literal("Current Rank:").formatted(Formatting.GRAY);
        context.drawText(client.textRenderer, currentRankLabel, backgroundX + 20, startY, 0xFFFFFF, false);

        Text currentRank = rankData.getCurrentRank().getDisplayText().copy().formatted(Formatting.GREEN);
        context.drawText(client.textRenderer, currentRank, backgroundX + 120, startY, 0xFFFFFF, false);

        // Specialization path
        Text pathLabel = Text.literal("Specialization:").formatted(Formatting.GRAY);
        context.drawText(client.textRenderer, pathLabel, backgroundX + 20, startY + 15, 0xFFFFFF, false);

        Text pathText = Text.literal(rankData.getCurrentPath().getDisplayName()).formatted(Formatting.AQUA);
        context.drawText(client.textRenderer, pathText, backgroundX + 120, startY + 15, 0xFFFFFF, false);

        // Tier progress
        Text tierLabel = Text.literal("Tier:").formatted(Formatting.GRAY);
        context.drawText(client.textRenderer, tierLabel, backgroundX + 20, startY + 30, 0xFFFFFF, false);

        Text tierText = Text.literal(rankData.getCurrentTier() + "/4").formatted(Formatting.GOLD);
        context.drawText(client.textRenderer, tierText, backgroundX + 120, startY + 30, 0xFFFFFF, false);

        // Total emeralds spent
        if (rankData.getTotalEmeraldsSpent() > 0) {
            Text spentLabel = Text.literal("Emeralds Invested:").formatted(Formatting.GRAY);
            context.drawText(client.textRenderer, spentLabel, backgroundX + 20, startY + 45, 0xFFFFFF, false);

            Text spentText = Text.literal(String.valueOf(rankData.getTotalEmeraldsSpent())).formatted(Formatting.GREEN);
            context.drawText(client.textRenderer, spentText, backgroundX + 150, startY + 45, 0xFFFFFF, false);
        }

        // Path lock status for specialized guards
        if (rankData.hasChosenPath() && rankData.getChosenPath() != GuardPath.RECRUIT) {
            Text pathLockLabel = Text.literal("Path Locked:").formatted(Formatting.GRAY);
            context.drawText(client.textRenderer, pathLockLabel, backgroundX + 20, startY + 60, 0xFFFFFF, false);

            Text pathLockText = Text.literal("✓ " + rankData.getChosenPath().getDisplayName()).formatted(Formatting.GOLD);
            context.drawText(client.textRenderer, pathLockText, backgroundX + 120, startY + 60, 0xFFFFFF, false);
        }
    }

    private void renderUpgradeOptions(DrawContext context) {
        int startY = backgroundY + 110;

        if (rankData.getCurrentRank() == GuardRank.RECRUIT) {
            // Show path selection with path lock warning if applicable
            if (!rankData.hasChosenPath()) {
                Text chooseLabel = Text.literal("Choose Specialization:").formatted(Formatting.YELLOW);
                int labelX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(chooseLabel)) / 2;
                context.drawText(client.textRenderer, chooseLabel, labelX, startY - 5, 0xFFFFFF, false);

                // Add warning text about permanent choice
                Text warningText = Text.literal("Warning: This choice is permanent!").formatted(Formatting.RED);
                int warningX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(warningText)) / 2;
                context.drawText(client.textRenderer, warningText, warningX, startY + 75, 0xFFFFFF, false);
            }
        } else if (!rankData.isMaxRank()) {
            // Show next upgrade
            GuardRank[] upgrades = rankData.getAvailableUpgrades();
            if (upgrades.length > 0) {
                GuardRank nextRank = upgrades[0];
                Text upgradeLabel = Text.literal("Next Rank: " + nextRank.getDisplayName()).formatted(Formatting.YELLOW);
                int labelX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(upgradeLabel)) / 2;
                context.drawText(client.textRenderer, upgradeLabel, labelX, startY - 5, 0xFFFFFF, false);
            }
        } else {
            // Max rank reached
            Text maxRankText = Text.literal("Maximum Rank Achieved!").formatted(Formatting.GOLD, Formatting.BOLD);
            int textX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(maxRankText)) / 2;
            context.drawText(client.textRenderer, maxRankText, textX, startY + 5, 0xFFFFFF, false);
        }
    }

    /**
     * Renders comprehensive cost information section.
     * Always shows costs regardless of player emerald count.
     */
    private void renderCostInformation(DrawContext context) {
        int startY = backgroundY + 175;

        // Background panel for cost information
        context.fill(backgroundX + 10, startY - 5, backgroundX + BACKGROUND_WIDTH - 10, startY + 35, 0xFF2A2A2A);
        context.drawBorder(backgroundX + 10, startY - 5, BACKGROUND_WIDTH - 20, 35, 0xFF404040);

        // Player emerald count - always visible
        int emeraldCount = getPlayerEmeraldCount();
        Text emeraldText = Text.literal("You have: " + emeraldCount + " emeralds").formatted(Formatting.GREEN);
        context.drawText(client.textRenderer, emeraldText, backgroundX + 15, startY, EMERALD_COLOR, false);

        // Cost information based on current rank
        GuardRank currentRank = rankData.getCurrentRank();
        if (currentRank == GuardRank.RECRUIT) {
            renderRecruitCostInfo(context, startY + 12);
        } else {
            renderUpgradeCostInfo(context, startY + 12, currentRank);
        }
    }

    /**
     * Renders cost information for recruit path selection.
     */
    private void renderRecruitCostInfo(DrawContext context, int y) {
        int meleePathCost = GuardRank.MAN_AT_ARMS_I.getEmeraldCost();
        int rangedPathCost = GuardRank.MARKSMAN_I.getEmeraldCost();
        int playerEmeralds = getPlayerEmeraldCount();

        // Path costs
        boolean canAffordMelee = playerEmeralds >= meleePathCost;
        boolean canAffordRanged = playerEmeralds >= rangedPathCost;

        String meleeText = String.format("⚔ Melee: %d emeralds", meleePathCost);
        context.drawText(client.textRenderer, Text.literal(meleeText),
            backgroundX + 15, y, canAffordMelee ? AFFORDABLE_COLOR : UNAFFORDABLE_COLOR, false);

        String rangedText = String.format("🏹 Ranged: %d emeralds", rangedPathCost);
        context.drawText(client.textRenderer, Text.literal(rangedText),
            backgroundX + 15, y + 12, canAffordRanged ? AFFORDABLE_COLOR : UNAFFORDABLE_COLOR, false);

        // Affordability status
        String statusText;
        int statusColor;
        if (!canAffordMelee && !canAffordRanged) {
            statusText = "❌ Cannot afford upgrades";
            statusColor = UNAFFORDABLE_COLOR;
        } else if (canAffordMelee && canAffordRanged) {
            statusText = "✓ Can afford both";
            statusColor = AFFORDABLE_COLOR;
        } else {
            statusText = "⚠ Partial affordability";
            statusColor = WARNING_COLOR;
        }

        int statusWidth = client.textRenderer.getWidth(statusText);
        context.drawText(client.textRenderer, Text.literal(statusText),
            backgroundX + BACKGROUND_WIDTH - statusWidth - 15, y + 6, statusColor, false);
    }

    /**
     * Renders cost information for rank upgrades.
     */
    private void renderUpgradeCostInfo(DrawContext context, int y, GuardRank currentRank) {
        GuardRank[] upgrades = rankData.getAvailableUpgrades();
        int playerEmeralds = getPlayerEmeraldCount();

        if (upgrades.length > 0) {
            GuardRank nextRank = upgrades[0];
            int cost = nextRank.getEmeraldCost();
            boolean canAfford = playerEmeralds >= cost;

            // Next rank cost
            String costText = String.format("Next rank cost: %d emeralds", cost);
            context.drawText(client.textRenderer, Text.literal(costText),
                backgroundX + 15, y, COST_LABEL_COLOR, false);

            // Affordability status
            String statusText;
            int statusColor;
            if (canAfford) {
                int remaining = playerEmeralds - cost;
                statusText = String.format("✓ Affordable (%d remaining)", remaining);
                statusColor = AFFORDABLE_COLOR;
            } else {
                int needed = cost - playerEmeralds;
                statusText = String.format("❌ Need %d more", needed);
                statusColor = UNAFFORDABLE_COLOR;
            }

            context.drawText(client.textRenderer, Text.literal(statusText),
                backgroundX + 15, y + 12, statusColor, false);
        } else if (rankData.isMaxRank()) {
            // Maximum rank achieved
            String maxRankText = "🏆 Maximum rank achieved";
            int textWidth = client.textRenderer.getWidth(maxRankText);
            int centerX = backgroundX + (BACKGROUND_WIDTH - textWidth) / 2;
            context.drawText(client.textRenderer, Text.literal(maxRankText),
                centerX, y + 6, 0xFFFFD700, false); // Gold color
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        LOGGER.debug("Closed GuardRankScreen for villager {}", targetVillager.getId());
    }
}