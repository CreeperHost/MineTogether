package net.creeperhost.minetogether.module.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.mixin.MixinShareToLanScreen;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;

public class GuiShareToFriends extends ShareToLanScreen
{
    public GuiShareToFriends(Screen lastScreenIn)
    {
        super(lastScreenIn);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.title"), this.width / 2, 50, 16777215);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);

        //Super should render these
        for (Widget b : this.buttons)
        {
            b.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void init()
    {
        super.init();
        AbstractWidget startButton = ScreenHelpers.removeButton("lanServer.start", this.buttons);

        addButton(new Button(startButton.x, startButton.y, startButton.getWidth(), 20, new TranslatableComponent("minetogether.connect.open.start"), (button1) ->
        {
            this.minecraft.setScreen(null);
            MixinShareToLanScreen thisMixin = (MixinShareToLanScreen)this;
            net.creeperhost.minetogether.module.connect.ConnectHelper.shareToFriends(GameType.byName(thisMixin.getGameModeName()), thisMixin.getCommands());
        }));
    }
}
