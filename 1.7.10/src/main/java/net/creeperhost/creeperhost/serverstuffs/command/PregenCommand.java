package net.creeperhost.creeperhost.serverstuffs.command;

import net.creeperhost.creeperhost.serverstuffs.CreeperHostServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class PregenCommand extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "chpregen";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("creeperhostserver.command.pregen.usage1");
        chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation1);
        ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("creeperhostserver.command.pregen.usage2");
        chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation2);
        return "creeperhostserver.command.pregen.usage3";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 3 || args.length >= 7)
            throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
        int dimension, xRadius, zRadius;
        dimension = xRadius = zRadius = 0;
        int chunksPerTick = 20;
        World world = null;

        if (args.length >= 3)
        {
            if (args[0].equals("current"))
            {
                if (sender instanceof MinecraftServer)
                    throw new WrongUsageException("creeperhostserver.command.pregen.wrongconsole");
                world = sender.getEntityWorld();
                dimension = world.provider.dimensionId;
            } else {
                dimension = parseInt(sender, args[0]);
                world = sender.getEntityWorld();
            }

            xRadius = parseInt(sender, args[1]);
            zRadius = parseInt(sender, args[2]);

            if (args.length == 4) {
                chunksPerTick = parseInt(sender, args[3]);
            }
        }

        int xCenter, zCenter;
        xCenter = zCenter = 0;

         if (args.length >= 5)
         {
             xCenter = parseInt(sender, args[3]);
             zCenter = parseInt(sender, args[4]);
         }
         else
         {
             ChunkCoordinates spawnPoint = world.getSpawnPoint();
             xCenter = spawnPoint.posX >> 4;
             zCenter = spawnPoint.posZ >> 4;
         }

        if (args.length == 6)
        {
            chunksPerTick = parseInt(sender, args[5]);
        }

        // We have all the info; time to create a task!

        int chunkMinX = xCenter - (xRadius / 2);
        int chunkMaxX = xCenter + (xRadius / 2);
        int chunkMinZ = zCenter - (zRadius / 2);
        int chunkMaxZ = zCenter + (zRadius / 2);

        if (CreeperHostServer.INSTANCE.createTask(dimension, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, chunksPerTick))
            sender.addChatMessage(new ChatComponentText("creeperhostserver.command.pregen.added"));
        else
            throw new WrongUsageException("creeperhostserver.command.pregen.alreadyexists");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        System.out.println(args);
        if (args.length == 1)
        {
            List<String> completions = new ArrayList<String>();
            completions.add("current");
            Integer[] dimensions = DimensionManager.getStaticDimensionIDs();
            for (int dimension : dimensions)
            {
                System.out.println(dimension);
                completions.add(String.valueOf(dimension));
            }
            return getListOfStringsFromIterableMatchingLastWord(args, completions);
        }
        return super.addTabCompletionOptions(sender, args);
    }
}
