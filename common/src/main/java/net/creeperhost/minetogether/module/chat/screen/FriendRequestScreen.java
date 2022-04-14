package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.sentry.Sentry;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FriendRequestScreen extends Screen
{
    private final String playerName;
    private final String chatInternalName;
    private final String friendCode;
    private final boolean accept;
    private final boolean update;
    private final String friendName;
    private final Screen parent;
    private Profile profile;
    private Button acceptBtn;
    private Button cancelBtn;
    boolean first = true;
    private EditBox nameEntry;
    Minecraft mc = Minecraft.getInstance();

    public FriendRequestScreen(Screen parent, String playerName, Profile friendTarget, String friendCode, String friendName, boolean accept, boolean update)
    {
        super(new TranslatableComponent(""));
        this.playerName = playerName;
        this.chatInternalName = friendTarget == null ? "" : friendTarget.getCurrentIRCNick();
        this.friendCode = friendCode;
        this.accept = accept;
        this.parent = parent;
        this.friendName = friendName;
        this.profile = friendTarget;
        this.update = update;
    }

    @Override
    public void init()
    {
        super.init();
        clearWidgets();
        mc.keyboardHandler.setSendRepeatsToGui(true);

        this.addRenderableWidget(cancelBtn = new Button(width / 2 - 180, height - 50, 80, 20, new TranslatableComponent("Cancel"), (button) -> Minecraft.getInstance().setScreen(parent)));

        this.addRenderableWidget(acceptBtn = new Button(width / 2 + 100, height - 50, 80, 20, accept ? new TranslatableComponent("Accept") : new TranslatableComponent("Send request"), (buttons) ->
        {
            if(update)
            {
                try
                {
                    ChatCallbacks.removeFriend(friendCode, MineTogetherClient.getPlayerHash());
                } catch (IOException e)
                {
                    Sentry.captureException(e);
                }
                CompletableFuture.runAsync(() ->
                {
                    try
                    {
                        ChatCallbacks.addFriend(friendCode, nameEntry.getValue().trim(), MineTogetherClient.getPlayerHash());
                    } catch (IOException e)
                    {
                        Sentry.captureException(e);
                    }
                }, MineTogetherChat.otherExecutor);
                Minecraft.getInstance().setScreen(parent);
                return;
            }

            if (accept)
            {
                ChatHandler.acceptFriendRequest(chatInternalName, friendName);
                CompletableFuture.runAsync(() ->
                {
                    try
                    {
                        ChatCallbacks.addFriend(friendCode, nameEntry.getValue().trim(), MineTogetherClient.getPlayerHash());
                    } catch (IOException e)
                    {
                        Sentry.captureException(e);
                    }
                }, MineTogetherChat.otherExecutor);
            }
            else
            {
                ChatHandler.sendFriendRequest(chatInternalName, nameEntry.getValue().trim());
            }
            Minecraft.getInstance().setScreen(parent);
        }));

        addRenderableWidget(nameEntry = new EditBox(mc.font, width / 2 - 100, height / 2 - 10, 200, 20, new TranslatableComponent("")));

        if (first)
        {
            String name = !profile.getFriendName().isEmpty() ? profile.getFriendName() : profile.getUserDisplay();
            nameEntry.setValue(name); // default to player name
            first = false;
        }

        acceptBtn.active = nameEntry.getValue().trim().length() >= 3;
        nameEntry.setFocus(true);
        nameEntry.setCanLoseFocus(false);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        nameEntry.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, mc.font, accept ? I18n.get("minetogether.friend.acceptgui") : I18n.get("minetogether.friend.addgui"), width / 2, 5, 0xFFFFFFFF);
        drawCenteredString(matrixStack, mc.font, accept ? I18n.get("minetogether.friend.acceptname") : I18n.get("minetogether.friend.addname"), width / 2, height / 2 - 30, 0xFFFFFFFF);
    }
}
