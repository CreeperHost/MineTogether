package net.creeperhost.minetogether.client.screen.order;

import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.element.GuiButtonRefresh;
import net.creeperhost.minetogether.client.screen.list.GuiList;
import net.creeperhost.minetogether.client.screen.list.GuiListEntryModpack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.ModPack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class GuiModPackList extends Screen
{
    private final Screen parent;
    private GuiList<GuiListEntryModpack> list;
    private boolean first = true;
    private TextFieldWidget displayEntry;
    
    public GuiModPackList(Screen currentScreen)
    {
        super(new StringTextComponent(""));
        this.parent = currentScreen;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void init()
    {
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        if (list == null)
        {
            list = new GuiList(this, minecraft, width, height, 32, this.height - 64, 36);
        }
        {
            list.updateSize(width, height, 32, this.height - 64);
        }
        
        this.children.add(list);
        
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
        
        this.addButton(new Button(buttonX, y, buttonWidth, 20, Util.localize("button.cancel"), (button) -> minecraft.displayGuiScreen(new MainMenuScreen())));
        
        this.addButton(new Button(this.width - 90, y, buttonWidth, 20, "Select", (button) ->
        {
            if (list.getSelected() != null)
            {
                String ID = ((GuiListEntryModpack) list.getSelected()).getModpack().getId();
                Config.getInstance().setVersion(ID);
                //Restart the order with the stored modpack version
                minecraft.displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }));
        
        this.addButton(new GuiButtonRefresh(this.width / 2 + 72, y, (button) -> refreshList()));
    }
    
    private void refreshList()
    {
        String s = "";
        if (displayEntry != null)
        {
            s = displayEntry.getText();
        }
        
        List<ModPack> modpacks = Callbacks.getModpackFromCurse(s, 10);
        list.clearList();
        if (modpacks != null)
        {
            for (ModPack mp : modpacks)
            {
                GuiListEntryModpack entry = new GuiListEntryModpack(this, list, mp);
                list.add(entry);
            }
        }
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        if (list != null) this.list.render(mouseX, mouseY, partialTicks);
        
        if (displayEntry != null) this.displayEntry.render(mouseX, mouseX, partialTicks);
        
        this.drawCenteredString(this.font, Util.localize("gui.modpack.selector"), this.width / 2, 10, -1);
        
        super.render(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        this.list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return true;
    }
    
    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        this.list.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        this.displayEntry.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        return true;
    }
    
    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        if (displayEntry != null && displayEntry.isFocused())
        {
            displayEntry.charTyped(p_charTyped_1_, p_charTyped_2_);
        }
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (displayEntry != null && displayEntry.isFocused())
        {
            displayEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
}
