package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntry;
import net.creeperhost.minetogether.gui.list.GuiListEntryLocation;
import net.creeperhost.minetogether.paul.Callbacks;

import java.io.IOException;
import java.util.Map;

public class GuiServerLocation extends GuiGetServer
{
    private GuiList list;
    
    public GuiServerLocation(int stepId, Order order)
    {
        super(stepId, order);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void initGui()
    {
        super.initGui();
        
        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height - 36, 36);
        
        Map<String, String> locations = Callbacks.getAllServerLocations();
        
        String topEntryName = "";
        
        for (Map.Entry<String, String> entry : locations.entrySet())
        {
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            
            if (this.order.serverLocation.equals(listEntry.locationName))
            {
                topEntryName = entry.getKey();
                this.list.addEntry(listEntry);
                this.list.setCurrSelected(listEntry);
                break;
            }
        }
        
        for (Map.Entry<String, String> entry : locations.entrySet())
        {
            if (topEntryName.equals(entry.getKey()))
            {
                continue;
            }
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            this.list.addEntry(listEntry);
        }
    }
    
    @Override
    public String getStepName()
    {
        return Util.localize("gui.server_location");
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        
        this.buttonNext.enabled = this.list.getCurrSelected() != null;
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        GuiListEntry entry = this.list.getCurrSelected();
        if (entry instanceof GuiListEntryLocation)
        {
            this.order.serverLocation = ((GuiListEntryLocation) entry).locationName;
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.list.mouseReleased(mouseX, mouseY, state);
    }
}
