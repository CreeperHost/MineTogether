package net.creeperhost.minetogether.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MTCommands
{
    public static void registerCommand(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, Commands.CommandSelection commandSelection)
    {
        commandSourceStackCommandDispatcher.register(CommandInvite.register());
        commandSourceStackCommandDispatcher.register(CommandPregen.register());
    }
}
