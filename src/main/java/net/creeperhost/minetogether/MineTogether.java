package net.creeperhost.minetogether;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.api.CreeperHostAPI;
import net.creeperhost.minetogether.api.ICreeperHostMod;
import net.creeperhost.minetogether.api.IServerHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.gui.serverlist.data.Invite;
import net.creeperhost.minetogether.common.GDPR;
import net.creeperhost.minetogether.common.HostHolder;
import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.common.IngameChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.events.ClientTickEvents;
import net.creeperhost.minetogether.events.ScreenEvents;
import net.creeperhost.minetogether.lib.ModInfo;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.paul.CreeperHostServerHost;
import net.creeperhost.minetogether.proxy.*;
import net.creeperhost.minetogether.server.MineTogetherPropertyManager;
import net.creeperhost.minetogether.server.command.CommandKill;
import net.creeperhost.minetogether.server.hacky.IPlayerKicker;
import net.creeperhost.minetogether.server.pregen.PregenTask;
import net.creeperhost.minetogether.util.WebUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerProperties;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

@Mod(value = ModInfo.MOD_ID)
public class MineTogether implements ICreeperHostMod, IHost
{
    public static final Logger logger = LogManager.getLogger("minetogether");
    public HashMap<DimensionType, PregenTask> pregenTasks = new HashMap<DimensionType, PregenTask>();
    public static ArrayList<String> mutedUsers = new ArrayList<>();
    public static ArrayList<String> bannedUsers = new ArrayList<>();
    public static IProxy proxy;
    public static IServerProxy serverProxy;
    public final Object inviteLock = new Object();
    public ArrayList<IServerHost> implementations = new ArrayList<IServerHost>();
    public IServerHost currentImplementation;
    public File configFile;
    public int curServerId = -1;
    public Invite handledInvite;
    public boolean active = true;
    public Invite invite;
    public GDPR gdpr;
    public IngameChat ingameChat;
    public String activeMinigame;
    public int minigameID;
    public boolean trialMinigame;
    public long joinTime;
    public String realName;
    public boolean online;
    public static boolean serverOn;
    private String lastCurse = "";
    private Random randomGenerator;
    private CreeperHostServerHost implement;
    Discoverability discoverMode = Discoverability.UNLISTED;
    ArrayList<UUID> playersJoined = new ArrayList<UUID>();

    public static int updateID;
    static int tries = 0;
    public IPlayerKicker kicker;

    public String ourNick;
    public File mutedUsersFile;
    
    public static MineTogether instance;
    public static MinecraftServer server;
    public static String secret;

    private boolean needsToBeKilled = true;
    private boolean watchdogKilled = false;
    private boolean watchdogChecked = false;

    public MineTogether()
    {
        instance = this;
        proxy = DistExecutor.runForDist(() -> Client::new, () -> Server::new);
        serverProxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::preInit);
        eventBus.addListener(this::preInitClient);
        eventBus.addListener(this::serverStarting);
//        eventBus.addListener(this::serverStarted);

        MinecraftForge.EVENT_BUS.register(new ScreenEvents());
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void preInit(FMLCommonSetupEvent event)
    {
        ConfigHandler.init();
        proxy.checkOnline();
        registerImplementation(new CreeperHostServerHost());
        
        proxy.registerKeys();

//        PacketHandler.packetRegister();
    }
    
