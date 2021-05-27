package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryLanDetected;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class OurServerListEntryLanDetected extends ServerListEntryLanDetected {
    
    final LanServerInfoConnect serverData; 

    public OurServerListEntryLanDetected(GuiMultiplayer p_i47141_1_, LanServerInfoConnect p_i47141_2_) {
        super(p_i47141_1_, p_i47141_2_);
        serverData = p_i47141_2_;
    }

    @Override
    public void func_192634_a(int p_192634_1_, int x, int y, int listWidth, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_)
    {
        this.mc.fontRendererObj.drawString(I18n.format("minetogether.connect.friendentry.title"), x + 32 + 3, y + 1, 16777215);
        this.mc.fontRendererObj.drawString(serverData.getFriend().getChosenName(), x + 32 + 3, y + 12, 8421504);
        this.mc.fontRendererObj.drawString(TextFormatting.DARK_GRAY + serverData.getFriend().getDisplayName(), x + listWidth - this.mc.fontRendererObj.getStringWidth(serverData.getFriend().getDisplayName()) - 20, y + 12, 16777215);
    }
}
