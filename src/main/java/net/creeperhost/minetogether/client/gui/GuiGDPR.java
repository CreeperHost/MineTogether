package net.creeperhost.minetogether.client.gui;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiGDPR extends Screen
{
    private final String GDPRText = GDPRText0 + "\n" + GDPRText1 + "\n" + GDPRText2 + "\n" + GDPRText3 + "\n" + GDPRText4 + "\n" + GDPRText5 + "\n" + GDPRText6;

    private static final String GDPRText0 = I18n.format("minetogether.gdprtext");
    private static final String GDPRText1 = I18n.format("minetogether.gdprtext1");
    private static final String GDPRText2 = I18n.format("minetogether.gdprtext2");
    private static final String GDPRText3 = I18n.format("minetogether.gdprtext3");
    private static final String GDPRText4 = I18n.format("minetogether.gdprtext4");
    private static final String GDPRText5 = I18n.format("minetogether.gdprtext5");
    private static final String GDPRText6 = I18n.format("minetogether.gdprtext6");

    private final String GDPRTextData = GDPRTextData1 + "\n" + GDPRTextData2 + "\n" + GDPRTextData3;

    private static final String GDPRTextData1 = I18n.format("minetogether.gdprtextdata1");
    private static final String GDPRTextData2 = I18n.format("minetogether.gdprtextdata2");
    private static final String GDPRTextData3 = I18n.format("minetogether.gdprtextdata3");

    private IScreenGetter getter = null;
    private Screen parent = null;

    private Button acceptButton;
    private Button declineButton;
    private Button moreInfoButton;

    private List<ITextComponent> gdprlines;
    private boolean moreInfo = false;

    public GuiGDPR(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
    }

    public GuiGDPR(Screen parent, IScreenGetter getterIn)
    {
        this(parent);
        getter = getterIn;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(mouseX, mouseY, partialTicks);

        drawCenteredString(minecraft.fontRenderer, "MineTogether GDPR", width / 2, 10, -1);
        int start = 30;

        for (ITextComponent gdprline : gdprlines)
        {
            int left = (width - minecraft.fontRenderer.getStringWidth(gdprline.getFormattedText())) / 2;
            minecraft.fontRenderer.drawString(gdprline.getFormattedText(), left, start += 10, -1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private ITextComponent getComponentUnderMouse(int mouseX, int mouseY)
    {
        int line = ((mouseY - 30) / 10) - 1;

        if (line >= 0 && line < gdprlines.size())
        {
            ITextComponent gdprline = gdprlines.get(line);
            int left = (width - minecraft.fontRenderer.getStringWidth(gdprline.getFormattedText())) / 2;
            int offset = left;
            for (ITextComponent sibling : gdprline.getSiblings())
            {
                int oldOffset = offset;
                offset += minecraft.fontRenderer.getStringWidth(sibling.getFormattedText());
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
    public void init()
    {
        super.init();

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
                    component = new StringTextComponent(part);
                else
                    component.appendText(part);
            }

            lastEnd = end;
            ITextComponent link = new StringTextComponent(matcher.group(1));
            Style style = link.getStyle();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.setColor(TextFormatting.BLUE);
            style.setUnderlined(true);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(Util.localize("order.url"))));

            if (component == null)
                component = link;
            else
                component.appendSibling(link);
        }

        if (component == null)
            component = new StringTextComponent("");

        component.appendSibling(new StringTextComponent(currentText.substring(lastEnd)));

        gdprlines = ScreenUtils.splitText(component, width - 10, minecraft.fontRenderer, false, true);
        this.addButton(moreInfoButton = new Button((width / 2) - 40, (gdprlines.size() * 10) + 50, 80, 20, (moreInfo ? "Less" : "More") + " Info", b ->
        {
            moreInfoButton.visible = moreInfoButton.active = false;
            moreInfo = !moreInfo;
            this.buttons.clear();
            init();
        }));

        this.addButton(declineButton = new Button(50, (gdprlines.size() * 10) + 50, 80, 20, "Decline", b ->
        {
            Minecraft.getInstance().displayGuiScreen(parent);
        }));
        this.addButton(acceptButton = new Button(width - 80 - 50, (gdprlines.size() * 10) + 50, 80, 20, "Accept", b ->
        {
            MineTogether.instance.gdpr.setAcceptedGDPR();
            MineTogether.proxy.startChat();
            Minecraft.getInstance().displayGuiScreen(parent);
        }));
    }

    @FunctionalInterface
    public interface IScreenGetter
    {
        Screen method();
    }
}
