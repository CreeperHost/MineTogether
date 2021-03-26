package net.creeperhost.minetogether.serverstuffs;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.PacketHandler;
import net.creeperhost.minetogether.chat.ChatUtil;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.common.WebUtils;
import net.creeperhost.minetogether.misc.Callbacks;
import net.creeperhost.minetogether.serverstuffs.command.CommandInvite;
import net.creeperhost.minetogether.serverstuffs.command.CommandPregen;
import net.creeperhost.minetogether.serverstuffs.hacky.IPlayerKicker;
import net.creeperhost.minetogether.serverstuffs.pregen.PregenTask;
import net.creeperhost.minetogether.trade.commands.CommandTrade;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
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
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mod(modid = CreeperHostServer.MOD_ID, name = CreeperHostServer.NAME, version = CreeperHost.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.9.4,1.10.2,1.11.2")
public class CreeperHostServer
{
    public static final String MOD_ID = "minetogetherserver";
    public static final String NAME = "MineTogetherServer";
    public static Logger logger;
    
    @SidedProxy(clientSide = "net.creeperhost.minetogether.serverstuffs.ClientProxy", serverSide = "net.creeperhost.minetogether.serverstuffs.ServerProxy")
    public static IServerProxy proxy;

    @Mod.Instance(value = "minetogetherserver")
    public static CreeperHostServer INSTANCE;
    public static int updateID;
    public static String secret;
    @SuppressWarnings("Duplicates")
    private static ArrayList<String> oldVersions = new ArrayList<String>()
    {{
        add("1.9");
        add("1.9.4");
        add("1.10");
        add("1.10.2");
        add("1.11");
        add("1.11.2");
    }};
    public HashMap<Integer, PregenTask> pregenTasks = new HashMap<Integer, PregenTask>();
    public static boolean serverOn;
    public IPlayerKicker kicker;
    Discoverability discoverMode = Discoverability.UNLISTED;
    static int tries = 0;
    ArrayList<UUID> playersJoined = new ArrayList<UUID>();
    private boolean needsToBeKilled = true;
    private boolean watchdogKilled = false;
    private boolean watchdogChecked = false;
    public String ftbPackID = "";
    public String base64;
    public String requestedID;
//    public ChatHandlerServer chatHandlerServer = null;
    public String serverNick = "";
    public String realName = "";
    
    public static Thread getThreadByName(String threadName)
    {
        for (Thread t : Thread.getAllStackTraces().keySet())
        {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }
    
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

    public String getNick()
    {
        return serverNick;
    }

//    public Client getClient(){ return chatHandlerServer.client; }

    public void createID(MinecraftServer minecraftServer)
    {
        //Contents of api.callbacks.io/ip + machines ip + port + hash all 3 and take first 28 char + prefix with MS
        String ipResp = WebUtils.getWebResponse("https://api.callbacks.io/ip");
        if(ipResp.isEmpty()) return;
        if(minecraftServer == null) return;
        String serverIP = "";
        try
        {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) { e.printStackTrace(); }

        String serverPort = "25565";

        if(minecraftServer.isDedicatedServer())
            serverPort = String.valueOf(minecraftServer.getServerPort());

        if(serverIP.isEmpty()) return;

        String joined = ipResp + serverIP + serverPort;
        byte[] messageDigest = new byte[0];
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-256").digest(joined.getBytes());
            realName = bytesToHex(messageDigest).toUpperCase();
            serverNick =  "MS" + realName.substring(0, 28);
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
    }

    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
//        createID(event.getServer());
//        ChatUtil.IRCServer ircServer = ChatUtil.getIRCServerDetails();
//        if(!serverNick.isEmpty() && !realName.isEmpty())
//            chatHandlerServer = new ChatHandlerServer(serverNick, realName, ircServer.address, ircServer.port, ircServer.ssl);

        if (!CreeperHost.instance.active) return;
        event.registerServerCommand(new CommandInvite());
        event.registerServerCommand(new CommandPregen());
        if(Config.getInstance().isTradeEnabled()) event.registerServerCommand(new CommandTrade());
        deserializePreload(new File(getSaveFolder(), "pregenData.json"));

        CompletableFuture.runAsync(() -> {
            updateFtbPackID();
            int packID;

            HashMap<String, String> jsonObj = new HashMap<>();
            if (this.ftbPackID.length() < 1) // Even if we get "m", we can throw it away.
            {
                try {
                    packID = Integer.parseInt(Config.getInstance().curseProjectID);
                } catch (NumberFormatException e) {
                    packID = -1;
                }
                jsonObj.put("p", String.valueOf(packID));
            } else {
                jsonObj.put("p", ftbPackID);
                jsonObj.put("b", base64);
            }
        }).join();
    }

    public void updateFtbPackID()
    {
        File versions = new File("." + File.separator + "version.json");
        if(versions.exists())
        {
            try (InputStream stream = new FileInputStream(versions))
            {
                try
                {
                    JsonElement json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (json.isJsonObject())
                    {
                        JsonObject object = json.getAsJsonObject();
                        int versionID = object.getAsJsonPrimitive("id").getAsInt();
                        int ftbPackID = object.getAsJsonPrimitive("parent").getAsInt();

                        base64 = Base64.getEncoder().encodeToString((String.valueOf(ftbPackID) + String.valueOf(versionID)).getBytes());
                        requestedID = Callbacks.getVersionFromApi(base64);
                        if (requestedID.isEmpty()) return;

                        Config.getInstance().setVersion(requestedID);

                        this.ftbPackID = "m" + ftbPackID;
                    }
                } catch (Exception MalformedJsonException)
                {
                    logger.error("version.json is not valid returning to curse ID");
                }
            } catch (IOException ignored)
            {
                logger.info("version.json not found returning to curse ID");
            }
        }
    }
    
    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        if (!CreeperHost.instance.active) return;
//        try
//        {
//            if(chatHandlerServer != null) chatHandlerServer.init();
//
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null && !server.isSinglePlayer())
        {
            DedicatedServer dediServer = (DedicatedServer) server;
            String discoverModeString = dediServer.getStringProperty("discoverability", "unlisted");
            String displayNameTemp = dediServer.getStringProperty("displayname", "Fill this in if you have set the server to public!");
            String serverIP = dediServer.getStringProperty("server-ip", "");
            final String projectid = Config.getInstance().curseProjectID;

            if (displayNameTemp.equals("Fill this in if you have set the server to public!") && discoverModeString.equals("unlisted"))
            {
                File outProperties = new File("minetogether.properties");
                if (outProperties.exists())
                {
                    MineTogetherPropertyManager manager = new MineTogetherPropertyManager(outProperties);
                    displayNameTemp = manager.getStringProperty("displayname", "Fill this in if you have set the server to public!");
                    discoverModeString = manager.getStringProperty("discoverability", "unlisted");
                    serverIP = dediServer.getStringProperty("server-ip", "");
                } else
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
            } catch (IllegalArgumentException ignored) {}
            
            if (discoverMode != Discoverability.UNLISTED)
            {
                Config defConfig = new Config();
                if ((projectid.isEmpty() || projectid.equals(defConfig.curseProjectID)) && base64 == null)
                {
                    CreeperHostServer.logger.warn("Curse project ID in creeperhost.cfg not set correctly - please set this to utilize the server list feature.");
                    return;
                }
                startMinetogetherThread(serverIP, displayName, base64 == null ? projectid : base64, server.getServerPort(), discoverMode);
            }
        }
    }
    
    static Thread mtThread;
    public static boolean isActive;
    public static boolean failed;
    
    @SuppressWarnings("Duplicates")
    public static void startMinetogetherThread(String serverIP, String displayName, String projectid, int port, Discoverability discoverMode)
    {
        mtThread = new Thread(() ->
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
                send.put("port", String.valueOf(port));
                
                send.put("invite-only", discoverMode == Discoverability.INVITE ? "1" : "0");
                
                send.put("version", 2);
                
                Gson gson = new Gson();
                
                String sendStr = gson.toJson(send);

                String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/update", sendStr, true, true);

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
                            isActive = true;
                        } else
                        {
                            if (tries >= 4)
                            {
                                CreeperHostServer.logger.error("Unable to do call to server list - disabling for 45 minutes. Reason: " + jObject.get("message").getAsString());
                                tries = 0;
                                sleepTime = 60 * 1000 * 45;
                            } else
                            {
                                CreeperHostServer.logger.error("Unable to do call to server list - will try again in 90 seconds. Reason: " + jObject.get("message").getAsString());
                                tries++;
                            }
                            failed = true;
                        }
                        
                        if (first)
                        {
                            CommandInvite.reloadInvites(new String[0]);
                            first = false;
                        }
                    }
                } catch (Exception e)
                {
                    // so our thread doens't go byebye
                }
                
                try
                {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e)
                {
                    // meh
                }
            }
        });
        mtThread.setDaemon(true);
        mtThread.start();
    }
    
    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        if (!CreeperHost.instance.active) return;
