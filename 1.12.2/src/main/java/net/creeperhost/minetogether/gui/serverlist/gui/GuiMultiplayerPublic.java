package net.creeperhost.minetogether.gui.serverlist.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.serverlist.data.ServerListNoEdit;
import net.creeperhost.minetogether.gui.serverlist.gui.elements.ServerListPublic;
import net.creeperhost.minetogether.gui.serverlist.gui.elements.ServerSelectionListPublic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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
    private ServerListPublic ourSavedServerList = null;
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
        if (!CreeperHost.instance.gdpr.hasAcceptedGDPR())
        {
            mc.displayGuiScreen(new GuiGDPR(parent, () -> new GuiMultiplayerPublic(parent, isPublic, sortOrder)));
            return;
        }

        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        if (this.initialized)
        {
            this.ourServerListSelector.setDimensions(this.width, this.height, 32, this.height - 64);
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
            ourServerListSelector.updateOnlineServers(this.ourSavedServerList);
        }

        this.createButtons();
    }

    @Override
    public boolean canMoveUp(ServerListEntryNormal p_175392_1_, int p_175392_2_)
    {
        return false;
    }

    @Override
    public boolean canMoveDown(ServerListEntryNormal p_175394_1_, int p_175394_2_)
    {
        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void createButtons()
    {
        super.createButtons();
        for (GuiButton button : buttonList)
        {
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
        sortOrderButton = new DropdownButton<>(80085101, width - 5 - 80 - 80, 5, 80, 20, "creeperhost.multiplayer.sort", sortOrder, false);
        buttonList.add(modeToggle);
        buttonList.add(sortOrderButton);
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
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
            return;
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
    public void connectToSelected()
    {
        GuiListExtended.IGuiListEntry entry = this.ourServerListSelector.getSelected() < 0 ? null : this.ourServerListSelector.getListEntry(this.ourServerListSelector.getSelected());
        ServerList savedServerList = new ServerListNoEdit(this.mc);
        savedServerList.loadServerList();
        savedServerList.addServerData(((ServerListEntryNormal) entry).getServerData());
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
    public void setHoveringText(String text)
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

    private void setServerList(ServerListPublic serverList)
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        ourTooltip = null;
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRendererObj, I18n.format("creeperhost.multiplayer.public.random"), this.width / 2, this.height - 62, 0xFFFFFF);

        if (this.ourTooltip != null)
        {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(ourTooltip)), mouseX, mouseY);
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
}
