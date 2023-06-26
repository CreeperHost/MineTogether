package net.creeperhost.minetogether.connect.gui;

import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.mixin.connect.ShareToLanScreenAccessor;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
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

        if (portEdit != null){
            portEdit.setVisible(false);
            portEdit.active = false;
        }

        AbstractWidget startButton = ButtonHelper.removeButton("lanServer.start", this);
        AbstractWidget cancelButton = ButtonHelper.removeButton("gui.cancel", this);

        if (startButton == null || !ConnectHandler.isEnabled()) {
            if (cancelButton != null) {
                cancelButton.active = cancelButton.visible = true;
                clearWidgets();
//                if (ConnectMain.authError.startsWith(findStr)) {
//                    //TODO v2
//                    //addRenderableWidget(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, CommonComponents.GUI_YES, (a) -> Util.getPlatform().openUri("https://minetogether.io/")));
//                } else {
                cancelButton.setX((this.width / 2) - (cancelButton.getWidth() / 2));
//                }
                addRenderableWidget(cancelButton);
            }

            return;
        }

        startButton.active = startButton.visible = false;
        addRenderableWidget(Button.builder(Component.translatable("minetogether.connect.open.start"), (button1) -> {
                            this.minecraft.setScreen(null);
                            // TODO nuke accessor in favor of AT
                            Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("minetogether.connect.open.attempting"));
                            ShareToLanScreenAccessor thisMixin = (ShareToLanScreenAccessor) this;
                            ConnectHandler.publishToFriends(thisMixin.getGameMode(), thisMixin.getCommands());
                        })
                        .bounds(startButton.getX(), startButton.getY(), startButton.getWidth(), 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);

        graphics.drawCenteredString(this.font, Component.translatable("minetogether.connect.open.title"), this.width / 2, 50, 16777215);

        if (ConnectHandler.isEnabled()) {
            graphics.drawCenteredString(this.font, Component.translatable("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);
        } else {
            //TODO unavailable message
            // Will probably want to completely redo this in v2.
        }

        for (GuiEventListener guiEventListener : this.children()) {
            Renderable renderable = (Renderable) guiEventListener;
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }
    }
}
