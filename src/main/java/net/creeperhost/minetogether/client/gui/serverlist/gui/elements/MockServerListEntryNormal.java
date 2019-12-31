package net.creeperhost.minetogether.client.gui.serverlist.gui.elements;

import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiMockMultiplayer;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;

public class MockServerListEntryNormal extends ServerSelectionList.Entry
{
    public static final GuiMockMultiplayer mockMP = new GuiMockMultiplayer();
    
    protected MockServerListEntryNormal(MultiplayerScreen p_i45048_1_, ServerData serverIn)
    {
//        super(p_i45048_1_, serverIn);
    }
    
    public MockServerListEntryNormal(ServerData serverIn)
    {
//        super(mockMP, serverIn);
    }
    
    @Override
    public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_)
    {
    
    }
}
