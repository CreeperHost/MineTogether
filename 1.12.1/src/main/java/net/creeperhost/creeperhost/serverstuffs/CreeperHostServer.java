package net.creeperhost.creeperhost.serverstuffs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.common.Pair;
import net.creeperhost.creeperhost.serverstuffs.command.PregenCommand;
import net.creeperhost.creeperhost.serverstuffs.hacky.IPlayerKicker;
import net.creeperhost.creeperhost.serverstuffs.pregen.PregenTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
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
    public static Logger logger;

    @SidedProxy(clientSide = "net.creeperhost.creeperhost.serverstuffs.ClientProxy", serverSide = "net.creeperhost.creeperhost.serverstuffs.ServerProxy")
    public static IServerProxy proxy;

    @Mod.Instance(value = "creeperhostserver")
    public static CreeperHostServer INSTANCE;

    private HashMap<Integer, PregenTask> pregenTasks = new HashMap<Integer, PregenTask>();

    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    private boolean needsToBeKilled = true;
    private boolean watchdogKilled = false;
    private boolean watchdogChecked = false;

    private void killWatchdog()
    {
        if (!watchdogChecked)
        {
            needsToBeKilled = proxy.needsToBeKilled();
            watchdogChecked = true;
        }
        if (!watchdogKilled && needsToBeKilled)
        {
            watchdogKilled = proxy.killWatchdog();
        }

    }

    private void resuscitateWatchdog()
    {
        if (watchdogKilled && needsToBeKilled)
        {
            proxy.resuscitateWatchdog();
            watchdogKilled = false;
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new PregenCommand());
        deserializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    public boolean serverOn;

    @Mod.EventHandler
    public void serverStarted (FMLServerStartedEvent event)
    {
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null && !server.isSinglePlayer())
        {
            PropertyManager manager = new PropertyManager(new File("server.properties"));
            final boolean serverPublic = manager.getBooleanProperty("public", false);
            final String displayName = manager.getStringProperty("displayname", "Fill this in, and curseprojectid, if you have set the server to public!");
            final String serverIP = manager.getStringProperty("server-ip", "");
            final String projectid = manager.getStringProperty("curseprojectid", "");
            serverOn = true;
            if (serverPublic)
            {
                if (projectid.isEmpty())
                {
                    CreeperHostServer.logger.warn("projectid in server.properties is not set - please set this to a curse project ID to use the public server list!");
                    return;
                }
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while (serverOn)
                        {
                            Map send = new HashMap<String, String>();

                            if (!serverIP.isEmpty())
                            {
                                send.put("ip", serverIP);
                            }
                            send.put("name", displayName);
                            send.put("projectid", projectid);
                            send.put("port", String.valueOf(server.getServerPort()));

                            Util.putWebResponse("https://api.creeper.host/serverlist/update", new Gson().toJson(send), true, true);

                            try
                            {
                                Thread.sleep(120000);
                            }
                            catch (InterruptedException e)
                            {
                                // meh
                            }
                        }
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        serverOn = false;
        serializePreload();
        pregenTasks.clear();
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.isSinglePlayer())
            return;
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP)
        {
            for (PregenTask task : pregenTasks.values())
            {
                kicker.kickPlayer((EntityPlayerMP) entity, "Server is still pre-generating!\n" + task.lastPregenString);
                logger.error("Kicked player " + entity.getName() + " as still pre-generating");
                break;
            }
            event.setCanceled(true);
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(this);
        logger = e.getModLog();
        setupPlayerKicker();
    }

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

        int dimension = world.provider.getDimension();

        PregenTask task = pregenTasks.get(dimension);

        if (task == null)
            return;

        if (task.chunksToGen.isEmpty())
        {
            logger.info("No more chunks to generate for dimension " + dimension + " - removing task!");
            pregenTasks.remove(dimension);
            if (pregenTasks.isEmpty())
            {
                resuscitateWatchdog();
            }
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

        killWatchdog();

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

    private static ArrayList<String> oldVersions = new ArrayList<String>()
    {{
        add("1.9");
        add("1.9.4");
        add("1.10");
        add("1.10.2");
        add("1.11");
        add("1.11.2");
    }};

    public IPlayerKicker kicker;

    public void setupPlayerKicker()
    {

        if (kicker == null)
        {
            String className = "net.creeperhost.creeperhost.serverstuffs.hacky.NewPlayerKicker";
            String mcVersion;
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            }
            catch (Throwable e)
            {
                mcVersion = "unknown"; // will default to new method
            }
            if (oldVersions.contains(mcVersion))
            {
                className = "net.creeperhost.creeperhost.serverstuffs.hacky.OldPlayerKicker";
            }

            try
            {
                Class clazz = Class.forName(className);
                kicker = (IPlayerKicker) clazz.newInstance();
            }
            catch (Throwable t)
            {
            }
        }
    }

}