package com.xeenaa.villagepicker.client.gui;

import com.xeenaa.villagepicker.registry.ProfessionData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Custom button widget for displaying professions with icons
 */
public class ProfessionButton extends ButtonWidget {
    private final ProfessionData professionData;
    private final ItemStack icon;

    public ProfessionButton(int x, int y, int width, int height, ProfessionData professionData, PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER); // Use empty text to avoid duplicate
        this.professionData = professionData;
        this.icon = professionData.getIcon();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the button background first
        super.renderWidget(context, mouseX, mouseY, delta);

        // Draw the icon on the left side of the button
        if (!icon.isEmpty()) {
            int iconX = this.getX() + 4; // 4px padding from left
            int iconY = this.getY() + (this.getHeight() - 16) / 2; // Center vertically
            context.drawItem(icon, iconX, iconY);
        }

        // Draw the profession name next to the icon with pixel-perfect positioning
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text displayText = professionData.getTranslatedName();
        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;

        // Calculate exact text position (after icon + padding)
        int textX = this.getX() + 24; // 4px margin + 16px icon + 4px padding
        int textY = this.getY() + (this.getHeight() - textRenderer.fontHeight) / 2;

        // Ensure text fits in button by truncating if necessary
        int maxTextWidth = this.getWidth() - 28; // Account for icon and padding
        String displayString = displayText.getString();
        if (textRenderer.getWidth(displayString) > maxTextWidth) {
            displayString = textRenderer.trimToWidth(displayString, maxTextWidth - textRenderer.getWidth("...")) + "...";
        }

        // Render text without shadow for crisp appearance
        context.drawText(textRenderer, displayString, textX, textY, textColor, false);
    }

    public ProfessionData getProfessionData() {
        return professionData;
    }
}