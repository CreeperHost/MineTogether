package net.creeperhost.minetogether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.creeperhost.minetogetherlib.util.MathHelper;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
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
    @Shadow public abstract int getWidth();
    @Shadow @Final private List<GuiMessage<FormattedCharSequence>> trimmedMessages;
    @Shadow @Final private List<GuiMessage<Component>> allMessages;

    @Shadow protected abstract void addMessage(Component component, int i, int j, boolean bl);

    @Shadow protected abstract boolean isChatFocused();

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Nullable public abstract Style getClickedComponentStyleAt(double d, double e);

    private final List<GuiMessage<FormattedCharSequence>> mtChatMessages = new ArrayList<>();
    private final List<GuiMessage<Component>> mtAllMessages = new ArrayList<>();

    @Inject(at=@At("RETURN"), method="processPendingMessages", cancellable = true)
    public void getProcessPendingMessages(CallbackInfo ci)
    {
        if(!Config.getInstance().isChatEnabled()) return;

        if(ChatModule.showMTChat && ChatModule.hasNewMessage)
        {
            ChatModule.hasNewMessage = false;
            updateList();
        }
    }

    @Inject(at = @At("HEAD"), method = "render")
    public void render(PoseStack poseStack, int i, CallbackInfo ci)
    {
        if(!Config.getInstance().isChatEnabled()) return;

        if(isChatFocused())
        {
            ChatComponent chatComponent = Minecraft.getInstance().gui.getChat();
            int y = chatComponent.getHeight() - 175 - (minecraft.font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
            GuiComponent.fill(poseStack, 0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + 10 + y, minecraft.options.getBackgroundColor(-2147483648));
            int k = MathHelper.ceil((float) minecraft.gui.getChat().getWidth() / (float) minecraft.options.chatScale);
            int z = MathHelper.ceil((float) minecraft.gui.getChat().getHeight() / (float) minecraft.options.chatScale);

            if(ChatModule.showMTChat) ScreenHelpers.drawLogo(poseStack, minecraft.font, k + 6, z + 6, -2, minecraft.gui.getChat().getHeight() - 340, 0.75F);
        }
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> trimmedMessages(ChatComponent chatComponent)
    {
        if(!Config.getInstance().isChatEnabled()) return trimmedMessages;

        return ChatModule.showMTChat ? mtChatMessages : trimmedMessages;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"))
    private void fill(PoseStack poseStack, int i, int j, int k, int l, int m)
    {
        if(!Config.getInstance().isChatEnabled()) return;

        if(!isChatFocused())
        {
            GuiComponent.fill(poseStack, i, j, k, l, m);
        }
    }

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> addMessage(ChatComponent chatComponent)
    {
        if(!Config.getInstance().isChatEnabled()) return trimmedMessages;

        return ChatModule.showMTChat ? mtChatMessages : trimmedMessages;
    }

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;allMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<Component>> addMessageAll(ChatComponent chatComponent)
    {
        if(!Config.getInstance().isChatEnabled()) return allMessages;

        return ChatModule.showMTChat ? mtAllMessages : allMessages;
    }

    @Redirect(method = "scrollChat(D)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> scrollChat(ChatComponent chatComponent)
    {
        if(!Config.getInstance().isChatEnabled()) return trimmedMessages;

        return ChatModule.showMTChat ? mtChatMessages : trimmedMessages;
    }

    @Redirect(method = "getClickedComponentStyleAt", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> getClickedComponentStyleAt(ChatComponent chatComponent)
    {
        if(!Config.getInstance().isChatEnabled()) return trimmedMessages;

        return ChatModule.showMTChat ? mtChatMessages : trimmedMessages;
    }

    public void updateList()
    {
        try
        {
            LimitedSizeQueue<Message> temp = ChatHandler.messages.get(ChatHandler.CHANNEL);
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
