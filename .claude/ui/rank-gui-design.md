# Guard Rank System - GUI Design

## Overview
Replace the current Equipment Tab with a new Rank Tab that provides rank progression, path selection, and upgrade management functionality.

## Tab Structure

### Main Rank Tab Layout
```
┌─────────────────────────────────────────────────────────────┐
│  [Profession Tab] [Rank Tab] [Other Tabs...]               │
├─────────────────────────────────────────────────────────────┤
│                    RANK MANAGEMENT                          │
│                                                             │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   CURRENT RANK  │    │      PROGRESSION TREE           │ │
│  │                 │    │                                 │ │
│  │  [Rank Icon]    │    │  [Path Selection / Rank Tree]  │ │
│  │  Rank Name      │    │                                 │ │
│  │  Health: XX HP  │    │                                 │ │
│  │  Attack: XX DMG │    │                                 │ │
│  │  [Abilities]    │    │                                 │ │
│  │                 │    │                                 │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                UPGRADE SECTION                          │ │
│  │                                                         │ │
│  │  Next Rank: [Name]        Cost: [X] Emeralds          │ │
│  │  Requirements: [Current Rank + Payment]                │ │
│  │  Player Emeralds: [Y] 💎                              │ │
│  │                                                         │ │
│  │         [UPGRADE RANK]  [CANCEL]                       │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Component Specifications

### Current Rank Display (Left Panel)
- **Size**: 180x220 pixels
- **Background**: Semi-transparent dark panel (0x80000000)
- **Contents**:
  - Rank icon (32x32) centered at top
  - Rank name (large text, centered)
  - Current stats list:
    - Health: XX HP ❤️
    - Attack: XX DMG ⚔️
    - Special abilities (if any)
  - Equipment preview (small icons showing cosmetic gear)

### Progression Tree (Right Panel)
- **Size**: 280x220 pixels
- **Background**: Semi-transparent dark panel (0x80000000)

#### For Recruit (Path Selection)
```
┌─────────────────────────────────────┐
│           CHOOSE YOUR PATH           │
│                                     │
│  ┌───────────┐      ┌─────────────┐ │
│  │  MELEE    │      │   RANGED    │ │
│  │ [⚔️ Icon] │      │  [🏹 Icon]  │ │
│  │Man-at-Arms│      │  Marksman   │ │
│  │           │      │             │ │
│  │ Cost: 10💎│      │ Cost: 10💎  │ │
│  │ [SELECT]  │      │ [SELECT]    │ │
│  └───────────┘      └─────────────┘ │
│                                     │
│ "Specialization unlocks progression" │
└─────────────────────────────────────┘
```

#### For Specialized Guards (Linear Progression)
```
┌─────────────────────────────────────┐
│         MELEE PROGRESSION            │
│                                     │
│  ✅ Recruit → ✅ Man-at-Arms I      │
│                                     │
│  🔒 Man-at-Arms II (20💎)          │
│  🔒 Man-at-Arms III (30💎)         │
│  🔒 Knight (40💎)                  │
│                                     │
│  Progress: ████▒▒▒▒ (2/5)          │
└─────────────────────────────────────┘
```

### Upgrade Section (Bottom Panel)
- **Size**: 460x100 pixels
- **Background**: Semi-transparent panel with green accent if can upgrade
- **Contents**:
  - Next rank information
  - Cost display with emerald icons
  - Player's current emerald count
  - Upgrade/Cancel buttons

## Widget Implementations

### RankDisplayWidget.java
```java
public class RankDisplayWidget extends PanelWidget {
    private final GuardRank currentRank;
    private final GuardRankData rankData;

    public RankDisplayWidget(int x, int y, GuardRank rank, GuardRankData data) {
        super(x, y, 180, 220, Text.literal("Current Rank"));
        this.currentRank = rank;
        this.rankData = data;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background panel
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);

        // Render rank icon
        Identifier rankIcon = currentRank.getIcon();
        context.drawTexture(rankIcon, getX() + 74, getY() + 10, 0, 0, 32, 32, 32, 32);

