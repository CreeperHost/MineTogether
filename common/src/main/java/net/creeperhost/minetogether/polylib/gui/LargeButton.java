package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LargeButton extends Button {

    private final Component description;
    private final ItemStack stack;

    public LargeButton(int x, int y, int widthIn, int heightIn, Component buttonText, Component description, ItemStack stack, OnPress onPress) {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        this.x = x;
        this.y = y;
        width = widthIn;
        height = heightIn;
        this.description = description;
        this.stack = stack;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partial) {
        if (visible) {
            Minecraft mc = Minecraft.getInstance();
            isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            int k = getYImage(isHovered);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            ScreenHelper.drawContinuousTexturedBox(matrixStack, x, y, 0, 46 + k * 20, width, height, 200, 20, 2, 3, 2, 2, getBlitOffset());
            renderBg(matrixStack, mc, mouseX, mouseY);
            int color = 14737632;

            List<FormattedCharSequence> newstring = ComponentRenderUtils.wrapComponents(description, width - 12, mc.font);
            int start = y + 40;

            for (FormattedCharSequence s : newstring) {
                int left = ((x + 4));
                mc.font.drawShadow(matrixStack, s, left, start += 10, -1);
            }

            drawCenteredString(matrixStack, mc.font, getMessage(), x + width / 2, y + 10, color);
            ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
            matrixStack.pushPose();
            renderItem.renderGuiItem(stack, (x) + (width / 2) - 8, (y) + 24);
            matrixStack.popPose();
        }
    }
}
