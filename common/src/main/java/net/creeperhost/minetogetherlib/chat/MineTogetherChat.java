package net.creeperhost.minetogetherlib.chat;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.module.chat.screen.ChatListener;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class MineTogetherChat
{
    public static Executor profileExecutor = Executors.newCachedThreadPool();
    public static Executor otherExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("minetogether-other-%d").build());
    public static Executor ircEventExecutor = Executors.newFixedThreadPool(15, new ThreadFactoryBuilder().setNameFormat("minetogether-ircevent-%d").build());
    public static Executor chatMessageExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-chatmessage-%d").build());
    public static Executor messageHandlerExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-messagehandler-%d").build());
    public static Executor whoIsExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-whoisexecuter-%d").build());
    public static Executor friendExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-friend-%d").build());

    private static CompletableFuture chatThread = null;

    public String ourNick = "";
    public String realName = "";
    public UUID uuid = null;
    public String signature = null;
    public String serverID = "";

    public static AtomicReference<Profile> profile = new AtomicReference<>();

    public static Logger logger = LogManager.getLogger();

    public static MineTogetherChat INSTANCE;
    public boolean online;

    public MineTogetherChat(String ourNick, UUID uuid, boolean online, String realName, String signature, String serverID)
    {
        INSTANCE = this;
        this.ourNick = ourNick;
        this.uuid = uuid;
        this.online = online;
        this.realName = realName;
        this.signature = signature;
        this.serverID = serverID;
    }

    public void startChat()
    {
        if(chatThread != null) {
            chatThread.cancel(true);
            chatThread = null;
        }
        if (profile.get() == null) {
            profile.set(new Profile(MineTogetherChat.INSTANCE.ourNick));
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
            profile.get().setPackID(realName);
        }
        chatThread = CompletableFuture.runAsync(() -> ChatHandler.init(MineTogetherChat.INSTANCE.ourNick, MineTogetherChat.INSTANCE.realName, ChatListener.INSTANCE, MineTogetherChat.INSTANCE.online), MineTogetherChat.profileExecutor); // start in thread as can hold up the UI thread for some reason.
    }
}
