package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.network.chat.TextComponent.EMPTY;

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
        this(x, y, index, sheet, onPress, NO_TOOLTIP);
    }

    public IconButton(int x, int y, int index, ResourceLocation sheet, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, 20, 20, EMPTY, onPress, onTooltip);
        this.index = index;
        this.sheet = sheet;
    }

    @Override
    public void renderButton(PoseStack pStack, int mx, int my, float partialTicks) {
        if (!visible) return;

        int yOffset = !active ? 40 : isHovered ? 20 : 0;
        Minecraft.getInstance().getTextureManager().bind(sheet);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(pStack, x, y, index * 20, yOffset, width, height);
    }
}
