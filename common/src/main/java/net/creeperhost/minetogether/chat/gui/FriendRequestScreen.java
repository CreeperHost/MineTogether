package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 21/9/22.
 */
public class FriendRequestScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Screen previous;
    @Nullable
    private final ProfileManager.FriendRequest request;
    private final Profile target;
    private final Type type;

    private Button cancelButton;
    private Button acceptButton;

    private EditBox nameBox;

    public FriendRequestScreen(Screen previous, ProfileManager.FriendRequest request) {
        this(previous, request, request.from, Type.ACCEPT);

    }

    public FriendRequestScreen(Screen previous, Profile target, Type type) {
        this(previous, null, target, type);
    }

    private FriendRequestScreen(Screen previous, @Nullable ProfileManager.FriendRequest request, Profile target, Type type) {
        super(type.title);
        this.previous = previous;
        this.request = request;
        this.target = target;
        this.type = type;

        assert type != Type.ACCEPT || request != null;
    }

    @Override
    protected void init() {
        cancelButton = addRenderableWidget(Button.builder(Component.translatable("minetogether:button.cancel"), e -> minecraft.setScreen(previous))
                .bounds(width / 2 - 180, height - 50, 80, 20)
                .build()
        );

        acceptButton = addRenderableWidget(Button.builder(type.button, this::onAccept)
                .bounds(width / 2 + 100, height - 50, 80, 20)
                .build());

        String boxString = nameBox != null ? nameBox.getValue() : target.isFriend() ? target.getFriendName() : target.getDisplayName();
        nameBox = addRenderableWidget(new EditBox(minecraft.font, width / 2 - 100, height / 2 - 10, 200, 20, Component.empty()));

        nameBox.setValue(boxString);

        acceptButton.active = nameBox.getValue().trim().length() >= 3;
        nameBox.setFocused(true);
        nameBox.setCanLoseFocus(false);
    }

    @Override
    public boolean charTyped(char c, int i) {
        boolean ret = super.charTyped(c, i);
        if (nameBox.isFocused()) {
            acceptButton.active = nameBox.getValue().trim().length() >= 3;
        }
        return ret;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        boolean ret = super.keyPressed(i, j, k);
        if (nameBox.isFocused()) {
            acceptButton.active = nameBox.getValue().trim().length() >= 3;
        }
        return ret;
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        renderDirtBackground(graphics);
        super.render(graphics, i, j, f);
        graphics.drawCenteredString(minecraft.font, type.title, width / 2, 5, 0xFFFFFFFF);
        graphics.drawCenteredString(minecraft.font, type.desc, width / 2, height / 2 - 30, 0xFFFFFFFF);
    }

    private void onAccept(Button b) {
        if (MineTogetherChat.CHAT_STATE.ircClient.getState() == IrcState.CONNECTED) {
            ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
            if (type == Type.REQUEST) {
                profileManager.sendFriendRequest(target, nameBox.getValue().trim());
            } else if (type == Type.ACCEPT) {
                assert request != null;
                profileManager.acceptFriendRequest(request, nameBox.getValue().trim());
            } else {
                assert type == Type.UPDATE;
                LOGGER.fatal("Not currently implemented!");
            }
        } else {
            LOGGER.warn("IRC not connected. Did nothing.");
        }
        Minecraft.getInstance().setScreen(previous);
    }

    public enum Type {
        REQUEST(
                Component.translatable("minetogether:screen.friendreq.title.request"),
                Component.translatable("minetogether:screen.friendreq.button.request"),
                Component.translatable("minetogether:screen.friendreq.desc.request")
        ),
        ACCEPT(
                Component.translatable("minetogether:screen.friendreq.title.accept"),
                Component.translatable("minetogether:screen.friendreq.button.accept"),
                Component.translatable("minetogether:screen.friendreq.desc.accept")
        ),
        UPDATE(
                Component.translatable("minetogether:screen.friendreq.title.update"),
                Component.translatable("minetogether:screen.friendreq.button.update"),
                Component.translatable("minetogether:screen.friendreq.desc.update")
        );

        private final Component title;
        private final Component button;
        private final Component desc;

        Type(Component title, Component button, Component desc) {
            this.title = title;
            this.button = button;
            this.desc = desc;
        }
    }
}
