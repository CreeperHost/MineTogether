package net.creeperhost.minetogether.client.screen.list;

public class GuiListEntryCountry extends GuiListEntry
{
    public final String countryID;
    public final String countryName;
    
    public GuiListEntryCountry(GuiList list, String countryID, String countryName)
    {
        super(list);
        this.countryID = countryID;
        this.countryName = countryName;
    }
    
    @Override
    public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        this.mc.fontRenderer.drawString(this.countryName, x + 5, y + 5, 16777215);
    }
}
