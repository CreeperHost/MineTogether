package net.creeperhost.minetogether.threads;

import com.google.gson.*;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.KnownUsers;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.util.WebUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendUpdateThread
{
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public static void init()
    {
        Runnable runnable = FriendUpdateThread::updateFriendsList;
        executorService.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

    public static void updateFriendsList()
    {
        try
        {
            Map<String, String> sendMap = new HashMap<String, String>();
            {
                sendMap.put("hash", ChatCallbacks.getPlayerHash(MineTogetherClient.getUUID()));
            }
            String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/listfriend", new Gson().toJson(sendMap), true, true);
            JsonElement el = new JsonParser().parse(resp);
            if (el.isJsonObject())
            {

                JsonObject obj = el.getAsJsonObject();
                if (obj.get("status").getAsString().equals("success"))
                {
                    JsonArray array = obj.getAsJsonArray("friends");
                    for (JsonElement friendEl : array)
                    {
                        JsonObject friend = (JsonObject) friendEl;
                        String name = "null";

                        if (!friend.get("name").isJsonNull())
                        {
                            name = friend.get("name").getAsString();
                        }
                        String code = friend.get("hash").isJsonNull() ? "" : friend.get("hash").getAsString();
                        boolean accepted = friend.get("accepted").getAsBoolean();
                        if (accepted)
                        {
                            Profile friendProfile = KnownUsers.findByHash(code);
                            if (friendProfile == null) friendProfile = KnownUsers.add(code);
                            friendProfile.setFriendName(name);
                            friendProfile.setFriend(true);
                            KnownUsers.update(friendProfile);
                        }
                    }
                }
            }
            //This can fail due to web request timing out it seems, Catching the exception to avoid killing the thread
        } catch (Exception ignored)
        {
        }
    }
}
