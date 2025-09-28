package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.network.GuardProfessionChangePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Confirmation dialog shown when a player attempts to change a guard villager's profession.
 * Warns about emerald loss and allows the player to confirm or cancel the change.
 */
public class GuardProfessionChangeScreen extends Screen {
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 150;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 10;

    private final Screen parentScreen;
    private final int villagerEntityId;
    private final Identifier newProfessionId;
    private final int emeraldsToLose;
    private final String currentRankName;

    private ButtonWidget confirmButton;
    private ButtonWidget cancelButton;

    public GuardProfessionChangeScreen(Screen parentScreen, int villagerEntityId,
                                     Identifier newProfessionId, int emeraldsToLose,
                                     String currentRankName) {
        super(Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.title"));
        this.parentScreen = parentScreen;
        this.villagerEntityId = villagerEntityId;
        this.newProfessionId = newProfessionId;
        this.emeraldsToLose = emeraldsToLose;
        this.currentRankName = currentRankName;
    }

    @Override
    protected void init() {
        super.init();

        int dialogX = (width - DIALOG_WIDTH) / 2;
        int dialogY = (height - DIALOG_HEIGHT) / 2;

        // Create confirm button
        int confirmButtonX = dialogX + DIALOG_WIDTH / 2 - BUTTON_WIDTH - BUTTON_SPACING / 2;
        int buttonY = dialogY + DIALOG_HEIGHT - 35;

        confirmButton = ButtonWidget.builder(
            Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.confirm"),
            button -> confirmProfessionChange()
        ).dimensions(confirmButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
         .build();

        // Create cancel button
        int cancelButtonX = dialogX + DIALOG_WIDTH / 2 + BUTTON_SPACING / 2;

        cancelButton = ButtonWidget.builder(
            Text.translatable("gui.cancel"),
            button -> close()
        ).dimensions(cancelButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
         .build();

        addDrawableChild(confirmButton);
        addDrawableChild(cancelButton);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Custom background rendering without blur shader - prevents blurry appearance
        // Create a simple dark overlay without using Minecraft's blur effect
        context.fill(0, 0, this.width, this.height, 0x66000000); // Semi-transparent dark overlay
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render our custom background (no blur)
        this.renderBackground(context, mouseX, mouseY, delta);

        // Calculate dialog position
        int dialogX = (width - DIALOG_WIDTH) / 2;
        int dialogY = (height - DIALOG_HEIGHT) / 2;

        // Render dialog background with better contrast
        context.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF2C2C2C); // Solid dark gray
        context.drawBorder(dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, 0xFFFFFFFF); // White border

        // Render title with shadow for better readability
        Text title = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.title");
        int titleX = dialogX + (DIALOG_WIDTH - textRenderer.getWidth(title)) / 2;
        context.drawText(textRenderer, title, titleX, dialogY + 10, 0xFFFFFF, true);

        // Render warning message with shadow for clarity
        Text warningText = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.warning")
            .formatted(Formatting.YELLOW);
        int warningX = dialogX + 10;
        context.drawText(textRenderer, warningText, warningX, dialogY + 30, 0xFFFF00, true);

        // Render current rank info with shadow
        Text rankText = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.current_rank",
            currentRankName);
        context.drawText(textRenderer, rankText, warningX, dialogY + 45, 0xFFAAAAA, true);

        // Render new profession info
        String translationKey;
        if ("minecraft".equals(newProfessionId.getNamespace())) {
            // Vanilla profession
            translationKey = "entity.minecraft.villager." + newProfessionId.getPath();
        } else {
            // Modded profession - use full namespaced identifier
            translationKey = "entity.minecraft.villager." + newProfessionId.getNamespace() + "." + newProfessionId.getPath();
        }

        Text professionDisplayName = Text.translatable(translationKey);
        String translatedString = professionDisplayName.getString();

        // If translation fails, use formatted fallback
        if (translatedString.equals(translationKey) || translatedString.startsWith("entity.minecraft.villager.")) {
            // Format the profession name nicely
            String formattedName = newProfessionId.getPath().substring(0, 1).toUpperCase() +
                                 newProfessionId.getPath().substring(1).replace("_", " ");
            professionDisplayName = Text.literal(formattedName);
        }

        Text newProfessionText = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.new_profession",
            professionDisplayName);
        context.drawText(textRenderer, newProfessionText, warningX, dialogY + 60, 0xFFAAAAA, true);

        // Render emerald loss info
        Text emeraldText;
        if (emeraldsToLose > 0) {
            emeraldText = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.emerald_loss",
                emeraldsToLose).formatted(Formatting.RED);
        } else {
            emeraldText = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.no_emerald_loss")
                .formatted(Formatting.GRAY);
        }
        context.drawText(textRenderer, emeraldText, warningX, dialogY + 75,
            emeraldsToLose > 0 ? 0xFF5555 : 0xAAAAA, true);

        // Render confirmation message with shadow
        Text confirmText = Text.translatable("gui.xeenaa_villager_manager.guard_profession_change.confirm_message")
            .formatted(Formatting.RED);
        context.drawText(textRenderer, confirmText, warningX, dialogY + 95, 0xFF5555, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void confirmProfessionChange() {
        // Send confirmation packet to server
        GuardProfessionChangePacket packet = new GuardProfessionChangePacket(
            villagerEntityId,
            newProfessionId,
            true
        );
        ClientPlayNetworking.send(packet);

        // Close dialog and return to parent screen
        close();
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parentScreen);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}