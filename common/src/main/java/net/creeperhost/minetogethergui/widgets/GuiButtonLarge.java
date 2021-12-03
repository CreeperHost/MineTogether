package net.creeperhost.minetogethergui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GuiButtonLarge extends Button
{
    private final String description;
    private final ItemStack stack;
    
    public GuiButtonLarge(int x, int y, int widthIn, int heightIn, String buttonText, String description, ItemStack stack, Button.OnPress onPress)
    {
        super(x, y, widthIn, heightIn, new TranslatableComponent(buttonText), onPress);
        this.width = 200;
        this.height = 20;
        this.visible = true;
        this.active = true;
        this.x = x;
        this.y = y;
        this.width = widthIn;
        this.height = heightIn;
        this.setMessage(new TranslatableComponent(buttonText));
        this.description = description;
        this.stack = stack;
    }
    
    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getYImage(this.isHovered);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            ScreenHelpers.drawContinuousTexturedBox(matrixStack, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
            this.renderBg(matrixStack, mc, mouseX, mouseY);
            int color = 14737632;
            
            List<FormattedCharSequence> newstring = ComponentRenderUtils.wrapComponents(new TranslatableComponent(description), width -12, mc.font);
            int start = y + 40;

            for (FormattedCharSequence s : newstring)
            {
                int left = ((this.x + 4));
                mc.font.drawShadow(matrixStack, s, left, start += 10, -1);
            }
            
            Component buttonText = this.getMessage();
            
            drawCenteredString(matrixStack, mc.font, buttonText, this.x + this.width / 2, this.y + 10, color);
            ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
            matrixStack.pushPose();
            renderItem.renderGuiItem(stack, (this.x) + (width / 2) - 8, (this.y) + 24);
            matrixStack.popPose();
        }
    }
}
