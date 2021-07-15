package net.creeperhost.minetogether.module.chat.screen.social;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ScrollingChat;
import net.creeperhost.minetogether.screen.MineTogetherScreen;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class MineTogetherSocialChatScreen extends MineTogetherScreen
{
    Screen parent;
    private ScrollingChat chat;
    private EditBox chatBox;
    private String channel;

    public MineTogetherSocialChatScreen(Screen parent, Profile profile)
    {
        super(new TranslatableComponent(""));
        this.parent = parent;
        this.channel = profile.getMediumHash();
    }

    public MineTogetherSocialChatScreen(Screen parent, String channel)
    {
        super(new TranslatableComponent(""));
        this.parent = parent;
        this.channel = channel;
    }

    @Override
    public void init()
    {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int m = 64 + 16 * this.backgroundUnits();
        int listend = 80 + backgroundUnits() * 16 - 8;

        chat = new ScrollingChat(this, 222, height, 70, listend, marginX() + 14, false);
        chat.setLeftPos(marginX() + 10);

        chatBox = new EditBox(this.font, (width / 2) - 100, m + 10, 200, 20, new TranslatableComponent(""));
        chatBox.setMaxLength(256);

        children.add(chat);
        children.add(chatBox);
        chat.updateLines(channel);
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(poseStack);
        chat.render(poseStack, mouseX, mouseY, partialTicks);
        chatBox.render(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void tick()
    {
        if (ChatHandler.hasNewMessages(channel))
        {
            chat.updateLines(channel);
            ChatHandler.setMessagesRead(channel);
        }
        super.tick();
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        if (chatBox.isFocused())
        {
            return chatBox.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if (chatBox.isFocused())
        {
            if ((i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) && !chatBox.getValue().trim().isEmpty())
            {
                ChatHandler.sendMessage(channel, ChatFormatter.getStringForSending(chatBox.getValue()));
                chatBox.setValue("");
            }
            return chatBox.keyPressed(i, j, k);
        }
        return super.keyPressed(i, j, k);
    }

    public void renderBackground(PoseStack poseStack)
    {
        int i = marginX() + 3;
        super.renderBackground(poseStack);
        minecraft.getTextureManager().bind(Constants.SOCIAL_INTERACTIONS_LOCATION);
        blit(poseStack, i, 64, 1, 1, 236, 8);
        int j = backgroundUnits();

        for (int k = 0; k < j; ++k)
        {
            blit(poseStack, i, 72 + 16 * k, 1, 10, 236, 16);
        }

        blit(poseStack, i, 72 + 16 * j, 1, 27, 236, 8);
    }

    private int windowHeight()
    {
        return Math.max(52, height - 128 - 16);
    }

    private int backgroundUnits()
    {
        return windowHeight() / 16;
    }

    private int marginX()
    {
        return (width - 238) / 2;
    }
}
