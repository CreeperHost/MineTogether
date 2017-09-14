package net.creeperhost.creeperhost.serverstuffs.hacky;

import net.minecraft.entity.player.EntityPlayerMP;

public class OldPlayerKicker implements IPlayerKicker
{
    @Override
    public void kickPlayer(EntityPlayerMP player, String message)
    {
        player.connection.kickPlayerFromServer(message);
    }
}
