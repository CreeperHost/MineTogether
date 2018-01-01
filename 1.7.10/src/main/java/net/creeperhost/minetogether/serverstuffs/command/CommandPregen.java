package net.creeperhost.minetogether.serverstuffs.command;

import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.serverstuffs.CreeperHostServer;
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

public class CommandPregen extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "chpregen";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        ChatComponentText chatcomponenttranslation1 = new ChatComponentText("creeperhostserver.command.pregen.usage1");
        chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation1);
        ChatComponentText chatcomponenttranslation2 = new ChatComponentText("creeperhostserver.command.pregen.usage2");
        chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation2);
        ChatComponentText chatcomponenttranslation3 = new ChatComponentText("creeperhostserver.command.pregen.usage3");
        chatcomponenttranslation3.getChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation3);
        ChatComponentText chatcomponenttranslation4 = new ChatComponentText("creeperhostserver.command.pregen.usage4");
        chatcomponenttranslation4.getChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation4);
        return "creeperhostserver.command.pregen.usage5";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        int xDiameterPos = -1;
        int yDiameterPos = -1;
        int dimensionPos = -1;
        int xStartChunkPos = -1;
        int yStartChunkPos = -1;
        int chunksPerTickPos = -1;
        int preventJoinPos = -1;
        if (args.length <= 1)
        {
            throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
        } else if (args.length == 2) {
            if (args[0].equals("remove"))
            {
                int dimension = -999;
                if (args[1].equals("current"))
                {
                    if (sender instanceof MinecraftServer)
                        throw new WrongUsageException("creeperhostserver.command.pregen.wrongconsole");
                    World world = sender.getEntityWorld();
                    dimension = world.provider.dimensionId;
                } else {
                    dimension = parseInt(sender, args[1]);
                }
                if (!CreeperHostServer.INSTANCE.pregenTasks.containsKey(dimension))
                {
                    throw new WrongUsageException("creeperhostserver.command.pregen.noexists");
                }

                CreeperHostServer.INSTANCE.pregenTasks.get(dimension).chunksToGen = new ArrayList<Pair<Integer, Integer>>();
                sender.addChatMessage(new ChatComponentText("creeperhostserver.command.pregen.removed"));
                return;
            } else {
                throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
            }
        } else if (args.length == 3) {
            dimensionPos = 0;
            xDiameterPos = 1;
            yDiameterPos = 2;
        } else if (args.length == 4) {
            dimensionPos = 0;
            xDiameterPos = 1;
            yDiameterPos = 2;
            chunksPerTickPos = 3;
        } else if (args.length == 5) {
            dimensionPos = 0;
            xDiameterPos = 1;
            yDiameterPos = 2;
            if (args[4].equals("true") || args[4].equals("false")) {
                preventJoinPos = 4;
            } else {
                xStartChunkPos = 3;
                yStartChunkPos = 4;
            }
        } else if (args.length == 6) {
            dimensionPos = 0;
            xDiameterPos = 1;
            yDiameterPos = 2;
            xStartChunkPos = 3;
            yStartChunkPos = 4;
            chunksPerTickPos = 5;
        } else if (args.length == 7) {
            dimensionPos = 0;
            xDiameterPos = 1;
            yDiameterPos = 2;
            xStartChunkPos = 3;
            yStartChunkPos = 4;
            chunksPerTickPos = 5;
            preventJoinPos = 6;
        } else if (args.length > 7) {
            throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
        }

        int dimension;
        if (args[dimensionPos].equals("current"))
        {
            if (sender instanceof MinecraftServer)
                throw new WrongUsageException("creeperhostserver.command.pregen.wrongconsole");

            dimension = sender.getEntityWorld().provider.dimensionId;
        } else {
            dimension = parseInt(sender, args[dimensionPos]);
        }
        int xDiameter = parseInt(sender, args[xDiameterPos]);
        int zDiameter = parseInt(sender, args[yDiameterPos]);
        int xStartChunk;
        int zStartChunk;
        if (xStartChunkPos == -1)
        {
            World world = DimensionManager.getWorld(dimension);

            if (world != null)
            {
                ChunkCoordinates spawnPoint = world.getSpawnPoint();
                xStartChunk = spawnPoint.posX;
                zStartChunk = spawnPoint.posZ;
            } else {
                throw new WrongUsageException("creeperhostserver.command.pregen.dimensionnoexists");
            }
        } else {
            xStartChunk = parseInt(sender, args[xStartChunkPos]);
            zStartChunk = parseInt(sender, args[yStartChunkPos]);
        }

        int chunksPerTick = 5;
        if (chunksPerTickPos != -1)
        {
            chunksPerTick = parseInt(sender, args[chunksPerTickPos]);
        }

        boolean preventJoin = true;
        if (preventJoinPos != -1)
        {
            preventJoin = args[preventJoinPos].equals("true");
        }

        int chunkMinX = xStartChunk - (xDiameter / 2);
        int chunkMaxX = xStartChunk + (xDiameter / 2);
        int chunkMinZ = zStartChunk - (zDiameter / 2);
        int chunkMaxZ = zStartChunk + (zDiameter / 2);

        if (CreeperHostServer.INSTANCE.createTask(dimension, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, chunksPerTick, preventJoin))
            sender.addChatMessage(new ChatComponentText("creeperhostserver.command.pregen.added"));
        else
            throw new WrongUsageException("creeperhostserver.command.pregen.alreadyexists");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        Integer[] dimensions = DimensionManager.getStaticDimensionIDs();
        if (args.length == 1)
        {
            List<String> completions = new ArrayList<String>();
            completions.add("current");
            completions.add("remove");
            for (int dimension : dimensions)
            {
                completions.add(String.valueOf(dimension));
            }
            return getListOfStringsFromIterableMatchingLastWord(args, completions);
        } else if (args.length == 2 && args[0].equals("remove"))
        {
            List<String> completions = new ArrayList<String>();
            completions.add("current");
            for (int dimension : dimensions)
            {
                completions.add(String.valueOf(dimension));
            }
            return getListOfStringsFromIterableMatchingLastWord(args, completions);
        }
        return super.addTabCompletionOptions(sender, args);
    }
}
