package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiProgressDisconnected extends GuiScreen
{

    private String ourReason;
    private ITextComponent ourMessage;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int textHeight;
    private long lastConnectAttempt;
    private NetworkManager lastNetworkManager;
    private GuiConnecting captiveConnecting;
    private String ip = "";

    public GuiProgressDisconnected(GuiScreen screen, String reasonLocalizationKey, ITextComponent chatComp, NetworkManager lastNetworkManager)
    {
        this.parentScreen = screen;
        this.ourReason = I18n.format(reasonLocalizationKey);
        this.ourMessage = chatComp;
        this.lastNetworkManager = lastNetworkManager;
        this.lastConnectAttempt = System.currentTimeMillis();
        if (lastNetworkManager != null)
        {
            InetSocketAddress address = (InetSocketAddress)lastNetworkManager.getRemoteAddress();
            ip = address.getHostName() + ":" + address.getPort();
        }
    }

    double percent = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.ourReason, this.width / 2, this.height / 2 - this.textHeight / 2 - this.fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int x = this.height / 2 - this.textHeight / 2;

        if (this.multilineMessage != null)
        {
            for (String s : this.multilineMessage)
            {
                this.drawCenteredString(this.fontRendererObj, s, this.width / 2, x, 16777215);
                x += this.fontRendererObj.FONT_HEIGHT;
            }
        }

        //percent = (percent + 1) % 100;

        int loadingBackColour = 0xFF000000;
        int loadingFrontColour = 0xFF00FF00;
        int loadingOutsideColour = 0xFF222222;

        int loadingHeight = 20;
        int loadingWidth = this.width - 60;
        int left = this.width / 2 - (loadingWidth / 2);
        int top = this.height / 2 - (loadingHeight / 2) + 45;

        int loadingPercentWidth = (int) (((double)loadingWidth / (double)100) * (double)percent);

        drawRect(left - 1, top - 1, left + loadingWidth + 1, top + loadingHeight + 1, loadingOutsideColour);
        drawRect(left, top, left + loadingWidth, top + loadingHeight, loadingBackColour);
        drawRect(left, top, left + loadingPercentWidth, top + loadingHeight, loadingFrontColour);

        if (false)
        {
            lastConnectAttempt = System.currentTimeMillis();
            captiveConnecting = new GuiConnecting(this, Minecraft.getMinecraft(), new ServerData("lol", ip, false));
            lastNetworkManager = EventHandler.getNetworkManager(captiveConnecting);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        //drawCenteredString(fontRendererObj, I18n.format("creeperhost.pregen.refresh"), this.width / 2, Math.min(this.height / 2 + 60, this.height - 30), 0xFFFFFFFF);
    }

    @Override
    public void updateScreen()
    {
        if (captiveConnecting != null)
            captiveConnecting.updateScreen();
    }

    @Override
    public void initGui()
    {
        update(ourReason, ourMessage);
        this.buttonList.clear();
        this.textHeight = this.multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, Math.min(this.height / 2 + 80, this.height - 30), I18n.format("gui.toMenu")));
    }

    Pattern pattern = Pattern.compile("(\\d+/\\d+).*");

    public void update(String reason, ITextComponent message)
    {
        lastConnectAttempt = System.currentTimeMillis();
        ourMessage = message;
        ourReason = reason;
        captiveConnecting = null;
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(ourMessage.getFormattedText(), this.width - 50);
        for (String aMultilineMessage : multilineMessage)
        {
            Matcher matcher = pattern.matcher(aMultilineMessage);
            if (matcher.matches())
            {
                String match = matcher.group(1);
                String[] split = match.split("/");
                int done = Integer.parseInt(split[0]);
                int total = Integer.parseInt(split[1]);
                percent = ((double)done / (double)total) * (double)100;
                break;
            }
        }
    }

    Field cancelField = null;

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            if (captiveConnecting != null)
            {
                if (lastNetworkManager != null)
                {
                    lastNetworkManager.closeChannel(new TextComponentString("Aborted"));
                }

                try
                {
                    if (cancelField == null)
                    {
                        cancelField = ReflectionHelper.findField(GuiConnecting.class, "field_146373_h", "cancel");
                    }
                    cancelField.set(captiveConnecting, true);
                }
                catch (Throwable e)
                {

                }
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
        super.actionPerformed(button);
    }
}
