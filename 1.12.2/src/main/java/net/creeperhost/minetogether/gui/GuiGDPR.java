package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiGDPR extends GuiScreen
{
    private static final String GDPRText = "Welcome new player!\n" +
            "\n" +
            "Our name is CreeperHost, and we created MineTogether to fill in some of the community features we felt modded Minecraft was missing.\n" +
            "\n" +
            "In doing so, we require a way to identify you to make sure we send you all the right information, like who is in your friends list etc.\n" +
            "\n" +
            "We do this by taking the unique identifier for your Minecraft account (UUID), then running it through the same kind of cryptography (hashing) that most websites use to keep your password secure and cannot be reversed.\n" +
            "\n" +
            "You can click 'More Info' to learn a little more about this and access links to our privacy policy and terms of service.\n" +
            "\n" +
            "Beyond this magical accept button, is a world of multiplayer servers, global (all servers and even single player) chat and much more, we hope you'll join us!\n" +
            "\n" +
            "To provide your consent to us storing data for you, please click 'Accept', otherwise please click 'Decline' and this part of the mod will be disabled.\n";
    private static final String GDPRTextData = "The data we collect is a hash of your Minecraft UUID to our servers which may be identifying information, despite our efforts to anonymize it.\n" +
            "\n" +
            "We only use this in order to find servers which you are invited to, to provide an in game friends list, to provide an identifier for chat, and to enable tracking of free Mini-Game credits used.\n" +
            "\n" +
            "To view the CreeperHost LTD privacy policy, please click (here|https://www.creeperhost.net/privacy). To view our TOS, please click (here|https://www.creeperhost.net/tos).\n" +
            "\n" +
            "If you consent to this, please press 'Accept'. Otherwise, press 'Decline'. If you decline this, you will be unable to access the feature.\n";
    private IScreenGetter getter = null;
    private GuiScreen parent = null;
    
    private GuiButton acceptButton;
    private GuiButton declineButton;
    private GuiButton moreInfoButton;
    
    private List<ITextComponent> gdprlines;
    private boolean moreInfo = false;
    
    public GuiGDPR()
    {
    }
    
    public GuiGDPR(GuiScreen parent)
    {
        this.parent = parent;
    }
    
    public GuiGDPR(GuiScreen parent, IScreenGetter getterIn)
    {
        this(parent);
        getter = getterIn;
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button == acceptButton)
        {
            CreeperHost.instance.gdpr.setAcceptedGDPR();
            CreeperHost.proxy.startChat();
            Minecraft.getMinecraft().displayGuiScreen(getter == null ? parent : getter.method());
        } else if (button == declineButton)
        {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        } else
        {
            button.visible = button.enabled = false;
            moreInfo = !moreInfo;
            buttonList.clear();
            initGui();
        }
        
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        drawCenteredString(fontRendererObj, "MineTogether GDPR", width / 2, 10, -1);
        int start = 30;
        
        for (ITextComponent gdprline : gdprlines)
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
            for (ITextComponent sibling : gdprline.getSiblings())
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
    
    @SuppressWarnings("Duplicates")
    @Override
    public void initGui()
    {
        super.initGui();
        
        
        final String regex = "\\((.*?)\\|(.*?)\\)";
        
        final Pattern pattern = Pattern.compile(regex);
        String currentText = moreInfo ? GDPRTextData : GDPRText;
        final Matcher matcher = pattern.matcher(currentText);
        
        int lastEnd = 0;
        
        ITextComponent component = null;
        
        while (matcher.find())
        {
            
            int start = matcher.start();
            int end = matcher.end();
            
            String part = currentText.substring(lastEnd, start);
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
        
        if (component == null)
            component = new TextComponentString("");
        
        component.appendSibling(new TextComponentString(currentText.substring(lastEnd)));
        
        
        gdprlines = GuiUtilRenderComponents.splitText(component, width - 10, fontRendererObj, false, true);
        this.buttonList.add(moreInfoButton = new GuiButton(8008, (width / 2) - 40, (gdprlines.size() * 10) + 50, 80, 20, (moreInfo ? "Less" : "More") + " Info"));
        this.buttonList.add(declineButton = new GuiButton(8008, 50, (gdprlines.size() * 10) + 50, 80, 20, "Decline"));
        this.buttonList.add(acceptButton = new GuiButton(8008, width - 80 - 50, (gdprlines.size() * 10) + 50, 80, 20, "Accept"));
    }
    
    @FunctionalInterface
    public interface IScreenGetter
    {
        GuiScreen method();
    }
}
