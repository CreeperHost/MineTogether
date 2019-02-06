package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonRefresh extends GuiButton
{
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("creeperhost", "textures/gui.png");
    private final int index;

    public GuiButtonRefresh(int buttonID, int xPos, int yPos, int index)
    {
        super(buttonID, xPos, yPos, 20, 20, "");
        this.index = index;
    }

    public GuiButtonRefresh(int buttonID, int xPos, int yPos)
    {
        this(buttonID, xPos, yPos, 0);
    }
    
    public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_)
    {
        myDrawButton(p_191745_1_, p_191745_2_, p_191745_3_);
    }
    
    // < 1.12 compat
    public void func_146112_a(Minecraft mc, int mouseX, int mouseY)
    {
        myDrawButton(mc, mouseX, mouseY);
    }
    
    public void myDrawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            boolean over = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.drawTexturedModalRect(this.xPosition, this.yPosition, (index + 2)* 20, over ? this.height : 0, this.width, this.height);
        }
    }
}