    @SubscribeEvent
    public void preInitClient(FMLClientSetupEvent event)
    {
        File gdprFile = new File("local/minetogether/gdpr.txt");
        gdpr = new GDPR(gdprFile);
        
        HostHolder.host = this;
        File ingameChatFile = new File("local/minetogether/ingameChatFile.txt");
        ingameChat = new IngameChat(ingameChatFile);
        ourNick = "MT" + Callbacks.getPlayerHash(MineTogether.proxy.getUUID()).substring(0, 15);
        
        HashMap<String, String> jsonObj = new HashMap<>();
        
        int packID;
        
        try
        {
            packID = Integer.parseInt(Config.getInstance().curseProjectID);
        } catch (NumberFormatException e)
        {
            packID = -1;
        }
        
        jsonObj.put("p", String.valueOf(packID));
        
        Gson gson = new Gson();
        try //Temp fix until we cxan figure out why this fails
        {
            realName = gson.toJson(jsonObj);
        } catch (Exception ignored)
        {
        }
        
        MinecraftForge.EVENT_BUS.register(new ClientTickEvents());
    }
    
    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event)
    {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal("ch")
                .then(CommandKill.register());

        event.getCommandDispatcher().register(root);

        server = event.getServer();
        deserializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

//    @SubscribeEvent
//    public void serverStarted(FMLServerStartedEvent event)
//    {
//        if (!MineTogether.instance.active)
//            return;
//        final MinecraftServer server = MineTogether.server;
//        if (server != null && !server.isSinglePlayer())
//        {
//            DedicatedServer dediServer = (DedicatedServer) server;
//            String discoverModeString = dediServer.getStringProperty("discoverability", "unlisted");
//            String displayNameTemp = dediServer.getStringProperty("displayname", "Fill this in if you have set the server to public!");
//            String serverIP = dediServer.getStringProperty("server-ip", "");
//            final String projectid = Config.getInstance().curseProjectID;
//
//            if (displayNameTemp.equals("Fill this in if you have set the server to public!") && discoverModeString.equals("unlisted"))
//            {
//                File outProperties = new File("minetogether.properties");
//                if (outProperties.exists())
//                {
//                    MineTogetherPropertyManager manager = new MineTogetherPropertyManager(outProperties);
//                    displayNameTemp = manager.getStringProperty("displayname", "Fill this in if you have set the server to public!");
//                    discoverModeString = manager.getStringProperty("discoverability", "unlisted");
//                    serverIP = dediServer.getStringProperty("server-ip", "");
//                } else
//                {
//                    displayNameTemp = "Unknown";
//                    discoverModeString = "unlisted";
//                }
//            }
//
//            final String displayName = displayNameTemp;
//
//            serverOn = true;
//            try
//            {
//                discoverMode = Discoverability.valueOf(discoverModeString.toUpperCase());
//            } catch (IllegalArgumentException ignored) {}
//
//            if (discoverMode != Discoverability.UNLISTED)
//            {
//                Config defConfig = new Config();
//                if (projectid.isEmpty() || projectid.equals(defConfig.curseProjectID))
//                {
//                    MineTogether.logger.warn("Curse project ID in creeperhost.cfg not set correctly - please set this to utilize the server list feature.");
//                    return;
//                }
//                startMinetogetherThread(serverIP, displayName, projectid, server.getServerPort(), discoverMode);
//            }
//        }
//    }

    @SubscribeEvent
    public void serverStopping(FMLServerStoppingEvent event)
    {
        if (!MineTogether.instance.active)
            return;
        serverOn = false;
        serializePreload();
        pregenTasks.clear();
    }

    @SuppressWarnings("Duplicates")
    public void saveConfig()
    {
        FileOutputStream configOut = null;
        try
        {
            configOut = new FileOutputStream(configFile);
            IOUtils.write(Config.saveConfig(), configOut);
            configOut.close();
        } catch (Throwable ignored)
        {
        } finally
        {
            try
            {
                if (configOut != null)
                {
                    configOut.close();
                }
            } catch (Throwable ignored)
            {
            }
        }
        
        if (Config.getInstance().isCreeperhostEnabled())
        {
            MineTogether.instance.implementations.remove(implement);
            implement = new CreeperHostServerHost();
            CreeperHostAPI.registerImplementation(implement);
        }
        
        if (!Config.getInstance().isCreeperhostEnabled())
        {
            MineTogether.instance.implementations.remove(implement);
            implement = null;
        }
    }
    
    public void updateCurse()
    {
        if (!Config.getInstance().curseProjectID.equals(lastCurse) && Config.getInstance().isCreeperhostEnabled())
        {
            Config.getInstance().setVersion(Callbacks.getVersionFromCurse(Config.getInstance().curseProjectID));
        }
        lastCurse = Config.getInstance().curseProjectID;
    }
    
    public void setRandomImplementation()
    {
        if (randomGenerator == null)
            randomGenerator = new Random();
        if (implementations.size() == 0)
        {
            currentImplementation = null;
            return;
        }
        int random = randomGenerator.nextInt(implementations.size());
        currentImplementation = implementations.get(random);
    }
    
    public IServerHost getImplementation()
    {
        return currentImplementation;
    }
    
    @Override
    public void registerImplementation(IServerHost serverHost)
    {
        implementations.add(serverHost);
    }

    @Override
    public ArrayList<Friend> getFriends()
    {
        return Callbacks.getFriendsList(false);
    }
    
    public final Object friendLock = new Object();
    public String friend = null;
    public boolean friendMessage = false;
    
    @Override
    public void friendEvent(String name, boolean isMessage)
    {
        synchronized (friendLock)
        {
            friend = ChatHandler.getNameForUser(name);
            friendMessage = isMessage;
        }
    }
    
    @Override
    public Logger getLogger()
    {
        return logger;
    }
    
    @Override
    public void messageReceived(String target, Message messagePair)
    {
        proxy.messageReceived(target, messagePair);
    }
    
    private static boolean anonLoaded = false;
    
    public String getNameForUser(String nick)
    {
        if (!anonLoaded)
        {
            File anonUsersFile = new File("local/minetogether/anonusers.json");
            InputStream anonUsersStream = null;
            try
            {
                String configString;
                if (anonUsersFile.exists())
                {
                    anonUsersStream = new FileInputStream(anonUsersFile);
                    configString = IOUtils.toString(anonUsersStream);
                } else
                {
                    anonUsersFile.getParentFile().mkdirs();
                    configString = "{}";
                }
                
                Gson gson = new Gson();
                ChatHandler.anonUsers = gson.fromJson(configString, new TypeToken<HashMap<String, String>>()
                {
                }.getType());
                ChatHandler.anonUsersReverse = new HashMap<>();
                for (Map.Entry<String, String> entry : ChatHandler.anonUsers.entrySet())
                {
                    ChatHandler.anonUsersReverse.put(entry.getValue(), entry.getKey());
                }
            } catch (Throwable ignored)
            {
            } finally
            {
                try
                {
                    if (anonUsersStream != null)
                    {
                        anonUsersStream.close();
                    }
                } catch (Throwable ignored)
                {
                }
            }
            anonLoaded = true;
        }
        
        if (nick.length() < 16)
            return null;
        
        nick = nick.substring(0, 17); // should fix where people join and get ` on their name for friends if connection issues etc
        if (ChatHandler.friends.containsKey(nick))
        {
            return ChatHandler.friends.get(nick);
        }
        if (nick.startsWith("MT"))
        {
            if (ChatHandler.anonUsers.containsKey(nick))
            {
                return ChatHandler.anonUsers.get(nick);
            } else
            {
                String anonymousNick = "User" + ChatHandler.random.nextInt(10000);
                while (ChatHandler.anonUsers.containsValue(anonymousNick))
                {
                    anonymousNick = "User" + ChatHandler.random.nextInt(10000);
                }
                ChatHandler.anonUsers.put(nick, anonymousNick);
                ChatHandler.anonUsersReverse.put(anonymousNick, nick);
                saveAnonFile();
                return anonymousNick;
            }
        }
        return null;
    }
    
    public void saveAnonFile()
    {
        Gson gson = new Gson();
        File anonUsersFile = new File("local/minetogether/anonusers.json");
        try
        {
            FileUtils.writeStringToFile(anonUsersFile, gson.toJson(ChatHandler.anonUsers));
        } catch (IOException ignored)
        {
        }
    }
    
    public void muteUser(String user)
    {
        mutedUsers.add(user);
        Gson gson = new Gson();
        try
        {
            FileUtils.writeStringToFile(mutedUsersFile, gson.toJson(mutedUsers));
        } catch (IOException ignored)
        {
        }
    }
    
    public void unmuteUser(String user)
    {
        String mtUser = ChatHandler.anonUsersReverse.get(user);
        mutedUsers.remove(mtUser);
        mutedUsers.remove(mtUser + "`");
        Gson gson = new Gson();
        try
        {
            FileUtils.writeStringToFile(mutedUsersFile, gson.toJson(mutedUsers));
        } catch (IOException ignored)
        {
        }
    }
    
    @Override
    public String getFriendCode()
    {
        return Callbacks.getFriendCode();
    }
    
    @Override
    public void acceptFriend(String friendCode, String name)
    {
        new Thread(() -> Callbacks.addFriend(friendCode, name)).start();
    }
    
    @Override
    public void closeGroupChat()
    {
        proxy.closeGroupChat();
    }
    
    @Override
    public void updateChatChannel()
    {
        proxy.updateChatChannel();
    }
    
    @Override
    public void userBanned(String username)
    {
        bannedUsers.add(username);
        proxy.refreshChat();
    }

    public static Thread getThreadByName(String threadName)
    {
        for (Thread t : Thread.getAllStackTraces().keySet())
        {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    @SuppressWarnings("Duplicates")
    private void deserializePreload(File file)
    {
        Gson gson = new GsonBuilder().create();
        HashMap output = null;
        Type listOfPregenTask = new TypeToken<HashMap<DimensionType, PregenTask>>()
        {
        }.getType();
        try
        {
            output = gson.fromJson(IOUtils.toString(file.toURI()), listOfPregenTask);
        } catch (Exception e)
        {
        }
        if (output == null)
            pregenTasks = new HashMap<DimensionType, PregenTask>();
        else
            pregenTasks = output;

        Collection<PregenTask> tasks = pregenTasks.values();

        for (PregenTask task : tasks)
        {
            task.init();
        }
    }

    @SuppressWarnings("Duplicates")
    public File getSaveFolder()
    {
        MinecraftServer server = MineTogether.server;
        if (server != null && !server.isSinglePlayer())
            return server.getFile("");
        return null;
    }

    public enum Discoverability
    {
        UNLISTED,
        PUBLIC,
        INVITE
    }

    public static class InviteClass
    {
        public int id = MineTogether.updateID;
        public ArrayList<String> hash;
    }

    static Thread mtThread;
    public static boolean isActive;
    public static boolean failed;

    @SuppressWarnings("Duplicates")
    public static void startMinetogetherThread(String serverIP, String displayName, String projectid, int port, Discoverability discoverMode)
    {
        mtThread = new Thread(() ->
        {
            MineTogether.logger.info("Enabling server list. Servers found to be breaking Mojang's EULA may be removed if complaints are received.");
            boolean first = true;
            while (serverOn)
            {
                Map send = new HashMap<String, String>();

                if (!serverIP.isEmpty())
                {
                    send.put("ip", serverIP);
                }

                if (MineTogether.secret != null)
                    send.put("secret", MineTogether.secret);
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
                            MineTogether.updateID = jObject.get("id").getAsNumber().intValue();
                            if (jObject.has("secret"))
                                MineTogether.secret = jObject.get("secret").getAsString();
                            isActive = true;
                        } else
                        {
                            if (tries >= 4)
                            {
                                MineTogether.logger.error("Unable to do call to server list - disabling for 45 minutes. Reason: " + jObject.get("message").getAsString());
                                tries = 0;
                                sleepTime = 60 * 1000 * 45;
                            } else
                            {
                                MineTogether.logger.error("Unable to do call to server list - will try again in 90 seconds. Reason: " + jObject.get("message").getAsString());
                                tries++;
                            }
                            failed = true;
                        }

                        if (first)
                        {
//                            CommandInvite.reloadInvites(new String[0]);
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

    public boolean createTask(DimensionType dimension, int xMin, int xMax, int zMin, int zMax, int chunksPerTick, boolean preventJoin)
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
            String className = "net.creeperhost.minetogether.server.hacky.NewPlayerKicker";
            String mcVersion;
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            } catch (Throwable ignored) {}

            try
            {
                Class clazz = Class.forName(className);
                kicker = (IPlayerKicker) clazz.newInstance();
            } catch (Throwable ignored) {}
        }
    }

    private void killWatchdog()
    {
        if (!watchdogChecked)
        {
            needsToBeKilled = serverProxy.needsToBeKilled();
            watchdogChecked = true;
        }
        if (!watchdogKilled && needsToBeKilled)
        {
            watchdogKilled = serverProxy.killWatchdog();
        }
    }

    private void resuscitateWatchdog()
    {
        if (watchdogKilled && needsToBeKilled)
        {
            serverProxy.resuscitateWatchdog();
            watchdogKilled = false;
        }
    }

    @SubscribeEvent
    public void clientConnectedtoServer(NetworkEvent.ClientCustomPayloadLoginEvent event)
    {
        if (!MineTogether.instance.active)
            return;
        MinecraftServer server = MineTogether.server;
        if (server == null || server.isSinglePlayer() || discoverMode != Discoverability.PUBLIC)
            return;

//        IServerPlayNetHandler handler = event.getHandler();
//        if (handler instanceof ServerPlayNetHandler)
//        {
//            ServerPlayerEntity entity = ((ServerPlayNetHandler) handler).player;
//            playersJoined.add(entity.getUniqueID());
//        }
    }
}
