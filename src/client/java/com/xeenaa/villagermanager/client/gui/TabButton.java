package com.xeenaa.villagermanager.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Custom button widget for tab navigation in the villager management interface.
 *
 * <p>This button provides visual feedback for active/inactive states and
 * handles proper text rendering with truncation for long tab names.</p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class TabButton extends ButtonWidget {
    private static final int TEXT_COLOR_ACTIVE = 0xFFFFFF;
    private static final int TEXT_COLOR_INACTIVE = 0xB0B0B0;
    private static final int TEXT_COLOR_HOVER = 0xFFFFAA;

    private final Text displayText;
    private boolean isActive;

    /**
     * Creates a new tab button.
     *
     * @param x the x position
     * @param y the y position
     * @param width the button width
     * @param height the button height
     * @param text the display text for the tab
     * @param onPress the action to perform when pressed
     * @param isActive whether this tab is currently active
     */
    public TabButton(int x, int y, int width, int height, Text text, PressAction onPress, boolean isActive) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.displayText = Objects.requireNonNull(text, "Display text cannot be null");
        this.isActive = isActive;
    }

    /**
     * Sets the active state of this tab button.
     *
     * @param active true if this tab is active, false otherwise
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Gets whether this tab button is currently active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Don't render background - parent will handle tab backgrounds
        // We only render the text here

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // Determine text color based on state
        int textColor = getTextColor(mouseX, mouseY);

        // Calculate text position (centered)
        String displayString = getDisplayString(textRenderer);
        int textWidth = textRenderer.getWidth(displayString);
        int textX = getX() + (getWidth() - textWidth) / 2;
        int textY = getY() + (getHeight() - textRenderer.fontHeight) / 2;

        // Render text with shadow for better readability
        context.drawText(textRenderer, displayString, textX, textY, textColor, true);

        // Handle tooltip for truncated text
        if (isHovered() && isTextTruncated(textRenderer)) {
            // The tooltip will be handled by the parent screen
        }
    }

    /**
     * Determines the appropriate text color based on button state.
     */
    private int getTextColor(int mouseX, int mouseY) {
        if (!active) {
            return TEXT_COLOR_INACTIVE;
        }

        if (isActive) {
            return isHovered() ? TEXT_COLOR_HOVER : TEXT_COLOR_ACTIVE;
        }

        return isHovered() ? TEXT_COLOR_HOVER : TEXT_COLOR_INACTIVE;
    }

    /**
     * Gets the display string, truncating if necessary to fit the button width.
     */
    private String getDisplayString(TextRenderer textRenderer) {
        String fullText = displayText.getString();
        int maxWidth = getWidth() - 8; // Leave 4px padding on each side

        if (textRenderer.getWidth(fullText) <= maxWidth) {
            return fullText;
        }

        // Truncate text and add ellipsis
        String ellipsis = "...";
        int ellipsisWidth = textRenderer.getWidth(ellipsis);
        int availableWidth = maxWidth - ellipsisWidth;

        if (availableWidth <= 0) {
            return ellipsis;
        }

        String truncated = textRenderer.trimToWidth(fullText, availableWidth);
        return truncated + ellipsis;
    }

    /**
     * Checks if the text is truncated in the current display.
     */
    private boolean isTextTruncated(TextRenderer textRenderer) {
        String fullText = displayText.getString();
        int maxWidth = getWidth() - 8;
        return textRenderer.getWidth(fullText) > maxWidth;
    }

    /**
     * Gets the full display text for tooltip purposes.
     *
     * @return the complete display text
     */
    public Text getDisplayText() {
        return displayText;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Provide accessibility narration
        this.appendDefaultNarrations(builder);

        if (isActive) {
            builder.put(NarrationPart.HINT, Text.translatable("gui.xeenaa_villager_manager.tab.active"));
        } else {
            builder.put(NarrationPart.HINT, Text.translatable("gui.xeenaa_villager_manager.tab.inactive"));
        }
    }
}