package net.creeperhost.minetogetherlib.chat;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogetherlib.chat.data.IHost;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class MineTogetherChat implements IHost
{
    public static Executor profileExecutor = Executors.newCachedThreadPool();
    public static Executor otherExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("minetogether-other-%d").build());
    public static Executor ircEventExecutor = Executors.newFixedThreadPool(15, new ThreadFactoryBuilder().setNameFormat("minetogether-ircevent-%d").build());
    public static Executor chatMessageExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-chatmessage-%d").build());
    public static Executor messageHandlerExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-messagehandler-%d").build());
    public static Executor whoIsExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-whoisexecuter-%d").build());

    public String ourNick = "";
    public String realName = "";
    public UUID uuid = null;
    public String signature = null;
    public String serverID = "";

    public static AtomicReference<Profile> profile = new AtomicReference<>();

    public static Logger logger = LogManager.getLogger();

    public static MineTogetherChat INSTANCE;
    public boolean online;

    public MineTogetherChat()
    {
        INSTANCE = this;
    }

    @Override
    public String getNameForUser(String nick) {
        return "";
    }

    @Override
    public List<Friend> getFriends() {
        return null;
    }

    @Override
    public void friendEvent(String name, boolean isMessage) {

    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void messageReceived(String target, Message messagePair) {

    }

    @Override
    public String getFriendCode() {
        return null;
    }

    @Override
    public void acceptFriend(String s, String trim) {

    }

    @Override
    public void closeGroupChat() {

    }

    @Override
    public void updateChatChannel() {

    }

    @Override
    public void userBanned(String username) {

    }
}
