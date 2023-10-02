package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiRectangle;
import net.creeperhost.polylib.client.modulargui.elements.GuiText;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * TODO, Turn this into a more general purpose context menu and move it to Polylib
 * Created by brandon3055 on 23/09/2023
 */
public class ContextMenu extends GuiElement<ContextMenu> implements BackgroundRender {

    private final int maxWidth;
    private LinkedList<GuiElement<?>> menuElements = new LinkedList<>();
    private Map<GuiElement<?>, Runnable> options = new HashMap<>();
    private double menuWidth = 0;
    private double menuHeight = 0;

    //When opened, should always be added to the root element to ensure it is on top of everything else.
    public ContextMenu(@NotNull GuiParent<?> parent) {
        this(parent, 300);
    }

    public ContextMenu(@NotNull GuiParent<?> parent, int maxWidth) {
        super(parent);
        this.maxWidth = maxWidth;

        constrain(WIDTH, dynamic(() -> menuWidth));
        constrain(HEIGHT, dynamic(() -> menuHeight));
    }

    public void addTitle(Component title) {
        addOption(title, null);
    }

    public void addOption(Component option, @Nullable Runnable runnable) {
        int height = font().wordWrapHeight(option, maxWidth);
        int width = Math.min(maxWidth, font().width(option));

        GuiElement<?> container = new GuiElement<>(this)
                .constrain(HEIGHT, literal(height + 3))
                .constrain(WIDTH, literal(width + 4));

        if (runnable != null) {
            GuiRectangle background = new GuiRectangle(container)
                    .constrain(TOP, match(container.get(TOP)))
                    .constrain(BOTTOM, match(container.get(BOTTOM)))
                    .constrain(LEFT, relative(get(LEFT), 2))
                    .constrain(RIGHT, relative(get(RIGHT), -2));
            background.fill(() -> background.hovered() ? 0x50FFFFFF : 0);
        }

        new GuiText(container, option)
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .constrain(LEFT, relative(container.get(LEFT), 2))
                .constrain(RIGHT, relative(container.get(RIGHT), -2))
                .constrain(TOP, relative(container.get(TOP), 1))
                .constrain(HEIGHT, literal(height));

        menuElements.add(container);
        if (runnable != null) {
            options.put(container, runnable);
        }
        arrangeElements();
    }

    private void arrangeElements() {
        double yOffset = 2;
        double width = 0;
        for (GuiElement<?> element : menuElements) {
            element.constrain(TOP, relative(get(TOP), +yOffset));
            element.constrain(LEFT, relative(get(LEFT), +2));
            yOffset += element.ySize();
            width = Math.max(width, element.xSize());
        }
        menuHeight = yOffset + 3;
        menuWidth = width + 4;
    }

    public void setPosition(double mouseX, double mouseY) {
        constrain(LEFT, literal(Math.min(mouseX, scaledScreenWidth() - xSize())));
        constrain(TOP, literal(Math.min(mouseY, scaledScreenHeight() - ySize())));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean consumed) {
        if (isMouseOver(mouseX, mouseY)) {
            for (GuiElement<?> element : menuElements) {
                if (element.isMouseOver(mouseX, mouseY) && options.containsKey(element)) {
                    options.get(element).run();
                    mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
            }
        }
        getParent().removeChild(this);
        return true;
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        render.toolTipBackground(xMin(), yMin(), xSize(), ySize(), 0xFF100010, 0xFF5000FF, 0xFF28007f);
    }
}
