package net.creeperhost.creeperhost.serverstuffs.hacky;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class NewPlayerKicker implements IPlayerKicker
{
    @Override
    public void kickPlayer(EntityPlayerMP player, String message)
    {
        player.connection.func_194028_b(new TextComponentString(message));
    }
}
