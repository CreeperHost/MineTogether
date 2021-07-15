package net.creeperhost.minetogether.module.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;

public class OurServerListEntryLanScan extends ServerSelectionList.LANHeader
{
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(PoseStack matrixStack, int p_192634_1_, int p_192634_2_, int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_)
    {
        int i = p_192634_3_ + p_192634_5_ / 2 - this.mc.font.lineHeight / 2;
        String locString = ConnectHelper.isEnabled ? I18n.get("minetogether.connect.scan") : I18n.get("minetogether.connect.scan.offline");
        this.mc.font.draw(matrixStack, locString, this.mc.screen.width / 2 - this.mc.font.width(locString) / 2, i, 16777215);
        String s;

        switch ((int) (Util.getMillis() / 300L % 4L))
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

        this.mc.font.draw(matrixStack, s, this.mc.screen.width / 2 - this.mc.font.width(s) / 2, i + this.mc.font.lineHeight, 8421504);
    }
}