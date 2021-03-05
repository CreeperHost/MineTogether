package net.creeperhost.minetogether;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.api.ICreeperHostMod;
import net.creeperhost.minetogether.api.IServerHost;
import net.creeperhost.minetogether.api.MineTogetherAPI;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.screen.serverlist.data.Invite;
import net.creeperhost.minetogether.common.GDPR;
import net.creeperhost.minetogether.common.HostHolder;
import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.common.IngameChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.events.ClientTickEvents;
import net.creeperhost.minetogether.events.ScreenEvents;
import net.creeperhost.minetogether.handler.PreGenHandler;
import net.creeperhost.minetogether.handler.ServerListHandler;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.handler.WatchdogHandler;
import net.creeperhost.minetogether.lib.Constants;
import net.creeperhost.minetogether.network.PacketHandler;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.paul.CreeperHostServerHost;
import net.creeperhost.minetogether.proxy.*;
import net.creeperhost.minetogether.server.command.CommandInvite;
import net.creeperhost.minetogether.server.command.CommandKill;
import net.creeperhost.minetogether.server.command.CommandPregen;
import net.creeperhost.minetogether.server.hacky.IPlayerKicker;
import net.creeperhost.minetogether.util.WebUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

@Mod(value = Constants.MOD_ID)
public class MineTogether implements ICreeperHostMod, IHost
{
    public static final Logger logger = LogManager.getLogger("minetogether");
    public static ArrayList<String> mutedUsers = new ArrayList<>();
    public static ArrayList<String> bannedUsers = new ArrayList<>();
    public static IProxy proxy;
    public static IServerProxy serverProxy;
    public final Object inviteLock = new Object();
    public IServerHost currentImplementation;
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
    public static Discoverability discoverMode = Discoverability.UNLISTED;
    public static int updateID;
    static int tries = 0;
    public IPlayerKicker kicker;
    
    public String ourNick;
    public String playerName;
    public File mutedUsersFile;
    public String ftbPackID = "";
    public String base64;
    public String requestedID;
    
    public static MineTogether instance;
    public static MinecraftServer server;
    public static String secret;
    public static PreGenHandler preGenHandler;
    public AtomicBoolean isBanned = new AtomicBoolean(false);

