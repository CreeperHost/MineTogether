package net.creeperhost.minetogether.gui.serverlist.gui.elements;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.gui.serverlist.data.EnumFlag;
import net.creeperhost.minetogether.gui.serverlist.data.Server;
import net.creeperhost.minetogether.gui.serverlist.data.ServerDataPublic;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class ServerListEntryPublic extends ServerListEntryNormal
{
    public final ServerListEntryNormal wrapped;
    public final GuiMultiplayer owner;

    public ServerListEntryPublic(GuiMultiplayer mp, ServerListEntryNormal wrapped)
    {
        super(mp, wrapped.func_148296_a());
        owner = mp;
        this.wrapped = wrapped;
    }

    public ServerListEntryPublic(MockServerListEntryNormal wrapped)
    {
        this(wrapped.mockMP, wrapped);
    }


    private ResourceLocation flags = new ResourceLocation("creeperhost", "textures/flags/flags.png");
    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isHovering)
    {
        wrapped.drawEntry(slotIndex, x, y, listWidth, slotHeight, tessellator, mouseX > (listWidth / 2) ? mouseX : Integer.MAX_VALUE, mouseY,false);

        Server server = func_148296_a().server;
        EnumFlag flag = server.flag;
        if (flag == null)
            return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(flags);
        int flagWidth = 16;
        int flagHeight = flag.height / (flag.width / flagWidth);
        Gui.func_152125_a(x + listWidth - 5 - flagWidth, y + slotHeight - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
        if (mouseX >= x + listWidth - 5 - flagWidth
          && mouseX <= x + listWidth - 5
          && mouseY >= y + slotHeight - flagHeight
          && mouseY <= y + slotHeight - flagHeight + flagHeight)
        {
            String countryName = Callbacks.getCountries().get(flag.name());
            if (countryName == null)
            {
                countryName = flag.name();
            }
            owner.func_146793_a(countryName + (server.subdivision.equals("Unknown") ? "" : "\n" + server.subdivision));
        }
    }

    @Override
    public ServerDataPublic func_148296_a()
    {
        return (ServerDataPublic) super.func_148296_a();
    }
}