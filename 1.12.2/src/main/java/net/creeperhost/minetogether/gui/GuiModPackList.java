package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.element.GuiButtonRefresh;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryModpack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class GuiModPackList extends GuiScreen
{
    private final GuiScreen parent;
    private GuiList<GuiListEntryModpack> list;
    private GuiButton buttonCancel;
    private GuiButton buttonSelect;
    private boolean first = true;
    private GuiTextFieldCompat displayEntry;

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
            list = new GuiList(this, mc, width, height, 40, this.height - 32, 36);
        }
        {
            list.setDimensions(width, height, 32, this.height - 40);
        }

        if (first)
        {
            first = false;
            refreshList();
        }

        int y = this.height - 32;

        int margin = 10;
        int buttonWidth = 80;

        int buttonX = margin;

        displayEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, y, 160, 20);
        displayEntry.setVisible(true);

        buttonCancel = new GuiButton(0, buttonX, y, buttonWidth, 20, Util.localize("button.cancel"));
        buttonList.add(buttonCancel);

        buttonSelect = new GuiButton(0, this.width - 90, y, buttonWidth, 20, "Select");
        buttonList.add(buttonSelect);
    }

    public static List<Callbacks.Modpack> modpacks;

    private void refreshList()
    {
        try
        {
            String s = "";
            if (displayEntry != null)
            {
                s = displayEntry.getText();
            }
            list.clearList();

            modpacks = Callbacks.getModpackFromCurse(s, 10);
            if (!modpacks.isEmpty())
            {
                modpacks.forEach(mp ->
                {
                    GuiListEntryModpack entry = new GuiListEntryModpack(this, list, mp);
                    list.addEntry(entry);
                });
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground(0);

        this.list.drawScreen(mouseX, mouseY, partialTicks);

        if(displayEntry != null) this.displayEntry.drawTextBox();

        if(displayEntry != null && displayEntry.getText().trim().isEmpty() && !displayEntry.isFocused())
        {
            fontRendererObj.drawStringWithShadow(TextFormatting.ITALIC + "Search", displayEntry.xPosition + 3, displayEntry.yPosition + 5, 14737632);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRendererObj, "Minecraft Modpack Selector", this.width / 2, 10, -1);
    }

    int delay = 10;
    int i;
    boolean hasChanged;

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if(displayEntry.isFocused() && hasChanged)
        {
            i++;
            if(i >= delay)
            {
                refreshList();
                i = 0;
                hasChanged = false;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button == buttonCancel)
        {
            mc.displayGuiScreen(new GuiMainMenu());
        }
        if(button == buttonSelect)
        {
            if(list.getCurrSelected() != null)
            {
                String ID = list.getCurrSelected().getModpack().getId();
                Config.getInstance().setVersion(ID);
                //Restart the order with the stored modpack version
                mc.displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
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
        if (displayEntry != null && displayEntry.isFocused())
        {
            displayEntry.textboxKeyTyped(typedChar, keyCode);
            i = 0;
            hasChanged = true;
            if(Keyboard.KEY_RETURN == keyCode)
            {
                refreshList();
                displayEntry.setFocused(false);
            }
        }
    }
}
