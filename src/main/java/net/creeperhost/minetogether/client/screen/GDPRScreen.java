package net.creeperhost.minetogether.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GDPRScreen extends Screen
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
    
    private List<IReorderingProcessor> gdprlines;
    private boolean moreInfo = false;
    
    public GDPRScreen(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
    }
    
    public GDPRScreen(Screen parent, IScreenGetter getterIn)
    {
        this(parent);
        getter = getterIn;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        drawCenteredString(matrixStack, minecraft.fontRenderer, "MineTogether GDPR", width / 2, 10, -1);
        int start = 30;
        
        for (IReorderingProcessor gdprline : gdprlines)
        {
            int left = (width - minecraft.fontRenderer.func_243245_a(gdprline)) / 2;
            minecraft.fontRenderer.func_238407_a_(matrixStack, gdprline, left, start += 10, -1);
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
            ITextComponent gdprline = (ITextComponent) gdprlines.get(line);
            int left = (width - minecraft.fontRenderer.getStringWidth(gdprline.getString())) / 2;
            int offset = left;
            for (ITextComponent sibling : gdprline.getSiblings())
            {
                int oldOffset = offset;
                offset += minecraft.fontRenderer.getStringWidth(sibling.getString());
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
                    component = component.deepCopy().append(new StringTextComponent(part));
            }
            
            lastEnd = end;
            ITextComponent link = new StringTextComponent(matcher.group(1));
            Style style = link.getStyle();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.setColor(Color.fromTextFormatting(TextFormatting.BLUE));
            style.setUnderlined(true);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(Util.localize("order.url"))));
            
            if (component == null)
                component = link;
            else
            component = component.deepCopy().append(link);
        }
        
        if (component == null)
            component = new StringTextComponent("");

        component = component.deepCopy().append(new StringTextComponent(currentText.substring(lastEnd)));
        component.getSiblings().add(new StringTextComponent(currentText.substring(lastEnd)));
        
        gdprlines = RenderComponentsUtil.func_238505_a_(component, width - 10, minecraft.fontRenderer);
        this.addButton(moreInfoButton = new Button((width / 2) - 40, (gdprlines.size() * 10) + 50, 80, 20, (moreInfo ? new StringTextComponent("Less Info") : new StringTextComponent("More Info")), b ->
        {
            moreInfoButton.visible = moreInfoButton.active = false;
            moreInfo = !moreInfo;
            this.buttons.clear();
            init();
        }));
        
        this.addButton(declineButton = new Button(50, (gdprlines.size() * 10) + 50, 80, 20, new StringTextComponent("Decline"), b ->
        {
            Minecraft.getInstance().displayGuiScreen(parent);
        }));
        this.addButton(acceptButton = new Button(width - 80 - 50, (gdprlines.size() * 10) + 50, 80, 20, new StringTextComponent("Accept"), b ->
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
