package net.creeperhost.minetogether.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MTCommands
{
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection)
    {
        dispatcher.register(CommandInvite.register());
        dispatcher.register(CommandPregen.register());
    }
}
