package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.List;

public class GuiButtonLarge extends Button
{
    private String description;
    private ItemStack stack;
    
    public GuiButtonLarge(int x, int y, int widthIn, int heightIn, String buttonText, String description, ItemStack stack, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, new StringTextComponent(buttonText), onPress);
        this.width = 200;
        this.height = 20;
        this.visible = true;
        this.active = true;
        this.x = x;
        this.y = y;
        this.width = widthIn;
        this.height = heightIn;
        this.setMessage(new StringTextComponent(buttonText));
        this.description = description;
        this.stack = stack;
    }
    
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getYImage(this.isHovered);
            GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
            this.renderBg(matrixStack, mc, mouseX, mouseY);
            int color = 14737632;
            
            if (packedFGColor != 0)
            {
                color = packedFGColor;
            } else if (!this.active)
            {
                color = 10526880;
            } else if (this.isHovered)
            {
                color = 16777120;
            }
            
            List<IReorderingProcessor> newstring = RenderComponentsUtil.func_238505_a_(new StringTextComponent(description), width -12, mc.fontRenderer);
            //Start needs to move based on GUI scale and screen size I guess, plz help @cloudhunter, @gigabit101, you're our only hope. (ihavenoideawhatimdoingdog.jpg)
            int start = y + 40;

            for (IReorderingProcessor s : newstring)
            {
                int left = ((this.x + 4));
                mc.fontRenderer.func_238407_a_(matrixStack, s, left, start += 10, -1);
            }
            
            ITextComponent buttonText = this.getMessage();
            
            this.drawCenteredString(matrixStack, mc.fontRenderer, buttonText, this.x + this.width / 2, this.y + 10, color);
            ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
            matrixStack.push();
            renderItem.renderItemAndEffectIntoGUI(stack, (this.x) + (width / 2) - 8, (this.y) + 24);
            matrixStack.pop();
        }
    }
    
    public static String padLeft(String s, int n)
    {
        return String.format("%1$" + n + "s", s);
    }
}
