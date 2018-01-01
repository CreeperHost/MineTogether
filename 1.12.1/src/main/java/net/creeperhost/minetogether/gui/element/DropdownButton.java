package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class DropdownButton<E extends Enum> extends GuiButton
{
    public boolean dropdownOpen;
    public String translateBase;
    private E selected;
    private E[] possibleVals;
    private String baseButtonText;

    public DropdownButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String translateBase, E def)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.translateBase = translateBase;
        this.selected = def;
        possibleVals = (E[]) def.getClass().getEnumConstants();
        baseButtonText = buttonText;
        displayString = I18n.format(baseButtonText, I18n.format(translateBase + selected.name().toLowerCase()));
    }

    public DropdownButton(int buttonId, int x, int y, String buttonText, String translateBase, E def)
    {
        this(buttonId, x, y, 200, 20, buttonText, translateBase, def);
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
            }
            else
            if (!this.enabled)
            {
                j = 10526880;
            }
            else if (this.hovered)
            {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);

            if (dropdownOpen)
            {
                drawY += 1;
                for(E e: possibleVals)
                {
                    drawY += height - 2;
                    boolean ourHovered = x >= this.xPosition && y >= drawY && x < this.xPosition + this.width && y < drawY + this.height - 2;

                    int subHovered = ourHovered ? 2 : 0;

                    mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.drawTexturedModalRect(this.xPosition, drawY, 0, 46 + subHovered * 20 + 1, this.width / 2, this.height - 1);
                    this.drawTexturedModalRect(this.xPosition + this.width / 2, drawY, 200 - this.width / 2, 46 + subHovered * 20 + 1, this.width / 2, this.height - 1);

                    String name = I18n.format(translateBase + e.name().toLowerCase());
                    int textColour = 14737632;

                    if (packedFGColour != 0)
                    {
                        textColour = packedFGColour;
                    }
                    else if (ourHovered)
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
                dropdownOpen = false;
                return false; // selection not changed, so no need to return true which will trigger actionPerformed.
            }
            E clickedElement = getClickedElement(mouseX, mouseY);
            if (clickedElement != null)
            {
                setSelected(clickedElement);
                dropdownOpen = false;
                return true;
            }
        }
        else
        {
            if (pressed)
            {
                dropdownOpen = true;
                return false; // selection not changed, so no need to return true which will trigger actionPerformed.
            }
        }

        return false; // at this stage we've handled all the "true" options, so it ain't been pressed
    }

    public E getSelected()
    {
        return selected;
    }

    private E getClickedElement(int mouseX, int mouseY)
    {
        E clickedElement = null;
        int y = yPosition + 1;
        for (E e: possibleVals)
        {
            y += height - 2;
            if (mouseX >= this.xPosition && mouseY >= y && mouseX < this.xPosition + this.width && mouseY < y + this.height - 2)
            {
                clickedElement = e;
                break;
            }

        }
        return clickedElement;
    }

    public void setSelected(E selected)
    {
        this.selected = selected;
        displayString = I18n.format(baseButtonText, I18n.format(translateBase + selected.name().toLowerCase()));
    }
}