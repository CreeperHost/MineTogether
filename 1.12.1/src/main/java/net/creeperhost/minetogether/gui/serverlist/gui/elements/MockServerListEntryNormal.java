package net.creeperhost.minetogether.gui.serverlist.gui.elements;

import net.creeperhost.minetogether.gui.serverlist.gui.GuiMockMultiplayer;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;

public class MockServerListEntryNormal extends ServerListEntryNormal
{
    public static final GuiMockMultiplayer mockMP = new GuiMockMultiplayer();

    protected MockServerListEntryNormal(GuiMultiplayer p_i45048_1_, ServerData serverIn)
    {
        super(p_i45048_1_, serverIn);
    }

    public MockServerListEntryNormal(ServerData serverIn)
    {
        super(mockMP, serverIn);
    }
}
