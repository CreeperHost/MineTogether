package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.chat.GuiOurChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiGDPR extends GuiScreen
{
    private static final String GDPRText = "You, or the modpack maker, have opted to enable the Server List and/or cross modpack chat function.\n" +
        "\n" +
        "In order to provide this, we need to send a hash of your Minecraft UUID to our servers which may be identifying information.\n" +
        "\n" +
        "We only use this in order to find servers which you are invited to, and to provide an in game friends list.\n" +
        "\n" +
        "To view our privacy policy, please click (here|https://www.creeperhost.net/privacy). To view our TOS, please click (here|https://www.creeperhost.net/tos).\n" +
        "\n" +
        "If you consent to this, please press Accept. Otherwise, press Decline. If you decline this, the feature will be disabled.\n";

    private GuiButton acceptButton;
    private GuiButton declineButton;

    private List<ITextComponent> gdprlines;

    public GuiGDPR()
    {
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button == acceptButton)
        {
            CreeperHost.instance.gdpr.setAcceptedGDPR();
        } else if (button == declineButton) {
            Config.getInstance().setServerListEnabled(false);
            Config.getInstance().setChatEnabled(false);
        }
        CreeperHost.instance.saveConfig();
        Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRendererObj, "MineTogether GDPR", width / 2, 10, -1);
        int start = 30;

        for(ITextComponent gdprline : gdprlines)
        {
            int left = (width - fontRendererObj.getStringWidth(gdprline.getFormattedText())) / 2;
            fontRendererObj.drawString(gdprline.getFormattedText(), left, start += 10, -1);
        }

        handleComponentHover(getComponentUnderMouse(mouseX, mouseY), mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        handleComponentClick(getComponentUnderMouse(mouseX, mouseY));
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private ITextComponent getComponentUnderMouse(int mouseX, int mouseY)
    {
        int line = ((mouseY - 30) / 10) - 1;

        if (line >= 0 && line < gdprlines.size())
        {
            ITextComponent gdprline = gdprlines.get(line);
            int left = (width - fontRendererObj.getStringWidth(gdprline.getFormattedText())) / 2;
            int offset = left;
            for(ITextComponent sibling : gdprline.getSiblings())
            {
                int oldOffset = offset;
                offset += fontRendererObj.getStringWidth(sibling.getFormattedText());
                if (mouseX >= oldOffset && mouseX <= offset)
                {
                    return sibling;
                }
            }
        }
        return null;
    }

    @Override
    public void initGui()
    {
        super.initGui();


        final String regex = "\\((.*?)\\|(.*?)\\)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(GDPRText);

        int lastEnd = 0;

        ITextComponent component = null;

        while (matcher.find())
        {

            int start = matcher.start();
            int end = matcher.end();

            String part = GDPRText.substring(lastEnd, start);
            if (part.length() > 0)
            {
                if (component == null)
                    component = new TextComponentString(part);
                else
                    component.appendText(part);
            }

            lastEnd = end;
            ITextComponent link = new TextComponentString(matcher.group(1));
            Style style = link.getStyle();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.setColor(TextFormatting.BLUE);
            style.setUnderlined(true);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(Util.localize("order.url"))));

            if (component == null)
                component = link;
            else
                component.appendSibling(link);
        }

        component.appendSibling(new TextComponentString(GDPRText.substring(lastEnd)));



        gdprlines = GuiUtilRenderComponents.splitText(component, width - 10, fontRendererObj, false, true);
        this.buttonList.add(declineButton = new GuiButton(8008, 50, (gdprlines.size() * 10) + 50, 80, 20, "Decline"));
        this.buttonList.add(acceptButton = new GuiButton(8008, width - 80 - 50, (gdprlines.size() * 10) + 50, 80, 20, "Accept"));
    }
}
