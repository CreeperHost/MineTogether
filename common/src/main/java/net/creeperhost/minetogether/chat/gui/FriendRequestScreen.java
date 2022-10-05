package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
        cancelButton = addRenderableWidget(new Button(width / 2 - 180, height - 50, 80, 20, new TranslatableComponent("minetogether:button.cancel"), e -> {
            minecraft.setScreen(previous);
        }));

        acceptButton = addRenderableWidget(new Button(width / 2 + 100, height - 50, 80, 20, type.button, this::onAccept));

        String boxString = nameBox != null ? nameBox.getValue() : target.isFriend() ? target.getFriendName() : target.getDisplayName();
        nameBox = addRenderableWidget(new EditBox(minecraft.font, width / 2 - 100, height / 2 - 10, 200, 20, new TextComponent("")));

        nameBox.setValue(boxString);

        acceptButton.active = nameBox.getValue().trim().length() >= 3;
        nameBox.setFocus(true);
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
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(1);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, minecraft.font, type.title, width / 2, 5, 0xFFFFFFFF);
        drawCenteredString(poseStack, minecraft.font, type.desc, width / 2, height / 2 - 30, 0xFFFFFFFF);
    }

    private void onAccept(Button b) {
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
        Minecraft.getInstance().setScreen(previous);
    }

    public enum Type {
        REQUEST(
                new TranslatableComponent("minetogether:screen.friendreq.title.request"),
                new TranslatableComponent("minetogether:screen.friendreq.button.request"),
                new TranslatableComponent("minetogether:screen.friendreq.desc.request")
        ),
        ACCEPT(
                new TranslatableComponent("minetogether:screen.friendreq.title.accept"),
                new TranslatableComponent("minetogether:screen.friendreq.button.accept"),
                new TranslatableComponent("minetogether:screen.friendreq.desc.accept")
        ),
        UPDATE(
                new TranslatableComponent("minetogether:screen.friendreq.title.update"),
                new TranslatableComponent("minetogether:screen.friendreq.button.update"),
                new TranslatableComponent("minetogether:screen.friendreq.desc.update")
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
