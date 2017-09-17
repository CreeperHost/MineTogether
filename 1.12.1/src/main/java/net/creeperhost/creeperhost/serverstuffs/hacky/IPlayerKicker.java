package net.creeperhost.creeperhost.serverstuffs.hacky;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IPlayerKicker
{
    void kickPlayer(EntityPlayerMP player, String message);
}
