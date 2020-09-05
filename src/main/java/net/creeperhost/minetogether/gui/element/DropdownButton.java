package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class DropdownButton<E extends DropdownButton.IDropdownOption> extends GuiButton
{
    public boolean dropdownOpen;
    private E selected;
    private List<E> possibleVals;
    private String baseButtonText;
    private final boolean dynamic;
    public boolean wasJustClosed = false;
    
    public DropdownButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, E def, boolean dynamic)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.selected = def;
        possibleVals = (List<E>) def.getPossibleVals();
        baseButtonText = buttonText;
        displayString = I18n.format(baseButtonText, I18n.format(selected.getTranslate(selected, false)));
        this.dynamic = dynamic;
    }
    
    public DropdownButton(int buttonId, int x, int y, String buttonText, E def, boolean dynamic)
    {
        this(buttonId, x, y, 200, 20, buttonText, def, dynamic);
    }
    
    @Override
    public void func_191745_a(Minecraft mc, int x, int y, float partialTicks)
    {
        realDrawButton(mc, x, y, partialTicks);
    }
    
    // < 1.12 compat
    public void func_146112_a(Minecraft mc, int mouseX, int mouseY)
    {
        realDrawButton(mc, mouseX, mouseY, 0);
    }
    
    public boolean flipped = false;
    
    @SuppressWarnings("Duplicates")
    public void realDrawButton(Minecraft mc, int x, int y, float partialTicks)
    {
        if (this.visible)
        {
            int drawY = yPosition;
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = x >= this.xPosition && y >= drawY && x < this.xPosition + this.width && y < drawY + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.xPosition, drawY, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, drawY, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.mouseDragged(mc, x, y);
            int j = 14737632;
            
            if (packedFGColour != 0)
            {
                j = packedFGColour;
            } else if (!this.enabled)
            {
                j = 10526880;
            } else if (this.hovered)
            {
                j = 16777120;
            }
            
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
            
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
                    boolean ourHovered = x >= this.xPosition && y >= drawY && x < this.xPosition + this.width && y < drawY + this.height - 2;
                    
                    int subHovered = ourHovered ? 2 : 0;
                    
                    mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.drawTexturedModalRect(this.xPosition, drawY, 0, 46 + subHovered * 20 + 1, this.width / 2, this.height - 1);
                    this.drawTexturedModalRect(this.xPosition + this.width / 2, drawY, 200 - this.width / 2, 46 + subHovered * 20 + 1, this.width / 2, this.height - 1);
                    
                    String name = I18n.format(e.getTranslate(selected, true));
                    int textColour = 14737632;
                    
                    if (packedFGColour != 0)
                    {
                        textColour = packedFGColour;
                    } else if (ourHovered)
                    {
                        textColour = 16777120;
                    }
                    this.drawCenteredString(fontrenderer, name, this.xPosition + this.width / 2, drawY + (this.height - 10) / 2, textColour);
                }
            }
        }
    }
    
    @Override
    protected int getHoverState(boolean mouseOver)
    {
        return mouseOver ? 2 : enabled ? dropdownOpen ? 2 : 1 : 0;
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        if (dropdownOpen)
        {
            if (pressed)
            {
                close();
                return false; // selection not changed, so no need to return true which will trigger actionPerformed.
            }
            E clickedElement = getClickedElement(mouseX, mouseY);
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
        displayString = I18n.format(baseButtonText, I18n.format(selected.getTranslate(selected, false)));
    }
    
    private E getClickedElement(int mouseX, int mouseY)
    {
        E clickedElement = null;
        int y = yPosition + 1;
        
        int yOffset = height - 2;
        if (flipped)
        {
            yOffset = -yOffset;
            y -= 1;
        }
        for (IDropdownOption e : possibleVals)
        {
            y += yOffset;
            if (mouseX >= this.xPosition && mouseY >= y && mouseX < this.xPosition + this.width && mouseY < y + this.height - 2)
            {
                clickedElement = (E) e;
                break;
            }
        }
        return clickedElement;
    }

    public List<E> getPossibleVals() {
        return possibleVals;
    }

    public interface IDropdownOption
    {
        List<IDropdownOption> getPossibleVals();
        
        String getTranslate(IDropdownOption currentDO, boolean dropdownOpen);
        
        default void updateDynamic() {}
    }
}