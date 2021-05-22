package net.creeperhost.minetogether.mixin;

import com.google.common.collect.Lists;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.connect.FriendsServerList;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public class MixinChatComponent
{
    @Shadow private List<String> recentChat;
    @Shadow private List<GuiMessage<Component>> allMessages;
    @Shadow private List<GuiMessage<FormattedCharSequence>> trimmedMessages;

    @Inject(at=@At("HEAD"), method="processPendingMessages", cancellable = true)
    public void getProcessPendingMessages(CallbackInfo ci)
    {
        updateList();
    }

//    @Inject(at=@At("TAIL"), method="getRecentChat", cancellable = true)
//    public void getRecentChat(CallbackInfoReturnable<List<String>> cir)
//    {
//        updateList();
//    }

    public void updateList()
    {
        LimitedSizeQueue<Message> temp;
        if(ChatHandler.messages == null || ChatHandler.messages.isEmpty()) return;
        temp = ChatHandler.messages.get(ChatHandler.CHANNEL);
        List<FormattedCharSequence> lines = new ArrayList<>();
        List<GuiMessage<FormattedCharSequence>> newLines = new ArrayList<>();
        //There must be a better way of doing this but brain go brrr....
        for(Message message : temp)
        {
            Component component = ChatScreen.formatLine(message);
            if(component == null) continue;
            lines.addAll(ComponentRenderUtils.wrapComponents(component, Minecraft.getInstance().gui.getChat().getWidth() - 10, Minecraft.getInstance().font));
        }
        for(FormattedCharSequence formattedCharSequence : lines)
        {
            if(formattedCharSequence == null) continue;
            newLines.add(new GuiMessage<>(0, formattedCharSequence, 0));
        }
        trimmedMessages = Lists.reverse(newLines);
    }
}
