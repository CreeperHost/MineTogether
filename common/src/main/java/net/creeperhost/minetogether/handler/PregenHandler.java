package net.creeperhost.minetogether.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherServer;
import net.creeperhost.minetogetherlib.serverorder.Pair;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PregenHandler
{
    public static HashMap<ResourceKey<Level>, PregenTask> pregenTasks = new HashMap<ResourceKey<Level>, PregenTask>();
    public static boolean shouldKickPlayer;
    @Nullable private static PregenTask activeTask;

    public static void addTask(ResourceKey<Level> dimension, int minX, int maxX, int minZ, int maxZ, int chunksPerTick, boolean preventJoin)
    {
        if(pregenTasks.get(dimension) != null) return;
        pregenTasks.put(dimension, new PregenTask(dimension, minX, maxX, minZ, maxZ, chunksPerTick, preventJoin));
    }

    public static void onWorldTick(MinecraftServer minecraftServer)
    {
        //No point doing anything if the list is empty
        if(pregenTasks.isEmpty()) return;

        if(pregenTasks.containsKey(ServerLevel.OVERWORLD))
        {
            //TODO more than just the overworld
            ServerLevel serverLevel = minecraftServer.getLevel(ServerLevel.OVERWORLD);
            PregenTask pregenTask = pregenTasks.get(ServerLevel.OVERWORLD);

            if(pregenTask == null) return;

            shouldKickPlayer = pregenTask.preventJoin;
            activeTask = pregenTask;

            if (pregenTask.chunksToGen.isEmpty())
            {
                MineTogether.logger.info("No more chunks to generate for dimension " + pregenTask.dimension + " - removing task!");
                pregenTasks.remove(pregenTask.dimension);
                shouldKickPlayer = false;
                activeTask = null;
                if (pregenTasks.isEmpty())
                {
                    WatchDogHandler.resuscitateWatchdog();
                }
                serializePreload();
                return;
            }

            int max = pregenTask.chunksPerTick;
            ArrayList<Pair<Integer, Integer>> chunkToGen = new ArrayList<Pair<Integer, Integer>>();
            int i = 0;

            for (Pair<Integer, Integer> pair : pregenTask.chunksToGen)
            {
                if (i < max) chunkToGen.add(pair);
                else break;
                i++;
            }

            long curTime = System.currentTimeMillis();
            if (pregenTask.startTime == 0)
            {
                pregenTask.lastCheckedTime = curTime;
                pregenTask.startTime = curTime;
            }

            if (curTime - pregenTask.lastCheckedTime >= 10000)
            {
                pregenTask.lastCheckedTime = curTime;
                int lastChunks = pregenTask.lastChunksDone;
                pregenTask.lastChunksDone = pregenTask.chunksDone;
                int chunksDelta = pregenTask.chunksDone - lastChunks;

                pregenTask.lastPregenString = "Pre-generating chunks for dimension " + pregenTask.dimension.location() + ", current speed " + chunksDelta + " every 10 seconds." + "\n" + pregenTask.chunksDone + "/" + pregenTask.totalChunks + " " + getTimeRemaining(pregenTask) + " remaining";

                MineTogether.logger.info(pregenTask.lastPregenString);
                long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                double percentage = ((double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().totalMemory()) * 100;

                MineTogether.logger.info("Memory usage " + formatMemory(usedMemory) + "/" + formatMemory(Runtime.getRuntime().totalMemory()) + " " + (int) percentage + "%");
                MineTogether.logger.info(pregenTask.lastPregenString);

                if (pregenTask.curChunksPerTick == 0)
                {
                    if (serverLevel.getChunkSource().getLoadedChunksCount() < pregenTask.chunkLoadCount)
                    {
                        MineTogether.logger.info("Chunks appear to be unloading now - going to tentatively restart the pregen.");
                        pregenTask.curChunksPerTick = 1;
                    }
                }

                if (serverLevel.getChunkSource().getLoadedChunksCount() >= pregenTask.chunkLoadCount + (chunksDelta * 2) || percentage >= 80)
                {
                    // handle runaway unloading - if we've stored up the equivalent of 20 seconds worth of chunks not being unloaded, if a mod is doing bad(tm) things.
                    pregenTask.chunkLoadCount = serverLevel.getChunkSource().getLoadedChunksCount();
                    pregenTask.curChunksPerTick--; // slow it down nelly
                    if(percentage >= 80)
                    {
                        MineTogether.logger.info("Memory usage too high, Forcing Garbage collection.");
                        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        System.gc();
                        long newUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        long freed = used - newUsed;
                        MineTogether.logger.info("New used memory " + formatMemory(newUsed) + ", Freed memory " + formatMemory(freed));
                    }
                    if (pregenTask.curChunksPerTick == 0)
                    {
                        MineTogether.logger.info("Frozen chunk generating as it appears that chunks aren't being unloaded fast enough. Will check the status in another 10 seconds.");
                    } // not gong to log slowing down or speeding up
                } else if (pregenTask.curChunksPerTick < pregenTask.chunksPerTick)
                {
                    pregenTask.curChunksPerTick++; // things seem ok for now. Lets raise it back up
                }
                serializePreload();
            }
            WatchDogHandler.killWatchDog();

            for (Pair<Integer, Integer> pair : chunkToGen)
            {
                serverLevel.getChunkSource().getChunk(pair.getLeft(), pair.getRight(), true);
                pregenTask.storedCurX = pair.getLeft();
                pregenTask.storedCurZ = pair.getRight();
                pregenTask.chunksDone++;
            }

            if (pregenTask.chunksDone != 0 && pregenTask.chunksDone % 1000 == 0)
            {
                serverLevel.getChunkSource().save(true);
            }
            pregenTask.chunksToGen.removeAll(chunkToGen);
        }
    }

    public static void onPlayerJoin(ServerPlayer serverPlayer)
    {
        if(serverPlayer != null && PregenHandler.isPreGenerating() && PregenHandler.shouldKickPlayer)
        {
            String remainingTime = PregenHandler.getActiveTask() != null ? PregenHandler.getTimeRemaining(PregenHandler.getActiveTask()) : "";

            serverPlayer.connection.disconnect(new TranslatableComponent("MineTogether: Server is still pre-generating!\n" + remainingTime + " Remaining"));
            MineTogether.logger.error("Kicked player " + serverPlayer.getName() + " as still pre-generating");
        }
    }

    public static boolean isPreGenerating()
    {
        return !pregenTasks.isEmpty();
    }

    public static PregenTask getActiveTask()
    {
        return activeTask;
    }

    public static String getTimeRemaining(PregenTask pregenTask)
    {
        long curTime = System.currentTimeMillis();

        long deltaTime = curTime - pregenTask.startTime;

        double timePerChunk = (double) deltaTime / (double) pregenTask.chunksDone;

        long chunksRemaining = pregenTask.totalChunks - pregenTask.chunksDone;

        long estimatedTime = (long) (chunksRemaining * timePerChunk);

        long days = TimeUnit.MILLISECONDS.toDays(estimatedTime);
        estimatedTime -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(estimatedTime);
        estimatedTime -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(estimatedTime);
        estimatedTime -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(estimatedTime);

        return days + " day(s) " + hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }

    private static void serializePreload()
    {
        FileOutputStream pregenOut = null;
        Type listOfPregenTask = new TypeToken<HashMap<Integer, PregenTask>>()
        {
        }.getType();
        try
        {
            pregenOut = new FileOutputStream(new File(MineTogetherServer.minecraftServer.getServerDirectory(), "pregenData.json"));
            Gson gson = new GsonBuilder().create();
            String output = gson.toJson(pregenTasks, listOfPregenTask);
            IOUtils.write(output, pregenOut);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void deserializePreload()
    {
        MineTogether.logger.info("Attempting to load pregenData.json");
        Gson gson = new GsonBuilder().create();
        HashMap output = null;
        Type listOfPregenTask = new TypeToken<HashMap<Integer, PregenTask>>()
        {
        }.getType();
        try
        {
            output = gson.fromJson(IOUtils.toString(new File(MineTogetherServer.minecraftServer.getServerDirectory(), "pregenData.json").toURI()), listOfPregenTask);
        } catch (Exception ignored) {}
        if (output == null) pregenTasks = new HashMap<ResourceKey<Level>, PregenTask>();
        else pregenTasks = output;

        Collection<PregenTask> tasks = pregenTasks.values();

        for (PregenTask task : tasks)
        {
            task.init();
        }
    }

    private static String formatMemory(long value)
    {
        long megaByte = 1024L * 1024L;
        long returnValue = value / megaByte;
        return (returnValue + " MiB");
    }
}
