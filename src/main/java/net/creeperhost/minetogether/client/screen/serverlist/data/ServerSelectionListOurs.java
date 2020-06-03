package net.creeperhost.minetogether.client.screen.serverlist.data;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.client.screen.serverlist.gui.MultiplayerPublicScreen;
import net.creeperhost.minetogether.data.EnumFlag;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.Validate;

import java.net.UnknownHostException;
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
    }
    
    @Override
    protected int addEntry(Entry p_addEntry_1_)
    {
        return super.addEntry(p_addEntry_1_);
    }
    
    @Override
    public void updateOnlineServers(ServerList p_148195_1_)
    {
        this.serverListInternet.clear();
        ;
        this.serverListLan.clear();
        
        this.serverListInternetOurs.clear();
        
        for (int i = 0; i < p_148195_1_.countServers(); ++i)
        {
            this.serverListInternetOurs.add(new ServerSelectionListOurs.ServerListEntryPublic((MultiplayerPublicScreen) this.multiplayerScreen, p_148195_1_.getServerData(i)));
        }
        
        this.func_195094_h();
    }
    
    @Override
    public void updateNetworkServers(List<LanServerInfo> p_148194_1_)
    {
    }
    
    public class ServerListEntryPublic extends ServerSelectionList.NormalEntry
    {
        MultiplayerPublicScreen multiplayerScreen;
        ServerData wrappedEntry;
        Minecraft mc = Minecraft.getInstance();
        private ResourceLocation flags = new ResourceLocation("creeperhost", "textures/flags/flags.png");
        private ResourceLocation applicationGui = new ResourceLocation("creeperhost", "textures/gui.png");
        private String lastIconB64;
        private ResourceLocation serverIcon;
        private DynamicTexture icon;
        private long lastClickTime;
        
        public ServerListEntryPublic(MultiplayerPublicScreen multiplayerScreen, ServerData wrappedEntry)
        {
            super(multiplayerScreen, wrappedEntry);
            this.multiplayerScreen = multiplayerScreen;
            this.wrappedEntry = wrappedEntry;
        }
        
        @Override
        public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_)
        {
            vanilaRender(slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isHovering, p_render_9_);
            
            Server server = getServerData().server;
            EnumFlag flag = server.flag;
            String applicationURL = server.applicationURL;
            if (flag != null)
            {
                Minecraft.getInstance().getTextureManager().bindTexture(flags);
                int flagWidth = 16;
                int flagHeight = flag.height / (flag.width / flagWidth);
                drawScaledCustomSizeModalRect(x + listWidth - 5 - flagWidth, y + slotHeight - 10 - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
                if (mouseX >= x + listWidth - 5 - flagWidth
                        && mouseX <= x + listWidth - 5
                        && mouseY >= y + slotHeight - 10 - flagHeight
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
                blit(x, y + slotHeight - 10 - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
                if (mouseX >= x
                        && mouseX <= x + flagWidth
                        && mouseY >= y + slotHeight - flagHeight
                        && mouseY <= y + slotHeight - flagHeight + flagHeight)
                {
                    multiplayerScreen.setHoveringText("Click here to open the application link in a browser window!");
                }
            }
        }
        
        private void prepareServerIcon()
        {
            String s = this.wrappedEntry.getBase64EncodedIconData();
            if (s == null)
            {
                this.mc.getTextureManager().deleteTexture(this.serverIcon);
                if (this.icon != null && this.icon.getTextureData() != null)
                {
                    this.icon.getTextureData().close();
                }
                
                this.icon = null;
            } else
            {
                try
                {
                    NativeImage nativeimage = NativeImage.readBase64(s);
                    Validate.validState(nativeimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(nativeimage.getHeight() == 64, "Must be 64 pixels high");
                    if (this.icon == null)
                    {
                        this.icon = new DynamicTexture(nativeimage);
                    } else
                    {
                        this.icon.setTextureData(nativeimage);
                        this.icon.updateDynamicTexture();
                    }
                    
                    this.mc.getTextureManager().loadTexture(this.serverIcon, this.icon);
                } catch (Throwable throwable)
                {
                    this.wrappedEntry.setBase64EncodedIconData((String) null);
                }
            }
        }
        
        public void vanilaRender(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_)
        {
            if (!this.wrappedEntry.pinged)
            {
                this.wrappedEntry.pinged = true;
                this.wrappedEntry.pingToServer = -2L;
                this.wrappedEntry.serverMOTD = "";
                this.wrappedEntry.populationInfo = "";
                ServerSelectionList.field_214358_b.submit(() ->
                {
                    try
                    {
                        this.multiplayerScreen.getOldServerPinger().ping(this.wrappedEntry);
                    } catch (UnknownHostException var2)
                    {
                        this.wrappedEntry.pingToServer = -1L;
                        this.wrappedEntry.serverMOTD = TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_resolve");
                    } catch (Exception var3)
                    {
                        this.wrappedEntry.pingToServer = -1L;
                        this.wrappedEntry.serverMOTD = TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_connect");
                    }
                });
            }
            
            boolean flag = this.wrappedEntry.version > SharedConstants.getVersion().getProtocolVersion();
            boolean flag1 = this.wrappedEntry.version < SharedConstants.getVersion().getProtocolVersion();
            boolean flag2 = flag || flag1;
            this.mc.fontRenderer.drawString(this.wrappedEntry.serverName, (float) (p_render_3_ + 32 + 3), (float) (p_render_2_ + 1), 16777215);
            List<String> list = this.mc.fontRenderer.listFormattedStringToWidth(this.wrappedEntry.serverMOTD, p_render_4_ - 32 - 2);
            
            for (int i = 0; i < Math.min(list.size(), 2); ++i)
            {
                this.mc.fontRenderer.drawString(list.get(i), (float) (p_render_3_ + 32 + 3), (float) (p_render_2_ + 12 + 9 * i), 8421504);
            }
            
            String s2 = flag2 ? TextFormatting.DARK_RED + this.wrappedEntry.gameVersion : this.wrappedEntry.populationInfo;
            int j = this.mc.fontRenderer.getStringWidth(s2);
            this.mc.fontRenderer.drawString(s2, (float) (p_render_3_ + p_render_4_ - j - 15 - 2), (float) (p_render_2_ + 1), 8421504);
            int k = 0;
            String s = null;
            int l;
            String s1;
            if (flag2)
            {
                l = 5;
                s1 = I18n.format(flag ? "multiplayer.status.client_out_of_date" : "multiplayer.status.server_out_of_date");
                s = this.wrappedEntry.playerList;
            } else if (this.wrappedEntry.pinged && this.wrappedEntry.pingToServer != -2L)
            {
                if (this.wrappedEntry.pingToServer < 0L)
                {
                    l = 5;
                } else if (this.wrappedEntry.pingToServer < 150L)
                {
                    l = 0;
                } else if (this.wrappedEntry.pingToServer < 300L)
                {
                    l = 1;
                } else if (this.wrappedEntry.pingToServer < 600L)
                {
                    l = 2;
                } else if (this.wrappedEntry.pingToServer < 1000L)
                {
                    l = 3;
                } else
                {
                    l = 4;
                }
                
                if (this.wrappedEntry.pingToServer < 0L)
                {
                    s1 = I18n.format("multiplayer.status.no_connection");
                } else
                {
                    s1 = this.wrappedEntry.pingToServer + "ms";
                    s = this.wrappedEntry.playerList;
                }
            } else
            {
                k = 1;
                l = (int) (Util.milliTime() / 100L + (long) (p_render_1_ * 2) & 7L);
                if (l > 4)
                {
                    l = 8 - l;
                }
                
                s1 = I18n.format("multiplayer.status.pinging");
            }
            
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
            AbstractGui.blit(p_render_3_ + p_render_4_ - 15, p_render_2_, (float) (k * 10), (float) (176 + l * 8), 10, 8, 256, 256);
            if (this.wrappedEntry.getBase64EncodedIconData() != null && !this.wrappedEntry.getBase64EncodedIconData().equals(this.lastIconB64))
            {
                this.lastIconB64 = this.wrappedEntry.getBase64EncodedIconData();
                this.prepareServerIcon();
                this.multiplayerScreen.getServerList().saveServerList();
            }
            
            if (this.icon != null)
            {
                this.drawTextureAt(p_render_3_, p_render_2_, this.serverIcon);
            } else
            {
                this.drawTextureAt(p_render_3_, p_render_2_, ServerSelectionList.field_214359_c);
            }
            
            int i1 = p_render_6_ - p_render_3_;
            int j1 = p_render_7_ - p_render_2_;
            if (i1 >= p_render_4_ - 15 && i1 <= p_render_4_ - 5 && j1 >= 0 && j1 <= 8)
            {
                this.multiplayerScreen.setHoveringText(s1);
            } else if (i1 >= p_render_4_ - j - 15 - 2 && i1 <= p_render_4_ - 15 - 2 && j1 >= 0 && j1 <= 8)
            {
                this.multiplayerScreen.setHoveringText(s);
            }
            
            if (this.mc.gameSettings.touchscreen || p_render_8_)
            {
                this.mc.getTextureManager().bindTexture(ServerSelectionList.field_214360_d);
                AbstractGui.fill(p_render_3_, p_render_2_, p_render_3_ + 32, p_render_2_ + 32, -1601138544);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int k1 = p_render_6_ - p_render_3_;
                int l1 = p_render_7_ - p_render_2_;
                if (this.canJoin())
                {
                    if (k1 < 32 && k1 > 16)
                    {
                        AbstractGui.blit(p_render_3_, p_render_2_, 0.0F, 32.0F, 32, 32, 256, 256);
                    }
                }
            }
        }
        
        private boolean canJoin()
        {
            return true;
        }
        
        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
        {
            double d0 = p_mouseClicked_1_ - (double) ServerSelectionListOurs.this.getRowLeft();
            double d1 = p_mouseClicked_3_ - (double) ServerSelectionListOurs.this.getRowTop(ServerSelectionListOurs.this.children().indexOf(this));
            if (d0 <= 32.0D)
            {
                if (d0 < 32.0D && d0 > 16.0D && this.canJoin())
                {
                    this.multiplayerScreen.func_214287_a(this);
                    this.multiplayerScreen.connectToSelected();
                    return true;
                }
            }
            
            this.multiplayerScreen.func_214287_a(this);
            if (Util.milliTime() - this.lastClickTime < 250L)
            {
                this.multiplayerScreen.connectToSelected();
            }
            
            this.lastClickTime = Util.milliTime();
            return false;
        }
        
        public void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight)
        {
            float f = 1.0F / tileWidth;
            float f1 = 1.0F / tileHeight;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos((double) x, (double) (y + height), 0.0D).tex((u * f), ((v + (float) vHeight) * f1)).endVertex();
            bufferbuilder.pos((double) (x + width), (double) (y + height), 0.0D).tex(((u + (float) uWidth) * f), ((v + (float) vHeight) * f1)).endVertex();
            bufferbuilder.pos((double) (x + width), (double) y, 0.0D).tex(((u + (float) uWidth) * f), (v * f1)).endVertex();
            bufferbuilder.pos((double) x, (double) y, 0.0D).tex((u * f), (v * f1)).endVertex();
            tessellator.draw();
        }
        
        @Override
        public ServerDataPublic getServerData()
        {
            return (ServerDataPublic) super.getServerData();
        }
    }
}
