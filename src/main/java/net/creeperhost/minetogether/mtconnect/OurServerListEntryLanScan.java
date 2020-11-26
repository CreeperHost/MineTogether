package net.creeperhost.minetogether.mtconnect;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;

public class OurServerListEntryLanScan extends ServerSelectionList.LanScanEntry
{
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(MatrixStack matrixStack, int p_192634_1_, int p_192634_2_, int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_)
    {
        int i = p_192634_3_ + p_192634_5_ / 2 - this.mc.fontRenderer.FONT_HEIGHT / 2;
        String locString = ConnectHelper.isEnabled ? I18n.format("minetogether.connect.scan") : I18n.format("minetogether.connect.scan.offline");
        this.mc.fontRenderer.drawString(matrixStack, locString, this.mc.currentScreen.width / 2 - this.mc.fontRenderer.getStringWidth(locString) / 2, i, 16777215);
        String s;

        switch ((int)(Util.milliTime() / 300L % 4L))
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

        this.mc.fontRenderer.drawString(matrixStack, s, this.mc.currentScreen.width / 2 - this.mc.fontRenderer.getStringWidth(s) / 2, i + this.mc.fontRenderer.FONT_HEIGHT, 8421504);
    }
}
