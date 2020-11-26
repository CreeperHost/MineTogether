package net.creeperhost.minetogether.mtconnect;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GuiShareToFriends extends ShareToLanScreen
{
    public GuiShareToFriends(Screen lastScreenIn)
    {
        super(lastScreenIn);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, new StringTextComponent(I18n.format("minetogether.connect.open.title")), this.width / 2, 50, 16777215);
        drawCenteredString(matrixStack, this.font, new StringTextComponent(I18n.format("minetogether.connect.open.settings")), this.width / 2, 82, 16777215);

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
        Widget startButton = ScreenUtils.removeButton("lanServer.start", this.buttons);

        Button ourButton = new Button(startButton.x, startButton.y, startButton.getWidth(), 20, new StringTextComponent(I18n.format("minetogether.connect.open.start")), (button1) ->
        {
            Object gameModeString = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, this, "field_146599_h");
            Object allowCheatsBoolean = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, this, "field_146600_i");

            this.minecraft.displayGuiScreen(null);
            boolean s = ConnectHelper.shareToFriends(GameType.getByName((String) gameModeString), (Boolean) allowCheatsBoolean);
            ITextComponent itextcomponent;

            if (s)
            {
                itextcomponent = new StringTextComponent("minetogether.connect.open.success");
            }
            else
            {
                itextcomponent = new StringTextComponent("minetogether.connect.open.failed");
            }

            this.minecraft.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
        });
        this.buttons.add(ourButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_231044_5_) {
        this.buttons.forEach(widget -> widget.mouseClicked(mouseX, mouseY, p_231044_5_));
        return false;
    }
}
