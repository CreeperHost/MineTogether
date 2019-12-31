package net.creeperhost.minetogether.client.gui.serverlist.gui;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.client.gui.GuiGDPR;
import net.creeperhost.minetogether.client.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.client.gui.element.DropdownButton;
import net.creeperhost.minetogether.client.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.gui.serverlist.gui.elements.ServerListPublic;
import net.creeperhost.minetogether.client.gui.serverlist.gui.elements.ServerSelectionListPublic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class GuiMultiplayerPublic extends MultiplayerScreen
{
    private static Field savedServerListField;
    private static Field lanServerDetectorField;
    private static Field lanServerListField;
    private static Field serverListSelectorField;
    public ListType listType = ListType.PUBLIC;
    public SortOrder sortOrder = SortOrder.RANDOM;
    private boolean initialized;
    private Screen parent;
    private Screen modeToggle;
    private boolean changeSort;
    //private DropdownButton<ListType> modeToggle;
    private DropdownButton<SortOrder> sortOrderButton;
    private ServerListPublic ourSavedServerList = null;
    private LanServerDetector.LanServerFindThread ourLanServerDetector = null;
    private LanServerDetector.LanServerList ourLanServerList = null;
    private ServerSelectionListPublic ourServerListSelector = null;
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

//        if (!selectedListType)
//        {
//            mc.displayGuiScreen(new GuiServerType(this));
//        }
        
        super.init();
        
        this.buttons.clear();
        
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


//        this.minecraft.keyboardListener.enableRepeatEvents(true);
//
//        this.buttons.clear();
//
//        if (this.initialized)
//        {
////            this.ourServerListSelector.setDimensions(this.width, this.height, 32, this.height - 64);
//        } else
//        {
//            this.initialized = true;
////            setServerList(new ServerListPublic(this.mc, this));
////            ourSavedServerList.loadServerList();
////            setLanServerList(new LanServerDetector.LanServerList());
//
//            try
//            {
////                setLanServerDetector(new LanServerDetector.ThreadLanServerFind(this.ourLanServerList));
////                ourLanServerDetector.start();
//            } catch (Exception exception)
//            {
//            }
//
////            setServerListSelector(new ServerSelectionListPublic(this, this.mc, this.width, this.height, 32, this.height - 64, 46));
////            ourServerListSelector.updateOnlineServers(this.ourSavedServerList);
//        }
////        this.createButtons();
    }

//    @Override
//    public boolean canMoveUp(ServerListEntryNormal p_175392_1_, int p_175392_2_)
//    {
//        return false;
//    }
//
//    @Override
//    public boolean canMoveDown(ServerListEntryNormal p_175394_1_, int p_175394_2_)
//    {
//        return false;
//    }
    
    @SuppressWarnings("Duplicates")
//    @Override
//    public void createButtons()
//    {
//        super.createButtons();
//        for (Button button : buttonList)
//        {
//            if (button.id != 0 && button.id != 1 && button.id != 3 && button.id != 7)
//            {
//                button.visible = false;
//            } else if (button.id == 1) // original connect button
//            {
//                button.displayString = I18n.format("selectServer.add");
//            } else if (button.id == 3) // original add button
//            {
//                button.displayString = I18n.format("selectServer.refresh");
//            } else if (button.id == 7) // original edit button
//            {
//                button.displayString = I18n.format("creeperhost.multiplayer.friends");
//                button.enabled = true;
//            }
//        }
//        modeToggle = new Button(80085101, width - 85, 5, 80, 20, I18n.format("minetogether.listing.title"));
//        //modeToggle = new DropdownButton<>(80085101, width - 5 - 80, 5, 80, 20, "creeperhost.multiplayer.list", listType, false);
//        sortOrderButton = new DropdownButton<>(80085102, width - 5 - 80 - 80, 5, 80, 20, "creeperhost.multiplayer.sort", sortOrder, false);
//        buttonList.add(modeToggle);
//        buttonList.add(sortOrderButton);
//    }


//    @SuppressWarnings("Duplicates")
//    @Override
//    protected void actionPerformed(GuiButton button) throws IOException
//    {
//        if (button.id == 3)
//        {
//            refresh();
//            return;
//        } else if (button.id == modeToggle.id)
//        {
//            if(changeSort) {
//                changeSort = false;
//            }
//            mc.displayGuiScreen(new GuiServerType(this));
//            return;
//        } else if (button.id == 7)
//        {
//            MineTogether.proxy.openFriendsGui();
//            return;
//        } else if (button.id == sortOrderButton.id)
//        {
//            changeSort=true;
//            sortOrder = sortOrderButton.getSelected();
//            ourServerListSelector.sort();
//            return;
//        } else if(button.id == 0) {
//            mc.displayGuiScreen(new GuiMockMultiplayer());
//            return;
//        }
//        super.actionPerformed(button);
//    }

