package net.creeperhost.minetogether.client.screen;

import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineScreen extends Screen
{
    private final String GDPRText = GDPRText0 + "\n\n\n" + GDPRText1 + "\n\n" + GDPRText2 + "\n\n";

    private static final String GDPRText0 = I18n.format("minetogether.offlinetext");
    private static final String GDPRText1 = I18n.format("minetogether.offlinetext1");
    private static final String GDPRText2 = I18n.format("minetogether.offlinetext2");

    private Button acceptButton;
    private CheckboxButton checkBox;

    private List<ITextComponent> gdprlines;
    private Screen parent;

    public OfflineScreen(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        final String regex = "\\((.*?)\\|(.*?)\\)";

        final Pattern pattern = Pattern.compile(regex);
        String currentText = GDPRText;
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

            if (component == null) component = link;
            else component.appendSibling(link);
        }

        if (component == null) component = new StringTextComponent("");

        component.appendSibling(new StringTextComponent(currentText.substring(lastEnd)));

        gdprlines = RenderComponentsUtil.splitText(component, width - 10, font, false, true);
        this.buttons.add(acceptButton = new Button((width / 2) - 40, height - 40, 80, 20, "Continue", (b) ->
        {
            File offline = new File("local/minetogether/offline.txt");
            if(checkBox.isChecked()) {
                try {
                    offline.createNewFile();
                } catch (IOException e) { e.printStackTrace(); }
            }
            Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
        }));
        buttons.add(checkBox = new CheckboxButton( (width / 2) - (font.getStringWidth("Do not show this screen again") / 2), height - 80, 150, 20, "Do not show this screen again", true));
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        if(checkBox != null) checkBox.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if(acceptButton != null) acceptButton.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(mouseX, mouseY, partialTicks);

        drawCenteredString(font, "MineTogether is in offline mode", width / 2, 10, -1);
        int start = 30;

        for (ITextComponent gdprline : gdprlines)
        {
            int left = (width - font.getStringWidth(gdprline.getFormattedText())) / 2;
            font.drawString(gdprline.getFormattedText(), left, start += 10, -1);
        }
    }
}
