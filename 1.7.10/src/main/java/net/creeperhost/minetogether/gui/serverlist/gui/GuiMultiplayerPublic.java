package net.creeperhost.minetogether.gui.serverlist.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.serverlist.data.ServerListNoEdit;
import net.creeperhost.minetogether.gui.serverlist.gui.elements.ServerListPublic;
import net.creeperhost.minetogether.gui.serverlist.gui.elements.ServerSelectionListPublic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

public class GuiMultiplayerPublic extends GuiMultiplayer
{
    private static Field savedServerListField;
    private static Field lanServerDetectorField;
    private static Field lanServerListField;
    private static Field serverListSelectorField;
    public boolean isPublic = true;
    public SortOrder sortOrder = SortOrder.RANDOM;
    private boolean initialized;
    private GuiScreen parent;
    private GuiButton modeToggle;
    private DropdownButton<SortOrder> sortOrderButton;
    private ServerList ourSavedServerList = null;
    private LanServerDetector.ThreadLanServerFind ourLanServerDetector = null;
    private LanServerDetector.LanServerList ourLanServerList = null;
    private ServerSelectionListPublic ourServerListSelector = null;
    private String ourTooltip;

    public GuiMultiplayerPublic(GuiScreen parentScreen)
    {
        super(parentScreen);
        parent = parentScreen;
    }

    public GuiMultiplayerPublic(GuiScreen parentScreen, boolean isPublic, SortOrder order)
    {
        this(parentScreen);
        this.isPublic = isPublic;
        sortOrder = order;
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        if (this.initialized)
        {
            this.ourServerListSelector.func_148122_a(this.width, this.height, 32, this.height - 64);
        }
        else
        {
            this.initialized = true;
            setServerList(new ServerListPublic(this.mc, this));
            ourSavedServerList.loadServerList();
            setLanServerList(new LanServerDetector.LanServerList());

            try
            {
                setLanServerDetector(new LanServerDetector.ThreadLanServerFind(this.ourLanServerList));
                ourLanServerDetector.start();
            }
            catch (Exception exception)
            {
            }

            setServerListSelector(new ServerSelectionListPublic(this, this.mc, this.width, this.height, 32, this.height - 64, 46));
            ourServerListSelector.func_148195_a(this.ourSavedServerList);
        }

        this.func_146794_g();
    }

