package net.creeperhost.minetogether.client.screen;

import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgressDisconnectedScreen extends Screen
{
    private final Screen parentScreen;
    double percent = 0;
    Pattern pattern = Pattern.compile("(\\d+/\\d+).*");
    Field cancelField = null;
    private String ourReason;
    private ITextComponent ourMessage;
    private List<String> multilineMessage;
    private int textHeight;
    private long lastConnectAttempt;
    private NetworkManager lastNetworkManager;
    private ConnectingScreen captiveConnecting;
    private String ip = "";
    
    public ProgressDisconnectedScreen(ConnectingScreen screen, String reasonLocalizationKey, ITextComponent chatComp, NetworkManager lastNetworkManager)
    {
        super(new StringTextComponent(""));
        this.parentScreen = screen;
        this.ourReason = I18n.format(reasonLocalizationKey);
        this.ourMessage = chatComp;
        this.lastNetworkManager = lastNetworkManager;
        this.lastConnectAttempt = System.currentTimeMillis();
        if (lastNetworkManager != null)
        {
            InetSocketAddress address = (InetSocketAddress) lastNetworkManager.getRemoteAddress();
            ip = address.getHostName() + ":" + address.getPort();
        }
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        this.drawCenteredString(this.font, this.ourReason, this.width / 2, this.height / 2 - this.textHeight / 2 - this.font.FONT_HEIGHT * 2, 11184810);
        int x = this.height / 2 - this.textHeight / 2;
        
        if (this.multilineMessage != null)
        {
            for (String s : this.multilineMessage)
            {
                this.drawCenteredString(this.font, s, this.width / 2, x, 16777215);
                x += this.font.FONT_HEIGHT;
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
        
        int loadingPercentWidth = (int) (((double) loadingWidth / (double) 100) * (double) percent);

//        drawRect(left - 1, top - 1, left + loadingWidth + 1, top + loadingHeight + 1, loadingOutsideColour);
//        drawRect(left, top, left + loadingWidth, top + loadingHeight, loadingBackColour);
//        drawRect(left, top, left + loadingPercentWidth, top + loadingHeight, loadingFrontColour);

//        if (false)
//        {
//            lastConnectAttempt = System.currentTimeMillis();
//            captiveConnecting = new ConnectingScreen(this, Minecraft.getInstance(), new ServerData("lol", ip, false));
//            lastNetworkManager = EventHandler.getNetworkManager(captiveConnecting);
//        }
        
        super.render(mouseX, mouseY, partialTicks);
        
        //drawCenteredString(fontRendererObj, I18n.format("creeperhost.pregen.refresh"), this.width / 2, Math.min(this.height / 2 + 60, this.height - 30), 0xFFFFFFFF);
    }
    
    @Override
    public void tick()
    {
        if (captiveConnecting != null)
            captiveConnecting.tick();
    }
    
    @SuppressWarnings("all")
    @Override
    public void init()
    {
        update(ourReason, ourMessage);
        this.textHeight = this.multilineMessage.size() * this.font.FONT_HEIGHT;
        addButton(new Button(this.width / 2 - 100, Math.min(this.height / 2 + 80, this.height - 30), 20, 20, I18n.format("gui.toMenu"), p ->
        {
            if (captiveConnecting != null)
            {
                if (lastNetworkManager != null)
                {
                    lastNetworkManager.closeChannel(new StringTextComponent("Aborted"));
                }
                
                try
                {
                    if (cancelField == null)
                    {
//                        cancelField = ReflectionHelper.findField(GuiConnecting.class, "cancel", "field_146373_h", "");
                    }
                    cancelField.set(captiveConnecting, true);
                } catch (Throwable ignored)
                {
                }
            }
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
    }
    
    @SuppressWarnings("Duplicates")
    public void update(String reason, ITextComponent message)
    {
        lastConnectAttempt = System.currentTimeMillis();
        ourMessage = message;
        ourReason = reason;
        captiveConnecting = null;
        this.multilineMessage = this.font.listFormattedStringToWidth(ourMessage.getFormattedText(), this.width - 50);
        for (String aMultilineMessage : multilineMessage)
        {
            Matcher matcher = pattern.matcher(aMultilineMessage);
            if (matcher.matches())
            {
                String match = matcher.group(1);
                String[] split = match.split("/");
                int done = Integer.parseInt(split[0]);
                int total = Integer.parseInt(split[1]);
                percent = ((double) done / (double) total) * (double) 100;
                break;
            }
        }
    }
}