    public static boolean isOnline = false;
    public static Executor profileExecutor = Executors.newCachedThreadPool(); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static Executor otherExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("minetogether-other-%d").build()); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static Executor ircEventExecutor = Executors.newFixedThreadPool(15, new ThreadFactoryBuilder().setNameFormat("minetogether-ircevent-%d").build()); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static Executor chatMessageExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-chatmessage-%d").build()); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static Executor messageHandlerExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-messagehandler-%d").build()); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static Executor whoIsExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-whoisexecuter-%d").build()); //new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static DebugHandler debugHandler = new DebugHandler();
    public static AtomicReference<Profile> profile = new AtomicReference<>();
    public static AtomicReference<UUID> UUID = new AtomicReference<>();
    public ToastHandler toastHandler;
    protected static String signature = null;

    public static String getSignature()
    {
        return signature;
    }

    public static String getServerIDAndVerify()
    {
        return proxy.getServerIDAndVerify();
    }

    public MineTogether()
    {
        instance = this;
        proxy = DistExecutor.runForDist(() -> Client::new, () -> Server::new);
        serverProxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::preInit);
        eventBus.addListener(this::preInitClient);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void preInit(FMLCommonSetupEvent event)
    {
        ConfigHandler.init();
        updateFtbPackID();
        proxy.checkOnline();
        proxy.registerKeys();
        PacketHandler.register();
        //Lets check this early so we can decide if we need to start chat or not
    }
    
    @SubscribeEvent
    public void preInitClient(FMLClientSetupEvent event)
    {
        signature = verifySignature(findOurJar(FMLPaths.MODSDIR.get().toFile()));
        File serverModsFolder = new File(FMLPaths.MODSDIR.get().toFile().getParent() + File.separator + "servermods" + File.separator);

        if(signature == null && serverModsFolder.exists())
        {
            signature = verifySignature(findOurJar(serverModsFolder));
        }

        if(signature == null) signature = "Development";

        isOnline = proxy.checkOnline();
        if (!isOnline) {
            logger.error("Client is in offline mode");
        }

        MinecraftForge.EVENT_BUS.register(new net.creeperhost.minetogether.mtconnect.EventHandler());

        toastHandler = new ToastHandler();
        registerImplementation(new CreeperHostServerHost());
        
        File gdprFile = new File("local/minetogether/gdpr.txt");
        gdpr = new GDPR(gdprFile);
        
        HostHolder.host = this;
        File ingameChatFile = new File("local/minetogether/ingameChatFile.txt");
        ingameChat = new IngameChat(ingameChatFile);
        ourNick = "MT" + Callbacks.getPlayerHash(MineTogether.proxy.getUUID()).substring(0, 28);
        UUID.set(proxy.getUUID());

        if (debugHandler.isDebug()) {
            logger.debug("Nick " + ourNick);
        }

        int packID;
        HashMap<String, String> jsonObj = new HashMap<>();
        if(this.ftbPackID.length() <= 1) // Even if we get "m", we can throw it away.
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
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try //Temp fix until we cxan figure out why this fails
        {
            realName = gson.toJson(jsonObj);
        } catch (Exception e)
        {
            realName = "{\"p\": \"-1\"}";
        }
        
        MinecraftForge.EVENT_BUS.register(new ScreenEvents());
        MinecraftForge.EVENT_BUS.register(new ClientTickEvents());

        if (profile.get() == null) {
            profile.set(new Profile(ourNick));
            CompletableFuture.runAsync(() ->
            {
                while (profile.get().getLongHash().isEmpty()) {
                    profile.get().loadProfile();
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, profileExecutor);
        }
    }

    private String verifySignature(File jarFile)
    {
        if(jarFile == null) return null;
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }

        byte[] fileBytes;
        try
        {
            fileBytes = FileUtils.readFileToByteArray(jarFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        //TODO more efficient by iterating file into buffer
        messageDigest.update(fileBytes);

        //Now we have the hash of the whole jar, not just a single class, previous implementation meant you could change anything except the first class...
        return bytesToHex(messageDigest.digest());
    }

    private File findOurJar(File modsFolder)
    {
        try {
            logger.info("Scanning mods directory for MineTogether jar");
            File[] modsDir = modsFolder.listFiles();//FMLPaths.MODSDIR.get().toFile().listFiles();
            if (modsDir == null) return null;

            for (File file : modsDir) {
                try {
                    JarFile jarFile = new JarFile(file);
                    if (jarFile == null) continue;

                    logger.info("JARFILE " + jarFile.getName());
                    if(jarFile.getManifest() == null) continue;
                    Map<String, Attributes> attributesMap = jarFile.getManifest().getEntries();

                    if (attributesMap == null) continue;

                    for (String s : attributesMap.keySet()) {
                        if (s.equalsIgnoreCase("net/creeperhost/minetogether/MineTogether.class")) {
                            logger.error("Main class found, MineTogether Jar found");
                            try {
                                jarFile.close();
                                jarFile = new JarFile(file, true);
                            } catch (SecurityException ignored) {
                                ignored.printStackTrace();
                                return null;
                            }
                            return file;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


        public void registerCommands(RegisterCommandsEvent event)
        {
            event.getDispatcher().register(
                    LiteralArgumentBuilder.<CommandSource>literal("mt")
                            .then(CommandKill.register())
                            .then(CommandPregen.register()));

        }
    
    public void serverStarted(FMLServerStartedEvent event)
    {
        MineTogether.serverOn = true;
        server = event.getServer();
        new WatchdogHandler();
        new ServerListHandler();
        preGenHandler = new PreGenHandler();
    }
    
    public void serverStopping(FMLServerStoppingEvent event)
    {
        if (!MineTogether.instance.active) return;
        serverOn = false;
        preGenHandler.serializePreload();
        preGenHandler.clear();
    }

    public void updateFtbPackID()
    {
        File versions = new File(FMLPaths.GAMEDIR.get().toFile() + File.separator + "version.json");
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
                        if (requestedID.isEmpty()) requestedID = "-1";

                        Config.instance.curseProjectID = requestedID;
                        Config.saveConfig();
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
        if (MineTogetherAPI.getImplementations().size() == 0)
        {
            currentImplementation = null;
            return;
        }
        int random = randomGenerator.nextInt(MineTogetherAPI.getImplementations().size());
        currentImplementation = MineTogetherAPI.getImplementations().get(random);
    }
    
    public IServerHost getImplementation()
    {
        return currentImplementation;
    }
    
    @Override
    public void registerImplementation(IServerHost serverHost)
    {
        MineTogetherAPI.registerImplementation(serverHost);
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
                    configString = IOUtils.toString(anonUsersStream, Charset.defaultCharset());
                } else
                {
                    anonUsersFile.getParentFile().mkdirs();
                    configString = "{}";
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
                if (profile != null) {
                    anonymousNick = profile.getUserDisplay();
                }
                //saveAnonFile();
                return anonymousNick;
            }
        }
        return null;
    }
    
    public void muteUser(String user)
    {
        mutedUsers.add(user);
        Gson gson = new Gson();
        try
        {
            FileUtils.writeStringToFile(mutedUsersFile, gson.toJson(mutedUsers), Charset.defaultCharset());
        } catch (IOException ignored)
        {
        }
    }
    
    public void unmuteUser(String user)
    {
        Profile profile = ChatHandler.knownUsers.findByDisplay(user);
        mutedUsers.remove(profile.getShortHash());
        mutedUsers.remove(profile.getMediumHash());
        Gson gson = new Gson();
        try
        {
            FileUtils.writeStringToFile(mutedUsersFile, gson.toJson(mutedUsers), Charset.defaultCharset());
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
                
                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                
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
                            CommandInvite.reloadInvites(new String[0]);
                            first = false;
                        }
                    }
                } catch (Exception ignored)
                {
                }
                
                try
                {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored)
                {
                }
            }
        });
        mtThread.setDaemon(true);
        mtThread.start();
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
            } catch (Throwable ignored)
            {
            }
            
            try
            {
                Class clazz = Class.forName(className);
                kicker = (IPlayerKicker) clazz.newInstance();
            } catch (Throwable ignored)
            {
            }
        }
    }
}