//    private void refresh()
//    {
//        Minecraft.getInstance().displayGuiScreen(new GuiMultiplayerPublic(parent, listType, sortOrder, true));
//    }
//
//    @Override
//    public void connectToSelected()
//    {
////        GuiListExtended.IGuiListEntry entry = this.ourServerListSelector.getSelected() < 0 ? null : this.ourServerListSelector.getListEntry(this.ourServerListSelector.getSelected());
//        ServerList savedServerList = new ServerListNoEdit(this.mc);
//        savedServerList.loadServerList();
////        savedServerList.addServerData(((ServerListEntryNormal) entry).getServerData());
//        savedServerList.saveServerList();
//
//        Minecraft mc = Minecraft.getInstance();
//        if (parent instanceof MultiplayerScreen)
//        {
//            mc.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
//            return;
//        }
//
//        mc.displayGuiScreen(parent);
//    }
//
//    @Override
//    public void setHoveringText(String text)
//    {
//        if (sortOrderButton.dropdownOpen)
//        {
//            this.ourTooltip = null;
//        } else
//        {
//            this.ourTooltip = text;
//        }
//    }

//    private void setServerList(ServerListPublic serverList)
//    {
//        ourSavedServerList = serverList;
//        if (savedServerListField == null)
//        {
//            savedServerListField = ReflectionHelper.findField(GuiMultiplayer.class, "savedServerList", "field_146804_i", "");
//            savedServerListField.setAccessible(true);
//        }
//
//        try
//        {
//            savedServerListField.set(this, serverList);
//        } catch (IllegalAccessException e)
//        {
//            MineTogether.logger.error("Unable to set server list", e);
//        }
//    }

//    private void setLanServerDetector(LanServerDetector.ThreadLanServerFind detector)
//    {
//        ourLanServerDetector = detector;
//        if (lanServerDetectorField == null)
//        {
//            lanServerDetectorField = ReflectionHelper.findField(GuiMultiplayer.class, "lanServerDetector", "field_146800_B", "");
//            lanServerDetectorField.setAccessible(true);
//        }
//
//        try
//        {
//            lanServerDetectorField.set(this, detector);
//        } catch (IllegalAccessException e)
//        {
//            MineTogether.logger.error("Unable to set server list", e);
//        }
//    }

//    private void setLanServerList(LanServerDetector.LanServerList detector)
//    {
//        ourLanServerList = detector;
//        if (lanServerListField == null)
//        {
//            lanServerListField = ReflectionHelper.findField(GuiMultiplayer.class, "lanServerList", "field_146799_A", "");
//            lanServerListField.setAccessible(true);
//        }
//
//        try
//        {
//            lanServerListField.set(this, detector);
//        } catch (IllegalAccessException e)
//        {
//            MineTogether.logger.error("Unable to set server list", e);
//        }
//    }
//
//    private void setServerListSelector(ServerSelectionListPublic list)
//    {
//        ourServerListSelector = list;
//        if (serverListSelectorField == null)
//        {
//            serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "serverListSelector", "field_146803_h", "");
//            serverListSelectorField.setAccessible(true);
//        }
//
//        try
//        {
//            serverListSelectorField.set(this, list);
//        } catch (IllegalAccessException e)
//        {
//            MineTogether.logger.error("Unable to set server list", e);
//        }
//    }

//    @Override
//    public void render(int mouseX, int mouseY, float partialTicks)
//    {
//        renderDirtBackground(0);
//        ourTooltip = null;
////        super.render(mouseX, mouseY, partialTicks);
//
//        drawCenteredString(font, I18n.format("creeperhost.multiplayer.public.random"), this.width / 2, this.height - 62, 0xFFFFFF);
//
//        if (this.ourTooltip != null)
//        {
//            this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(ourTooltip)), mouseX, mouseY);
//        }
//    }
//
//    @Override
//    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
//    {
//        if (text.equals(I18n.format("multiplayer.title")))
//        {
//            String prefix = I18n.format("creeperhost.multiplayer.title.prefix.public");
//            if(listType == ListType.APPLICATION)
//            {
//                prefix = I18n.format("creeperhost.multiplayer.title.prefix.application");
//            } else if(listType == ListType.INVITE)
//            {
//                prefix = I18n.format("creeperhost.multiplayer.title.prefix.invite");
//            }
//            text = prefix + " " + I18n.format("creeperhost.multiplayer.title.suffix.generic");
//        }
//        super.drawCenteredString(fontRendererIn, text, x, y, color);
//    }
    
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
