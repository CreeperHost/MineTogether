package net.creeperhost.creeperhost.gui.list;

public class GuiListEntryFriend extends GuiListEntry {

  public final String displayName;
  public final boolean status;

  public GuiListEntryFriend(GuiList list, String displayName, boolean status){
    super(list);
    this.displayName = displayName;
    this.status = status;
  }

  @Override
  public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected){
    this.mc.fontRendererObj.drawString(this.displayName, x+5, y+5, 16777215);
  }
}
