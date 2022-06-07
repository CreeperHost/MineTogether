package net.creeperhost.minetogether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.data.Message;
import net.creeperhost.minetogether.lib.util.LimitedSizeQueue;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.ClientChatTarget;
import net.creeperhost.minetogether.screen.MineTogetherScreen;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent
{
    @Shadow
    public abstract int getWidth();

    @Shadow
    @Final
    private List<GuiMessage<FormattedCharSequence>> trimmedMessages;
    @Shadow
    @Final
    private List<GuiMessage<Component>> allMessages;

    @Shadow
    protected abstract void addMessage(Component component, int i, int j, boolean bl);

    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    public abstract Style getClickedComponentStyleAt(double d, double e);

    @Shadow public abstract void addMessage(Component component);

    private final List<GuiMessage<FormattedCharSequence>> mtChatMessages = new ArrayList<>();
    private final List<GuiMessage<Component>> mtAllMessages = new ArrayList<>();

    private final List<GuiMessage<FormattedCharSequence>> partyChatMessages = new ArrayList<>();
    private final List<GuiMessage<Component>> partyAllMessages = new ArrayList<>();

    @Inject(at = @At("RETURN"), method = "processPendingMessages", cancellable = true)
    public void getProcessPendingMessages(CallbackInfo ci)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        if (ChatModule.clientChatTarget != ClientChatTarget.DEFAULT && ChatModule.hasNewMessage)
        {
            ChatModule.hasNewMessage = false;
            updateList();
        }
    }

    @Inject(at = @At("HEAD"), method = "render")
    public void render(PoseStack poseStack, int i, CallbackInfo ci)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        if (isChatFocused())
        {
            ChatComponent chatComponent = Minecraft.getInstance().gui.getChat();
            int y = chatComponent.getHeight() - 175 - (minecraft.font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
            GuiComponent.fill(poseStack, 0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + 10 + y, minecraft.options.getBackgroundColor(-2147483648));
            int k = Mth.ceil((float) minecraft.gui.getChat().getWidth() / (float) minecraft.options.chatScale);
            int z = Mth.ceil((float) minecraft.gui.getChat().getHeight() / (float) minecraft.options.chatScale);

            if (ChatModule.clientChatTarget != ClientChatTarget.DEFAULT)
                MineTogetherScreen.drawLogo(poseStack, minecraft.font, k + 6, z + 6, -2, minecraft.gui.getChat().getHeight() - 340, 0.75F);
        }
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> trimmedMessages(ChatComponent chatComponent)
    {
        if (!Config.getInstance().isChatEnabled()) return trimmedMessages;

        switch (ChatModule.clientChatTarget)
        {
            case DEFAULT:
                return trimmedMessages;
            case PARTY:
                return partyChatMessages;
            case MINETOGETHER:
                return mtChatMessages;
        }

        return trimmedMessages;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"))
    private void fill(PoseStack poseStack, int i, int j, int k, int l, int m)
    {
        if (Config.getInstance().isChatEnabled())
        {
            if(!isChatFocused())
            {
                GuiComponent.fill(poseStack, i, j, k, l, m);
            }
        }
        else
        {
            ChatComponent.fill(poseStack, i, j, k, l, m);
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void addMessage(Component component, CallbackInfo ci)
    {
        if(component == null) return;
        if(!component.getString().startsWith("[!MineTogetherMessage]"))
        {
            //Store the last use chat
            ClientChatTarget current = ChatModule.clientChatTarget;
            //Set the chat back to default
            ChatModule.clientChatTarget = ClientChatTarget.DEFAULT;
            //Send the message
            ((ChatComponentInvoker) Minecraft.getInstance().gui.getChat()).invokeAddMessage(component, 0, Minecraft.getInstance().gui.getGuiTicks(), false);
            //Reset the tab back to the last used tab
            ChatModule.clientChatTarget = current;
            ci.cancel();
        }
        else
        {
            String message = component.getString().substring(22);
            Component newComponent = new TextComponent(message).withStyle(component.getStyle());
            ((ChatComponentInvoker) Minecraft.getInstance().gui.getChat()).invokeAddMessage(newComponent, 0, Minecraft.getInstance().gui.getGuiTicks(), false);
            ci.cancel();
        }
    }


    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> addMessage(ChatComponent chatComponent)
    {
        if (!Config.getInstance().isChatEnabled()) return trimmedMessages;

        switch (ChatModule.clientChatTarget)
        {
            case DEFAULT:
                return trimmedMessages;
            case PARTY:
                return partyChatMessages;
            case MINETOGETHER:
                return mtChatMessages;
        }

        return trimmedMessages;
    }

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;allMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<Component>> addMessageAll(ChatComponent chatComponent)
    {
        if (!Config.getInstance().isChatEnabled()) return allMessages;

        switch (ChatModule.clientChatTarget)
        {
            case DEFAULT:
                return allMessages;
            case MINETOGETHER:
                return mtAllMessages;
            case PARTY:
                return partyAllMessages;
        }

        return allMessages;
    }

    @Redirect(method = "scrollChat(I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> scrollChat(ChatComponent chatComponent)
    {
        if (!Config.getInstance().isChatEnabled()) return trimmedMessages;

        switch (ChatModule.clientChatTarget)
        {
            case DEFAULT:
                return trimmedMessages;
            case PARTY:
                return partyChatMessages;
            case MINETOGETHER:
                return mtChatMessages;
        }

        return trimmedMessages;
    }

    @Redirect(method = "getClickedComponentStyleAt", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> getClickedComponentStyleAt(ChatComponent chatComponent)
    {
        if (!Config.getInstance().isChatEnabled()) return trimmedMessages;

        switch (ChatModule.clientChatTarget)
        {
            case DEFAULT:
                return trimmedMessages;
            case PARTY:
                return partyChatMessages;
            case MINETOGETHER:
                return mtChatMessages;
        }

        return trimmedMessages;
    }

    public void updateList()
    {
        String channel = ChatHandler.CHANNEL;
        if (ChatModule.clientChatTarget == ClientChatTarget.PARTY) channel = ChatHandler.currentParty;
        try
        {
            LimitedSizeQueue<Message> temp = ChatHandler.messages.get(channel);
            for (Message message : temp)
            {
                Component component = ChatFormatter.formatLine(message);
                if (component == null) continue;
                addMessage(component, 0, Minecraft.getInstance().gui.getGuiTicks(), false);
            }
        } catch (Exception e)
        {
            //            e.printStackTrace();
        }
    }
}
