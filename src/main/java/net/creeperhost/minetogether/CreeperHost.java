package net.creeperhost.minetogether;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.api.CreeperHostAPI;
import net.creeperhost.minetogether.api.ICreeperHostMod;
import net.creeperhost.minetogether.api.IServerHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.common.*;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.gui.serverlist.data.Invite;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.paul.CreeperHostServerHost;
import net.creeperhost.minetogether.proxy.IProxy;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.serverstuffs.command.CommandKill;
import net.creeperhost.minetogether.siv.QueryGetter;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Mod(modid = CreeperHost.MOD_ID, name = CreeperHost.NAME, version = CreeperHost.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.9.4,1.10.2,1.11.2", guiFactory = "net.creeperhost.minetogether.gui.config.GuiCreeperConfig")
public class CreeperHost implements ICreeperHostMod, IHost
{
    public static final String MOD_ID = "minetogether";
    public static final String NAME = "MineTogether";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("minetogether");
    public static ArrayList<String> mutedUsers = new ArrayList<>();
    public static ArrayList<String> bannedUsers = new ArrayList<>();
    
    @Mod.Instance(value = "minetogether", owner = "minetogether")
    public static CreeperHost instance;
    
    @SidedProxy(clientSide = "net.creeperhost.minetogether.proxy.Client", serverSide = "net.creeperhost.minetogether.proxy.Server")
    public static IProxy proxy;
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
    String toastText;
    long endTime;
    long fadeTime;
    private QueryGetter queryGetter;
    private String lastCurse = "";
    private Random randomGenerator;
    private CreeperHostServerHost implement;
    
    public String ourNick;
    public String playerName;
    public File mutedUsersFile;
    public Runnable toastMethod;
    public String ftbPackID = "";
    public String base64;
    public String requestedID;

    public HoverEvent.Action TIMESTAMP = EnumHelper.addEnum(HoverEvent.Action.class, "TIMESTAMP", new Class[]{String.class, boolean.class}, "timestamp_hover", true);
    public static Executor profileExecutor = Executors.newCachedThreadPool(); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static DebugHandler debugHandler = new DebugHandler();
    public static AtomicReference<Profile> profile = new AtomicReference<Profile>();
    public static AtomicReference<UUID> UUID = new AtomicReference<UUID>();

    @SuppressWarnings("Duplicates")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() != Side.SERVER)
        {
            EventHandler.isOnline = proxy.checkOnline();

            if(!EventHandler.isOnline)
            {
                logger.error("Client is in offline mode");
            }
        }

        configFile = event.getSuggestedConfigurationFile();

        InputStream configStream = null;
        try
        {
            String configString;
            if (configFile.exists())
            {
                configStream = new FileInputStream(configFile);
                configString = IOUtils.toString(configStream);
            } else
            {
                File parent = configFile.getParentFile();
                File tempConfigFile = new File(parent, "creeperhost.cfg");
                if (tempConfigFile.exists())
                {
                    configStream = new FileInputStream(tempConfigFile);
                    configString = IOUtils.toString(configStream);
                } else
                {
                    configString = "{}";
                }
            }
            Config.loadConfig(configString);
        } catch (Throwable t)
        {
            logger.error("Fatal error, unable to read config. Not starting mod.", t);
            active = false;
        } finally
        {
            try
            {
                if (configStream != null)
                {
                    configStream.close();
                }
            } catch (Throwable ignored) {}
            if (!active) return;
        }
        saveConfig(event.getSide() == Side.SERVER);

        PacketHandler.packetRegister();
        
        if (event.getSide() != Side.SERVER)
        {
            updateFtbPackID();

            HostHolder.host = this;
            File gdprFile = new File("local/minetogether/gdpr.txt");
            gdpr = new GDPR(gdprFile);
            File ingameChatFile = new File("local/minetogether/ingameChatFile.txt");
            ingameChat = new IngameChat(ingameChatFile);
            ourNick = "MT" + Callbacks.getPlayerHash(CreeperHost.proxy.getUUID()).substring(0, 28);
            UUID.set(CreeperHost.proxy.getUUID());

            if(debugHandler.isDebug())
            {
                logger.debug("Nick " + ourNick);
            }

            int packID;

            HashMap<String, String> jsonObj = new HashMap<>();
            if(this.ftbPackID.length() < 1) // Even if we get "m", we can throw it away.
            {
                try
                {
                    packID = Integer.parseInt(Config.getInstance().curseProjectID);
                }
                catch (NumberFormatException e)
                {
                    packID = -1;
                }
                jsonObj.put("p", String.valueOf(packID));
            }
            else
                {
                    jsonObj.put("p", ftbPackID);
                    jsonObj.put("b", base64);
                }
            Gson gson = new Gson();
            try //Temp fix until we cxan figure out why this fails
            {
                realName = gson.toJson(jsonObj);
            } catch (Exception e)
            {
                realName = "{\"p\": \"-1\"}";
            }

            MinecraftForge.EVENT_BUS.register(new EventHandler());
            proxy.registerKeys();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        if(event.getSide().isServer()) return;

        if(profile.get() == null)
        {
            profile.set(new Profile(ourNick));
            CompletableFuture.runAsync(() -> profile.get().loadProfile(), profileExecutor);
        }
    }
    
    public void updateFtbPackID()
    {
        File versions = new File(configFile.getParentFile().getParentFile() + File.separator + "version.json");
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
                        String ID = Callbacks.getVersionFromApi(base64);

                        if (ID.isEmpty()) return;

                        requestedID = ID;

                        Config.getInstance().setVersion(requestedID);

                        this.ftbPackID = "m" + ftbPackID;

                        if(debugHandler.isDebug())
                        {
                            logger.debug("Base64 " + base64);
                            logger.debug("Requested FTB Pack ID " + requestedID);
                        }
                    }
                } catch (Exception exception)
                {
                    logger.error("MalformedJsonException version.json is not valid returning to curse ID");
                    exception.printStackTrace();
                }
            } catch (IOException ignored)
            {
                logger.info("IOException version.json not found returning to curse ID");
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    public void saveConfig(boolean isServer)
    {
        FileOutputStream configOut = null;
        try
        {
            configOut = new FileOutputStream(configFile);
            IOUtils.write(Config.saveConfig(), configOut);
            configOut.close();
        } catch (Throwable ignored) {}
        finally
        {
            try
            {
                if (configOut != null)
                {
                    configOut.close();
                }
            } catch (Throwable ignored) {}
        }
        
        if (!isServer && Config.getInstance().isCreeperhostEnabled())
        {
            CreeperHost.instance.implementations.remove(implement);
            implement = new CreeperHostServerHost();
            CreeperHostAPI.registerImplementation(implement);
        }
        
        if (!isServer && !Config.getInstance().isCreeperhostEnabled())
        {
            CreeperHost.instance.implementations.remove(implement);
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
    
    public void makeQueryGetter()
    {
        try
        {
            if (FMLClientHandler.instance().getClientToServerNetworkManager() != null)
            {
                SocketAddress socketAddress = FMLClientHandler.instance().getClientToServerNetworkManager().getRemoteAddress();
                
                String host = "127.0.0.1";
                int port = 25565;
                
                if (socketAddress instanceof InetSocketAddress)
                {
                    InetSocketAddress add = (InetSocketAddress) socketAddress;
                    host = add.getHostName();
                    port = add.getPort();
                }
                
                queryGetter = new QueryGetter(host, port);
            }
        } catch (Throwable ignored) {}
    }
    
    public QueryGetter getQueryGetter()
    {
        if (queryGetter == null)
        {
            makeQueryGetter();
        }
        return queryGetter;
    }
    
    public void displayToast(String text, int duration, Runnable method)
    {
        toastText = text;
        endTime = System.currentTimeMillis() + duration;
        fadeTime = endTime + 500;
        toastMethod = method;
    }
    
    public void clearToast(boolean fade)
    {
        toastText = null;
        endTime = System.currentTimeMillis();
        toastMethod = null;
        fadeTime = endTime + (fade ? 500 : 0);
    }

    public boolean isActiveToast()
    {
        return fadeTime >= System.currentTimeMillis();
    }
    
    @Override
    public ArrayList<Friend> getFriends()
    {
        return Callbacks.getFriendsList(false);
    }
    
    final Object friendLock = new Object();
    String friend = null;
    boolean friendMessage = false;
    
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
            } catch (Throwable ignored) {}
            finally
            {
                try
                {
                    if (anonUsersStream != null)
                    {
                        anonUsersStream.close();
                    }
                } catch (Throwable ignored) {}
            }
            anonLoaded = true;
        }
        
        if (nick.length() < 16)
            return null;
        
        nick = nick.replace("`", ""); // should fix where people join and get ` on their name for friends if connection issues etc
        if (ChatHandler.friends.containsKey(nick))
        {
            return ChatHandler.friends.get(nick);
        }
        if (nick.startsWith("MT"))
        {
            if (ChatHandler.knownUsers.findByNick(nick) != null)
            {
                return ChatHandler.knownUsers.findByNick(nick).getUserDisplay();
            } else
            {
                String anonymousNick = "User" + ChatHandler.random.nextInt(10000);

                Profile profile = ChatHandler.knownUsers.add(nick);
                if(profile != null)
                {
                    anonymousNick = profile.getUserDisplay();
                }
//                saveAnonFile();
                return anonymousNick;
            }
        }
        return null;
    }

    @Deprecated
    public void saveAnonFile()
    {
        Gson gson = new Gson();
        File anonUsersFile = new File("local/minetogether/anonusers.json");
        try
        {
            FileUtils.writeStringToFile(anonUsersFile, gson.toJson(ChatHandler.knownUsers));
        } catch (IOException ignored) {}
    }
    
    public void muteUser(String user)
    {
        mutedUsers.add(user);
        Gson gson = new Gson();
        try
        {
            FileUtils.writeStringToFile(mutedUsersFile, gson.toJson(mutedUsers));
        } catch (IOException ignored) {}
    }

    public void unmuteUser(String user)
    {
        Profile profile = ChatHandler.knownUsers.findByDisplay(user);
        mutedUsers.remove(profile.getShortHash());
        mutedUsers.remove(profile.getMediumHash());
        Gson gson = new Gson();
        try
        {
            FileUtils.writeStringToFile(mutedUsersFile, gson.toJson(mutedUsers));
        } catch (IOException ignored) {}
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
    public void closeGroupChat() {
        proxy.closeGroupChat();
    }

    @Override
    public void updateChatChannel() {
        proxy.updateChatChannel();
    }

    @Override
    public void userBanned(String username) {
        bannedUsers.add(username);
        proxy.refreshChat();
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandKill());
    }
}
