package net.creeperhost.creeperhost.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiProgressDisconnected extends GuiDisconnected
{

    private final String reason;
    private final ITextComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int textHeight;

    public GuiProgressDisconnected(GuiScreen screen, String reasonLocalizationKey, ITextComponent chatComp)
    {
        super(screen, reasonLocalizationKey, chatComp);
        this.parentScreen = screen;
        this.reason = I18n.format(reasonLocalizationKey);
        this.message = chatComp;
    }

    int percent = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
/*        this.drawCenteredString(this.fontRendererObj, this.reason, this.width / 2, this.height / 2 - this.textHeight / 2 - this.fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int x = this.height / 2 - this.textHeight / 2;

        if (this.multilineMessage != null)
        {
            for (String s : this.multilineMessage)
            {
                this.drawCenteredString(this.fontRendererObj, s, this.width / 2, x, 16777215);
                x += this.fontRendererObj.FONT_HEIGHT;
            }
        }*/

        percent = (percent + 1) % 100;

        int loadingBackColour = 0xFF000000;
        int loadingFrontColour = 0xFF00FF00;
        int loadingOutsideColour = 0xFF222222;

        int loadingHeight = 20;
        int loadingWidth = this.width - 60;
        int left = this.width / 2 - (loadingWidth / 2);
        int top = this.height / 2 - (loadingHeight / 2);

        int loadingPercentWidth = (int) (((double)loadingWidth / (double)100) * (double)percent);

        drawRect(left - 1, top - 1, left + loadingWidth + 1, top + loadingHeight + 1, loadingOutsideColour);
        drawRect(left, top, left + loadingWidth, top + loadingHeight, loadingBackColour);
        drawRect(left, top, left + loadingPercentWidth, top + loadingHeight, loadingFrontColour);

        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            ((GuiButton)this.buttonList.get(i)).func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }

        for (int j = 0; j < this.labelList.size(); ++j)
        {
            ((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
        }
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.textHeight = this.multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, Math.min(this.height / 2 + 80, this.height - 30), I18n.format("gui.toMenu")));
    }
}
