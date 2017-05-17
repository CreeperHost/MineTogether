package net.creeperhost.creeperhost.gui.list;

public class GuiListEntryLocation extends GuiListEntry{

    public final String locationName;
    public final String locationDisplay;

    public GuiListEntryLocation(GuiList list, String locationName, String locationDisplay){
        super(list);
        this.locationName = locationName;
        this.locationDisplay = locationDisplay;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected){
        this.mc.fontRendererObj.drawSplitString(this.locationDisplay, x+5, y+5, listWidth,16777215);
    }
}
