package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A simple {@link Button} implementation using an indexed icon
 * from the given texture sheet.
 * <p>
 * Expects 3 rows of at most 12 icons with the size of 20x20.<br>
 * Row 1, Active, non-hovered.<br>
 * Row 2, Active, hovered.<br>
 * Row 3, Inactive.<br>
 * <p>
 *
 * @author covers1624
 */
// TODO This can easily be expanded to support indexes higher than 12, and buttons with a size less than 20.
public class IconButton extends Button {

    private final ResourceLocation sheet;
    private final int index;

    public IconButton(int x, int y, int index, ResourceLocation sheet, OnPress onPress) {
        super(x, y, 20, 20, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.index = index;
        this.sheet = sheet;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTicks) {
        if (!visible) return;

        int yOffset = !active ? 40 : isHovered ? 20 : 0;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(sheet, getX(), getY(), index * 20, yOffset, width, height);
    }
}
