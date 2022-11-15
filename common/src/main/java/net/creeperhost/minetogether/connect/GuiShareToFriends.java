package net.creeperhost.minetogether.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.mixin.connect.ShareToLanScreenAccessor;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.creeperhost.polylib.client.screen.ButtonHelper;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.Iterator;
import java.util.List;

public class GuiShareToFriends extends ShareToLanScreen {

    final Screen ourLastScreen;
    private static final String findStr = "website:";

    public GuiShareToFriends(Screen lastScreenIn) {
        super(lastScreenIn);
        ourLastScreen = lastScreenIn;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.title"), this.width / 2, 50, 16777215);
        if (ConnectHelper.isEnabled) {
            drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);
        } else {
            FormattedText renderComponent;
            String findStr = "website:";
            String sanitisedAuthError = ConnectMain.authError.substring(findStr.length());
            if (ConnectMain.authError.contains(findStr)) {
                renderComponent = new TranslatableComponent("minetogether.connect.unavailable.website", sanitisedAuthError);
            } else {
                renderComponent = new TranslatableComponent("minetogether.connect.unavailable", ConnectMain.authError);
            }

            List<FormattedCharSequence> formattedCharSequences = ComponentRenderUtils.wrapComponents(renderComponent, this.width - 5, this.minecraft.font);
            int startHeight = (this.height / 2) - ((formattedCharSequences.size() * this.minecraft.font.lineHeight) / 2);
            for (FormattedCharSequence sequence : formattedCharSequences) {
                int left = (width - minecraft.font.width(sequence)) / 2;
                minecraft.font.draw(matrixStack, sequence, left, startHeight += this.minecraft.font.lineHeight, -1);
            }
        }

        //Super would usually render these, but we don't want to call super
        Iterator var5 = this.children().iterator();

        while (var5.hasNext()) {
            Widget widget = (Widget) var5.next();
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void init() {
        super.init();

        AbstractWidget startButton = ButtonHelper.removeButton("lanServer.start", this);

        if (ConnectHelper.isEnabled && startButton != null) {
            startButton.active = false;
            startButton.visible = false;

            addRenderableWidget(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, new TranslatableComponent("minetogether.connect.open.start"), (button1) ->
            {
                this.minecraft.setScreen(null);
                ShareToLanScreenAccessor thisMixin = (ShareToLanScreenAccessor) this;
                ConnectHelper.shareToFriends(thisMixin.getGameMode(), thisMixin.getCommands());
                TranslatableComponent itextcomponent = new TranslatableComponent("minetogether.connect.open.attempting");
                Minecraft.getInstance().gui.getChat().addMessage(itextcomponent);
            }));
        } else {
            AbstractWidget cancelButton = ButtonHelper.removeButton("gui.cancel", this);

            if (cancelButton != null) {
                cancelButton.active = true;
                cancelButton.visible = true;
                clearWidgets();
                if (ConnectMain.authError.startsWith(findStr)) {
                    addRenderableWidget(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, CommonComponents.GUI_YES, (a) -> Util.getPlatform().openUri("https://minetogether.io/")));
                } else {
                    cancelButton.x = (this.width / 2) - (cancelButton.getWidth() / 2);
                }
                addRenderableWidget(cancelButton);
            }
        }
    }
}
