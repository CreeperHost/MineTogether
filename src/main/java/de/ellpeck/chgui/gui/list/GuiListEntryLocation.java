package de.ellpeck.chgui.gui.list;

public class GuiListEntryLocation extends GuiListEntry{

    public final int locationId;
    public final String locationName;

    public GuiListEntryLocation(GuiList list, int locationId, String locationName){
        super(list);
        this.locationId = locationId;
        this.locationName = locationName;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected){
        this.mc.fontRendererObj.drawString(this.locationName, x+5, y+5, 16777215);
    }
}