        // Render rank name
        Text rankName = Text.literal(currentRank.getDisplayName());
        context.drawCenteredTextWithShadow(textRenderer, rankName,
                                         getX() + getWidth()/2, getY() + 50, 0xFFFFFF);

        // Render stats
        renderStats(context);

        // Render equipment preview
        renderEquipmentPreview(context);
    }

    private void renderStats(DrawContext context) {
        RankStats stats = currentRank.getStats();
        int y = getY() + 75;

        // Health
        Text healthText = Text.literal("❤️ " + stats.getHealth() + " HP");
        context.drawTextWithShadow(textRenderer, healthText, getX() + 10, y, 0xFF5555);
        y += 15;

        // Attack damage
        Text attackText = Text.literal("⚔️ " + stats.getAttackDamage() + " DMG");
        context.drawTextWithShadow(textRenderer, attackText, getX() + 10, y, 0xFFAA00);
        y += 15;

        // Special abilities
        for (SpecialAbility ability : currentRank.getAbilities()) {
            Text abilityText = Text.literal("✨ " + ability.getDisplayName());
            context.drawTextWithShadow(textRenderer, abilityText, getX() + 10, y, 0xAA55FF);
            y += 15;
        }
    }
}
```

### PathSelectionWidget.java
```java
public class PathSelectionWidget extends PanelWidget {
    private final Consumer<GuardPath> onPathSelected;
    private final int playerEmeralds;

    public PathSelectionWidget(int x, int y, Consumer<GuardPath> onPathSelected, int emeralds) {
        super(x, y, 280, 220, Text.literal("Choose Path"));
        this.onPathSelected = onPathSelected;
        this.playerEmeralds = emeralds;

        // Add path selection buttons
        addPathButton(GuardPath.MELEE, x + 20, y + 50);
        addPathButton(GuardPath.RANGED, x + 160, y + 50);
    }

    private void addPathButton(GuardPath path, int x, int y) {
        GuardRank firstRank = GuardRankManager.getInstance().getRanksForPath(path).get(1); // Skip recruit

        ButtonWidget pathButton = ButtonWidget.builder(
            Text.literal(path.getDisplayName()),
            button -> {
                if (playerEmeralds >= firstRank.getEmeraldCost()) {
                    onPathSelected.accept(path);
                }
            })
            .dimensions(x, y, 100, 80)
            .build();

        // Custom render to show path info
        addDrawableChild(pathButton);
    }
}
```

### RankProgressionWidget.java
```java
public class RankProgressionWidget extends PanelWidget {
    private final GuardPath path;
    private final GuardRank currentRank;
    private final List<GuardRank> pathRanks;

    public RankProgressionWidget(int x, int y, GuardPath path, GuardRank currentRank) {
        super(x, y, 280, 220, Text.literal("Progression"));
        this.path = path;
        this.currentRank = currentRank;
        this.pathRanks = GuardRankManager.getInstance().getRanksForPath(path);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        // Render path title
        Text pathTitle = Text.literal(path.getDisplayName().toUpperCase() + " PROGRESSION");
        context.drawCenteredTextWithShadow(textRenderer, pathTitle,
                                         getX() + getWidth()/2, getY() + 15, 0xFFFFFF);

        // Render rank progression
        int y = getY() + 40;
        int currentIndex = pathRanks.indexOf(currentRank);

        for (int i = 0; i < pathRanks.size(); i++) {
            GuardRank rank = pathRanks.get(i);
            boolean unlocked = i <= currentIndex;
            boolean current = i == currentIndex;

            renderRankNode(context, getX() + 20, y, rank, unlocked, current);
            y += 25;
        }

        // Render progress bar
        renderProgressBar(context, currentIndex, pathRanks.size());
    }

