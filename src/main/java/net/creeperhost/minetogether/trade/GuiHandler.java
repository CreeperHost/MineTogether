package net.creeperhost.minetogether.trade;

import net.creeperhost.minetogether.trade.client.GuiTrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public static final int trade = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if(ID == trade)
        {
            return new ContainerTrade(player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if(ID == trade)
        {
            return new GuiTrade(player);
        }
        return null;
    }
}
