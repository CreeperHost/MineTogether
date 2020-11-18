package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
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
                itextcomponent = new TextComponentTranslation("commands.publish.started", "");
            }
            else
            {
                itextcomponent = new TextComponentString("commands.publish.failed");
            }

            this.mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
        } else {
            super.actionPerformed(button);
        }
    }
}
