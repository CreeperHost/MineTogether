package net.creeperhost.minetogether.gui.element;

import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.common.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

public class GuiButtonCreeper extends Button
{
    private static ResourceLocation buttonImg = new ResourceLocation("creeperhost", "textures/nobrand.png");
    private final int index;
    
    public GuiButtonCreeper(int xPos, int yPos, int index, Button.IPressable onPress)
    {
        super(xPos, yPos, 20, 20, "", onPress);
        this.index = index;
    }
    
    public GuiButtonCreeper(int xPos, int yPos, Button.IPressable onPress)
    {
        this(xPos, yPos, 0, onPress);
    }

    @Override
    public void renderButton(int x, int y, float partialTicks)
    {
        if (this.visible)
        {
            ResourceLocation buttonImage = buttonImg;
            if (Config.getInstance().isServerHostButtonImage())
                buttonImage = CreeperHost.instance.getImplementation().getButtonIcon();
            Minecraft.getInstance().getTextureManager().bindTexture(buttonImage);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            //TODO
//            boolean over = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
//            this.drawTexturedModalRect(this.xPosition, this.yPosition, index * 20, over ? this.height : 0, this.width, this.height);
        }
    }
}
