package net.creeperhost.minetogether.client.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class SettingsScreen extends Screen
{
    private Screen parent;
    
    public SettingsScreen(Screen screen)
    {
        super(new TranslationTextComponent("Settings"));
        this.parent = screen;
    }
    
    @Override
    protected void init()
    {
        super.init();
        buttons.clear();
        
        this.addButton(new Button(this.width / 2 - 123, 40, 120, 20, I18n.format("Chat Enabled: " + format(Config.getInstance().isChatEnabled())), p ->
        {
            if(Config.getInstance().isChatEnabled())
            {
                MineTogether.instance.getLogger().info("Disabling in-game chat");
                Config.getInstance().setChatEnabled(false);
                MineTogether.proxy.stopChat();
                MineTogether.proxy.disableIngameChat();
            }
            else
            {
                MineTogether.instance.getLogger().info("Enabling in-game chat");
                Config.getInstance().setChatEnabled(true);
                MineTogether.proxy.enableIngameChat();
            }
            saveConfig();
        }));
        this.addButton(new Button(this.width / 2 + 3, 40, 120, 20, I18n.format("Friend Toasts: " + format(Config.getInstance().isFriendOnlineToastsEnabled())), p ->
        {
            boolean enabled = Config.getInstance().isFriendOnlineToastsEnabled();
            Config.getInstance().setEnableFriendOnlineToasts(!enabled);
            saveConfig();
        }));
        this.addButton(new Button(this.width / 2 - 123, 60, 120, 20, I18n.format("Menu Buttons: " + format(Config.getInstance().isEnableMainMenuFriends())), p ->
        {
            boolean enabled = Config.getInstance().isEnableMainMenuFriends();
            Config.getInstance().setEnableMainMenuFriends(!enabled);
            saveConfig();
        }));
        addButton(new Button(this.width / 2 - 100, this.height - 47, 200, 20, I18n.format("Link Account"), p -> {
            minecraft.displayGuiScreen(new ConfirmScreen(e -> {
                if (e) {
                    KeycloakOAuth.main(new String[]{});
                }
                minecraft.displayGuiScreen(this);
            }, new StringTextComponent(I18n.format("minetogether.linkaccount1")), new StringTextComponent(I18n.format("minetogether.linkaccount2"))));
        }));
        
        //Done button
        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, I18n.format("gui.done"), p ->
        {
            this.minecraft.displayGuiScreen(parent);
        }));
    }
    
    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_)
    {
        renderDirtBackground(1);
        super.render(p_render_1_, p_render_2_, p_render_3_);
        drawCenteredString(font, "MineTogether Settings", width / 2, 5, 0xFFFFFF);
    }

    public String format(boolean value)
    {
        if(value) return TextFormatting.GREEN + "Enabled";

        return TextFormatting.RED + "Disabled";
    }
    
    private void saveConfig()
    {
        ConfigHandler.saveConfig();
        this.minecraft.displayGuiScreen(new SettingsScreen(parent));
    }
}
