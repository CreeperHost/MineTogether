package net.creeperhost.polylib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.minecraft.network.chat.TextComponent.EMPTY;

/**
 * A simple button which renders as a single string with no background.
 * <p>
 * Be sure to call {@link #tick()} from your GUI tick method to update the text
 * supplied by the text supplier.
 * <p>
 * If the text evaluates to empty, the button is disabled and set to be invisible.
 *
 * @author covers1624
 */
public class StringButton extends Button {

    private final Supplier<Component> textSupplier;
    private final boolean centered;

    @Nullable
    private Component cachedText;

    public StringButton(int i, int j, int k, int l, Supplier<Component> textSupplier, OnPress onPress) {
        this(i, j, k, l, true, textSupplier, onPress);
    }

    public StringButton(int x, int y, int width, int height, boolean centered, Supplier<Component> textSupplier, OnPress onPress) {
        super(x, y, width, height, EMPTY, onPress, NO_TOOLTIP);
        this.textSupplier = textSupplier;
        this.centered = centered;
    }

    @Override
    public void renderButton(PoseStack pStack, int mx, int my, float partialTicks) {
        if (cachedText == null) return;
        if (centered) {
            GuiComponent.drawCenteredString(pStack, Minecraft.getInstance().font, cachedText, x + width / 2, y + (height - 8) / 2, 0xFFFFFF);
        } else {
            GuiComponent.drawString(pStack, Minecraft.getInstance().font, cachedText, x, y, 0xFFFFFF);
        }
    }

    public void tick() {
        cachedText = textSupplier.get();
        visible = cachedText != null && !cachedText.toString().isEmpty();
    }
}
