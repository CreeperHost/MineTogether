package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.element.GuiButtonRefresh;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryModpack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.io.IOException;
import java.util.List;

public class GuiModPackList extends Screen
{
    private final Screen parent;
    private GuiList<GuiListEntryModpack> list;
    private Button buttonCancel;
    private Button buttonRefresh;
    private Button buttonSelect;
    private boolean first = true;
    private TextFieldWidget displayEntry;

    public GuiModPackList(Screen currentScreen)
    {
        super(new StringTextComponent(""));
        this.parent = currentScreen;
    }

    @Override
    public void init()
    {
        super.init();
        if(list == null)
        {
            list = new GuiList(this, minecraft, width, height, 32, this.height - 64, 36);
        }
        {
//            list.setDimensions(width, height, 32, this.height - 64);
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

        displayEntry = new TextFieldWidget(this.font, this.width / 2 - 90, y, 160, 20, "");
        displayEntry.setVisible(true);

        this.addButton(buttonCancel = new Button(buttonX, y, buttonWidth, 20, Util.localize("button.cancel"), (button) -> minecraft.displayGuiScreen(new MainMenuScreen())));

        this.addButton(buttonSelect = new Button(this.width - 90, y, buttonWidth, 20, "Select", (button) ->
        {
            if(list.getCurrSelected() != null)
            {
                String ID = list.getCurrSelected().getModpack().getId();
                Config.getInstance().setVersion(ID);
                //Restart the order with the stored modpack version
                minecraft.displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }));

        this.addButton(buttonRefresh = new GuiButtonRefresh(this.width / 2 + 72, y, (button) -> refreshList()));
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
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);

        this.list.drawScreen(mouseX, mouseY, partialTicks);

        if(displayEntry != null) this.displayEntry.drawTextBox();

        super.render(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.font, Util.localize("gui.modpack.selector"), this.width / 2, 10, -1);
    }

//    @Override
//    public void handleMouseInput() throws IOException
//    {
//        super.handleMouseInput();
//        this.list.handleMouseInput();
//    }


    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        this.list.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        this.displayEntry.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (displayEntry != null && displayEntry.isFocused())
        {
            displayEntry.textboxKeyTyped(typedChar, keyCode);
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
}
