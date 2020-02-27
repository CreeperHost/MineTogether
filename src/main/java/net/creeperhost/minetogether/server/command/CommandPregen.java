package net.creeperhost.minetogether.server.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.server.pregen.PregenTask;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;

public class CommandPregen
{
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("pregen")
                .requires(cs -> cs.hasPermissionLevel(4))
                    .then(Commands.argument("dim", DimensionArgument.getDimension())
                            .then(Commands.argument("chunkMinX", IntegerArgumentType.integer(1))
                                    .then(Commands.argument("chunkMaxX", IntegerArgumentType.integer(1))
                                            .then(Commands.argument("chunkMinZ", IntegerArgumentType.integer(1))
                                                    .then(Commands.argument("chunkMaxZ", IntegerArgumentType.integer(1))
                                                            .then(Commands.argument("chunksPerTick", IntegerArgumentType.integer(1))
                                                                    .then(Commands.argument("preventJoin", BoolArgumentType.bool())
                                                                            .executes(cs -> execute(cs, DimensionArgument.getDimensionArgument(cs, "dim"),
                                                                                    IntegerArgumentType.getInteger(cs, "chunkMinX"),
                                                                                        IntegerArgumentType.getInteger(cs, "chunkMaxX"),
                                                                                            IntegerArgumentType.getInteger(cs, "chunkMinZ"),
                                                                                                IntegerArgumentType.getInteger(cs, "chunkMaxZ"),
                                                                                                    IntegerArgumentType.getInteger(cs, "chunksPerTick"),
                                                                                                        BoolArgumentType.getBool(cs, "preventJoin"))))))))));
    }

    public static int execute(CommandContext<CommandSource> ctx, DimensionType dimensionType, int chunkMinX, int chunkMaxX, int chunkMinZ, int chunkMaxZ, int chunksPerTick, boolean preventJoin) throws CommandException
    {
        PregenTask task = new PregenTask(dimensionType, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, chunksPerTick, preventJoin);

        MineTogether.preGenHandler.createTask(task);

        ctx.getSource().sendFeedback(new StringTextComponent("Starting pre-gen task for dimention " + task.dimension.toString()), true);

        return 0;
//        int xDiameterPos = -1;
//        int yDiameterPos = -1;
//        int dimensionPos = -1;
//        int xStartChunkPos = -1;
//        int yStartChunkPos = -1;
//        int chunksPerTickPos = -1;
//        int preventJoinPos = -1;
//        if (args.length <= 1)
//        {
//            throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
//        } else if (args.length == 2)
//        {
//            if (args[0].equals("remove"))
//            {
//                int dimension = -999;
//                if (args[1].equals("current"))
//                {
//                    if (sender.getCommandSenderEntity() == null)
//                        throw new WrongUsageException("creeperhostserver.command.pregen.wrongconsole");
//                    World world = sender.getEntityWorld();
//                    dimension = world.provider.getDimension();
//                } else
//                {
//                    dimension = parseInt(args[1]);
//                }
//                if (!CreeperHostServer.INSTANCE.pregenTasks.containsKey(dimension))
//                {
//                    throw new WrongUsageException("creeperhostserver.command.pregen.noexists");
//                }
//
//                CreeperHostServer.INSTANCE.pregenTasks.get(dimension).chunksToGen = new ArrayList<Pair<Integer, Integer>>();
//                sender.sendMessage(new TextComponentTranslation("creeperhostserver.command.pregen.removed"));
//                return;
//            } else
//            {
//                throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
//            }
//        } else if (args.length == 3)
//        {
//            dimensionPos = 0;
//            xDiameterPos = 1;
//            yDiameterPos = 2;
//        } else if (args.length == 4)
//        {
//            dimensionPos = 0;
//            xDiameterPos = 1;
//            yDiameterPos = 2;
//            chunksPerTickPos = 3;
//        } else if (args.length == 5)
//        {
//            dimensionPos = 0;
//            xDiameterPos = 1;
//            yDiameterPos = 2;
//            if (args[4].equals("true") || args[4].equals("false"))
//            {
//                preventJoinPos = 4;
//            } else
//            {
//                xStartChunkPos = 3;
//                yStartChunkPos = 4;
//            }
//        } else if (args.length == 6)
//        {
//            dimensionPos = 0;
//            xDiameterPos = 1;
//            yDiameterPos = 2;
//            xStartChunkPos = 3;
//            yStartChunkPos = 4;
//            chunksPerTickPos = 5;
//        } else if (args.length == 7)
//        {
//            dimensionPos = 0;
//            xDiameterPos = 1;
//            yDiameterPos = 2;
//            xStartChunkPos = 3;
//            yStartChunkPos = 4;
//            chunksPerTickPos = 5;
//            preventJoinPos = 6;
//        } else if (args.length > 7)
//        {
//            throw new WrongUsageException("creeperhostserver.command.pregen.wrong");
//        }
//
//        int dimension;
//        if (args[dimensionPos].equals("current"))
//        {
//            if (sender.getCommandSenderEntity() == null)
//                throw new WrongUsageException("creeperhostserver.command.pregen.wrongconsole");
//
//            dimension = sender.getEntityWorld().provider.getDimension();
//        } else
//        {
//            dimension = parseInt(args[dimensionPos]);
//        }
//        int xDiameter = parseInt(args[xDiameterPos]);
//        int zDiameter = parseInt(args[yDiameterPos]);
//        int xStartChunk;
//        int zStartChunk;
//        if (xStartChunkPos == -1)
//        {
//            World world = DimensionManager.getWorld(dimension);
//
//            if (world != null)
//            {
//                BlockPos spawnPoint = world.getSpawnPoint();
//                xStartChunk = spawnPoint.getX();
//                zStartChunk = spawnPoint.getZ();
//            } else
//            {
//                throw new WrongUsageException("creeperhostserver.command.pregen.dimensionnoexists");
//            }
//        } else
//        {
//            xStartChunk = parseInt(args[xStartChunkPos]);
//            zStartChunk = parseInt(args[yStartChunkPos]);
//        }
//
//        int chunksPerTick = 5;
//        if (chunksPerTickPos != -1)
//        {
//            chunksPerTick = parseInt(args[chunksPerTickPos]);
//        }
//
//        boolean preventJoin = true;
//        if (preventJoinPos != -1)
//        {
//            preventJoin = args[preventJoinPos].equals("true");
//        }
//
//        int chunkMinX = xStartChunk - (xDiameter / 2);
//        int chunkMaxX = xStartChunk + (xDiameter / 2);
//        int chunkMinZ = zStartChunk - (zDiameter / 2);
//        int chunkMaxZ = zStartChunk + (zDiameter / 2);
//
//        if (CreeperHostServer.INSTANCE.createTask(dimension, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, chunksPerTick, preventJoin))
//            sender.sendMessage(new TextComponentTranslation("creeperhostserver.command.pregen.added"));
//        else
//            throw new WrongUsageException("creeperhostserver.command.pregen.alreadyexists");
    }
}
