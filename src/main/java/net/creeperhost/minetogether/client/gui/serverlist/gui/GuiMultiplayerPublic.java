package net.creeperhost.minetogether.client.gui.serverlist.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.client.gui.GuiGDPR;
import net.creeperhost.minetogether.client.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.client.gui.element.DropdownButton;
import net.creeperhost.minetogether.client.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.gui.serverlist.gui.elements.ServerListPublic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

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
}
