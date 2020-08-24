package net.creeperhost.minetogether.client.screen;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
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
        
        this.addButton(new Button(this.width / 2 - 123, 40, 120, 20, I18n.format("Chat Enabled: " + Config.getInstance().isChatEnabled()), p ->
        {
            boolean enabled = Config.getInstance().isChatEnabled();
            Config.getInstance().setChatEnabled(!enabled);
            saveConfig();
        }));
        this.addButton(new Button(this.width / 2 + 3, 40, 120, 20, I18n.format("Friend Toasts: " + Config.getInstance().isFriendOnlineToastsEnabled()), p ->
        {
            boolean enabled = Config.getInstance().isFriendOnlineToastsEnabled();
            Config.getInstance().setEnableFriendOnlineToasts(!enabled);
            saveConfig();
        }));
        this.addButton(new Button(this.width / 2 - 123, 60, 120, 20, I18n.format("Mainmenu Buttons: " + Config.getInstance().isEnableMainMenuFriends()), p ->
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
            }, new StringTextComponent(I18n.format("Link your Minecraft account to your MineTogether account.")), new StringTextComponent(I18n.format("Linking your accounts will unlock abilities link being able to set your own nickname.\n\nThis will open a web-browser for you to sign in securely."))));
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
    
    private void saveConfig()
    {
        ConfigHandler.saveConfig();
        this.minecraft.displayGuiScreen(new SettingsScreen(parent));
    }
}
