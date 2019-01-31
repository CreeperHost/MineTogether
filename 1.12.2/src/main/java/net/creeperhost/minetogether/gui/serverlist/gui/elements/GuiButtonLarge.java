package net.creeperhost.minetogether.gui.serverlist.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonLarge extends GuiButton
{
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("creeperhost:textures/widgets.png");
    public String description;
    public ItemStack stack;

    public GuiButtonLarge(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);
    }

    public GuiButtonLarge(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String description, ItemStack stack)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
        this.description = description;
        this.stack = stack;
    }

    public void func_191745_a(Minecraft mc, int p_191745_2_, int p_191745_3_, float p_191745_4_)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            this.hovered = p_191745_2_ >= this.xPosition && p_191745_3_ >= this.yPosition && p_191745_2_ < this.xPosition + this.width && p_191745_3_ < this.yPosition + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.mouseDragged(mc, p_191745_2_, p_191745_3_);
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

            this.drawCenteredString(fontrenderer, TextFormatting.BOLD + this.displayString, this.xPosition + this.width / 2, this.yPosition + ( +8), j);

            List<ITextComponent> newstring = GuiUtilRenderComponents.splitText(new TextComponentString(description), width - 10, fontrenderer, false, true);
            int start = 80;

            for (String s : fullJustify(description, 16))
            {
                int left = ((this.xPosition + 4));
                fontrenderer.drawStringWithShadow(s, left, start += 8, -1);
            }

            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.renderItemIntoGUI(stack, this.xPosition + width / 2 - 8, this.yPosition + 24);
        }
    }

    public List<String> fullJustify(String words, int maxWidth) {
        return fullJustify(words.split(" "), maxWidth);
    }

    public List<String> fullJustify(String[] words, int maxWidth) {
        int n = words.length;
        List<String> justifiedText = new ArrayList<>();
        int currLineIndex = 0;
        int nextLineIndex = getNextLineIndex(currLineIndex, maxWidth, words);
        while (currLineIndex < n) {
            StringBuilder line = new StringBuilder();
            for (int i = currLineIndex; i < nextLineIndex; i++) {
                line.append(words[i] + " ");
            }
            currLineIndex = nextLineIndex;
            nextLineIndex = getNextLineIndex(currLineIndex, maxWidth, words);
            justifiedText.add(line.toString());
        }
        for (int i = 0; i < justifiedText.size() - 1; i++) {
            String fullJustifiedLine = getFullJustifiedString(justifiedText.get(i).trim(), maxWidth);
            justifiedText.remove(i);
            justifiedText.add(i, fullJustifiedLine);
        }
        String leftJustifiedLine = getLeftJustifiedLine(justifiedText.get(justifiedText.size() - 1).trim(), maxWidth);
        justifiedText.remove(justifiedText.size() - 1);
        justifiedText.add(leftJustifiedLine);
        return justifiedText;
    }

    public static int getNextLineIndex(int currLineIndex, int maxWidth, String[] words) {
        int n = words.length;
        int width = 0;
        while (currLineIndex < n && width < maxWidth) {
            width += words[currLineIndex++].length() + 1;
        }
        if (width > maxWidth + 1)
            currLineIndex--;
        return currLineIndex;
    }

    public String getFullJustifiedString(String line, int maxWidth) {
        StringBuilder justifiedLine = new StringBuilder();
        String[] words = line.split(" ");
        int occupiedCharLength = 0;
        for (String word : words) {
            occupiedCharLength += word.length();
        }
        int remainingSpace = maxWidth - occupiedCharLength;
        int spaceForEachWordSeparation = words.length > 1 ? remainingSpace / (words.length - 1) : remainingSpace;
        int extraSpace = remainingSpace - spaceForEachWordSeparation * (words.length - 1);
        for (int j = 0; j < words.length - 1; j++) {
            justifiedLine.append(words[j]);
            for (int i = 0; i < spaceForEachWordSeparation; i++)
                justifiedLine.append(" ");
            if (extraSpace > 0) {
                justifiedLine.append(" ");
                extraSpace--;
            }
        }
        justifiedLine.append(words[words.length - 1]);
        for (int i = 0; i < extraSpace; i++)
            justifiedLine.append(" ");
        return justifiedLine.toString();
    }

    public String getLeftJustifiedLine(String line, int maxWidth) {
        int lineWidth = line.length();
        StringBuilder justifiedLine = new StringBuilder(line);
        for (int i = 0; i < maxWidth - lineWidth; i++)
            justifiedLine.append(" ");
        return justifiedLine.toString();
    }
}
