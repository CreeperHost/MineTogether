package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryModpack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class GuiModPackList extends GuiScreen
{
    private final GuiScreen parent;
    private GuiList<GuiListEntryModpack> list;
    private GuiButton buttonCancel;
    private GuiButton buttonRefresh;
    private boolean first = true;

    public GuiModPackList(GuiScreen currentScreen)
    {
        this.parent = currentScreen;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        if(list == null)
        {
            list = new GuiList(this, mc, width, height, 32, this.height - 64, 36);
        }
        {
            list.setDimensions(width, height, 32, this.height - 64);
        }

        if (first)
        {
            first = false;
            refreshList();
        }

        int y = this.height - 60;

        int margin = 10;
        int buttons = 3;
        int buttonWidth = 80;

        int totalButtonSize = (buttonWidth * buttons);
        int nonButtonSpace = (width - (margin * 2)) - totalButtonSize;

        int spaceInbetween = (nonButtonSpace / (buttons - 1)) + buttonWidth;

        int buttonX = margin;

        buttonCancel = new GuiButton(0, buttonX, y, buttonWidth, 20, Util.localize("button.cancel"));
        buttonList.add(buttonCancel);
        buttonX += spaceInbetween;
        buttonRefresh = new GuiButton(0, buttonX, y, buttonWidth, 20, Util.localize("button.refresh"));
        buttonList.add(buttonRefresh);
    }

    protected void refreshList()
    {
        List<Callbacks.Modpack> modpacks = Callbacks.getModpackFromCurse("");
        list.clearList();
        if (modpacks != null)
        {
            for (Callbacks.Modpack mp : modpacks)
            {
                GuiListEntryModpack entry = new GuiListEntryModpack(this, list, mp);
                list.addEntry(entry);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground(0);
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, Util.localize("ModPack Selector"), this.width / 2, 10, -1);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button == buttonCancel)
        {
            mc.displayGuiScreen(parent);
        }
        if(button == buttonRefresh)
        {
            refreshList();
        }
    }
}