    @Override
    public void func_146794_g()
    {
        super.func_146794_g();
        for (Object obj : buttonList)
        {
            GuiButton button = (GuiButton) obj;
            if (button.id != 0 && button.id != 1 && button.id != 3 && button.id != 7)
            {
                button.visible = false;
            }
            else if (button.id == 1) // original connect button
            {
                button.displayString = I18n.format("selectServer.add");
            }
            else if (button.id == 3) // original add button
            {
                button.displayString = I18n.format("selectServer.refresh");
            }
            else if (button.id == 7) // original edit button
            {
                button.displayString = I18n.format("creeperhost.multiplayer.friends");
                button.enabled = true;
            }
        }
        modeToggle = new GuiButton(80085, width - 5 - 80, 5, 80, 20, I18n.format(isPublic ? "creeperhost.multiplayer.button.public" : "creeperhost.multiplayer.button.private"));
        sortOrderButton = new DropdownButton<SortOrder>(80085101, width - 5 - 80 - 80, 5, 80, 20, "creeperhost.multiplayer.sort", "creeperhost.multiplayer.sort.", sortOrder);
        buttonList.add(modeToggle);
        buttonList.add(sortOrderButton);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 3)
        {
            refresh();
            return;
        }
        else if (button.id == modeToggle.id)
        {
            isPublic = !isPublic;
            button.displayString = I18n.format(isPublic ? "creeperhost.multiplayer.button.public" : "creeperhost.multiplayer.button.private");
            refresh();
        }
        else if (button.id == 7)
        {
            CreeperHost.proxy.openFriendsGui();
            return;
        }
        else if (button.id == sortOrderButton.id)
        {
            sortOrder = sortOrderButton.getSelected();
            ourServerListSelector.sort();
            return;
        }
        super.actionPerformed(button);
    }

    private void refresh()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, isPublic, sortOrder));
    }

    @Override
    public void func_146796_h()
    {
        GuiListExtended.IGuiListEntry entry = this.ourServerListSelector.func_148193_k() < 0 ? null : this.ourServerListSelector.getListEntry(this.ourServerListSelector.func_148193_k());
        ServerList savedServerList = new ServerListNoEdit(this.mc);
        savedServerList.loadServerList();
        savedServerList.addServerData(((ServerListEntryNormal) entry).func_148296_a());
        savedServerList.saveServerList();

        Minecraft mc = Minecraft.getMinecraft();
        if (parent instanceof GuiMultiplayer)
        {
            mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
            return;
        }

        mc.displayGuiScreen(parent);
    }

    @Override
    public void func_146793_a(String text)
    {
        if (sortOrderButton.dropdownOpen)
        {
            this.ourTooltip = null;
        }
        else
        {
            this.ourTooltip = text;
        }
    }

    private void setServerList(ServerList serverList)
    {
        ourSavedServerList = serverList;
        if (savedServerListField == null)
        {
            savedServerListField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146804_i", "savedServerList");
            savedServerListField.setAccessible(true);
        }

        try
        {
            savedServerListField.set(this, serverList);
        }
        catch (IllegalAccessException e)
        {
            CreeperHost.logger.error("Unable to set server list", e);
        }
    }

    private void setLanServerDetector(LanServerDetector.ThreadLanServerFind detector)
    {
        ourLanServerDetector = detector;
        if (lanServerDetectorField == null)
        {
            lanServerDetectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146800_B", "lanServerDetector");
            lanServerDetectorField.setAccessible(true);
        }

        try
        {
            lanServerDetectorField.set(this, detector);
        }
        catch (IllegalAccessException e)
        {
            CreeperHost.logger.error("Unable to set server list", e);
        }
    }

    private void setLanServerList(LanServerDetector.LanServerList detector)
    {
        ourLanServerList = detector;
        if (lanServerListField == null)
        {
            lanServerListField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146799_A", "lanServerList");
            lanServerListField.setAccessible(true);
        }

        try
        {
            lanServerListField.set(this, detector);
        }
        catch (IllegalAccessException e)
        {
            CreeperHost.logger.error("Unable to set server list", e);
        }
    }

    private void setServerListSelector(ServerSelectionListPublic list)
    {
        ourServerListSelector = list;
        if (serverListSelectorField == null)
        {
            serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
            serverListSelectorField.setAccessible(true);
        }

        try
        {
            serverListSelectorField.set(this, list);
        }
        catch (IllegalAccessException e)
        {
            CreeperHost.logger.error("Unable to set server list", e);
        }
    }

    @Override
    public void drawScreen(int mouseX, int p_73863_2_, float p_73863_3_)
    {
        this.ourTooltip = null;
        super.drawScreen(mouseX, p_73863_2_, p_73863_3_);
        drawCenteredString(fontRendererObj, I18n.format("creeperhost.multiplayer.public.random"), this.width / 2, this.height - 62, 0xFFFFFF);

        if (this.ourTooltip != null)
        {
            this.func_146283_a(Lists.newArrayList(Splitter.on("\n").split(this.ourTooltip)), mouseX, p_73863_2_);
        }
    }

    @Override
    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        if (text.equals(I18n.format("multiplayer.title")))
        {
            text = I18n.format("creeperhost.multiplayer.public");
        }
        super.drawCenteredString(fontRendererIn, text, x, y, color);
    }

    public enum SortOrder
    {
        RANDOM("multiplayer.sort.random"),
        PLAYER("multiplayer.sort.player"),
        NAME("multiplayer.sort.name"),
        UPTIME("multiplayer.sort.uptime"),
        LOCATION("multiplayer.sort.location"),
        PING("multiplayer.sort.ping", true);

        public final boolean constant;

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
    }
}
