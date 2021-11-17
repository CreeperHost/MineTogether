package net.creeperhost.minetogether.module.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.mixin.MixinShareToLanScreen;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameType;

import java.util.List;

public class GuiShareToFriends extends ShareToLanScreen
{
    final Screen ourLastScreen;
    public GuiShareToFriends(Screen lastScreenIn)
    {
        super(lastScreenIn);
        ourLastScreen = lastScreenIn;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.title"), this.width / 2, 50, 16777215);
        if (ConnectHelper.isEnabled) {
            drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);
        } else {
            FormattedText renderComponent;
            if (ConnectMain.authError.contains("upgrade")) {
                renderComponent = new TranslatableComponent("minetogether.connect.unavailable.upgrade", ConnectMain.authError);
            } else {
                renderComponent = new TranslatableComponent("minetogether.connect.unavailable", ConnectMain.authError);
            }

            List<FormattedCharSequence> formattedCharSequences = ComponentRenderUtils.wrapComponents(renderComponent, this.width - 5, this.minecraft.font);
            int startHeight = (this.height / 2) - ((formattedCharSequences.size() * this.minecraft.font.lineHeight) / 2);
            for (FormattedCharSequence sequence: formattedCharSequences) {
                int left = (width - minecraft.font.width(sequence)) / 2;
                minecraft.font.draw(matrixStack, sequence, left, startHeight += this.minecraft.font.lineHeight, -1);
            }
        }

        //Super should render these
        for (Widget b : this.buttons) {
            b.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void init()
    {
        super.init();

        AbstractWidget startButton = ScreenHelpers.removeButton("lanServer.start", this.buttons);
        if (ConnectHelper.isEnabled) {
            addButton(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, new TranslatableComponent("minetogether.connect.open.start"), (button1) ->
            {
                this.minecraft.setScreen(null);
                MixinShareToLanScreen thisMixin = (MixinShareToLanScreen) this;
                net.creeperhost.minetogether.module.connect.ConnectHelper.shareToFriends(GameType.byName(thisMixin.getGameModeName()), thisMixin.getCommands());
                TranslatableComponent itextcomponent = new TranslatableComponent("minetogether.connect.open.attempting");
                Minecraft.getInstance().gui.getChat().addMessage(itextcomponent);
            }));
        } else {
            AbstractWidget cancelButton = ScreenHelpers.removeButton(CommonComponents.GUI_CANCEL.getString(), this.buttons);
            cancelButton.active = true;
            cancelButton.visible = true;
            buttons.clear();
            if(ConnectMain.authError.contains("upgrade")) {
                addButton(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, CommonComponents.GUI_YES, (a) -> Util.getPlatform().openUri("https://minetogether.io/")));
            } else {
                cancelButton.x = (this.width / 2) - (cancelButton.getWidth() / 2);
            }
            addButton(cancelButton);
        }
    }
}
