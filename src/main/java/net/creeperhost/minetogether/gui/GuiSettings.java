package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.irc.IrcHandler;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class GuiSettings extends GuiScreen
{
    private GuiScreen parent;
    private GuiButton buttonEnableChat;
    private GuiButton buttonEnableFriendToasts;
    private GuiButton linkAccount;
    private GuiButton chatPos;

    private GuiButton buttonDone;

    public GuiSettings(GuiScreen parent)
    {
        this.parent = parent;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();

        buttonEnableChat = this.addButton(new GuiButton(0, this.width / 2 - 123, 40, 120, 20, I18n.format("Public Chat : " + format(Config.getInstance().isChatEnabled()))));
        buttonEnableFriendToasts = this.addButton(new GuiButton(1, this.width / 2 + 3, 40, 120, 20, I18n.format("Toasts : " + format(Config.getInstance().isFriendOnlineToastsEnabled()))));

//        chatPos = this.addButton(new GuiButton(3, this.width / 2 - 123, 60, 120, 20, I18n.format("Chat Location : " + formatLR(Config.getInstance().isLeft()))));

        linkAccount = this.addButton(new GuiButton(69, this.width / 2 - 100, this.height - 47, 200, 20, I18n.format("Link Account")));

        buttonDone = this.addButton(new GuiButton(8888, this.width / 2 - 100, this.height - 27, 200, 20, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        if(button == buttonEnableChat)
        {
            if(Config.getInstance().isChatEnabled())
            {
                CreeperHost.instance.getLogger().info("Disabling in-game chat");
                Config.getInstance().setChatEnabled(false);
                CreeperHost.proxy.disableIngameChat();
                IrcHandler.stop(true);
            }
            else
            {
                CreeperHost.instance.getLogger().info("Enabling in-game chat");
                Config.getInstance().setChatEnabled(true);
                CreeperHost.proxy.enableIngameChat();
                IrcHandler.reconnect();
            }
            saveConfig();
        }
        if(button == buttonEnableFriendToasts)
        {
            boolean enabled = Config.getInstance().isFriendOnlineToastsEnabled();
            Config.getInstance().setEnableFriendOnlineToasts(!enabled);
            saveConfig();
        }
        if(button == buttonDone)
        {
            this.mc.displayGuiScreen(parent);
        }
        if(button == linkAccount)
        {
            mc.displayGuiScreen(new GuiYahNah(this, I18n.format("minetogether.linkaccount1"), I18n.format("minetogether.linkaccount2"), 0));
        }
//        if(button == chatPos)
//        {
//            if(Config.getInstance().isLeft())
//            {
//                CreeperHost.instance.getLogger().info("Disabling in-game chat");
//                Config.getInstance().setLeft(false);
//            }
//            else
//            {
//                CreeperHost.instance.getLogger().info("Enabling in-game chat");
//                Config.getInstance().setLeft(true);
//            }
//            saveConfig();
//        }
    }

    @Override
    public void confirmClicked(boolean result, int id)
    {
        if(result)
        {
            if(id == 0)
            {
                KeycloakOAuth.main(new String[]{});
            }
        }
        mc.displayGuiScreen(this);
    }

    public String format(boolean value)
    {
        if(value) return TextFormatting.GREEN + "Enabled";

        return TextFormatting.RED + "Disabled";
    }

    public String formatLR(boolean value)
    {
        if(value) return TextFormatting.GREEN + "Left";

        return TextFormatting.RED + "Right";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, "MineTogether Settings", width / 2, 5, 0xFFFFFF);
    }

    private void saveConfig()
    {
        CreeperHost.instance.saveConfig(false);
        this.mc.displayGuiScreen(new GuiSettings(parent));
    }
}
