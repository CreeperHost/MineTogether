package net.creeperhost.minetogether.gui.serverlist.gui;

import net.creeperhost.minetogether.gui.GuiGDPR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class GuiServerType extends GuiScreen
{
    private GuiButton PUBLIC;
    private GuiButton INVITE;
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
        
        buttonList.add(PUBLIC = new GuiButton(1, width / 2 - 140, height / 2, 80, 20, "PUBLIC"));
        buttonList.add(INVITE = new GuiButton(2, width / 2 - 40, height / 2, 80, 20, "APPLICATION"));
        buttonList.add(APPLICATION = new GuiButton(3, width / 2 + 60, height / 2, 80, 20, "INVITE"));
        buttonList.add(EXIT = new GuiButton(4, width - 100, height - 20, 80, 20, "EXIT"));
        
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == PUBLIC.id)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.PUBLIC, GuiMultiplayerPublic.SortOrder.NAME, true));
        }
        if (button.id == INVITE.id)
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
