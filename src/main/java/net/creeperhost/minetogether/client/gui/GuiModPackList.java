package net.creeperhost.minetogether.client.gui;

import net.creeperhost.minetogether.util.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.client.gui.element.GuiButtonRefresh;
import net.creeperhost.minetogether.client.gui.list.GuiList;
import net.creeperhost.minetogether.client.gui.list.GuiListEntryModpack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @Override
    public void init()
    {
        super.init();
        if(list == null)
        {
            list = new GuiList(this, minecraft, width, height, 32, this.height - 64, 36);
        }
        {
            list.updateSize(width, height, 32, this.height - 64);
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

        this.addButton(new Button(buttonX, y, buttonWidth, 20, Util.localize("button.cancel"), (button) -> minecraft.displayGuiScreen(parent)));

        this.addButton(new Button(this.width - 90, y, buttonWidth, 20, "Select", (button) ->
        {
            if(list.getCurrSelected() != null)
            {
                String ID = list.getCurrSelected().getModpack().getId();
                Config.getInstance().setVersion(ID);
                //Restart the order with the stored modpack version
                minecraft.displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }));

        this.addButton(new GuiButtonRefresh(this.width / 2 + 72, y, (button) -> refreshList()));
    }

    @Override
    public void tick()
    {
        this.displayEntry.tick();
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
        this.list.render(mouseX, mouseY, partialTicks);

        super.render(mouseX, mouseY, partialTicks);

        if(displayEntry != null) this.displayEntry.render(mouseX, mouseX, partialTicks);

        this.drawCenteredString(this.font, Util.localize("gui.modpack.selector"), this.width / 2, 10, -1);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        this.list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        this.list.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        this.displayEntry.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        if(displayEntry != null && displayEntry.isFocused())
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

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ExtendedList.AbstractListEntry<GuiModPackList.Entry> {}

    @OnlyIn(Dist.CLIENT)
    public static class LanDetectedEntry extends GuiModPackList.Entry
    {
        private final GuiModPackList screen;
        protected final Minecraft mc;
        Callbacks.Modpack modpack;

        protected LanDetectedEntry(GuiModPackList screen, Callbacks.Modpack modpack)
        {
            this.screen = screen;
            this.modpack = modpack;
            this.mc = Minecraft.getInstance();
        }

        public void render(int p_render_1_, int x, int y, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_)
        {
            this.mc.fontRenderer.drawString(modpack.getName() + " (" + modpack.getDisplayVersion() + ")", x + 5, y + 5, 16777215);
        }

        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
        {
            return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        public Callbacks.Modpack getModpack()
        {
            return modpack;
        }
    }
}
