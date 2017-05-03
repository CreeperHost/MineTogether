package de.ellpeck.chgui.gui.element;

import de.ellpeck.chgui.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonCreeper extends GuiButton{

    public ButtonCreeper(int buttonID, int xPos, int yPos){
        super(buttonID, xPos, yPos, 20, 20, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY){
        if(this.visible){
            mc.getTextureManager().bindTexture(Util.GUI_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            boolean over = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition+this.width && mouseY < this.yPosition+this.height;
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, over ? this.height : 0, this.width, this.height);
        }
    }
}