    private void renderRankNode(DrawContext context, int x, int y, GuardRank rank, boolean unlocked, boolean current) {
        // Render status icon
        String statusIcon = unlocked ? "✅" : "🔒";
        if (current) statusIcon = "⭐";

        context.drawTextWithShadow(textRenderer, Text.literal(statusIcon), x, y, 0xFFFFFF);

        // Render rank name
        int color = unlocked ? 0xFFFFFF : 0x888888;
        if (current) color = 0x55FF55;

        Text rankText = Text.literal(rank.getDisplayName());
        if (!unlocked && rank.getEmeraldCost() > 0) {
            rankText = Text.literal(rank.getDisplayName() + " (" + rank.getEmeraldCost() + "💎)");
        }

        context.drawTextWithShadow(textRenderer, rankText, x + 20, y, color);
    }
}
```

### UpgradeWidget.java
```java
public class UpgradeWidget extends PanelWidget {
    private final GuardRank currentRank;
    private final GuardRank nextRank;
    private final int playerEmeralds;
    private final Runnable onUpgrade;

    public UpgradeWidget(int x, int y, GuardRank current, GuardRank next,
                        int emeralds, Runnable onUpgrade) {
        super(x, y, 460, 100, Text.literal("Upgrade"));
        this.currentRank = current;
        this.nextRank = next;
        this.playerEmeralds = emeralds;
        this.onUpgrade = onUpgrade;

        initializeButtons();
    }

    private void initializeButtons() {
        boolean canAfford = nextRank != null && playerEmeralds >= nextRank.getEmeraldCost();

        // Upgrade button
        ButtonWidget upgradeButton = ButtonWidget.builder(
            Text.literal("UPGRADE RANK"),
            button -> {
                if (canAfford) {
                    onUpgrade.run();
                }
            })
            .dimensions(getX() + 200, getY() + 60, 120, 20)
            .build();
        upgradeButton.active = canAfford;
        addDrawableChild(upgradeButton);

        // Cancel button
        ButtonWidget cancelButton = ButtonWidget.builder(
            Text.literal("CANCEL"),
            button -> {
                // Close upgrade interface
            })
            .dimensions(getX() + 330, getY() + 60, 80, 20)
            .build();
        addDrawableChild(cancelButton);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background color based on affordability
        boolean canAfford = nextRank != null && playerEmeralds >= nextRank.getEmeraldCost();
        int backgroundColor = canAfford ? 0x80004400 : 0x80440000; // Green or red tint

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);

        if (nextRank != null) {
            // Next rank info
            Text nextRankText = Text.literal("Next Rank: " + nextRank.getDisplayName());
            context.drawTextWithShadow(textRenderer, nextRankText, getX() + 10, getY() + 10, 0xFFFFFF);

            // Cost info
            Text costText = Text.literal("Cost: " + nextRank.getEmeraldCost() + " 💎");
            context.drawTextWithShadow(textRenderer, costText, getX() + 10, getY() + 25, 0x55FF55);

            // Player emeralds
            int emeraldColor = playerEmeralds >= nextRank.getEmeraldCost() ? 0x55FF55 : 0xFF5555;
            Text emeraldText = Text.literal("Your Emeralds: " + playerEmeralds + " 💎");
            context.drawTextWithShadow(textRenderer, emeraldText, getX() + 10, getY() + 40, emeraldColor);
        } else {
            // Max rank reached
            Text maxRankText = Text.literal("Maximum rank reached!");
            context.drawCenteredTextWithShadow(textRenderer, maxRankText,
                                             getX() + getWidth()/2, getY() + 25, 0xFFD700);
        }
    }
}
```

## Tab Integration

### RankTab.java
```java
public class RankTab extends Tab {
    private final VillagerEntity villager;
    private final GuardData guardData;
    private RankDisplayWidget rankDisplay;
    private Widget progressionWidget;
    private UpgradeWidget upgradeWidget;

    public RankTab(VillagerManagementScreen parent, VillagerEntity villager) {
        super(parent, "rank", Text.translatable("gui.xeenaa_villager_manager.tab.rank"));
        this.villager = villager;
        this.guardData = getGuardData(villager);

        initializeWidgets();
    }

