package net.creeperhost.creeperhost.serverstuffs.command;

import net.creeperhost.creeperhost.common.Pair;
import net.creeperhost.creeperhost.serverstuffs.CreeperHostServer;
import net.creeperhost.creeperhost.serverstuffs.pregen.PregenTask;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandPregen extends CommandBase
{
    /**
     * Gets the name of the command
     */
    @Override
    public String getName()
    {
        return "chpregen";
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender The ICommandSender who is requesting usage details
     */
    @Override
    public String getUsage(ICommandSender sender)
    {
        TextComponentTranslation chatcomponenttranslation1 = new TextComponentTranslation("creeperhostserver.command.pregen.usage1");
        chatcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(chatcomponenttranslation1);
        TextComponentTranslation chatcomponenttranslation2 = new TextComponentTranslation("creeperhostserver.command.pregen.usage2");
        chatcomponenttranslation2.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(chatcomponenttranslation2);
        return "creeperhostserver.command.pregen.usage3";
    }

    /**
     * Callback for when the command is executed
     *
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args   The arguments that were passed
     */
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 3 || args.length >= 7)
            throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
        int dimension, xRadius, zRadius;
        dimension = xRadius = zRadius = 0;
        int chunksPerTick = 5;
        World world = null;

        if (args.length >= 3)
        {
            if (args[0].equals("current"))
            {
                if (sender.getCommandSenderEntity() == null)
                    throw new WrongUsageException("creeperhostserver.command.pregen.wrongconsole");
                world = sender.getEntityWorld();
                dimension = world.provider.getDimension();
            } else {
                dimension = parseInt(args[0]);
                world = sender.getEntityWorld();
            }

            xRadius = parseInt(args[1]);
            zRadius = parseInt(args[2]);

            if (args.length == 4) {
                chunksPerTick = parseInt(args[3]);
            }
        }

        int xCenter, zCenter;
        xCenter = zCenter = 0;

         if (args.length >= 5)
         {
             xCenter = parseInt(args[3]);
             zCenter = parseInt(args[4]);
         }
         else
         {
             BlockPos spawnPoint = world.getSpawnPoint();
             xCenter = spawnPoint.getX() >> 4;
             zCenter = spawnPoint.getZ() >> 4;
         }

        if (args.length == 6)
        {
            chunksPerTick = parseInt(args[5]);
        }

        // We have all the info; time to create a task!

        int chunkMinX = xCenter - (xRadius / 2);
        int chunkMaxX = xCenter + (xRadius / 2);
        int chunkMinZ = zCenter - (zRadius / 2);
        int chunkMaxZ = zCenter + (zRadius / 2);

        if (CreeperHostServer.INSTANCE.createTask(dimension, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, chunksPerTick))
            sender.sendMessage(new TextComponentTranslation("creeperhostserver.command.pregen.added"));
        else
            throw new WrongUsageException("creeperhostserver.command.pregen.alreadyexists");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        System.out.println(args);
        if (args.length == 1)
        {
            List<String> completions = new ArrayList<String>();
            completions.add("current");
            Integer[] dimensions = DimensionManager.getStaticDimensionIDs();
            for (int dimension : dimensions)
            {
                completions.add(String.valueOf(dimension));
            }
            return getListOfStringsMatchingLastWord(args, completions);
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
