package net.creeperhost.minetogether.gui.serverlist.gui;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;

public class GuiMockMultiplayer extends GuiMultiplayer
{

  public GuiMockMultiplayer()
  {
    super(null);
  }

  /*@Override
  public boolean canMoveUp(ServerListEntryNormal p_175392_1_, int p_175392_2_)
  {
    return false;
  }

  @Override
  public boolean canMoveDown(ServerListEntryNormal p_175394_1_, int p_175394_2_)
  {
    return false;
  }*/ // not sure on replacement right now or if needed, tests will show me

  @Override
  public void func_146796_h()
  {
  }
}
