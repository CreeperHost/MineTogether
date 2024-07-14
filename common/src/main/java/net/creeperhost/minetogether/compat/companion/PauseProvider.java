package net.creeperhost.minetogether.compat.companion;

import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.api.client.pause.ScreenWidgetCollection;
import dev.ftb.packcompanion.client.screen.pause.providers.SupportPauseProvider;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.gui.FriendChatGui;
import net.creeperhost.minetogether.chat.gui.PublicChatGui;
import net.creeperhost.minetogether.config.LocalConfig;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.gui.GuiShareToFriends;
import net.creeperhost.minetogether.gui.SettingGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

public class PauseProvider implements AdditionalPauseProvider {
    @Override
    public ScreenWidgetCollection init(AdditionalPauseTarget target, ScreenHolder screen, int x, int y) {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        var collection = ScreenWidgetCollection.create();

        int xOffset = 0;
        if (integratedServer != null) {
            boolean isConnectPublished = ConnectHandler.isPublished();
            Component buttonText = isConnectPublished ? Component.translatable("minetogether.connect.close") : Component.translatable("minetogether.connect.open");
            Button.OnPress action = button -> {
                if (isConnectPublished) {
                    ConnectHandler.unPublish();
                    Minecraft.getInstance().setScreen(new PauseScreen(true));
                } else {
                    Minecraft.getInstance().setScreen(new GuiShareToFriends(screen.unsafeScreenAccess()));
                }
            };

            xOffset += 98;
            collection.addRenderableWidget(Button.builder(buttonText, action).bounds(x - xOffset, y, 98, 20).build());
        }

        xOffset += 22;

        var newSettingsBtn = new SupportPauseProvider.IconButton(x - xOffset, y, 20, 20, Constants.WIDGETS_SHEET, "minetogether:gui.button.settings.info", e -> Minecraft.getInstance().setScreen(new SettingGui.Screen(screen.unsafeScreenAccess())), 60, 0, 256, 256, 20, 20, 20, false);
        var newFriendChatBtn = new SupportPauseProvider.IconButton(x - xOffset - 22, y, 20, 20, Constants.WIDGETS_SHEET, "minetogether:gui.button.friends.info", e -> Minecraft.getInstance().setScreen(new FriendChatGui.Screen(screen.unsafeScreenAccess())), 140, 0, 256, 256, 20, 20, 20, false);
        collection.addRenderableWidget(newSettingsBtn);
        collection.addRenderableWidget(newFriendChatBtn);

        if (LocalConfig.instance().chatEnabled) {
            var newPublicChatBtn = new SupportPauseProvider.IconButton(x - xOffset - 44, y, 20, 20, Constants.WIDGETS_SHEET, "minetogether:gui.button.global_chat.info", e -> Minecraft.getInstance().setScreen(new PublicChatGui.Screen(screen.unsafeScreenAccess())), 20, 0, 256, 256, 20, 20, 20, false);
            collection.addRenderableWidget(newPublicChatBtn);
        }

        return collection;
    }
}