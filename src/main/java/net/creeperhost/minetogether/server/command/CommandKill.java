package net.creeperhost.minetogether.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class CommandKill
{
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("kill")
                .requires(cs -> cs.hasPermissionLevel(4))
                .then(Commands.argument("entity", EntityArgument.entity()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                        .executes(cs -> execute(cs, EntityArgument.getEntity(cs, "entity"))));
    }
    
    public static int execute(CommandContext<CommandSource> ctx, Entity entity) throws CommandException
    {
        AtomicInteger i = new AtomicInteger();
        ServerWorld world = ctx.getSource().getWorld();
        Stream<Entity> entityList = world.getEntities();
        entityList.forEach(index ->
        {
            if (index.getEntity() == entity && !(index.getEntity() instanceof PlayerEntity))
            {
                i.getAndIncrement();
                world.removeEntity(index);
            }
        });
        
        PlayerEntity playerEntity = (PlayerEntity) ctx.getSource().getEntity();
        if (playerEntity != null)
        {
            playerEntity.sendMessage(new TranslationTextComponent("Removed " + i + " entities from the world"));
        }
        return i.get();
    }
}
