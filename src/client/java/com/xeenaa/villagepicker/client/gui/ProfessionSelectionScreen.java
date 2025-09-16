package com.xeenaa.villagepicker.client.gui;

import com.xeenaa.villagepicker.XeenaaVillagePicker;
import com.xeenaa.villagepicker.network.SelectProfessionPacket;
import com.xeenaa.villagepicker.registry.ProfessionData;
import com.xeenaa.villagepicker.registry.ProfessionManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;

public class ProfessionSelectionScreen extends Screen {
    private static final int BACKGROUND_WIDTH = 380;
    private static final int BACKGROUND_HEIGHT = 240; // Reduced height to fit content better
    private static final int BUTTON_WIDTH = 115;
    private static final int BUTTON_HEIGHT = 24;
    private static final int COLUMNS = 3;
    private static final int BUTTON_SPACING = 5;

    private final VillagerEntity villager;
    private ButtonWidget closeButton;
    private final java.util.List<ProfessionButton> professionButtons = new java.util.ArrayList<>();

    public ProfessionSelectionScreen(VillagerEntity villager) {
        super(Text.translatable("gui.xeenaa_village_picker.profession_selection"));
        this.villager = villager;
    }

    @Override
    protected void init() {
        super.init();

        // Clear existing buttons
        professionButtons.clear();

        // Calculate centered position
        int x = (this.width - BACKGROUND_WIDTH) / 2;
        int y = (this.height - BACKGROUND_HEIGHT) / 2;

        // Add profession buttons in a 3-column grid
        ProfessionManager manager = ProfessionManager.getInstance();
        int startX = x + 10; // Left margin
        int startY = y + 55; // Start below title and villager info
        int buttonCount = 0;
        int row = 0;
        int col = 0;

        for (var professionData : manager.getAllProfessionData()) {
            if (buttonCount >= 15) break; // Limit to 15 buttons (5 rows x 3 columns)

            // Skip blacklisted professions
            com.xeenaa.villagepicker.config.ModConfig config = com.xeenaa.villagepicker.config.ModConfig.getInstance();
            if (config.isProfessionBlacklisted(professionData.getId())) {
                continue; // Skip this profession
            }

            // Calculate button position in grid
            int buttonX = startX + col * (BUTTON_WIDTH + BUTTON_SPACING);
            int buttonY = startY + row * (BUTTON_HEIGHT + BUTTON_SPACING);

            ProfessionButton professionButton = new ProfessionButton(
                buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                professionData,
                button -> selectProfession(((ProfessionButton) button).getProfessionData())
            );

            professionButtons.add(professionButton);
            this.addDrawableChild(professionButton);

            buttonCount++;
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }

        // Add close button at bottom
        this.closeButton = ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
            .dimensions(x + BACKGROUND_WIDTH - 80, y + BACKGROUND_HEIGHT - 30, 70, 20)
            .build();
        this.addDrawableChild(this.closeButton);

        XeenaaVillagePicker.LOGGER.info("ProfessionSelectionScreen initialized with {} profession buttons", professionButtons.size());

        // Log profession count for testing
        ProfessionManager.ProfessionStats stats = ProfessionManager.getInstance().getStats();
        XeenaaVillagePicker.LOGGER.info("Available professions: {} total ({} vanilla, {} modded)",
            stats.total(), stats.vanilla(), stats.modded());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background first
        this.renderBackground(context, mouseX, mouseY, delta);

        // Calculate centered position
        int x = (this.width - BACKGROUND_WIDTH) / 2;
        int y = (this.height - BACKGROUND_HEIGHT) / 2;

        // Draw main panel
        context.fill(x, y, x + BACKGROUND_WIDTH, y + BACKGROUND_HEIGHT, 0xFF2C2C2C);

        // Draw simple border
        context.drawBorder(x, y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0xFFFFFFFF);

        // Draw title
        Text title = this.getTitle();
        int titleWidth = this.textRenderer.getWidth(title);
        int titleX = x + (BACKGROUND_WIDTH - titleWidth) / 2;
        context.drawText(this.textRenderer, title, titleX, y + 8, 0xFFFFFF, true);


        // Draw instructions
        Text instructions = Text.literal("Select a profession:");
        context.drawText(this.textRenderer, instructions, x + 10, y + 40, 0xFFAAAAA, true);

        // Render widgets (buttons) last so they appear on top
        for (var child : this.children()) {
            if (child instanceof Drawable drawable) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        XeenaaVillagePicker.LOGGER.info("ProfessionSelectionScreen closing");
        super.close();
    }

    /**
     * Handle profession selection when a button is clicked
     */
    private void selectProfession(ProfessionData professionData) {
        XeenaaVillagePicker.LOGGER.info("Player selected profession: {} for villager",
            professionData.getTranslatedName().getString());

        XeenaaVillagePicker.LOGGER.info("Sending profession change packet: villager={}, profession={}",
            villager.getId(), professionData.getId());

        // Create and send the packet to the server
        SelectProfessionPacket packet = new SelectProfessionPacket(villager.getId(), professionData.getId());
        ClientPlayNetworking.send(packet);

        // Close the GUI after selection
        this.close();
    }
}