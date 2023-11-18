package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simple slider control.
 * <p>
 * Created by brandon3055 on 06/29/2023
 */
public class SlideButton extends Button {

    private float textScale = 1.0F;
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
    private Supplier<Boolean> enabled = null;

    public SlideButton(int x, int y, int width, int height) {
        this(x, y, width, height, TextComponent.EMPTY);
    }

    public SlideButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message, e -> {});
    }

    public SlideButton setEnabled(Supplier<Boolean> enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled == null || enabled.get();
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
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!isEnabled()) return;
        dragging = true;
        int endOffset = width / 20;
        rangeLeft = x + endOffset;
        rangeRight = x + width - endOffset;
        prevValue = valueGetter.get();
        updateDrag(mouseX);
    }

//    @Override
//    public void mouseMoved(double d, double e) {
//        super.mouseMoved(d, e);
//    }

    public void mouseMove(double mouseX, double mouseY) {
        if (!dragging) return;
        if (mouseY < y - 50 || mouseY > y + height + 50) {
            valueSetter.accept(prevValue);
            nextValue = prevValue;
            return;
        }

        updateDrag(mouseX);
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
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        if (!isEnabled()) return;
        int textColor = 0xFFFFFF;
        int fillColor = 0x80000000;
        int sliderColor = 0x64808080;

        if (isHovered || dragging) {
            sliderColor = 0xFFA0A0A0;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        fill(poseStack, x, y, x + width, y + height, fillColor);

        int slideWidth = width / 10;
        double pos = (nextValue - min) / (max - min);
        int slidePos = (int) ((width - slideWidth) * pos);

        fill(poseStack, x + slidePos, y, x + slidePos + slideWidth, y + height, sliderColor);

        Font font = Minecraft.getInstance().font;
        float scale = textScale;
        double lHeight = font.lineHeight * scale;
        double lWidth = font.width(getMessage()) * scale;

        int autoWidth = width - (autoScaleMargins * 2);
        if (autoScaleMargins > -1 && lWidth > autoWidth) {
            scale *= autoWidth / lWidth;
            lHeight = font.lineHeight * scale;
            lWidth = font.width(getMessage()) * scale;
        }

        poseStack.pushPose();
        poseStack.translate(x + (width / 2D) - (lWidth / 2D), y + (height / 2D) - (lHeight / 2D), 0);

        poseStack.scale(scale, scale, 1);

        drawString(poseStack, font, getMessage(), 0, 0, textColor);

        poseStack.popPose();
    }
}