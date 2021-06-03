package net.creeperhost.minetogether.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
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
    private List<GuiMessage<FormattedCharSequence>> mtChatMessages;

    @Inject(at=@At("HEAD"), method="processPendingMessages", cancellable = true)
    public void getProcessPendingMessages(CallbackInfo ci)
    {
        updateList();
    }

    @Inject(at=@At("HEAD"), method="render", cancellable = true)
    public void render(PoseStack poseStack, int i, CallbackInfo ci)
    {

    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/ChatComponent;trimmedMessages:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<GuiMessage<FormattedCharSequence>> trimmedMessages(ChatComponent chatComponent)
    {
        return ChatModule.showMTChat ? mtChatMessages : trimmedMessages;
    }

//    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"))
//    private static void fill(PoseStack poseStack, int i, int j, int k, int l, int m)
//    {
//
//    }

    public void updateList()
    {
        if(!ChatModule.showMTChat) return;
        if (ChatHandler.messages == null || ChatHandler.messages.isEmpty()) return;

        try
        {
            LimitedSizeQueue<Message> temp = ChatHandler.messages.get(ChatHandler.CHANNEL);
            List<FormattedCharSequence> lines = new ArrayList<>();
            List<GuiMessage<FormattedCharSequence>> newLines = new ArrayList<>();
            //There must be a better way of doing this but brain go brrr....
            for (Message message : temp) {
                Component component = ChatFormatter.formatLine(message);
                if (component == null) continue;
                lines.addAll(ComponentRenderUtils.wrapComponents(component, Minecraft.getInstance().gui.getChat().getWidth() - 10, Minecraft.getInstance().font));
            }
            for (FormattedCharSequence formattedCharSequence : lines) {
                if (formattedCharSequence == null) continue;
                newLines.add(new GuiMessage<>(0, formattedCharSequence, 0));
            }
            mtChatMessages = Lists.reverse(newLines);
        } catch (Exception e)
        {
//            e.printStackTrace();
        }
    }
}
