package net.creeperhost.minetogether.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Created by covers1624 on 19/10/22.
 */
public class SettingsScreen extends Screen {

    private final Screen parent;

    private Button linkButton;

    public SettingsScreen(Screen parent) {
        super(new TranslatableComponent("minetogether:screen.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Config config = Config.instance();
        addRenderableWidget(new Button(width / 2 - 123, 40, 120, 20, new TranslatableComponent("minetogether:screen.settings.button.chat").append(state(config.chatEnabled)), e -> {
            if (config.chatEnabled) {
                MineTogetherChat.disableChat();
                config.chatEnabled = false;
            } else {
                MineTogetherChat.enableChat();
                config.chatEnabled = true;
            }
            saveConfig();
        }));

        addRenderableWidget(new Button(width / 2 + 3, 40, 120, 20, new TranslatableComponent("minetogether:screen.settings.button.friend_toasts").append(state(config.friendNotifications)), e -> {
            config.friendNotifications = !config.friendNotifications;
            saveConfig();
        }));
        addRenderableWidget(new Button(width / 2 - 123, 60, 120, 20, new TranslatableComponent("minetogether:screen.settings.button.menu_buttons").append(state(config.mainMenuButtons)), e -> {
            config.mainMenuButtons = !config.mainMenuButtons;
            saveConfig();
        }));

        linkButton = addRenderableWidget(new Button(width / 2 - 100, height - 47, 200, 20, new TranslatableComponent("minetogether:screen.settings.button.link"), e -> {
            minecraft.setScreen(new ConfirmScreen(b -> {
                if (b) {
                    KeycloakOAuth.main(new String[0]);
                }
                minecraft.setScreen(this);
            }, new TranslatableComponent("minetogether:linkaccount1"), new TranslatableComponent("minetogether:linkaccount2")));
        }));
        linkButton.active = !MineTogetherChat.getOurProfile().hasAccount();

        addRenderableWidget(new Button(width / 2 - 100, height - 27, 200, 20, new TranslatableComponent("gui.done"), e -> {
            minecraft.setScreen(parent);
        }));
    }

    @Override
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(1);
        super.render(pStack, mouseX, mouseY, partialTicks);
        drawCenteredString(pStack, font, getTitle(), width / 2, 5, 0xFFFFFF);
    }

    private static Component state(boolean state) {
        if (state) {
            return new TranslatableComponent("minetogether:screen.settings.button.enabled").withStyle(e -> e.withColor(ChatFormatting.GREEN));
        }
        return new TranslatableComponent("minetogether:screen.settings.button.disabled").withStyle(e -> e.withColor(ChatFormatting.RED));
    }

    private void saveConfig() {
        Config.save();
        // Open new screen, really this is just to reload the button text.
        // TODO Don't do this, custom button with lambda for button text.
        minecraft.setScreen(new SettingsScreen(parent));
    }
}
