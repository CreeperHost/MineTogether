package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.element.GuiButtonRefresh;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryModpack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiModPackList extends GuiScreen
{
    private final GuiScreen parent;
    private GuiList<GuiListEntryModpack> list;
    private GuiButton buttonCancel;
    private GuiButton buttonRefresh;
    private GuiButton buttonSelect;
    private boolean first = true;
    private GuiTextField displayEntry;

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
        int buttonWidth = 80;

        int buttonX = margin;

        displayEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 90, y, 160, 20);
        displayEntry.setVisible(true);

        buttonCancel = new GuiButton(0, buttonX, y, buttonWidth, 20, Util.localize("button.cancel"));
        buttonList.add(buttonCancel);

        buttonSelect = new GuiButton(0, this.width - 90, y, buttonWidth, 20, "Select");
        buttonList.add(buttonSelect);

        buttonRefresh = new GuiButtonRefresh(0, this.width / 2 + 72, y);
        buttonList.add(buttonRefresh);
    }

    private void refreshList()
    {
        String s = "";
        if(displayEntry != null) {
            s = displayEntry.getText();
        }

        List<Callbacks.Modpack> modpacks = Callbacks.getModpackFromCurse(s);
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

        this.list.drawScreen(mouseX, mouseY, partialTicks);

        if(displayEntry != null)
            this.displayEntry.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRendererObj, Util.localize("gui.modpack.selector"), this.width / 2, 10, -1);
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
            mc.displayGuiScreen(new GuiMainMenu());
        }
        if(button == buttonRefresh)
        {
            refreshList();
        }
        if(button == buttonSelect)
        {
            String ID = list.getCurrSelected().getModpack().getId();
            Config.getInstance().setVersion(ID);
            //Restart the order with the stored modpack version
            mc.displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
        this.displayEntry.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (displayEntry != null && displayEntry.isFocused()){
            displayEntry.textboxKeyTyped(typedChar, keyCode);
        }
    }
}
