package net.creeperhost.minetogether.threads;

import com.google.gson.*;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.util.WebUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendUpdateThread
{
    private static Runnable friendUpdate;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public static void init()
    {
        Runnable runnable = FriendUpdateThread::updateFriendsList;
        executorService.scheduleAtFixedRate(runnable, 0,30, TimeUnit.SECONDS);
    }

    public static void setFriendUpdate(Runnable runnable)
    {
        friendUpdate = runnable;
    }

    public static void runFriendUpdate()
    {
        if(friendUpdate != null) CompletableFuture.runAsync(friendUpdate, MineTogetherChat.friendExecutor);
    }

    public static void updateFriendsList()
    {
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", MineTogetherChat.profile.get().getLongHash());
        }
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/listfriend", new Gson().toJson(sendMap), true, true);
        JsonElement el = new JsonParser().parse(resp);
        if (el.isJsonObject()) {

            JsonObject obj = el.getAsJsonObject();
            if (obj.get("status").getAsString().equals("success")) {
                JsonArray array = obj.getAsJsonArray("friends");
                for (JsonElement friendEl : array) {
                    JsonObject friend = (JsonObject) friendEl;
                    String name = "null";

                    if (!friend.get("name").isJsonNull()) {
                        name = friend.get("name").getAsString();
                    }
                    String code = friend.get("hash").isJsonNull() ? "" : friend.get("hash").getAsString();
                    boolean accepted = friend.get("accepted").getAsBoolean();
                    if(accepted)
                    {
                        Profile friendProfile = ChatHandler.knownUsers.findByHash(code);
                        if(friendProfile == null) friendProfile = ChatHandler.knownUsers.add(code);
                        friendProfile.setFriendName(name);
                        friendProfile.setFriend(true);
                        ChatHandler.knownUsers.update(friendProfile);
                    }
                }
            }
        }
    }
}
