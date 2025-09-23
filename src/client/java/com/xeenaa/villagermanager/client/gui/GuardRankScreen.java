package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import com.xeenaa.villagermanager.network.PurchaseRankPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
    private static final int BACKGROUND_HEIGHT = 192;

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
            meleePathButton = ButtonWidget.builder(Text.literal("Melee Path"), button -> purchaseRank(GuardRank.GUARD_MELEE))
                    .dimensions(backgroundX + 20, backgroundY + 120, 100, 20)
                    .build();
            addDrawableChild(meleePathButton);

            rangedPathButton = ButtonWidget.builder(Text.literal("Ranged Path"), button -> purchaseRank(GuardRank.GUARD_RANGED))
                    .dimensions(backgroundX + 136, backgroundY + 120, 100, 20)
                    .build();
            addDrawableChild(rangedPathButton);
        } else {
            // Upgrade button for existing ranks
            GuardRank[] upgrades = rankData.getAvailableUpgrades();
            if (upgrades.length > 0) {
                GuardRank nextRank = upgrades[0];
                Text buttonText = Text.literal("Upgrade to " + nextRank.getDisplayName())
                        .append(Text.literal(" (" + nextRank.getEmeraldCost() + " emeralds)"));

                upgradeButton = ButtonWidget.builder(buttonText, button -> purchaseRank(nextRank))
                        .dimensions(backgroundX + 20, backgroundY + 120, 216, 20)
                        .build();
                addDrawableChild(upgradeButton);
            }
        }
    }

    private void updateButtonStates() {
        int playerEmeralds = getPlayerEmeraldCount();

        if (meleePathButton != null) {
            meleePathButton.active = playerEmeralds >= GuardRank.GUARD_MELEE.getEmeraldCost();
        }

        if (rangedPathButton != null) {
            rangedPathButton.active = playerEmeralds >= GuardRank.GUARD_RANGED.getEmeraldCost();
        }

        if (upgradeButton != null) {
            GuardRank[] upgrades = rankData.getAvailableUpgrades();
            if (upgrades.length > 0) {
                upgradeButton.active = playerEmeralds >= upgrades[0].getEmeraldCost();
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

        // Render player emerald count
        renderPlayerEmeralds(context);

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
    }

    private void renderUpgradeOptions(DrawContext context) {
        int startY = backgroundY + 110;

        if (rankData.getCurrentRank() == GuardRank.RECRUIT) {
            // Show path selection
            Text chooseLabel = Text.literal("Choose Specialization:").formatted(Formatting.YELLOW);
            int labelX = backgroundX + (BACKGROUND_WIDTH - client.textRenderer.getWidth(chooseLabel)) / 2;
            context.drawText(client.textRenderer, chooseLabel, labelX, startY - 5, 0xFFFFFF, false);
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

    private void renderPlayerEmeralds(DrawContext context) {
        int emeraldCount = getPlayerEmeraldCount();
        Text emeraldText = Text.literal("Emeralds: " + emeraldCount).formatted(Formatting.GREEN);
        context.drawText(client.textRenderer, emeraldText, backgroundX + 20, backgroundY + 160, 0xFFFFFF, false);
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