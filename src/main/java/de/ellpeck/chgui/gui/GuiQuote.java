package de.ellpeck.chgui.gui;

import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.gui.element.GuiWell;
import de.ellpeck.chgui.gui.list.GuiList;
import de.ellpeck.chgui.gui.list.GuiListEntry;
import de.ellpeck.chgui.gui.list.GuiListEntryLocation;
import de.ellpeck.chgui.paul.Callbacks;
import de.ellpeck.chgui.paul.Order;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.Map;

public class GuiQuote extends GuiGetServer{

    private GuiList list;
    private boolean countryEnabled = false;

    public GuiQuote(int stepId, Order order){
        super(stepId, order);
    }

    @Override
    public void initGui(){
        super.initGui();

        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height-36, 36);

        Map<Integer, String> locations = Callbacks.getAllServerLocations();
        for(Map.Entry<Integer, String> entry : locations.entrySet()){
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            this.list.addEntry(listEntry);

            if(this.order.serverLocation == listEntry.locationId){
                this.list.setCurrSelected(listEntry);
            }
        }

        Callbacks.getSummary(this.order);
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
            this.order.serverLocation = ((GuiListEntryLocation)entry).locationId;
        }
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();
        if (this.countryEnabled) {
            this.list.handleMouseInput();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();

        if (countryEnabled) {
            this.list.drawScreen(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.countryEnabled)
        {
            this.list.mouseClicked(mouseX, mouseY, mouseButton);
        }

    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state){
        super.mouseReleased(mouseX, mouseY, state);
        if (this.countryEnabled)
        {
            this.list.mouseReleased(mouseX, mouseY, state);
        }
    }
}
