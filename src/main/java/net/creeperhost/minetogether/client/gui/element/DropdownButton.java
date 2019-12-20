package net.creeperhost.minetogether.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class DropdownButton<E extends DropdownButton.IDropdownOption> extends Button
{
    public boolean dropdownOpen;
    private E selected;
    private List<E> possibleVals;
    private String baseButtonText;
    private final boolean dynamic;
    public boolean wasJustClosed = false;
    Minecraft minecraft = Minecraft.getInstance();
    
    public DropdownButton(int x, int y, int widthIn, int heightIn, String buttonText, E def, boolean dynamic, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        this.selected = def;
        possibleVals = (List<E>) def.getPossibleVals();
        baseButtonText = buttonText;
        this.baseButtonText = I18n.format(baseButtonText, I18n.format(selected.getTranslate(selected, false)));
        this.dynamic = dynamic;
    }
    
    public DropdownButton(int x, int y, String buttonText, E def, boolean dynamic, Button.IPressable onPress)
    {
        this(x, y, 200, 20, buttonText, def, dynamic, onPress);
    }

    public boolean flipped = false;
    
    @SuppressWarnings("Duplicates")
    @Override
    public void renderButton(int x, int y, float partialTicks)
    {
        if (this.visible)
        {
            int drawY = y;
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = x >= this.x && y >= drawY && x < this.x + this.width && y < drawY + this.height;
            int i = this.getHoverState(this.isHovered);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.blit(this.x, drawY, 0, 46 + i * 20, this.width / 2, this.height);
            this.blit(this.x + this.width / 2, drawY, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
//            this.mouseDragged(minecraft, x, y);
            int j = 14737632;
            
            if (getFGColor() != 0)
            {
                j = getFGColor();
            } else if (!this.active)
            {
                j = 10526880;
            } else if (this.isHovered())
            {
                j = 16777120;
            }
            
            this.drawCenteredString(fontrenderer, this.baseButtonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
            
            if (dropdownOpen)
            {
                drawY += 1;
                int yOffset = height - 2;
                if (flipped)
                {
                    yOffset = -yOffset;
                    drawY -= 1;
                }
                for (E e : possibleVals)
                {
                    drawY += yOffset;
                    boolean ourHovered = x >= this.x && y >= drawY && x < this.x + this.width && y < drawY + this.height - 2;
                    
                    int subHovered = ourHovered ? 2 : 0;
                    
                    minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(this.x, drawY, 0, 46 + subHovered * 20 + 1, this.width / 2, this.height - 1);
                    this.blit(this.x + this.width / 2, drawY, 200 - this.width / 2, 46 + subHovered * 20 + 1, this.width / 2, this.height - 1);
                    
                    String name = I18n.format(e.getTranslate(selected, true));
                    int textColour = 14737632;
                    
                    if (getFGColor() != 0)
                    {
                        textColour = getFGColor();
                    } else if (ourHovered)
                    {
                        textColour = 16777120;
                    }
                    this.drawCenteredString(fontrenderer, name, this.x + this.width / 2, drawY + (this.height - 10) / 2, textColour);
                }
            }
        }
    }
    
    protected int getHoverState(boolean mouseOver)
    {
        return mouseOver ? 2 : active ? dropdownOpen ? 2 : 1 : 0;
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        boolean pressed = super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (dropdownOpen)
        {
            if (pressed)
            {
                close();
                return false; // selection not changed, so no need to return true which will trigger actionPerformed.
            }
            E clickedElement = getClickedElement(p_mouseClicked_1_, p_mouseClicked_3_);
            if (clickedElement != null)
            {
                setSelected(clickedElement);
                close();
                return true;
            }
            close();
            return false;
        } else if (pressed)
        {
            dropdownOpen = true;
            if (dynamic)
            {
                selected.updateDynamic();
                possibleVals = (List<E>) selected.getPossibleVals();
            }
        }
        return false; // at this stage we've handled all the "true" options, so it ain't been pressed
    }
    
    public void close()
    {
        dropdownOpen = false;
        wasJustClosed = true;
    }
    
    public E getSelected()
    {
        return selected;
    }
    
    public void setSelected(E selected)
    {
        try {
            this.selected = selected;
            updateDisplayString();
        }catch (Exception e){ e.printStackTrace(); }
    }
    
    public void updateDisplayString()
    {
        baseButtonText = I18n.format(baseButtonText, I18n.format(selected.getTranslate(selected, false)));
    }
    
    private E getClickedElement(double mouseX, double mouseY)
    {
        E clickedElement = null;
        int y = this.y + 1;
        
        int yOffset = height - 2;
        if (flipped)
        {
            yOffset = -yOffset;
            y -= 1;
        }
        for (IDropdownOption e : possibleVals)
        {
            y += yOffset;
            if (mouseX >= this.x && mouseY >= y && mouseX < this.x + this.width && mouseY < y + this.height - 2)
            {
                clickedElement = (E) e;
                break;
            }
        }
        return clickedElement;
    }
    
    public interface IDropdownOption
    {
        List<IDropdownOption> getPossibleVals();
        
        String getTranslate(IDropdownOption currentDO, boolean dropdownOpen);
        
        default void updateDynamic() {}
    }
}