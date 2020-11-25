package net.creeperhost.minetogether;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.concurrent.ThreadPoolExecutor;

public class CommandThreadDump extends CommandBase
{
    @Override
    public String getName()
    {
        return "dump";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
    
    @Override
    public String getUsage(ICommandSender sender)
    {
        return "creeperhostserver.commands.dump.usage";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) CreeperHost.ircEventExecutor;
        String compleated = "Completed tasks " + threadPoolExecutor.getCompletedTaskCount();
        String activeCount = "Active count " + threadPoolExecutor.getActiveCount();
        String corePoolSize = "Core Pool Size " + threadPoolExecutor.getCorePoolSize();
        sender.sendMessage(new TextComponentString(compleated));
        sender.sendMessage(new TextComponentString(activeCount));
        sender.sendMessage(new TextComponentString(corePoolSize));
    }
}