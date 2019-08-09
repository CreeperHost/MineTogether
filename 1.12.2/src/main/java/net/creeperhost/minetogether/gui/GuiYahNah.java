package net.creeperhost.minetogether.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;

/*
 * Our own version of GuiYesNo which handles \n
 */
public class GuiYahNah extends GuiScreen
{
    /** A reference to the screen object that created this. Used for navigating between screens. */
    protected GuiYesNoCallback parentScreen;
    protected String messageLine1;
    private final String messageLine2;
    private final List<String> listLines = Lists.newArrayList();
    protected String confirmButtonText;
    protected String cancelButtonText;
    protected int parentButtonClickedId;

    public GuiYahNah(GuiYesNoCallback parentScreenIn, String messageLine1In, String messageLine2In, int parentButtonClickedIdIn)
    {
        this.parentScreen = parentScreenIn;
        this.messageLine1 = messageLine1In;
        this.messageLine2 = messageLine2In;
        this.parentButtonClickedId = parentButtonClickedIdIn;
        this.confirmButtonText = I18n.format("gui.yes");
        this.cancelButtonText = I18n.format("gui.no");
    }

    public void initGui()
    {
        this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 155, this.height / 6 + 96, this.confirmButtonText));
        this.buttonList.add(new GuiOptionButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.cancelButtonText));
        this.listLines.clear();

        List<String> tempList = Arrays.asList(
                messageLine2.replace("\\n", "\n").split("\n") // I have no idea wht I can't just regex the literal "\n" but whatever, this works. Fuck Java Regex.
        );
        tempList.forEach(str -> listLines.addAll(fontRendererObj.listFormattedStringToWidth(str, this.width - 50)));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        this.parentScreen.confirmClicked(button.id == 0, this.parentButtonClickedId);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.messageLine1, this.width / 2, 70, 16777215);
        int i = 90;

        for (String s : this.listLines)
        {
            this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
            i += this.fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}