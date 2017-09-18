package net.creeperhost.creeperhost.serverstuffs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.server.FMLServerHandler;
import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.common.Pair;
import net.creeperhost.creeperhost.serverstuffs.command.PregenCommand;
import net.creeperhost.creeperhost.serverstuffs.pregen.PregenTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Mod(
        modid = CreeperHostServer.MOD_ID,
        name = CreeperHostServer.NAME,
        version = CreeperHost.VERSION,
        acceptableRemoteVersions="*",
        acceptedMinecraftVersions = "1.9.4,1.10.2,1.11.2"
)
public class CreeperHostServer
{
    public static final String MOD_ID = "creeperhostserver";
    public static final String NAME = "CreeperHostServer";
    private static Logger logger;

    private MinecraftServer server;

    @Mod.Instance(value = "creeperhostserver")
    public static CreeperHostServer INSTANCE;

    private HashMap<Integer, PregenTask> pregenTasks = new HashMap<Integer, PregenTask>();

    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new PregenCommand());
        deserializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        serializePreload();
        pregenTasks.clear();
        server = null;
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.isSinglePlayer())
            return;
        Entity entity = event.entity;
        if (entity instanceof EntityPlayerMP)
        {
            for (PregenTask task : pregenTasks.values())
            {
              ((EntityPlayerMP) entity).playerNetServerHandler.kickPlayerFromServer("Server is still pre-generating!\n" + task.lastPregenString);
                logger.error("Kicked player " + ((EntityPlayerMP) entity).getDisplayName() + " as still pre-generating");
                break;
            }

        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        logger = e.getModLog();
    }

    boolean first = true;

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent e)
    {
        if (e.phase == TickEvent.Phase.END)
        {
            return;
        }

        if (!FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning())
            return;

        World world = e.world;

        int dimension = world.provider.dimensionId;

        PregenTask task = pregenTasks.get(dimension);

        if (task == null)
            return;

        if (task.chunksToGen.isEmpty())
        {
            logger.info("No more chunks to generate for dimension " + dimension + " - removing task!");
            pregenTasks.remove(dimension);
            serializePreload();
            return;
        }

        int max = task.chunksPerTick;

        ArrayList<Pair<Integer, Integer>> chunkToGen = new ArrayList<Pair<Integer, Integer>>();

        int i = 0;

        for (Pair<Integer, Integer> pair : task.chunksToGen)
        {
            if (i < max)
                chunkToGen.add(pair);
            else
                break;
            i++;
        }

        long curTime = System.currentTimeMillis();

        if (task.startTime == 0)
        {
            task.lastCheckedTime = curTime;
            task.startTime = curTime;
        }

        if (curTime - task.lastCheckedTime >= 10000)
        {
            task.lastCheckedTime = curTime;
            int lastChunks = task.lastChunksDone;
            task.lastChunksDone = task.chunksDone;
            int chunksDelta = task.chunksDone - lastChunks;

            long deltaTime = curTime - task.startTime;

            double timePerChunk = (double)deltaTime / (double)task.chunksDone;

            long chunksRemaining = task.totalChunks - task.chunksDone;

            long estimatedTime = (long) (chunksRemaining * timePerChunk);

            long days = TimeUnit.MILLISECONDS
                    .toDays(estimatedTime);
            estimatedTime -= TimeUnit.DAYS.toMillis(days);

            long hours = TimeUnit.MILLISECONDS
                    .toHours(estimatedTime);
            estimatedTime -= TimeUnit.HOURS.toMillis(hours);

            long minutes = TimeUnit.MILLISECONDS
                    .toMinutes(estimatedTime);
            estimatedTime -= TimeUnit.MINUTES.toMillis(minutes);

            long seconds = TimeUnit.MILLISECONDS
                    .toSeconds(estimatedTime);

            String time = days + " day(s) " + hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";

            task.lastPregenString = "Pre-generating chunks for dimension " + dimension + ", current speed " + chunksDelta + " every 10 seconds." + "\n" + task.chunksDone + "/" + task.totalChunks + " " + time + " remaining";

            logger.info(task.lastPregenString);

            serializePreload();

        }

        for (Pair<Integer, Integer> pair : chunkToGen)
        {
            world.getChunkProvider().provideChunk(pair.getLeft(), pair.getRight());
            task.storedCurX = pair.getLeft();
            task.storedCurZ = pair.getRight();
            task.chunksDone++;
        }

        if (task.chunksDone != 0 && task.chunksDone % 1000 == 0)
        {
            world.getSaveHandler().flush();
        }

        task.chunksToGen.removeAll(chunkToGen);
    }

    public File getSaveFolder()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null && !server.isSinglePlayer())
                return server.getFile("");
        return DimensionManager.getCurrentSaveRootDirectory();
    }

    public void serializePreload()
    {
        serializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    private void serializePreload(File file)
    {
        FileOutputStream pregenOut = null;
        Type listOfPregenTask = new TypeToken<HashMap<Integer, PregenTask>>()
        {
        }.getType();
        try
        {
            pregenOut = new FileOutputStream(file);
            Gson gson = new GsonBuilder().create();
            String output = gson.toJson(pregenTasks, listOfPregenTask);
            IOUtils.write(output, pregenOut);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void deserializePreload(File file)
    {
        Gson gson = new GsonBuilder().create();
        HashMap output = null;
        Type listOfPregenTask = new TypeToken<HashMap<Integer, PregenTask>>()
        {
        }.getType();
        try
        {
            output = gson.fromJson(IOUtils.toString(file.toURI()), listOfPregenTask);
        }
        catch (IOException e)
        {
        }
        if (output == null)
            pregenTasks = new HashMap<Integer, PregenTask>();
        else
            pregenTasks = output;

        Collection<PregenTask> tasks = pregenTasks.values();

        for (PregenTask task : tasks)
        {
            task.init();
        }
    }

    public boolean createTask(int dimension, int xMin, int xMax, int zMin, int zMax, int chunksPerTick)
    {
        if (pregenTasks.get(dimension) != null)
            return false;

        pregenTasks.put(dimension, new PregenTask(dimension, xMin, xMax, zMin, zMax, chunksPerTick));

        return true;
    }
}