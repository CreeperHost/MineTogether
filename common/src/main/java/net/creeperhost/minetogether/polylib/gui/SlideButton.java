package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple button which remains 'active' once clicked, capable of being linked
 * to multiple other buttons to clear their 'active' state.
 * <p>
 * Created by covers1624 on 5/8/22.
 */
public class SlideButton extends Button {

    private float textScale = 1.0F;
    private boolean verticalText = false;
    private int autoScaleMargins = -1;

    private Function<Double, Component> messageGetter;
    private Supplier<Double> valueGetter;
    private Consumer<Double> valueSetter;
    private double min = 0;
    private double max = 1;

    private boolean dragging = false;
    private double prevValue = 0;
    private double nextValue = 0;
    private int rangeLeft = 0;
    private int rangeRight = 0;
    private boolean applyOnRelease = false;

    private Runnable onRelease = () -> {};

    public SlideButton(int x, int y, int width, int height) {
        this(x, y, width, height, Component.empty());
    }

    public SlideButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message, e -> {}, Button.DEFAULT_NARRATION);
    }

    @Override
    public Component getMessage() {
        return messageGetter == null ? super.getMessage() : messageGetter.apply(nextValue);
    }

    public SlideButton onRelease(Runnable onRelease) {
        this.onRelease = onRelease;
        return this;
    }

    public SlideButton setApplyOnRelease(boolean applyOnRelease) {
        this.applyOnRelease = applyOnRelease;
        return this;
    }

    public SlideButton setDynamicMessage(Function<Double, Component> messageGetter) {
        this.messageGetter = messageGetter;
        return this;
    }

    public SlideButton setDynamicMessage(Supplier<Component> messageGetter) {
        this.messageGetter = aDouble -> messageGetter.get();
        return this;
    }

    public SlideButton bindValue(Supplier<Double> valueGetter, Consumer<Double> valueSetter) {
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
        this.nextValue = valueGetter.get();
        return this;
    }

    public SlideButton setRange(double min, double max) {
        this.max = max;
        this.min = min;
        return this;
    }

    public void updateBounds(int x, int y, int width, int height) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.height = height; //Really??? There's a setter for everything except height?...
    }

    /**
     * When enabled text will automatically be scaled to with the button bounds with the specified margins at each end.
     * This will only scale down, and only if required.
     * If text scale is set then that scale will be used as the "target" scale but we will scale down if required.
     *
     * @param autoScaleMargins Minimum required space at ether end of the text, -1 to disable auto-scale.
     * @return The same button.
     */
    public SlideButton withAutoScaleText(int autoScaleMargins) {
        this.autoScaleMargins = autoScaleMargins;
        return this;
    }

    /**
     * Sets the scale for the text.
     *
     * @param scale The text scale.
     * @return The same button.
     */
    public SlideButton withTextScale(float scale) {
        textScale = scale;
        return this;
    }

// TODO Will get this working if we need it.
//    /**
//     * Sets if this button should render the text flipped.
//     *
//     * @return Render text vertically.
//     */
//    public SlideButton withVerticalText() {
//        verticalText = true;
//        return this;
//    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragging = true;
        int endOffset = width / 20;
        rangeLeft = getX() + endOffset;
        rangeRight = getX() + width - endOffset;
        prevValue = valueGetter.get();
        updateDrag(mouseX);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int i, double f, double g) {
        if (!dragging) return false;
        if (mouseY < getY() - 50 || mouseY > getY() + height + 50) {
            valueSetter.accept(prevValue);
            nextValue = prevValue;
            return true;
        }

        updateDrag(mouseX);

        return true;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (dragging) {
            dragging = false;
            onRelease.run();
            if (applyOnRelease) {
                valueSetter.accept(nextValue);
            }
        }

        return super.mouseReleased(d, e, i);
    }

    private void updateDrag(double mouseX) {
        double range = max - min;
        mouseX -= rangeLeft;
        double pos = mouseX / (rangeRight - rangeLeft);
        nextValue = Mth.clamp(min + (pos * range), min, max);
        if (!applyOnRelease) {
            valueSetter.accept(nextValue);
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int textColor = 0xFFFFFF;
        int fillColor = 0x80000000;
        int sliderColor = 0x64808080;

        if (isHovered || dragging) {
            sliderColor = 0xFFA0A0A0;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.fill(getX(), getY(), getX() + width, getY() + height, fillColor);

        int slideWidth = width / 10;
        double pos = (nextValue - min) / (max - min);
        int slidePos = (int) ((width - slideWidth) * pos);

        graphics.fill(getX() + slidePos, getY(), getX() + slidePos + slideWidth, getY() + height, sliderColor);


        Font font = Minecraft.getInstance().font;
        float scale = textScale;
        double lHeight = font.lineHeight * scale;
        double lWidth = font.width(getMessage()) * scale;

        int autoWidth = (verticalText ? height : width) - (autoScaleMargins * 2);
        if (autoScaleMargins > -1 && lWidth > autoWidth) {
            scale *= autoWidth / lWidth;
            lHeight = font.lineHeight * scale;
            lWidth = font.width(getMessage()) * scale;
        }

        graphics.pose().pushPose();
        if (verticalText) {
            graphics.pose().translate(getX() + lHeight + (width / 2D) - (lHeight / 2D), getY() + (height / 2D) - (lWidth / 2D), 0);
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0F, 0F, 90F * 0.017453292F));
        } else {
            graphics.pose().translate(getX() + (width / 2D) - (lWidth / 2D), getY() + (height / 2D) - (lHeight / 2D), 0);
        }

        graphics.pose().scale(scale, scale, scale);

        graphics.drawString(font, getMessage(), 0, 0, textColor);
        graphics.pose().popPose();
    }

}
