package net.creeperhost.creeperhost.gui.serverlist;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;

public class GuiMockMultiplayer extends GuiMultiplayer
{

  public GuiMockMultiplayer()
  {
    super(null);
  }

  @Override
  public boolean canMoveUp(ServerListEntryNormal p_175392_1_, int p_175392_2_)
  {
    return false;
  }

  @Override
  public boolean canMoveDown(ServerListEntryNormal p_175394_1_, int p_175394_2_)
  {
    return false;
  }

  @Override
  public void connectToSelected()
  {
  }
}
