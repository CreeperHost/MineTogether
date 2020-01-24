//package net.creeperhost.minetogether;
//
//import com.google.gson.*;
//import com.google.gson.reflect.TypeToken;
//import net.creeperhost.minetogether.network.PacketHandler;
//import net.creeperhost.minetogether.config.Config;
//import net.creeperhost.minetogether.util.Pair;
//import net.creeperhost.minetogether.util.WebUtils;
//import net.creeperhost.minetogether.proxy.IServerProxy;
//import net.creeperhost.minetogether.server.MineTogetherPropertyManager;
//import net.creeperhost.minetogether.server.command.CommandInvite;
//import net.creeperhost.minetogether.server.command.CommandPregen;
//import net.creeperhost.minetogether.server.hacky.IPlayerKicker;
//import net.creeperhost.minetogether.server.pregen.PregenTask;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.network.NetHandlerPlayServer;
//import net.minecraft.network.play.INetHandlerPlayServer;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.dedicated.DedicatedServer;
//import net.minecraft.world.WorldServer;
//import net.minecraftforge.common.DimensionManager;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.entity.EntityJoinWorldEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.apache.commons.io.IOUtils;
//import org.apache.logging.log4j.Logger;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.lang.reflect.Type;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//public class CreeperHostServer
//{
//
//    @SubscribeEvent
//    public void entityJoinedWorld(EntityJoinWorldEvent event)
//    {
//        if (playersJoined.contains(event.getEntity().getUniqueID()))
//        {
//            EntityPlayerMP entity = (EntityPlayerMP) event.getEntity();
//            logger.info("Sending ID packet to client {}", entity.getName());
//            PacketHandler.INSTANCE.sendTo(new PacketHandler.ServerIDMessage(updateID), entity);
//
//            for (PregenTask task : pregenTasks.values())
//            {
//                if (task.preventJoin)
//                {
//                    kicker.kickPlayer(entity, "Server is still pre-generating!\n" + task.lastPregenString);
//                    logger.error("Kicked player " + entity.getName() + " as still pre-generating");
//                    break;
//                }
//            }
//            playersJoined.remove(entity.getUniqueID());
//        }
//    }
//
//    @Mod.EventHandler
//    public void preInit(FMLPreInitializationEvent e)
//    {
//        if (!MineTogether.instance.active)
//            return;
//        MinecraftForge.EVENT_BUS.register(this);
//        logger = e.getModLog();
//        setupPlayerKicker();
//    }
//
//    @SuppressWarnings("Duplicates")
//    @SubscribeEvent
//    public void worldTick(TickEvent.WorldTickEvent e)
//    {
//        if (!MineTogether.instance.active)
//            return;
//        if (e.phase == TickEvent.Phase.END)
//        {
//            return;
//        }
//
//        if (!FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning())
//            return;
//
//        WorldServer world = (WorldServer) e.world;
//
//        int dimension = world.provider.getDimension();
//
//        PregenTask task = pregenTasks.get(dimension);
//
//        if (task == null)
//            return;
//
//        if (task.chunksToGen.isEmpty())
//        {
//            logger.info("No more chunks to generate for dimension " + dimension + " - removing task!");
//            pregenTasks.remove(dimension);
//            if (pregenTasks.isEmpty())
//            {
//                resuscitateWatchdog();
//            }
//            serializePreload();
//            return;
//        }
//
//        int max = task.chunksPerTick;
//
//        ArrayList<Pair<Integer, Integer>> chunkToGen = new ArrayList<Pair<Integer, Integer>>();
//
//        int i = 0;
//
//        for (Pair<Integer, Integer> pair : task.chunksToGen)
//        {
//            if (i < max)
//                chunkToGen.add(pair);
//            else
//                break;
//            i++;
//        }
//
//        long curTime = System.currentTimeMillis();
//
//        if (task.startTime == 0)
//        {
//            task.lastCheckedTime = curTime;
//            task.startTime = curTime;
//        }
//
//        if (curTime - task.lastCheckedTime >= 10000)
//        {
//            task.lastCheckedTime = curTime;
//            int lastChunks = task.lastChunksDone;
//            task.lastChunksDone = task.chunksDone;
//            int chunksDelta = task.chunksDone - lastChunks;
//
//            long deltaTime = curTime - task.startTime;
//
//            double timePerChunk = (double) deltaTime / (double) task.chunksDone;
//
//            long chunksRemaining = task.totalChunks - task.chunksDone;
//
//            long estimatedTime = (long) (chunksRemaining * timePerChunk);
//
//            long days = TimeUnit.MILLISECONDS
//                    .toDays(estimatedTime);
//            estimatedTime -= TimeUnit.DAYS.toMillis(days);
//
//            long hours = TimeUnit.MILLISECONDS
//                    .toHours(estimatedTime);
//            estimatedTime -= TimeUnit.HOURS.toMillis(hours);
//
//            long minutes = TimeUnit.MILLISECONDS
//                    .toMinutes(estimatedTime);
//            estimatedTime -= TimeUnit.MINUTES.toMillis(minutes);
//
//            long seconds = TimeUnit.MILLISECONDS
//                    .toSeconds(estimatedTime);
//
//            String time = days + " day(s) " + hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
//
//            task.lastPregenString = "Pre-generating chunks for dimension " + dimension + ", current speed " + chunksDelta + " every 10 seconds." + "\n" + task.chunksDone + "/" + task.totalChunks + " " + time + " remaining";
//
//            logger.info(task.lastPregenString);
//
//            if (task.curChunksPerTick == 0)
//            {
//                if (world.getChunkProvider().getLoadedChunkCount() < task.chunkLoadCount)
//                {
//                    logger.info("Chunks appear to be unloading now - going to tentatively restart the pregen.");
//                    task.curChunksPerTick = 1;
//                }
//            }
//
//            if (world.getChunkProvider().getLoadedChunkCount() >= task.chunkLoadCount + (chunksDelta * 2))
//            {
//                // handle runaway unloading - if we've stored up the equivalent of 20 seconds worth of chunks not being unloaded, if a mod is doing bad(tm) things.
//                task.chunkLoadCount = world.getChunkProvider().getLoadedChunkCount();
//                task.curChunksPerTick--; // slow it down nelly
//                if (task.curChunksPerTick == 0)
//                {
//                    logger.info("Frozen chunk generating as it appears that chunks aren't being unloaded fast enough. Will check the status in another 10 seconds.");
//                } // not gong to log slowing down or speeding up
//            } else if (task.curChunksPerTick < task.chunksPerTick)
//            {
//                task.curChunksPerTick++; // things seem ok for now. Lets raise it back up
//            }
//
//            serializePreload();
//
//        }
//
//        killWatchdog();
//
//        for (Pair<Integer, Integer> pair : chunkToGen)
//        {
//            world.getChunkProvider().provideChunk(pair.getLeft(), pair.getRight());
//            task.storedCurX = pair.getLeft();
//            task.storedCurZ = pair.getRight();
//            task.chunksDone++;
//        }
//
//        if (task.chunksDone != 0 && task.chunksDone % 1000 == 0)
//        {
//            world.getSaveHandler().flush();
//        }
//
//        task.chunksToGen.removeAll(chunkToGen);
//    }
//}