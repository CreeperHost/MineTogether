package net.creeperhost.minetogether.client.gui.serverlist.data;

import com.google.common.collect.Lists;
import net.creeperhost.minetogether.data.EnumFlag;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ServerSelectionListOurs extends ServerSelectionList
{
    public List<ServerListEntryPublic> serverListInternetOurs = Lists.newArrayList();
    public MultiplayerScreen multiplayerScreen;
    
    public ServerSelectionListOurs(MultiplayerScreen multiplayerScreen, Minecraft mc, int width, int height, int top, int bottom, int slotHeight)
    {
        super(multiplayerScreen, mc, width, height, top, bottom, slotHeight);
        this.multiplayerScreen = multiplayerScreen;
    }
    
    @Override
    public void func_195094_h()
    {
        this.clearEntries();
        this.serverListInternetOurs.forEach(this::addEntry);
        this.addEntry(this.lanScanEntry);
        this.serverListLan.forEach(this::addEntry);
    }
    
    @Override
    protected int addEntry(Entry p_addEntry_1_)
    {
        return super.addEntry(p_addEntry_1_);
    }
    
    @Override
    public void updateOnlineServers(ServerList p_148195_1_)
    {
        this.serverListInternet.clear();;
        this.serverListLan.clear();
        
        this.serverListInternetOurs.clear();
    
        for(int i = 0; i < p_148195_1_.countServers(); ++i)
        {
            this.serverListInternetOurs.add(new ServerSelectionListOurs.ServerListEntryPublic(this.multiplayerScreen, p_148195_1_.getServerData(i)));
        }
    
        this.func_195094_h();
    }
    
    public class ServerListEntryPublic extends ServerSelectionList.NormalEntry
    {
        MultiplayerScreen multiplayerScreen;
        ServerData wrappedEntry;
        private ResourceLocation flags = new ResourceLocation("creeperhost", "textures/flags/flags.png");
        private ResourceLocation applicationGui = new ResourceLocation("creeperhost", "textures/gui.png");
        
        public ServerListEntryPublic(MultiplayerScreen multiplayerScreen, ServerData wrappedEntry)
        {
            super(multiplayerScreen, wrappedEntry);
            this.multiplayerScreen = multiplayerScreen;
            this.wrappedEntry = wrappedEntry;
        }
    
        @Override
        public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_)
        {
            super.render(slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isHovering, p_render_9_);
            Server server = getServerData().server;
            EnumFlag flag = server.flag;
            String applicationURL = server.applicationURL;
            if (flag != null)
            {
                Minecraft.getInstance().getTextureManager().bindTexture(flags);
                int flagWidth = 16;
                int flagHeight = flag.height / (flag.width / flagWidth);
                drawScaledCustomSizeModalRect(x + listWidth - 5 - flagWidth, y + slotHeight - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
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
                    multiplayerScreen.setHoveringText(countryName + (server.subdivision.equals("Unknown") ? "" : "\n" + server.subdivision));
                }
            }
            if (applicationURL != null)
            {
                Minecraft.getInstance().getTextureManager().bindTexture(applicationGui);
                int flagWidth = 16;
                int flagHeight = flag.height / (flag.width / flagWidth);
                blit(x, y + slotHeight - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
                if (mouseX >= x
                        && mouseX <= x + flagWidth
                        && mouseY >= y + slotHeight - flagHeight
                        && mouseY <= y + slotHeight - flagHeight + flagHeight)
                {
                    multiplayerScreen.setHoveringText("Click here to open the application link in a browser window!");
                }
            }
        }
    
        public void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight)
        {
            float f = 1.0F / tileWidth;
            float f1 = 1.0F / tileHeight;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos((double)x, (double)(y + height), 0.0D).tex((u * f), ((v + (float)vHeight) * f1)).endVertex();
            bufferbuilder.pos((double)(x + width), (double)(y + height), 0.0D).tex(((u + (float)uWidth) * f), ((v + (float)vHeight) * f1)).endVertex();
            bufferbuilder.pos((double)(x + width), (double)y, 0.0D).tex(((u + (float)uWidth) * f), (v * f1)).endVertex();
            bufferbuilder.pos((double)x, (double)y, 0.0D).tex((u * f), (v * f1)).endVertex();
            tessellator.draw();
        }
    
        @Override
        public ServerDataPublic getServerData()
        {
            return (ServerDataPublic) super.getServerData();
        }
    }
}