    private void initializeWidgets() {
        GuardRankData rankData = guardData.getRankData();
        GuardRank currentRank = rankData.getCurrentRank();

        // Current rank display
        rankDisplay = new RankDisplayWidget(contentX + 10, contentY + 10, currentRank, rankData);
        addWidget(rankDisplay);

        // Progression widget (path selection or progression tree)
        if (rankData.canChoosePath()) {
            progressionWidget = new PathSelectionWidget(contentX + 200, contentY + 10,
                    this::handlePathSelection, getPlayerEmeralds());
        } else {
            progressionWidget = new RankProgressionWidget(contentX + 200, contentY + 10,
                    rankData.getChosenPath(), currentRank);
        }
        addWidget(progressionWidget);

        // Upgrade widget
        GuardRank nextRank = GuardRankManager.getInstance().getNextRank(currentRank);
        upgradeWidget = new UpgradeWidget(contentX + 10, contentY + 240, currentRank, nextRank,
                getPlayerEmeralds(), this::handleUpgrade);
        addWidget(upgradeWidget);
    }

    private void handlePathSelection(GuardPath path) {
        // Send path selection packet to server
        PathSelectionPacket packet = new PathSelectionPacket(villager.getUuid(), path);
        ClientPlayNetworking.send(packet);

        // Refresh widgets
        refreshWidgets();
    }

    private void handleUpgrade() {
        GuardRank nextRank = GuardRankManager.getInstance().getNextRank(
                guardData.getRankData().getCurrentRank());

        if (nextRank != null) {
            RankUpgradePacket packet = new RankUpgradePacket(villager.getUuid(), nextRank.getRankId());
            ClientPlayNetworking.send(packet);

            // Refresh widgets
            refreshWidgets();
        }
    }

    private void refreshWidgets() {
        clearWidgets();
        initializeWidgets();
    }
}
```

## Visual Assets Required

### Icons (32x32 PNG)
- `rank_recruit.png` - Basic guard icon
- `rank_man_at_arms_1.png` - Leather tunic
- `rank_man_at_arms_2.png` - Leather + iron helmet
- `rank_man_at_arms_3.png` - Partial iron armor
- `rank_knight.png` - Full iron armor
- `rank_marksman_1.png` - Bow + leather cap
- `rank_marksman_2.png` - Bow + more leather
- `rank_marksman_3.png` - Bow + full leather
- `rank_sharpshooter.png` - Elite archer

### Ability Icons (16x16 PNG)
- `ability_knockback.png` - Knockback strike effect
- `ability_piercing.png` - Piercing shot effect

### UI Elements
- `emerald_icon.png` - Small emerald for cost display
- `progress_bar_filled.png` - Filled progress bar segment
- `progress_bar_empty.png` - Empty progress bar segment

## Language Keys

### English (en_us.json)
```json
{
  "gui.xeenaa_villager_manager.tab.rank": "Rank",
  "gui.xeenaa_villager_manager.rank.current": "Current Rank",
  "gui.xeenaa_villager_manager.rank.choose_path": "Choose Your Path",
  "gui.xeenaa_villager_manager.rank.progression": "Progression",
  "gui.xeenaa_villager_manager.rank.upgrade": "Upgrade Rank",
  "gui.xeenaa_villager_manager.rank.cost": "Cost: %d Emeralds",
  "gui.xeenaa_villager_manager.rank.emeralds": "Emeralds: %d",
  "gui.xeenaa_villager_manager.rank.max_rank": "Maximum rank reached!",
  "gui.xeenaa_villager_manager.rank.cannot_afford": "Not enough emeralds",
  "gui.xeenaa_villager_manager.rank.melee_path": "Melee Path",
  "gui.xeenaa_villager_manager.rank.ranged_path": "Ranged Path",

  "rank.xeenaa_villager_manager.recruit": "Recruit",
  "rank.xeenaa_villager_manager.man_at_arms_1": "Man-at-Arms I",
  "rank.xeenaa_villager_manager.man_at_arms_2": "Man-at-Arms II",
  "rank.xeenaa_villager_manager.man_at_arms_3": "Man-at-Arms III",
  "rank.xeenaa_villager_manager.knight": "Knight",
  "rank.xeenaa_villager_manager.marksman_1": "Marksman I",
  "rank.xeenaa_villager_manager.marksman_2": "Marksman II",
  "rank.xeenaa_villager_manager.marksman_3": "Marksman III",
  "rank.xeenaa_villager_manager.sharpshooter": "Sharpshooter"
}
```