//        chatHandlerServer.client.shutdown();
        serverOn = false;
        serializePreload();
        pregenTasks.clear();
    }
    
    @SubscribeEvent
    public void clientConnectedtoServer(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        if (!CreeperHost.instance.active) return;
        INetHandlerPlayServer handler = event.getHandler();
        if (handler instanceof NetHandlerPlayServer)
        {
            EntityPlayerMP entity = ((NetHandlerPlayServer) handler).playerEntity;
            if(entity != null && !(entity instanceof FakePlayer))
            {
                logger.info("Connected to MineTogether server " + serverNick);
                //entity.sendMessage(new TextComponentString("Connected to MineTogether server " + TextFormatting.GREEN + serverNick));
            }
        }

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.isSinglePlayer() || discoverMode != Discoverability.PUBLIC) return;
        
        if (handler instanceof NetHandlerPlayServer)
        {
            EntityPlayerMP entity = ((NetHandlerPlayServer) handler).playerEntity;
            playersJoined.add(entity.getUniqueID());
        }
    }
    
    @SubscribeEvent
    public void entityJoinedWorld(EntityJoinWorldEvent event)
    {
        if (playersJoined.contains(event.getEntity().getUniqueID()))
        {
            EntityPlayerMP entity = (EntityPlayerMP) event.getEntity();
            logger.info("Sending ID packet to client {}", entity.getName());
            PacketHandler.INSTANCE.sendTo(new PacketHandler.ServerIDMessage(updateID), entity);
            
            for (PregenTask task : pregenTasks.values())
            {
                if (task.preventJoin)
                {
                    kicker.kickPlayer(entity, "Server is still pre-generating!\n" + task.lastPregenString);
                    logger.error("Kicked player " + entity.getName() + " as still pre-generating");
                    break;
                }
            }
            playersJoined.remove(entity.getUniqueID());
        }
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        if (!CreeperHost.instance.active)
            return;
        MinecraftForge.EVENT_BUS.register(this);
        logger = e.getModLog();
        setupPlayerKicker();
    }
    
    @SuppressWarnings("Duplicates")
    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent e)
    {
        if (!CreeperHost.instance.active)
            return;
        if (e.phase == TickEvent.Phase.END)
        {
            return;
        }
        
        if (!FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning())
            return;
        
        WorldServer world = (WorldServer) e.world;
        
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
            
            long seconds = TimeUnit.MILLISECONDS.toSeconds(estimatedTime);
            
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

            double percentage = ((double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().totalMemory()) * 100;

            if (world.getChunkProvider().getLoadedChunkCount() >= task.chunkLoadCount + (chunksDelta * 2) || percentage >= 80)
            {
                // handle runaway unloading - if we've stored up the equivalent of 20 seconds worth of chunks not being unloaded, if a mod is doing bad(tm) things.
                task.chunkLoadCount = world.getChunkProvider().getLoadedChunkCount();
                task.curChunksPerTick--; // slow it down nelly
                if (task.curChunksPerTick == 0)
                {
                    logger.info("Frozen chunk generating as it appears that chunks aren't being unloaded fast enough. Will check the status in another 10 seconds.");
                } // not gong to log slowing down or speeding up
            } else if (task.curChunksPerTick < task.chunksPerTick)
            {
                task.curChunksPerTick++; // things seem ok for now. Lets raise it back up
            }
            
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
    
    @SuppressWarnings("Duplicates")
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
    
    @SuppressWarnings("Duplicates")
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
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("Duplicates")
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
        } catch (Exception ignored) {}
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
    
    public void setupPlayerKicker()
    {
        
        if (kicker == null)
        {
            String className = "net.creeperhost.minetogether.serverstuffs.hacky.NewPlayerKicker";
            String mcVersion;
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            } catch (Throwable e)
            {
                mcVersion = "unknown"; // will default to new method
            }
            if (oldVersions.contains(mcVersion))
            {
                className = "net.creeperhost.minetogether.serverstuffs.hacky.OldPlayerKicker";
            }
            
            try
            {
                Class clazz = Class.forName(className);
                kicker = (IPlayerKicker) clazz.newInstance();
            } catch (Throwable t)
            {
            }
        }
    }
    
    public enum Discoverability
    {
        UNLISTED,
        PUBLIC,
        INVITE
    }
    
    public static class InviteClass
    {
        public int id = CreeperHostServer.updateID;
        public ArrayList<String> hash;
    }
    
}