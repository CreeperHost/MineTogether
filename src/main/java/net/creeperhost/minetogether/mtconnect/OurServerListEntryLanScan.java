package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ServerListEntryLanScan;
import net.minecraft.client.resources.I18n;

public class OurServerListEntryLanScan extends ServerListEntryLanScan {
    private final Minecraft mc = Minecraft.getMinecraft();
    @Override
    public void func_192634_a(int p_192634_1_, int p_192634_2_, int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_)
    {
        int i = p_192634_3_ + p_192634_5_ / 2 - this.mc.fontRendererObj.FONT_HEIGHT / 2;
        String locString = ConnectHelper.isEnabled ? I18n.format("minetogether.connect.scan") : I18n.format("minetogether.connect.scan.offline");
        this.mc.fontRendererObj.drawString(locString, this.mc.currentScreen.width / 2 - this.mc.fontRendererObj.getStringWidth(locString) / 2, i, 16777215);
        String s;

        switch ((int)(Minecraft.getSystemTime() / 300L % 4L))
        {
            case 0:
            default:
                s = "O o o";
                break;
            case 1:
            case 3:
                s = "o O o";
                break;
            case 2:
                s = "o o O";
        }

        this.mc.fontRendererObj.drawString(s, this.mc.currentScreen.width / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2, i + this.mc.fontRendererObj.FONT_HEIGHT, 8421504);
    }
}
