package net.creeperhost.minetogether.client.gui.serverlist.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.gui.GuiGDPR;
import net.creeperhost.minetogether.client.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.client.gui.element.DropdownButton;
import net.creeperhost.minetogether.client.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.gui.mpreplacement.CreeperHostServerSelectionList;
import net.creeperhost.minetogether.client.gui.order.GuiGetServer;
import net.creeperhost.minetogether.client.gui.serverlist.gui.elements.ServerListPublic;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.Arrays;
import java.util.List;

public class GuiMultiplayerPublic extends MultiplayerScreen
{
    public ListType listType = null;
    public SortOrder sortOrder = SortOrder.RANDOM;
    private Screen parent;
    private boolean changeSort;
    private String ourTooltip;
    public boolean selectedListType = false;
    private Minecraft mc = Minecraft.getInstance();
    
    public GuiMultiplayerPublic(Screen parentScreen)
    {
        super(parentScreen);
        parent = parentScreen;
    }
    
    public GuiMultiplayerPublic(Screen parentScreen, ListType listType, SortOrder order)
    {
        this(parentScreen);
        this.listType = listType;
        sortOrder = order;
    }
    
    public GuiMultiplayerPublic(Screen parentScreen, ListType listType, SortOrder order, boolean selectedListType)
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
            mc.displayGuiScreen(new GuiGDPR(parent, () -> new GuiMultiplayerPublic(parent, listType, sortOrder)));
            return;
        }
        
        super.init();

        addButton(new Button(width - 85, 5, 80, 20, I18n.format("minetogether.listing.title"), p ->
        {
            if (changeSort)
            {
                changeSort = false;
            }
            mc.displayGuiScreen(new GuiServerType(this));
        }));
        
        addButton(new GuiButtonCreeper(width - 105, 5, p ->
        {
            mc.displayGuiScreen(new GuiMTChat(this));
        }));

        mc.keyboardListener.enableRepeatEvents(true);

        if(listType == null)
        {
            serverListSelector.children().add(serverListSelector.children().lastIndexOf(serverListSelector.lanScanEntry), new CreeperHostEntry(serverListSelector));
        }
        
        if(listType != null)
        {
            ServerListPublic serverListPublic = new ServerListPublic(mc, this);
            serverListPublic.loadServerList();
    
            setServerList(serverListPublic);
        }
    }

    private void setServerList(ServerListPublic serverList)
    {
        serverListSelector.updateOnlineServers(serverList);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        ourTooltip = null;
        super.render(mouseX, mouseY, partialTicks);

        if(listType != null)
        {
            drawCenteredString(font, I18n.format("creeperhost.multiplayer.public.random"), this.width / 2, this.height - 62, 0xFFFFFF);
        }
        
        if (this.ourTooltip != null)
        {
            this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(ourTooltip)), mouseX, mouseY);
        }
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


        public CreeperHostEntry(ServerSelectionList list) {
            super();
            mc = Minecraft.getInstance();
            serverIcon = new ResourceLocation("creeperhost", "textures/creeperhost.png");
            cross = new String(Character.toChars(10006));
            stringWidth = this.mc.fontRenderer.getStringWidth(cross);
        }

        @Override
        public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_) {
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
            GlStateManager.enableBlend();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, transparency);
            blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            int transparentString = (int) (transparency * 254) << 24;
            this.mc.fontRenderer.drawString(Util.localize("mp.partner"), x+35, y, 16777215 + transparentString);
            GuiUtils.drawGradientRect(300, listWidth + x - stringWidth - 5, y - 1, listWidth + x - 3, y + 8 + 1, 0x90000000, 0x90000000);
            GlStateManager.enableBlend();
            this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 1, 16777215 + transparentString);
            String s = Util.localize("mp.clickherebranding");
            this.mc.fontRenderer.drawString(s, x + 32 + 3, y + (this.mc.fontRenderer.FONT_HEIGHT * 2) + 3, 8421504 + transparentString);
            this.mc.fontRenderer.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
            if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
            {

                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                final int tooltipX = mouseX - 72;
                final int tooltipY = mouseY + ((mc.mainWindow.getScaledWidth() / 2 >= mouseY) ? 11 : -11);
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

                GlStateManager.color4f(1.0F, 1.0F, 1.0F, transparency);
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                blit(mouseX - 74, tooltipY - 1, 0.0F, 0.0F, 60, 10, 60, 10);
            }
        }

        public int getRowTop(int p_getRowTop_1_)
        {
            return serverListSelector.getTop() + 4 - (int) serverListSelector.getScrollAmount() + p_getRowTop_1_ * serverListSelector.getHeight() - 64;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            int listWidth = serverListSelector.getRowWidth();
            int x = serverListSelector.getLeft();
            int y = getRowTop(serverListSelector.children().indexOf(this));

            if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
            {
                Config.getInstance().setMpMenuEnabled(false);
                MineTogether.instance.saveConfig();
                this.mc.displayGuiScreen(new GuiMultiplayerPublic(new MainMenuScreen()));
                return true;
            }
            Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            return true;
        }
    }
}
