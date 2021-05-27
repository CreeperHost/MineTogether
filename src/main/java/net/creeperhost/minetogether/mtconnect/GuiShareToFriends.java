package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
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
        for (GuiButton b : this.buttonList)
        {
            b.func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        for (GuiButton b : this.buttonList)
        {
            if(b.id == 101)
            {
                b.displayString = I18n.format("minetogether.connect.open.start");
            }
        }

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        Object gameModeString = ObfuscationReflectionHelper.getPrivateValue(GuiShareToLan.class, this, "gameMode", "field_146599_h");
        Object allowCheatsBoolean = ObfuscationReflectionHelper.getPrivateValue(GuiShareToLan.class, this, "allowCheats", "field_146600_i");

        if (button.id == 101)
        {
            this.mc.displayGuiScreen(null);
            ConnectHelper.shareToFriends(GameType.getByName((String) gameModeString), (Boolean) allowCheatsBoolean);
        } else {
            super.actionPerformed(button);
        }
    }
}
