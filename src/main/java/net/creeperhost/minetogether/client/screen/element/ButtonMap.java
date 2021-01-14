package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.lib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ButtonMap extends Button
{
    String buttonText;
    List<ITextProperties> tooltiplist = new ArrayList<>();

    public ButtonMap(int x, int y, int width, int height, ITextComponent title, boolean active, IPressable pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
        this.buttonText = title.getString();
        tooltiplist.add(new StringTextComponent(buttonText));
        this.active = active;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();

        ResourceLocation map = new ResourceLocation(Constants.MOD_ID, "textures/map/" + buttonText + ".png");
        minecraft.getTextureManager().bindTexture(map);

        if(isHovered())
        {
            RenderSystem.color4f(0F, 1F, 0F, alpha);
        }
        if(isFocused())
        {
            RenderSystem.color4f(0F, 0.6F, 0F, alpha);
        }
        if(!active)
        {
            RenderSystem.color4f(0.4F, 0.4F, 0.4F, alpha);
        }
        blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.color4f(1F, 1F, 1F, alpha);
    }
}
