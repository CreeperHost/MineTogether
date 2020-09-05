package net.creeperhost.minetogether.serverstuffs.hacky;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IPlayerKicker
{
    void kickPlayer(EntityPlayerMP player, String message);
}
