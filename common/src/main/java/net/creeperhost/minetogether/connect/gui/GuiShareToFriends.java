package net.creeperhost.minetogether.connect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.mixin.connect.ShareToLanScreenAccessor;
import net.creeperhost.minetogether.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;

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

        if (startButton == null || !ConnectHandler.isEnabled()) {
            if (cancelButton != null) {
                cancelButton.active = cancelButton.visible = true;
                children.clear();
                buttons.clear();
//                if (ConnectMain.authError.startsWith(findStr)) {
//                    //TODO v2
//                    //addRenderableWidget(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, CommonComponents.GUI_YES, (a) -> Util.getPlatform().openUri("https://minetogether.io/")));
//                } else {
                cancelButton.x = (this.width / 2) - (cancelButton.getWidth() / 2);
//                }
                addButton(cancelButton);
            }

            return;
        }

        startButton.active = startButton.visible = false;
        addButton(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, new TranslatableComponent("minetogether.connect.open.start"), (button1) -> {
            this.minecraft.setScreen(null);
            // TODO nuke accessor in favor of AT
            Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("minetogether.connect.open.attempting"));
            ShareToLanScreenAccessor thisMixin = (ShareToLanScreenAccessor) this;
            ConnectHandler.publishToFriends(GameType.byName(thisMixin.getGameModeName()), thisMixin.getCommands());
        }));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.title"), this.width / 2, 50, 16777215);

        if (ConnectHandler.isEnabled()) {
            drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);
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
