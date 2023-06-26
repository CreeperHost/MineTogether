package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple button which remains 'active' once clicked, capable of being linked
 * to multiple other buttons to clear their 'active' state.
 * <p>
 * Created by covers1624 on 5/8/22.
 */
public class RadioButton extends Button {

    private final List<RadioButton> otherButtons = new LinkedList<>();
    private final List<OnPress> actions = new LinkedList<>();

    private float textScale = 1.0F;
    private boolean verticalText = false;

    private boolean pressed;

    public RadioButton(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, text, e -> {
        }, Button.DEFAULT_NARRATION);
    }

    /**
     * Sets the scale for the text.
     *
     * @param scale The text scale.
     * @return The same button.
     */
    public RadioButton withTextScale(float scale) {
        textScale = scale;
        return this;
    }

    /**
     * Sets if this button should render the text flipped.
     *
     * @return Render text vertically.
     */
    public RadioButton withVerticalText() {
        verticalText = true;
        return this;
    }

    /**
     * Add an action callback when this button is pressed.
     *
     * @param action The action callback.
     * @return The same button.
     */
    public RadioButton onPressed(OnPress action) {
        actions.add(action);
        return this;
    }

    /**
     * Links all the supplied buttons to this button.
     *
     * @param buttons The other buttons, if this button is supplied it will be ignored.
     */
    public void linkButtons(RadioButton... buttons) {
        Collections.addAll(otherButtons, buttons);
        otherButtons.remove(this);
    }

    @Override
    public void onPress() {
        for (OnPress action : actions) {
            action.onPress(this);
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int textColor = 0xFFFFFF;
        int fillColor = 200 / 2 << 24;
        if (isHovered || isPressed()) {
            textColor = 0xffffa0;
            fillColor = 256 / 2 << 24;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.fill(getX(), getY(), getX() + width, getY() + height, fillColor);

        Font font = Minecraft.getInstance().font;
        int lHeight = (int) (font.lineHeight * textScale);

        graphics.pose().pushPose();
        graphics.pose().translate(getX() + (lHeight / 2D) + (width / 2D), getY() + (height - 8D) / 2, 20);
        if (verticalText) {
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(-1F * 0.017453292F, 0F, 90F * 0.017453292F));
        }
        graphics.pose().scale(textScale, textScale, textScale);
        graphics.drawCenteredString(font, getMessage(), 0, 0, textColor);
        graphics.pose().popPose();
    }

    @Override
    public void onClick(double d, double e) {
        super.onClick(d, e);
        selectButton();
    }

    /**
     * Selects the button, but does not fire the click event.
     */
    public void selectButton() {
        pressed = true;
        for (RadioButton button : otherButtons) {
            button.pressed = false;
        }
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isPressed() {
        return pressed;
    }
}
