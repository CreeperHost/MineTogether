package net.creeperhost.minetogether.serverstuffs.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandKill extends CommandBase
{
    @Override
    public String getName()
    {
        return "chkillall";
    }
    
    @Override
    public String getUsage(ICommandSender sender)
    {
        return "creeperhostserver.command.killall.usage";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        int killcouunt = 0;
        String target = null;
        
        if (args.length == 0)
        {
            throw new WrongUsageException("creeperhostserver.command.killall.wrong");
        }
        
        if (args.length >= 1)
        {
            target = args[0].toLowerCase();
        }
        if (target != null)
        {
            if ("items".equals(target) || "mobs".equals(target) || "animal".equals(target) || "all".equals(target))
            {
                List<Entity> list = server.getEntityWorld().loadedEntityList;
                for (Entity e : list)
                {
                    if ("items".equals(target) && e instanceof EntityItem)
                    {
                        server.getEntityWorld().removeEntity(e);
                        killcouunt++;
                    }
                    if ("hostile".equals(target) && e instanceof EntityMob)
                    {
                        server.getEntityWorld().removeEntity(e);
                        killcouunt++;
                    }
                    
                    if ("animal".equals(target) && e instanceof EntityAnimal)
                    {
                        server.getEntityWorld().removeEntity(e);
                        killcouunt++;
                    }
                    
                    if ("all".equals(target) && !(e instanceof EntityPlayer))
                    {
                        server.getEntityWorld().removeEntity(e);
                        killcouunt++;
                    }
                }
                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Removed " + killcouunt + " " + target + "'s" + " from the world"));
            } else
            {
                throw new WrongUsageException("creeperhostserver.command.killall.usage");
            }
        }
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            List<String> completions = new ArrayList<String>();
            completions.add("items");
            completions.add("hostile");
            completions.add("animal");
            completions.add("all");
            return getListOfStringsMatchingLastWord(args, completions);
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
