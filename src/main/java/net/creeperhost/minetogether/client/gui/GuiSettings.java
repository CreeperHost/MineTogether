package net.creeperhost.minetogether.client.gui;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiSettings extends Screen
{
    private Screen parent;
    
    public GuiSettings(Screen screen)
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
        this.minecraft.displayGuiScreen(new GuiSettings(parent));
    }
}
