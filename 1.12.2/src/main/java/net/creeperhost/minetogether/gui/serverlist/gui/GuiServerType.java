package net.creeperhost.minetogether.gui.serverlist.gui;

import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.serverlist.gui.elements.GuiButtonLarge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiServerType extends GuiScreen
{
    private GuiButton PUBLIC;
    private GuiButton COMMUNITY;
    private GuiButton APPLICATION;
    private GuiButton EXIT;
    
    private GuiGDPR.IScreenGetter getter = null;
    private GuiScreen parent = null;
    
    
    public GuiServerType()
    {
    }
    
    public GuiServerType(GuiScreen parent)
    {
        this.parent = parent;
    }
    
    public GuiServerType(GuiScreen parent, GuiGDPR.IScreenGetter getterIn)
    {
        this(parent);
        getter = getterIn;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        
        buttonList.add(PUBLIC = new GuiButtonLarge(1, width / 2 - 160, height / 8, 120, 180, "PUBLIC", I18n.format("minetogether.listing.public"), new ItemStack(Items.BANNER)));
        buttonList.add(COMMUNITY = new GuiButtonLarge(2, width / 2 - 40, height / 8, 120, 180, "COMMUNITY", I18n.format("minetogether.listing.community"), new ItemStack(Items.OAK_DOOR)));
        buttonList.add(APPLICATION = new GuiButtonLarge(3, width / 2 + 80, height / 8, 120, 180, "CLOSED", I18n.format("minetogether.listing.closed"), new ItemStack(Items.IRON_DOOR)));
        buttonList.add(EXIT = new GuiButton(4, width / 4, height - 20, 220, 20, "MAIN MENU"));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == PUBLIC.id)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.PUBLIC, GuiMultiplayerPublic.SortOrder.NAME, true));
        }
        if (button.id == COMMUNITY.id)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.INVITE, GuiMultiplayerPublic.SortOrder.NAME, true));
        }
        if (button.id == APPLICATION.id)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.APPLICATION, GuiMultiplayerPublic.SortOrder.NAME, true));
        }
        if (button.id == EXIT.id)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
        }
        super.actionPerformed(button);
    }
    
    @FunctionalInterface
    public interface IScreenGetter
    {
        GuiScreen method();
    }
    
}
