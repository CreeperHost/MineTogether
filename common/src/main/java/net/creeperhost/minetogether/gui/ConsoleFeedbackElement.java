package net.creeperhost.minetogether.gui;

import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.lib.ForegroundRender;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Simple element that allows you to sent chat style feedback messages to the user.
 * Created by brandon3055 on 11/05/2024
 */
public class ConsoleFeedbackElement extends GuiElement<ConsoleFeedbackElement> implements ForegroundRender {

    private final Map<Component, Long> DISPLAY_LIST = new IdentityHashMap<>();
    private final LinkedList<Component> DISPLAY_ORDER = new LinkedList<>();
    private long displayTime = 5000;
    private long fadeOutTime = 1000;
    private int maxWidth = 256;
    private Position position = Position.BOTTOM_LEFT;

    public ConsoleFeedbackElement(@NotNull GuiParent<?> parent) {
        super(parent);
    }

    public ConsoleFeedbackElement setDisplayTime(long displayTime) {
        this.displayTime = displayTime;
        return this;
    }

    public ConsoleFeedbackElement setFadeOutTime(long fadeOutTime) {
        this.fadeOutTime = fadeOutTime;
        return this;
    }

    public ConsoleFeedbackElement setPosition(Position position) {
        this.position = position;
        return this;
    }

    public ConsoleFeedbackElement setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public ConsoleFeedbackElement addMessage(Component message) {
        synchronized (DISPLAY_LIST) {
            DISPLAY_LIST.put(message, System.currentTimeMillis());
            DISPLAY_ORDER.addFirst(message);
        }
        return this;
    }

    @Override
    public void tick(double mouseX, double mouseY) {
        super.tick(mouseX, mouseY);
        synchronized (DISPLAY_LIST) {
            DISPLAY_LIST.entrySet().removeIf(e -> System.currentTimeMillis() > e.getValue() + displayTime + fadeOutTime);
            DISPLAY_ORDER.removeIf(e -> !DISPLAY_LIST.containsKey(e));
        }
    }

    @Override
    public void renderInFront(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        int inset = 5;
        int bgColour = 0x000000;

        synchronized (DISPLAY_LIST) {
            int yOffset = 0;
            for (Component message : DISPLAY_ORDER) {
                long time = System.currentTimeMillis() - DISPLAY_LIST.get(message) - displayTime;
                double fadeOut = 1F - (fadeOutTime > 0 && time > 0 ? time / (float) fadeOutTime : 0F);
                int colour = (int) (fadeOut * 0xFF) << 24;
                int colour2 = (int) (fadeOut * 0x90) << 24;
                if (time >= fadeOutTime - 20) continue;
                List<FormattedCharSequence> lines = font().split(message, maxWidth);
                yOffset += lines.size() * font().lineHeight;

                int i = 0;
                if (position == Position.BOTTOM_LEFT || position == Position.BOTTOM_RIGHT) {
                    i += font().lineHeight;
                } else {
                    i -= font().lineHeight * lines.size();
                }

                for (FormattedCharSequence line : lines) {
                    int lineWidth = font().width(line);
                    switch (position) {
                        case TOP_LEFT -> {
                            render.rect(xMin() + inset, yMin() + inset + yOffset + i, lineWidth + 2, font().lineHeight, colour2 | bgColour);
                            render.drawString(line, xMin() + inset + 1, yMin() + inset + yOffset + i, colour | 0xFFFFFF, false);
                        }
                        case TOP_RIGHT -> {
                            render.rect(xMax() - lineWidth - inset, yMin() + inset + yOffset + i, lineWidth + 2, font().lineHeight, colour2 | bgColour);
                            render.drawString(line, xMax() - lineWidth - inset + 1, yMin() + inset + yOffset + i, colour | 0xFFFFFF, false);
                        }
                        case BOTTOM_LEFT -> {
                            render.rect(xMin() + inset, yMax() - inset - yOffset - font().lineHeight + i, lineWidth + 2, font().lineHeight, colour2 | bgColour);
                            render.drawString(line, xMin() + inset + 1, yMax() - inset - yOffset - font().lineHeight + i, colour | 0xFFFFFF, false);
                        }
                        case BOTTOM_RIGHT -> {
                            render.rect(xMax() - lineWidth - inset, yMax() - inset - yOffset - font().lineHeight + i, lineWidth + 2, font().lineHeight, colour2 | bgColour);
                            render.drawString(line, xMax() - lineWidth - inset + 1, yMax() - inset - yOffset - font().lineHeight + i, colour | 0xFFFFFF, false);
                        }
                    }
                    i += font().lineHeight;
                }
            }
        }
    }

    public enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}
