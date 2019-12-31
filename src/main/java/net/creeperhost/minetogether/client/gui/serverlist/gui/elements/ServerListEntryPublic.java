package net.creeperhost.minetogether.client.gui.serverlist.gui.elements;

import net.creeperhost.minetogether.client.gui.serverlist.data.Server;
import net.creeperhost.minetogether.client.gui.serverlist.data.ServerDataPublic;
import net.creeperhost.minetogether.data.EnumFlag;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.screen.ServerSelectionList.NormalEntry;
import net.minecraft.util.ResourceLocation;

public class ServerListEntryPublic extends ServerSelectionList.Entry
{
    public final NormalEntry wrapped;
    public final MultiplayerScreen owner;
    private ResourceLocation flags = new ResourceLocation("creeperhost", "textures/flags/flags.png");
    private ResourceLocation applicationGui = new ResourceLocation("creeperhost", "textures/gui.png");
    
    public ServerListEntryPublic(MultiplayerScreen mp, NormalEntry wrapped)
    {
//        super(mp, wrapped.getServerData());
        owner = mp;
        this.wrapped = wrapped;
    }

//    public ServerListEntryPublic(MockServerListEntryNormal wrapped)
//    {
//        this(wrapped.mockMP, wrapped);
//    }
    
    public void func_192634_a(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float newthingy)
    {
        ourDrawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering);
    }
    
    // < 1.12 compat
    public void func_180790_a(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        ourDrawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering);
    }


//    @Override
//    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
//    {
//        if (y <= 32 && x <= 32)
//        {
//            x = 33;
//        }
//        //System.out.println(slotIndex + " " + mouseX + " " + mouseY + " " + mouseEvent + " " + x + " " + y);
//        /*Server server = getServerData().server;
//        EnumFlag flag = server.flag;
//        int flagWidth = 16;
//        int flagHeight = flag.height / (flag.width / flagWidth);
//        String applicationURL = server.applicationURL;
//        if (applicationURL != null && mouseX >= x + listWidth - 5 - flagWidth
//            && mouseX <= x + listWidth - 5
//            && mouseY >= y + slotHeight - flagHeight
//            && mouseY <= y + slotHeight - flagHeight + flagHeight)
//        {
//            System.out.println(applicationURL);
//            return false;
//        }*/
//        return super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, x, y);
//    }
    
    public void ourDrawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        Util.getWrapper().draw(wrapped, slotIndex, x, y, listWidth, slotHeight, mouseX > (listWidth / 2) ? mouseX : Integer.MAX_VALUE, mouseY, false);
        
        Server server = null;//getServerData().server;
        EnumFlag flag = server.flag;
        String applicationURL = server.applicationURL;
        if (flag != null)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(flags);
            int flagWidth = 16;
            int flagHeight = flag.height / (flag.width / flagWidth);
//            Gui.drawScaledCustomSizeModalRect(x + listWidth - 5 - flagWidth, y + slotHeight - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
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
                owner.setHoveringText(countryName + (server.subdivision.equals("Unknown") ? "" : "\n" + server.subdivision));
            }
        }
        
        if (applicationURL != null)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(applicationGui);
            int flagWidth = 16;
            int flagHeight = flag.height / (flag.width / flagWidth);
//            Gui.drawScaledCustomSizeModalRect(x, y + slotHeight - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
            if (mouseX >= x
                    && mouseX <= x + flagWidth
                    && mouseY >= y + slotHeight - flagHeight
                    && mouseY <= y + slotHeight - flagHeight + flagHeight)
            {
                owner.setHoveringText("Click here to open the application link in a browser window!");
            }
        }
    }
    
    //    @Override
    public ServerDataPublic getServerData()
    {
        return null;//(ServerDataPublic) super.getServerData();
    }
    
    @Override
    public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_)
    {
    
    }
}