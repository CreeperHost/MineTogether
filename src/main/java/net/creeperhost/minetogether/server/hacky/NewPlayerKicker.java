package net.creeperhost.minetogether.server.hacky;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class NewPlayerKicker implements IPlayerKicker
{
    @Override
    public void kickPlayer(ServerPlayerEntity player, String message)
    {
        player.connection.disconnect(new StringTextComponent(message));
    }
}
