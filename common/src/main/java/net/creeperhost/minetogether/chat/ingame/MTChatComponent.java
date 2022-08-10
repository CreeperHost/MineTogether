package net.creeperhost.minetogether.chat.ingame;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.LinkedList;

/**
 * Created by covers1624 on 20/7/22.
 */
public class MTChatComponent extends ChatComponent {

    private final ChatTarget target;

    // Flag to override explicitly delegating to vanilla, set in specific cases.
    private boolean internalUpdate = false;

    private final LinkedList<TextComponent> pendingMessages = new LinkedList<>();
    private IrcChannel channel;

    public MTChatComponent(ChatTarget target, Minecraft minecraft) {
        super(minecraft);
        this.target = target;
        assert target != ChatTarget.VANILLA : "MTChatComponent doesn't work this way";
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        channel.addListener(message -> {
            synchronized (pendingMessages) {
                pendingMessages.add(new TextComponent("<" + message.senderName + "> " + message.getMessage()));
            }
        });
    }

    @Override
    public void render(PoseStack poseStack, int i) {
        if (!pendingMessages.isEmpty()) {
            internalUpdate = true;
            synchronized (pendingMessages) {
                for (TextComponent pendingMessage : pendingMessages) {
                    super.addMessage(pendingMessage);
                }
                pendingMessages.clear();
            }
            internalUpdate = false;
        }
        super.render(poseStack, i);
    }

    @Override
    public void rescaleChat() {
        internalUpdate = true;

        // Only delegate if we are the target to prevent loops.
        if (MineTogetherChat.target != target) {
            switch (MineTogetherChat.target) {
                case VANILLA -> MineTogetherChat.vanillaChat.rescaleChat();
                case PUBLIC -> MineTogetherChat.publicChat.rescaleChat();
            }
        }
        super.rescaleChat();

        internalUpdate = false;
    }

    @Override
    public void clearMessages(boolean bl) {
        internalUpdate = true;

        // Only delegate if we are the target to prevent loops.
        if (MineTogetherChat.target != target) {
            switch (MineTogetherChat.target) {
                case VANILLA -> MineTogetherChat.vanillaChat.clearMessages(bl);
                case PUBLIC -> MineTogetherChat.publicChat.clearMessages(bl);
            }
        }
        super.clearMessages(bl);

        internalUpdate = false;
    }

    @Override
    public void addRecentChat(String string) {
        channel.sendMessage(string);
    }

    @Override
    public void removeById(int i) {
        if (internalUpdate) {
            super.removeById(i);
        } else {
            MineTogetherChat.vanillaChat.removeById(i);
        }
    }

    @Override
    public void addMessage(Component component, int i, int j, boolean bl) {
        if (internalUpdate) {
            super.addMessage(component, i, j, bl);
        } else {
            MineTogetherChat.vanillaChat.addMessage(component, i, j, bl);
        }
    }

    @Override
    public void addMessage(Component component) {
        if (internalUpdate) {
            super.addMessage(component);
        } else {
            MineTogetherChat.vanillaChat.addMessage(component);
        }
    }

    @Override
    public void addMessage(Component component, int i) {
        if (internalUpdate) {
            super.addMessage(component, i);
        } else {
            MineTogetherChat.vanillaChat.addMessage(component, i);
        }
    }
}
