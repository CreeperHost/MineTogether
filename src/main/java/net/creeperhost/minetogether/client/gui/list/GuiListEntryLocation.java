package net.creeperhost.minetogether.client.gui.list;

public class GuiListEntryLocation extends GuiListEntry
{
    public final String locationName;
    public final String locationDisplay;

    public GuiListEntryLocation(GuiList list, String locationName, String locationDisplay)
    {
        super(list);
        this.locationName = locationName;
        this.locationDisplay = locationDisplay;
    }

    @Override
    public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        this.mc.fontRenderer.drawSplitString(this.locationDisplay, x + 5, y + 5, listWidth, 16777215);
    }
}
