package net.creeperhost.minetogether.gui.element;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class ButtonMap extends FancyButton
{
    String buttonText;

    public ButtonMap(int id, int xPos, int yPos, int width, int height, String displayString, boolean active, FancyButton.IPressable pressedAction)
    {
        super(id, xPos, yPos, width, height, displayString, pressedAction);
        this.buttonText = displayString;
//        tooltiplist.add(new StringTextComponent(buttonText));
        this.enabled = active;
    }

    @Override
    public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partial)
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        ResourceLocation map = new ResourceLocation(CreeperHost.MOD_ID, "textures/map/" + buttonText + ".png");
        minecraft.getTextureManager().bindTexture(map);

        if(isMouseOver())
        {
            GlStateManager.color(0F, 1F, 0F, 1.0F);
        }
//        if(isFocused())
//        {
//            GlStateManager.color(0F, 0.6F, 0F, 1.0F);
//        }
        if(!enabled)
        {
            GlStateManager.color(0.4F, 0.4F, 0.4F, 1.0F);
        }
        Gui.drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, 0, 0, this.width, this.height, this.width, this.height);
        GlStateManager.color(1F, 1F, 1F, 1.0F);
    }
}
