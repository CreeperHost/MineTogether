package net.creeperhost.minetogether.server.hacky;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface IPlayerKicker
{
    void kickPlayer(ServerPlayerEntity player, String message);
}
