package net.creeperhost.creeperhost.gui;

import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.gui.list.GuiList;
import net.creeperhost.creeperhost.gui.list.GuiListEntry;
import net.creeperhost.creeperhost.gui.list.GuiListEntryLocation;
import net.creeperhost.creeperhost.paul.Callbacks;
import net.creeperhost.creeperhost.api.Order;

import java.util.Map;

public class GuiServerLocation extends GuiGetServer{

    private GuiList list;

    public GuiServerLocation(int stepId, Order order){
        super(stepId, order);
    }

    @Override
    public void initGui(){
        super.initGui();

        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height-36, 36);

        Map<String, String> locations = Callbacks.getAllServerLocations();
        for(Map.Entry<String, String> entry : locations.entrySet()){
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            this.list.addEntry(listEntry);

            if(this.order.serverLocation.equals(listEntry.locationName)){
                this.list.setCurrSelected(listEntry);
            }
        }
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.server_location");
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        this.buttonNext.enabled = this.list.getCurrSelected() != null;
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();

        GuiListEntry entry = this.list.getCurrSelected();
        if(entry instanceof GuiListEntryLocation){
            this.order.serverLocation = ((GuiListEntryLocation)entry).locationName;
        }
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        this.list.drawScreen(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.func_148179_a(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        this.list.func_148181_b(mouseX, mouseY, state);
    }
}
