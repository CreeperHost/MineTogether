package net.creeperhost.minetogether.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.handler.PregenHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class CommandPregen
{
    public static LiteralArgumentBuilder<CommandSourceStack> register()
    {

        return Commands.literal("pregen").requires((cs) -> cs.hasPermission(4))
                .then(Commands.argument("dimention", DimensionArgument.dimension())
                        .then(Commands.argument("minX", IntegerArgumentType.integer())
                                .then((Commands.argument("maxX", IntegerArgumentType.integer())
                                        .then(Commands.argument("minZ", IntegerArgumentType.integer())
                                                .then(Commands.argument("maxZ", IntegerArgumentType.integer())
                                                        .then(Commands.argument("chunksPerTick", IntegerArgumentType.integer())
                                                                .then(Commands.argument("preventJoin", BoolArgumentType.bool())
                                                                        .executes((ctx) -> execute(ctx.getSource(),
                                                                                DimensionArgument.getDimension(ctx, "dimention"),
                                                                                IntegerArgumentType.getInteger(ctx, "minX"),
                                                                                IntegerArgumentType.getInteger(ctx, "maxX"),
                                                                                IntegerArgumentType.getInteger(ctx, "minZ"),
                                                                                IntegerArgumentType.getInteger(ctx, "maxZ"),
                                                                                IntegerArgumentType.getInteger(ctx, "chunksPerTick"),
                                                                                BoolArgumentType.getBool(ctx, "preventJoin")))))))))));
    }

    private static int execute(CommandSourceStack cs, ServerLevel dimention, int minX, int maxX, int minZ, int maxZ, int chunksPerTick, boolean preventJoin)
    {
        int xStartChunk;
        int zStartChunk;

        ServerLevel serverLevel = cs.getLevel();
        BlockPos spawn = serverLevel.getSharedSpawnPos();
        xStartChunk = spawn.getX();
        zStartChunk = spawn.getY();

        int xDiameter = minX;
        int zDiameter = minZ;

        int chunkMinX = xStartChunk - (xDiameter / 2);
        int chunkMaxX = xStartChunk + (xDiameter / 2);
        int chunkMinZ = zStartChunk - (zDiameter / 2);
        int chunkMaxZ = zStartChunk + (zDiameter / 2);

        PregenHandler.addTask(dimention.dimension(), chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, chunksPerTick, preventJoin);
        cs.sendSuccess(new TranslatableComponent("new PregenTask added for " + dimention.dimension().location()), false);
        return 0;
    }

//    /pregen minecraft:overworld 500 500 500 500 1 true
}
