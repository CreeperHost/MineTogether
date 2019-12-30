package net.creeperhost.minetogether.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

public class GuiButtonMultiple extends Button
{
    private static ResourceLocation buttonImg = new ResourceLocation("creeperhost", "textures/gui.png");
    private final int index;
    
    public GuiButtonMultiple(int xPos, int yPos, int index, Button.IPressable onPress)
    {
        super(xPos, yPos, 20, 20, "", onPress);
        this.index = index;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            ResourceLocation buttonImage = buttonImg;
            Minecraft.getInstance().getTextureManager().bindTexture(buttonImage);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(this.x, this.y, index * 20, isHovered ? this.height : 0, this.width, this.height);
        }
    }
}
