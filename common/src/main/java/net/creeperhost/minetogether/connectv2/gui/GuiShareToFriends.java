package net.creeperhost.minetogether.connectv2.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.connectv2.ConnectHandlerV2;
import net.creeperhost.minetogether.mixin.connect.ShareToLanScreenAccessor;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;

public class GuiShareToFriends extends ShareToLanScreen {

    final Screen parent;
    private static final String findStr = "website:";

    public GuiShareToFriends(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();

        AbstractWidget startButton = ButtonHelper.removeButton("lanServer.start", this);
        AbstractWidget cancelButton = ButtonHelper.removeButton("gui.cancel", this);

        if (startButton == null || !ConnectHandlerV2.isEnabled()) {
            if (cancelButton != null) {
                cancelButton.active = cancelButton.visible = true;
                clearWidgets();
                if (ConnectMain.authError.startsWith(findStr)) {
                    //TODO v2
                    //addRenderableWidget(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, CommonComponents.GUI_YES, (a) -> Util.getPlatform().openUri("https://minetogether.io/")));
                } else {
                    cancelButton.x = (this.width / 2) - (cancelButton.getWidth() / 2);
                }
                addRenderableWidget(cancelButton);
            }

            return;
        }

        startButton.active = startButton.visible = false;
        addRenderableWidget(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, Component.translatable("minetogether.connect.open.start"), (button1) -> {
            this.minecraft.setScreen(null);
            ShareToLanScreenAccessor thisMixin = (ShareToLanScreenAccessor) this;
            //TODO v2 do share operation.
//            ConnectHelper.shareToFriends(thisMixin.getGameMode(), thisMixin.getCommands());
            Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("minetogether.connect.open.attempting"));
        }));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        drawCenteredString(matrixStack, this.font, Component.translatable("minetogether.connect.open.title"), this.width / 2, 50, 16777215);

        if (ConnectHandlerV2.isEnabled()) {
            drawCenteredString(matrixStack, this.font, Component.translatable("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);
        } else {
            //TODO unavailable message
            // Will probably want to completely redo this in v2.
        }

        //super.super.render()
        for (GuiEventListener guiEventListener : this.children()) {
            Widget widget = (Widget) guiEventListener;
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
