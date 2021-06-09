package net.creeperhost.minetogether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.widgets.GuiButtonPair;
import net.creeperhost.minetogether.util.MathHelper;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen
{
    @Shadow public abstract void render(PoseStack poseStack, int i, int j, float f);

    @Shadow protected EditBox input;
    @Shadow private CommandSuggestions commandSuggestions;
    private GuiButtonPair switchButton;

    protected MixinChatScreen(Component component)
    {
        super(component);
    }

    @Inject(at=@At("TAIL"), method="init()V")
    public void init(CallbackInfo ci)
    {
        int x = MathHelper.ceil(((float) Minecraft.getInstance().gui.getChat().getWidth())) + 16 + 2;

        addButton(switchButton = new GuiButtonPair(x, height - 215, 234, 16, 0, false, false, true, p ->
        {
            ChatModule.showMTChat = switchButton.activeButton == 1;

        }, I18n.get("minetogether.ingame.chat.local"), I18n.get("minetogether.ingame.chat.global")));
    }

    @Inject(at=@At("HEAD"), method="render", cancellable = true)
    public void render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci)
    {
        int k = MathHelper.ceil((float) Minecraft.getInstance().gui.getChat().getWidth() / (float) Minecraft.getInstance().options.chatScale);

        if(ChatModule.showMTChat) ScreenHelpers.drawLogo(poseStack, font, k + 6, height + 18, -2, 30, 0.75F);
        ChatComponent chatComponent = Minecraft.getInstance().gui.getChat();
        int y = height - 40 - (Minecraft.getInstance().font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
        //TODO bring this back when I can remove fill from the components
        //fill(poseStack, 0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + y, 0x99000000);

        setFocused(input);
        input.setFocus(true);
        fill(poseStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(-2147483648));

        input.render(poseStack, i, j, f);
        if(!ChatModule.showMTChat) commandSuggestions.render(poseStack, i, j);
        Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt((double)i, (double)j);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(poseStack, style, i, j);
        }
        super.render(poseStack, i, j, f);

        ci.cancel();
    }

    @Override
    public void sendMessage(String string)
    {
        if(ChatModule.showMTChat)
        {
            ChatHandler.sendMessage(ChatHandler.CHANNEL, string);
            //TODO stop this from logging sent messages
            Minecraft.getInstance().gui.getChat().addMessage(ChatFormatter.formatLine(new Message(0, MineTogetherChat.INSTANCE.ourNick, string)));
            return;
        }
        super.sendMessage(string);
    }
}
