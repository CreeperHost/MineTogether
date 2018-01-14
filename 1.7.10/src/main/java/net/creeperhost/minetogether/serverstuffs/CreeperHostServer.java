package net.creeperhost.minetogether.serverstuffs;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.PacketHandler;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.serverstuffs.command.CommandInvite;
import net.creeperhost.minetogether.serverstuffs.command.CommandPregen;
import net.creeperhost.minetogether.serverstuffs.pregen.PregenTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    acceptableRemoteVersions = "*"
)
public class CreeperHostServer
{
    public static final String MOD_ID = "minetogetherserver";
    public static final String NAME = "MineTogetherServer";
    public static Logger logger;

    @Mod.Instance(value = "minetogetherserver")
    public static CreeperHostServer INSTANCE;
    private static String secret;

    public HashMap<Integer, PregenTask> pregenTasks = new HashMap<Integer, PregenTask>();

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandInvite());
        event.registerServerCommand(new CommandPregen());
        deserializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    public boolean serverOn = false;

    private enum Discoverability
    {
        UNLISTED,
        PUBLIC,
        INVITE
    }

    public static int updateID;

    public static class InviteClass
    {
        public int id;
        public ArrayList<String> hash;
    }

    Discoverability discoverMode = Discoverability.UNLISTED;

    int tries = 0;

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        if (!CreeperHost.instance.active)
            return;
        final MinecraftServer serverTemp = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (serverTemp != null && !serverTemp.isSinglePlayer())
        {
            DedicatedServer dediServer = (DedicatedServer) serverTemp;
            String discoverModeString = dediServer.getStringProperty("discoverability", "unlisted");
            String displayNameTemp = dediServer.getStringProperty("displayname", "Fill this in if you have set the server to public!");
            final String serverIP = dediServer.getStringProperty("server-ip", "");
            final String projectid = Config.getInstance().curseProjectID;

            if (displayNameTemp.equals("Fill this in if you have set the server to public!") && discoverModeString.equals("unlisted"))
            {
                File outProperties = new File("minetogether.properties");
                if (outProperties.exists())
                {
                    MineTogetherPropertyManager manager = new MineTogetherPropertyManager(outProperties);
                    displayNameTemp = manager.getStringProperty("displayname", "Fill this in if you have set the server to public!");
                    discoverModeString = manager.getStringProperty("discoverability", "unlisted");
                }
                else
                {
                    displayNameTemp = "Unknown";
                    discoverModeString = "unlisted";
                }
            }

            final String displayName = displayNameTemp;

            serverOn = true;
            try
            {
                discoverMode = Discoverability.valueOf(discoverModeString.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
            }

            if (discoverMode != Discoverability.UNLISTED)
            {
                Config defConfig = new Config();
                if (projectid.isEmpty() || projectid.equals(defConfig.curseProjectID))
                {
                    CreeperHostServer.logger.warn("Curse project ID in creeperhost.cfg not set correctly - please set this to utilize the server list feature.");
                    return;
                }
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        CreeperHostServer.logger.info("Enabling server list. Servers found to be breaking Mojang's EULA may be removed if complaints are received.");
                        boolean first = true;
                        while (serverOn)
                        {
                            Map send = new HashMap<String, String>();

                            if (!serverIP.isEmpty())
                            {
                                send.put("ip", serverIP);
                            }

                            if (CreeperHostServer.secret != null)
                                send.put("secret", CreeperHostServer.secret);
                            send.put("name", displayName);
                            send.put("projectid", projectid);
                            send.put("port", String.valueOf(serverTemp.getServerPort()));

                            send.put("invite-only", discoverMode == Discoverability.INVITE ? "1" : "0");

                            send.put("version", 2);

                            Gson gson = new Gson();

                            String sendStr = gson.toJson(send);

                            String resp = Util.putWebResponse("https://api.creeper.host/serverlist/update", sendStr, true, true);

                            int sleepTime = 90000;

                            try
                            {
                                JsonElement jElement = new JsonParser().parse(resp);
                                if (jElement.isJsonObject())
                                {
                                    JsonObject jObject = jElement.getAsJsonObject();
                                    if (jObject.get("status").getAsString().equals("success"))
                                    {
                                        tries = 0;
                                        CreeperHostServer.updateID = jObject.get("id").getAsNumber().intValue();
                                        if (jObject.has("secret"))
                                            CreeperHostServer.secret = jObject.get("secret").getAsString();
                                    }
                                    else
                                    {
                                        if (tries >= 4)
                                        {
                                            CreeperHostServer.logger.error("Unable to do call to server list - disabling for 45 minutes. Reason: " + jObject.get("message").getAsString());
                                            tries = 0;
                                            sleepTime = 60 * 1000 * 45;
                                        }
                                        else
                                        {
                                            CreeperHostServer.logger.error("Unable to do call to server list - will try again in 90 seconds. Reason: " + jObject.get("message").getAsString());
                                            tries++;
                                        }
                                    }

                                    if (first)
                                    {
                                        CommandInvite.reloadInvites(new String[0]);
                                        first = false;
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                // so our thread doens't go byebye
                            }

                            try
                            {
                                Thread.sleep(sleepTime);
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
        if (!CreeperHost.instance.active)
            return;
        serverOn = false;
        serializePreload();
        pregenTasks.clear();
    }

    WeakHashMap<EntityPlayerMP, Boolean> playersJoined = new WeakHashMap<EntityPlayerMP, Boolean>();

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.isSinglePlayer())
            return;
        Entity entity = event.entity;
        if (entity instanceof EntityPlayerMP)
        {
            if (!playersJoined.containsKey(entity))
            {
                playersJoined.put((EntityPlayerMP) entity, null);
                PacketHandler.INSTANCE.sendTo(new PacketHandler.ServerIDMessage(updateID), (EntityPlayerMP) entity);
            }

            for (PregenTask task : pregenTasks.values())
            {
                if (task.preventJoin)
                    ((EntityPlayerMP) entity).playerNetServerHandler.kickPlayerFromServer("Server is still pre-generating!\n" + task.lastPregenString);
                logger.error("Kicked player " + ((EntityPlayerMP) entity).getDisplayName() + " as still pre-generating");
                break;
            }

        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        if (!CreeperHost.instance.active)
            return;
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        logger = e.getModLog();
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

        int max = task.curChunksPerTick;

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

            double timePerChunk = (double) deltaTime / (double) task.chunksDone;

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

            if (task.curChunksPerTick == 0)
            {
                if (world.getChunkProvider().getLoadedChunkCount() < task.chunkLoadCount)
                {
                    logger.info("Chunks appear to be unloading now - going to tentatively restart the pregen.");
                    task.curChunksPerTick = 1;
                }
            }

            if (world.getChunkProvider().getLoadedChunkCount() >= task.chunkLoadCount + (chunksDelta * 2))
            {
                // handle runaway unloading - if we've stored up the equivalent of 20 seconds worth of chunks not being unloaded, if a mod is doing bad(tm) things.
                task.chunkLoadCount = world.getChunkProvider().getLoadedChunkCount();
                task.curChunksPerTick--; // slow it down nelly
                if (task.curChunksPerTick == 0)
                {
                    logger.info("Frozen chunk generating as it appears that chunks aren't being unloaded fast enough. Will check the status in another 10 seconds.");
                } // not gong to log slowing down or speeding up
            }
            else if (task.curChunksPerTick < task.chunksPerTick)
            {
                task.curChunksPerTick++; // things seem ok for now. Lets raise it back up
            }

            serializePreload();

        }

        for (Pair<Integer, Integer> pair : chunkToGen)
        {
            if (world.getChunkProvider().chunkExists(pair.getLeft(), pair.getRight()))
            {
                world.getChunkProvider().provideChunk(pair.getLeft(), pair.getRight());
                ((ChunkProviderServer) world.getChunkProvider()).unloadChunksIfNotNearSpawn(pair.getLeft(), pair.getRight());
            }
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

    public boolean createTask(int dimension, int xMin, int xMax, int zMin, int zMax, int chunksPerTick, boolean preventJoin)
    {
        if (pregenTasks.get(dimension) != null)
            return false;

        pregenTasks.put(dimension, new PregenTask(dimension, xMin, xMax, zMin, zMax, chunksPerTick, preventJoin));

        return true;
    }
}