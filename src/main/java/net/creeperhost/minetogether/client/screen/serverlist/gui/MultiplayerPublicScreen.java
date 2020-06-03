package net.creeperhost.minetogether.client.screen.serverlist.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.GDPRScreen;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.element.DropdownButton;
import net.creeperhost.minetogether.client.screen.element.GuiButtonMultiple;
import net.creeperhost.minetogether.client.screen.order.GuiGetServer;
import net.creeperhost.minetogether.client.screen.serverlist.data.Server;
import net.creeperhost.minetogether.client.screen.serverlist.data.ServerSelectionListOurs;
import net.creeperhost.minetogether.client.screen.serverlist.gui.elements.ServerListPublic;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiplayerPublicScreen extends MultiplayerScreen
{
    public ListType listType = null;
    public SortOrder sortOrder = SortOrder.RANDOM;
    public Screen parent;
    private boolean changeSort;
    private String ourTooltip;
    public boolean selectedListType = false;
    private DropdownButton<SortOrder> sortOrderButton;
    private Minecraft mc = Minecraft.getInstance();
    public ServerSelectionListOurs serverListSelectorOurs;
    private boolean initialized;
    private ServerListPublic ourSavedServerList = null;
    
    public MultiplayerPublicScreen(Screen parentScreen)
    {
        super(parentScreen);
        parent = parentScreen;
    }
    
    public MultiplayerPublicScreen(Screen parentScreen, ListType listType, SortOrder order)
    {
        this(parentScreen);
        this.listType = listType;
        sortOrder = order;
    }
    
    public MultiplayerPublicScreen(Screen parentScreen, ListType listType, SortOrder order, boolean selectedListType)
    {
        this(parentScreen);
        this.listType = listType;
        this.selectedListType = selectedListType;
        sortOrder = order;
    }
    
    @Override
    public void init()
    {
        if (!MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            mc.displayGuiScreen(new GDPRScreen(parent, () -> new MultiplayerPublicScreen(parent, listType, sortOrder)));
            return;
        }
        
        String name = "minetogether.listing.title";
        
        if(listType != null)
        {
            name = listType.name();
        }
        
        addButton(new Button(width - 85, 5, 80, 20, I18n.format(name), p ->
        {
            if (changeSort)
            {
                changeSort = false;
            }
            mc.displayGuiScreen(new ServerTypeScreen(this));
        }));
        
        addButton(new GuiButtonMultiple(width - 105, 5, 1, p ->
        {
            mc.displayGuiScreen(new MTChatScreen(this));
        }));
        
        mc.keyboardListener.enableRepeatEvents(true);
        
        super.init();
        
        CreeperHostEntry creeperHostEntry = new CreeperHostEntry(serverListSelectorOurs);
        
        AtomicBoolean hasEntry = new AtomicBoolean(false);
        
        serverListSelector.serverListInternet.clear();
        
        if (listType == null && !hasEntry.get())
        {
            serverListSelector.children().forEach(p ->
            {
                if (p instanceof CreeperHostEntry)
                {
                    hasEntry.set(true);
                }
            });
            
            if (!hasEntry.get() && Config.getInstance().isMpMenuEnabled())
            {
                serverListSelector.children().add(serverListSelector.children().lastIndexOf(serverListSelector.lanScanEntry), creeperHostEntry);
            }
        }
        
        if (listType != null)
        {
            ServerListPublic serverListPublic = new ServerListPublic(mc, this);
            serverListPublic.loadServerList();
            
            ourSavedServerList = serverListPublic;
            
            this.serverListSelectorOurs = new ServerSelectionListOurs(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
            this.serverListSelectorOurs.updateOnlineServers(serverListPublic);
            
            setServerList(serverListPublic);
            
            sort();
        }
        
        if(listType != null)
        {
            addButton(sortOrderButton = new DropdownButton<>(width - 5 - 80 - 80, 5, 80, 20, "creeperhost.multiplayer.sort", sortOrder, false, p ->
            {
                if (sortOrder != sortOrderButton.getSelected())
                {
                    changeSort = true;
                    sortOrder = sortOrderButton.getSelected();
                    sort();
                    minecraft.displayGuiScreen(new MultiplayerPublicScreen(parent, listType, sortOrder));
                }
            }));
        }
    }
    
    private void setServerList(ServerListPublic serverList)
    {
        serverListSelectorOurs.updateOnlineServers(serverList);
    }
    
    @Override
    public void func_214287_a(ServerSelectionList.Entry p_214287_1_)
    {
        if (listType != null)
        {
            this.serverListSelectorOurs.setSelected(p_214287_1_);
            this.func_214295_b();
            return;
        }
        super.func_214287_a(p_214287_1_);
    }
    
    public void sort()
    {
        switch (this.sortOrder)
        {
            default:
            case RANDOM:
                Collections.shuffle(serverListSelectorOurs.serverListInternetOurs);
                break;
            case PLAYER:
                Collections.sort(serverListSelectorOurs.serverListInternetOurs, Server.PlayerComparator.INSTANCE);
                break;
            case UPTIME:
                Collections.sort(serverListSelectorOurs.serverListInternetOurs, Server.UptimeComparator.INSTANCE);
                break;
            case NAME:
                Collections.sort(serverListSelectorOurs.serverListInternetOurs, Server.NameComparator.INSTANCE);
                break;
            case LOCATION:
                Collections.sort(serverListSelectorOurs.serverListInternetOurs, Server.LocationComparator.INSTANCE);
                break;
            case PING:
                Collections.sort(serverListSelectorOurs.serverListInternetOurs, Server.PingComparator.INSTANCE);
                break;
        }
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        ourTooltip = null;
        this.renderBackground();
        if (listType == null)
        {
            this.serverListSelector.render(mouseX, mouseY, partialTicks);
        }
        
        if (serverListSelectorOurs != null)
        {
            this.serverListSelectorOurs.render(mouseX, mouseY, partialTicks);
        }
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 20, 16777215);
        
        if (listType != null)
        {
            drawCenteredString(font, I18n.format("creeperhost.multiplayer.public.random"), this.width / 2, this.height - 62, 0xFFFFFF);
        }
        if (this.ourTooltip != null)
        {
            this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(ourTooltip)), mouseX, mouseY);
        }
        
        if (listType != null)
        {
            buttons.forEach(c ->
            {
                if (c.getMessage().equalsIgnoreCase(I18n.format("selectServer.delete")) || c.getMessage().equalsIgnoreCase(I18n.format("selectServer.edit")))
                {
                    c.active = false;
                }
            });
        }
        
        this.buttons.forEach(button -> button.render(mouseX, mouseY, partialTicks));
    }
    
    @Override
    public void setHoveringText(String p_146793_1_)
    {
        super.setHoveringText(p_146793_1_);
        this.ourTooltip = p_146793_1_;
    }
    
    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        boolean flag = super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        if (serverListSelectorOurs != null && serverListSelectorOurs.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_))
        {
            return serverListSelectorOurs.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        }
        return flag;
    }
    
    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        boolean flag = super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (serverListSelectorOurs != null && serverListSelectorOurs.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_))
        {
            return serverListSelectorOurs.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }
        return flag;
    }
    
    public enum SortOrder implements DropdownButton.IDropdownOption
    {
        RANDOM("random"),
        PLAYER("player"),
        NAME("name"),
        UPTIME("uptime"),
        LOCATION("location"),
        PING("ping", true);
        
        public final boolean constant;
        
        private static List<DropdownButton.IDropdownOption> enumCache;
        
        public String translate;
        
        SortOrder(String translate, boolean constant)
        {
            this.translate = translate;
            this.constant = constant;
        }
        
        SortOrder(String translate)
        {
            this(translate, false);
        }
        
        @Override
        public String getTranslate(DropdownButton.IDropdownOption current, boolean dropdownOpen)
        {
            return "creeperhost.multiplayer.sort." + translate;
        }
        
        @Override
        public List<DropdownButton.IDropdownOption> getPossibleVals()
        {
            if (enumCache == null)
                enumCache = Arrays.asList(SortOrder.values());
            
            return enumCache;
        }
    }
    
    public enum ListType implements DropdownButton.IDropdownOption
    {
        PUBLIC, INVITE, APPLICATION;
        
        private static List<DropdownButton.IDropdownOption> enumCache;
        
        @Override
        public List<DropdownButton.IDropdownOption> getPossibleVals()
        {
            if (enumCache == null)
                enumCache = Arrays.asList(ListType.values());
            
            return enumCache;
        }
        
        @Override
        public String getTranslate(DropdownButton.IDropdownOption currentDO, boolean dropdownOpen)
        {
            return "creeperhost.multiplayer.list." + this.name().toLowerCase();
        }
    }
    
    public class CreeperHostEntry extends ServerSelectionList.LanScanEntry
    {
        private final Minecraft mc;
        private final ResourceLocation serverIcon;
        private float transparency = 0.5F;
        private final String cross;
        private final int stringWidth;
        protected final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("creeperhost", "textures/hidebtn.png");
        
        public CreeperHostEntry(ServerSelectionList list)
        {
            super();
            mc = Minecraft.getInstance();
            serverIcon = new ResourceLocation("creeperhost", "textures/creeperhost.png");
            cross = new String(Character.toChars(10006));
            stringWidth = this.mc.fontRenderer.getStringWidth(cross);
        }
        
        @Override
        public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_)
        {
            ourDrawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering);
        }
        
        public void ourDrawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
        {
            if (isHovering)
            {
                if (transparency <= 1.0F)
                    transparency += 0.04;
            } else
            {
                if (transparency >= 0.5F)
                    transparency -= 0.04;
            }
            
            this.mc.getTextureManager().bindTexture(serverIcon);
            RenderSystem.enableBlend();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, transparency);
            blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            int transparentString = (int) (transparency * 254) << 24;
            this.mc.fontRenderer.drawString(Util.localize("mp.partner"), x + 35, y, 16777215 + transparentString);
            GuiUtils.drawGradientRect(300, listWidth + x - stringWidth - 5, y - 1, listWidth + x - 3, y + 8 + 1, 0x90000000, 0x90000000);
            RenderSystem.enableBlend();
            this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 1, 16777215 + transparentString);
            String s = Util.localize("mp.clickherebrand");
            this.mc.fontRenderer.drawString(s, x + 32 + 3, y + (this.mc.fontRenderer.FONT_HEIGHT * 2) + 3, 8421504 + transparentString);
            this.mc.fontRenderer.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
            if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
            {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                
                final int tooltipX = mouseX - 72;
                final int tooltipY = mouseY + ((mc.getMainWindow().getScaledWidth() / 2 >= mouseY) ? 11 : -11);
                final int tooltipTextWidth = 56;
                final int tooltipHeight = 7;
                
                final int zLevel = 300;
                
                // re-purposed code from tooltip rendering
                final int backgroundColor = 0xF0100010;
                GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
                GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
                GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
                GuiUtils.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
                GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
                final int borderColorStart = 0x505000FF;
                final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
                GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
                GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
                GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
                GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
                
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, transparency);
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                blit(mouseX - 74, tooltipY - 1, 0.0F, 0.0F, 60, 10, 60, 10);
            }
        }
        
        private int getHeaderHeight()
        {
            return ((int) serverListSelector.getScrollAmount() - serverListSelector.getHeight()) - serverListSelector.getScrollBottom();
        }
        
        private int getRowTop(int p_getRowTop_1_)
        {
            return serverListSelector.getTop() + 4 - (int) serverListSelector.getScrollAmount() + p_getRowTop_1_ * 36 + getHeaderHeight();
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            int listWidth = ((serverListSelector.getWidth() - serverListSelector.getRowWidth()) / 2) + serverListSelector.getRowWidth();
            
            int x = serverListSelector.getLeft();
            int y = getRowTop(serverListSelector.children().indexOf(this));
            
            if (mouseX >= listWidth - stringWidth - 4 && mouseX <= listWidth - 5 && mouseY - y >= 0 && mouseY - y <= 7)
            {
                Config.getInstance().setMpMenuEnabled(false);
                ConfigHandler.saveConfig();
                this.mc.displayGuiScreen(new MultiplayerPublicScreen(new MainMenuScreen()));
                return true;
            }
            Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            return true;
        }
    }
}
