package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.IOException;

public class GuiShareToFriends extends GuiShareToLan {
    public GuiShareToFriends(GuiScreen lastScreenIn) {
        super(lastScreenIn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(mc.fontRendererObj, I18n.format("minetogether.connect.open.title"), this.width / 2, 50, 16777215);
        this.drawCenteredString(mc.fontRendererObj, I18n.format("minetogether.connect.open.settings"), this.width / 2, 82, 16777215);
        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            GuiButton b = this.buttonList.get(i);
            if(b.id == 101)
            {
                b.displayString = I18n.format("minetogether.connect.open.start");
            }
            b.func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }
        /*for (int j = 0; j < this.labelList.size(); ++j)
        {
            ((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
        }*/
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        Object gameModeString = ObfuscationReflectionHelper.getPrivateValue(GuiShareToLan.class, this, "gameMode", "field_146599_h");
        Object allowCheatsBoolean = ObfuscationReflectionHelper.getPrivateValue(GuiShareToLan.class, this, "allowCheats", "field_146600_i");


        if (button.id == 101)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            boolean s = ConnectHelper.shareToFriends(GameType.getByName((String) gameModeString), (Boolean) allowCheatsBoolean);
            ITextComponent itextcomponent;

            if (s)
            {
                itextcomponent = new TextComponentTranslation("minetogether.connect.open.success");
            }
            else
            {
                itextcomponent = new TextComponentTranslation("minetogether.connect.open.failed");
            }

            this.mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
        } else {
            super.actionPerformed(button);
        }
    }
}
