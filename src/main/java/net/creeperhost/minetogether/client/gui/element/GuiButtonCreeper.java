package net.creeperhost.minetogether.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.lib.Constants;
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
        super.renderButton(x, y, partialTicks);
        if (this.visible)
        {
            ResourceLocation buttonImage = Constants.NO_BUTTON_ICON;
            buttonImage = Constants.CREEPER_HOST_BUTTON_LOCATION;
            Minecraft.getInstance().getTextureManager().bindTexture(buttonImage);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(this.x, this.y, index * 20, isHovered ? this.height : 0, this.width, this.height);
        }
    }
}
