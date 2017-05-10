package net.creeperhost.creeperhost.gui.element;

import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.common.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ButtonCreeper extends GuiButton {
    private static ResourceLocation buttonImg = new ResourceLocation("creeperhost", "textures/nobrand.png");

    public ButtonCreeper(int buttonID, int xPos, int yPos){
        super(buttonID, xPos, yPos, 20, 20, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY){
        if(this.visible){
            ResourceLocation buttonImage = buttonImg;
            if (Config.getInstance().isServerHostButtonImage())
                buttonImage = CreeperHost.instance.getImplementation().getButtonIcon();
            mc.getTextureManager().bindTexture(buttonImage);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            boolean over = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition+this.width && mouseY < this.yPosition+this.height;
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, over ? this.height : 0, this.width, this.height);
        }
    }
}
