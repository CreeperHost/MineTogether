package net.creeperhost.minetogether.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.connect.FriendsServerList;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent
{
    @Shadow private Minecraft minecraft;
    @Shadow private int chatScrollbarPos;
    @Shadow protected abstract boolean isChatHidden();
    @Shadow protected abstract void processPendingMessages();
    @Shadow public abstract int getLinesPerPage();
    @Shadow protected abstract boolean isChatFocused();
    @Shadow public abstract double getScale();
    @Shadow public abstract int getWidth();

    @Shadow
    private static double getTimeFactor(int i)
    {
        return 0;
    }

    private List<GuiMessage<FormattedCharSequence>> mtChatMessages;

    @Inject(at=@At("HEAD"), method="processPendingMessages", cancellable = true)
    public void getProcessPendingMessages(CallbackInfo ci)
    {
        updateList();
    }

    @Inject(at=@At("HEAD"), method="render", cancellable = true)
    public void render(PoseStack poseStack, int i, CallbackInfo ci)
    {
        if(ChatModule.showMTChat)
        {
            if (!isChatHidden())
            {
                processPendingMessages();
                int j = getLinesPerPage();
                int k = this.mtChatMessages.size();
                if (k > 0)
                {
                    boolean bl = false;
                    if (isChatFocused()) bl = true;

                    double d = getScale();
                    int l = Mth.ceil((double) getWidth() / d);
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                    RenderSystem.scaled(d, d, 1.0D);
                    double e = this.minecraft.options.chatOpacity * 0.8999999761581421D + 0.10000000149011612D;
                    double f = this.minecraft.options.textBackgroundOpacity;
                    double g = 9.0D * (this.minecraft.options.chatLineSpacing + 1.0D);
                    double h = -8.0D * (this.minecraft.options.chatLineSpacing + 1.0D) + 4.0D * this.minecraft.options.chatLineSpacing;
                    int m = 0;

                    int n;
                    int y;
                    int ab;
                    int ac;
                    for (n = 0; n + this.chatScrollbarPos < this.mtChatMessages.size() && n < j; ++n)
                    {
                        GuiMessage<FormattedCharSequence> guiMessage = (GuiMessage) this.mtChatMessages.get(n + this.chatScrollbarPos);
                        if (guiMessage != null)
                        {
                            y = i - guiMessage.getAddedTime();
                            if (y < 200 || bl) {
                                double p = bl ? 1.0D : getTimeFactor(y);
                                ab = (int) (255.0D * p * e);
                                ac = (int) (255.0D * p * f);
                                ++m;
                                if (ab > 3)
                                {
                                    double t = (double) (-n) * g;
                                    poseStack.pushPose();
                                    poseStack.translate(0.0D, 0.0D, 50.0D);
                                    Screen.fill(poseStack, -2, (int) (t - g), 0 + l + 4, (int) t, ac << 24);
                                    RenderSystem.enableBlend();
                                    poseStack.translate(0.0D, 0.0D, 50.0D);
                                    this.minecraft.font.drawShadow(poseStack, (FormattedCharSequence) guiMessage.getMessage(), 0.0F, (float) ((int) (t + h)), 16777215 + (ab << 24));
                                    RenderSystem.disableAlphaTest();
                                    RenderSystem.disableBlend();
                                    poseStack.popPose();
                                }
                            }
                        }
                    }
                }
            }
            RenderSystem.popMatrix();
            //return here to allow vanilla render to take over in commands tab
            ci.cancel();
        }
    }

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
        mtChatMessages = Lists.reverse(newLines);
    }
}
