package net.creeperhost.minetogether.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.irc.IrcHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class SettingsScreen extends Screen
{
    private final Screen parent;

    public SettingsScreen(Screen parent)
    {
        super(new TranslatableComponent("minetogether.settings.title"));
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        buttons.clear();

        addButton(new Button(this.width / 2 - 123, 40, 120, 20, new TranslatableComponent(I18n.get("Chat Enabled: " + format(Config.getInstance().isChatEnabled()))), p ->
        {
            if(Config.getInstance().isChatEnabled())
            {
                MineTogether.logger.info("Disabling in-game chat");
                Config.getInstance().setChatEnabled(false);
                //TODO
                IrcHandler.stop(true);
            }
            else
            {
                MineTogether.logger.info("Enabling in-game chat");
                Config.getInstance().setChatEnabled(true);
                //TODO
//                MineTogether.proxy.enableIngameChat();
                IrcHandler.reconnect();
                MineTogetherClient.getMineTogetherChat().startChat();
            }
            saveConfig();
        }));
        this.addButton(new Button(this.width / 2 + 3, 40, 120, 20, new TranslatableComponent(I18n.get("Friend Toasts: " + format(Config.getInstance().isFriendOnlineToastsEnabled()))), p ->
        {
            boolean enabled = Config.getInstance().isFriendOnlineToastsEnabled();
            Config.getInstance().setEnableFriendOnlineToasts(!enabled);
            saveConfig();
        }));
        this.addButton(new Button(this.width / 2 - 123, 60, 120, 20, new TranslatableComponent(I18n.get("Menu Buttons: " + format(Config.getInstance().isEnableMainMenuFriends()))), p ->
        {
            boolean enabled = Config.getInstance().isEnableMainMenuFriends();
            Config.getInstance().setEnableMainMenuFriends(!enabled);
            saveConfig();
        }));
        addButton(new Button(this.width / 2 - 100, this.height - 47, 200, 20, new TranslatableComponent(I18n.get("Link Account")), p ->
        {
            //TODO
//            minecraft.displayGuiScreen(new ConfirmScreen(e -> {
//                if (e) {
//                    KeycloakOAuth.main(new String[]{});
//                }
//                minecraft.setScreen(this);
//            }, new TranslatableComponent(I18n.get("minetogether.linkaccount1")), new TranslatableComponent(I18n.get("minetogether.linkaccount2"))));
        }));

        //Done button
        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, new TranslatableComponent(I18n.get("gui.done")), p ->
        {
            this.minecraft.setScreen(parent);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 5, 0xFFFFFF);
    }

    public String format(boolean value)
    {
        return value ? ChatFormatting.GREEN + "Enabled" : ChatFormatting.RED + "Disabled";
    }

    private void saveConfig()
    {
        Config.saveConfig();
        this.minecraft.setScreen(new SettingsScreen(parent));
    }
}
