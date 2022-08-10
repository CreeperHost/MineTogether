package net.creeperhost.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
        super(x, y, width, height, text, e -> { });
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
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        isHovered = pressed || mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        int textColor = 0xFFFFFF;
        int fillColor = 200 / 2 << 24;
        if (isHovered) {
            textColor = 0xffffa0;
            fillColor = 256 / 2 << 24;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        fill(pStack, x, y, x + width, y + height, fillColor);

        Font font = Minecraft.getInstance().font;
        int lHeight = (int) (font.lineHeight * textScale);

        pStack.pushPose();
        pStack.translate(x + (lHeight / 2D) + (width / 2D), y + (height - 8D) / 2, 20);
        if (verticalText) {
            pStack.mulPose(new Quaternion(-1, 0, 90, true));
        }
        pStack.scale(textScale, textScale, textScale);
        drawCenteredString(pStack, font, getMessage(), 0, 0, textColor);
        pStack.popPose();
    }

    @Override
    public void onClick(double d, double e) {
        super.onClick(d, e);
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
