package net.creeperhost.minetogether.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineScreen extends MineTogetherScreen
{
    private final String offlineText = offline0 + "\n\n\n" + offline1 + "\n\n" + offline2 + "\n\n";
    private static final String offline0 = I18n.get("minetogether.offlinetext");
    private static final String offline1 = I18n.get("minetogether.offlinetext1");
    private static final String offline2 = I18n.get("minetogether.offlinetext2");
    private Checkbox checkBox;

    private List<FormattedCharSequence> gdprlines;

    public OfflineScreen()
    {
        super(new TranslatableComponent("minetogether.screen.offline"));
    }

    @Override
    public void init()
    {
        super.init();
        final String regex = "\\((.*?)\\|(.*?)\\)";

        final Pattern pattern = Pattern.compile(regex);
        String currentText = offlineText;
        final Matcher matcher = pattern.matcher(currentText);

        int lastEnd = 0;

        Component component = null;

        while (matcher.find())
        {

            int start = matcher.start();
            int end = matcher.end();

            String part = currentText.substring(lastEnd, start);
            if (part.length() > 0)
            {
                if (component == null)
                    component = new TranslatableComponent(part);
                else
                    component = component.copy().append(new TranslatableComponent(part));
            }

            lastEnd = end;
            Component link = new TranslatableComponent(matcher.group(1));
            Style style = link.getStyle();
            style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE));
            style.withUnderlined(true);
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("order.url")));

            if (component == null) component = link;
            else component = component.copy().append(link);
        }

        if (component == null) component = new TranslatableComponent("");

        component = component.copy().append(new TranslatableComponent(currentText.substring(lastEnd)));

        gdprlines = ComponentRenderUtils.wrapComponents(component, width - 10, minecraft.font);
        addButtons();
    }

    public void addButtons()
    {
        addButton(new Button((width / 2) - 40, height - 40, 80, 20, new TranslatableComponent("Continue"), (b) ->
        {
            File offline = new File(Platform.getGameFolder() + "/local/minetogether/offline.txt");
            if(checkBox.selected())
            {
                try {
                    offline.getParentFile().mkdirs();
                    offline.createNewFile();
                } catch (IOException e) { e.printStackTrace(); }
            }
            Minecraft.getInstance().setScreen(new TitleScreen());
        }));
        addButton(checkBox = new Checkbox( (width / 2) - (font.width("Do not show this screen again") / 2), height - 80, 150, 20, new TranslatableComponent("Do not show this screen again"), true));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        drawCenteredString(matrixStack, minecraft.font, "MineTogether is in offline mode", width / 2, 10, -1);
        int start = 30;

        for (FormattedCharSequence gdprline : gdprlines)
        {
            int left = (width - minecraft.font.width(gdprline)) / 2;
            minecraft.font.draw(matrixStack, gdprline, left, start += 10, -1);
        }
    }
}
