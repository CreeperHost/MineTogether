package net.creeperhost.minetogether.gui.dialogs;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Rectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.literal;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 14/10/2023
 */
public class OptionDialog extends GuiElement<OptionDialog> implements BackgroundRender {

    private final GuiButton[] buttons;

    public OptionDialog(@NotNull GuiParent<?> parent, Component title, Component... buttonLabels) {
        super(parent);
        this.setOpaque(true);
        int buttonCount = buttonLabels.length;
        this.buttons = new GuiButton[buttonCount];
        if (buttonCount == 0) throw new IllegalStateException("OptionDialog must have at least one button");

        //Buttons
        Constraint left = relative(get(LEFT), 5);
        Constraint right = relative(get(RIGHT), -5);
        for (int i = 0; i < buttonCount; i++) {
            int finalI = i;
            buttons[i] = MTStyle.Flat.button(this, buttonLabels[i])
                    .onPress(this::close)
                    .constrain(BOTTOM, relative(get(BOTTOM), -5))
                    .constrain(HEIGHT, literal(14))
                    .constrain(LEFT, dynamic(() -> left.get() + ((((right.get() + 1) - left.get()) / buttonCount) * finalI)).precise())
                    .constrain(WIDTH, dynamic(() -> (((right.get() + 1) - left.get()) / buttonCount) - 1).precise());
        }

        //Title
        GuiText titleText = new GuiText(this, title)
                .setWrap(true)
                .constrain(TOP, relative(get(TOP), 5))
                .constrain(BOTTOM, relative(buttons[0].get(TOP), -3))
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, relative(get(RIGHT), -5));

        //Position Dialog
        ModularGui gui = getModularGui();
        constrain(TOP, midPoint(gui.get(TOP), gui.get(BOTTOM), -30));
        constrain(LEFT, midPoint(gui.get(LEFT), gui.get(RIGHT), -100));
        constrain(WIDTH, literal(200));
        constrain(HEIGHT, literal(60));
    }

    public static GuiButton button(GuiElement<?> parent, Component label) {
        GuiButton button = new GuiButton(parent);
        GuiRectangle background = new GuiRectangle(button)
                .fill(() -> button.isDisabled() ? 0x88202020 : (button.isMouseOver() || button.toggleState() || button.isPressed() ? 0xFF909090 : 0xFF505050));
        Constraints.bind(background, button);

        GuiText text = new GuiText(button, label);
        button.setLabel(text);
        Constraints.bind(text, button, 0, 2, 0, 2);
        return button;
    }

    public OptionDialog onButtonPress(int button, Runnable run) {
        buttons[button].onPress(() -> {
            run.run();
            close();
        });
        return this;
    }

    public void close() {
        getParent().removeChild(this);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        return true;
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        render.toolTipBackground(xMin(), yMin(), xSize(), ySize(), 0xFF100010, 0xFF5000FF, 0xFF28007f);
    }
}