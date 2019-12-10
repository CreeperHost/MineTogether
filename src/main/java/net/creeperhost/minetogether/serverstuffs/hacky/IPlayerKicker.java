package net.creeperhost.minetogether.serverstuffs.hacky;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface IPlayerKicker
{
    void kickPlayer(ServerPlayerEntity player, String message);
}
