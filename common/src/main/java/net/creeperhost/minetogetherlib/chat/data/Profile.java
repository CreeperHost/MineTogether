package net.creeperhost.minetogetherlib.chat.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.irc.IrcHandler;
import net.creeperhost.minetogetherlib.util.WebUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Profile
{
    public String longHash = "";
    public String shortHash = "";
    public String mediumHash = "";
    private boolean online = false;
    public String display = "";
    public boolean premium = false;
    public String userDisplay = "";
    private boolean friend = false;
    private long lastFriendCheck = 0;
    public String friendName = "";
    public String friendCode = "";
    private boolean banned = false;
    private String packID = "";
    private long lastOnlineCheck = 0;
    private boolean isOnline = false;
    private boolean isLoadingProfile = false;
    private long profileAge;
    private boolean hasAccount = false;
    private boolean muted = false;

    public Profile(String serverNick)
    {
        if(serverNick.length() == 30)
        {
            this.mediumHash = serverNick;
            this.shortHash = serverNick.substring(0,16);
            userDisplay = "User#" + mediumHash.substring(2,7);
        }
        else if(serverNick.length() < 30)
        {
            this.shortHash = serverNick;
            userDisplay = "User#" + shortHash.substring(2,7);
        } else {
            //Got a FULL hash... Uh oh (This should never actually happen)
            this.shortHash = "MT" + serverNick.substring(0,14);
            this.mediumHash = "MT" + serverNick.substring(0,28);
            this.longHash = serverNick;
            this.userDisplay = "User#" + longHash.substring(0,5);
        }
    }

    public Profile(String longHash, String shortHash, String mediumHash, boolean online, String display, boolean premium)
    {
        //For creating a new user
        this.longHash = longHash;
        this.shortHash = shortHash;
        this.mediumHash = mediumHash;
        this.online = online;
        this.display = display;
        this.premium = premium;
        this.userDisplay = "User#" + longHash.substring(0,5);
        if(premium && display.length() > 0)
        {
            this.userDisplay = display;
        }
    }
    public Profile(String longHash, String shortHash, String mediumHash, boolean online, String display, boolean premium, String userDisplay)
    {
        //For when reloading from the JSON
        this.longHash = longHash;
        this.shortHash = shortHash;
        this.mediumHash = mediumHash;
        this.online = online;
        this.display = display;
        this.premium = premium;
        this.userDisplay = userDisplay;
    }
    public String getLongHash() {
        return longHash;
    }

    public String getShortHash() {
        return shortHash;
    }

    public String getMediumHash() {
        return mediumHash;
    }

    public boolean isOnline()
    {
        long currentTime = System.currentTimeMillis() / 1000;
        if(currentTime > (lastOnlineCheck + 10))
        {
            lastOnlineCheck = currentTime;

            CompletableFuture.runAsync(() ->
            {
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { e.printStackTrace(); }
                IrcHandler.whois(mediumHash);

            }, MineTogetherChat.whoIsExecutor);
        }
        if(isOnline && !ChatHandler.friends.containsKey(mediumHash))
        {
            ChatHandler.friends.put(mediumHash, friendName);
        }
        else if(!isOnline && ChatHandler.friends.containsKey(mediumHash))
        {
            ChatHandler.friends.remove(mediumHash);
        }
        if(isFriend())
        {
//            Target.updateCache();
            //Add them to DM list
        }
        return isOnline;
    }

    public void setOnline(boolean online)
    {
        this.isOnline = online;
    }

    public boolean hasAccount()
    {
        return hasAccount;
    }

    public String getCurrentIRCNick() {
        return mediumHash;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getConnectAddress()
    {
        return "2a04:de41:" + String.join(":", this.longHash.substring(0,24).split("(?<=\\G....)")).toLowerCase();
    }

    public String getUserDisplay() {
        if(!userDisplay.isEmpty())
        {
            //Update users profiles every 30 mins for registered users, every 2 hours for unregistered (to check if they have registered)
            if(profileAge > 0) {
                long age = (System.currentTimeMillis() / 1000) - profileAge;
                if ((hasAccount && age > 1800) || (!hasAccount && age > 7200)){
                    CompletableFuture.runAsync(() -> loadProfile(), MineTogetherChat.profileExecutor);
                }
            }
        }
        if(userDisplay.isEmpty() && longHash.length() > 0) return "User#"+longHash.substring(5);
        return userDisplay;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public boolean isFriend()
    {
        long currentTime = System.currentTimeMillis() / 1000;
        if(currentTime > (lastFriendCheck + 30)) {
            ArrayList<Friend> friendsList = ChatCallbacks.getFriendsList(false, MineTogetherChat.INSTANCE.uuid);
            if(friendsList == null) return false;
            for (Friend friend : friendsList) {
                if(getLongHash().length() == 0) continue;
                if (friend.getCode().equalsIgnoreCase(getLongHash())) {
                    this.friend = true;
                    this.lastFriendCheck = System.currentTimeMillis() / 1000;
                    this.friendName = friend.getName();
                    break;
                }
            }
        }
        return this.friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isMuted()
    {
        return muted;
    }

    public void setMuted(boolean muted)
    {
        this.muted = muted;
    }

    public boolean isBanned()
    {
        return banned;
    }

    public void setBanned(boolean banned)
    {
        this.banned = banned;
    }

    public void setPackID(String packID) {
        this.packID = packID;
    }

    public String getPackID() {
        return packID;
    }

    public boolean loadProfile()
    {
        if(isLoadingProfile) return false;
        isLoadingProfile = true;
        String playerHash = (longHash.length() > 0) ? longHash : (mediumHash.length() > 0) ? mediumHash : shortHash;
        if(playerHash.length() == 0) return false;
        profileAge = System.currentTimeMillis() / 1000;
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("target", playerHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/profile", sendStr, true, true);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        //WhoIs to get their pack
        if(!mediumHash.equals(MineTogetherChat.INSTANCE.ourNick)) CompletableFuture.runAsync(() ->
        {
            try
            {
                Thread.sleep(1000);

            } catch (InterruptedException e) { e.printStackTrace(); }
            IrcHandler.whois(mediumHash);

        }, MineTogetherChat.whoIsExecutor);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (status != null && status.getAsString().equals("success"))
            {
                JsonObject profileData = obj.getAsJsonObject("profileData").getAsJsonObject(playerHash);
                mediumHash = profileData.getAsJsonObject("chat").getAsJsonObject("hash").get("medium").getAsString();
                shortHash = profileData.getAsJsonObject("chat").getAsJsonObject("hash").get("short").getAsString();
                longHash = profileData.getAsJsonObject("hash").get("long").getAsString();

                display = profileData.get("display").getAsString();
                hasAccount = profileData.get("hasAccount").getAsBoolean();
                premium = profileData.get("premium").getAsBoolean();
                online = profileData.getAsJsonObject("chat").get("online").getAsBoolean();
                friendCode = profileData.get("friendCode").getAsString();
                userDisplay = "User#" + longHash.substring(0,5);
                if(display.length() > 8)
                {
                    userDisplay = display;
                }
                isLoadingProfile = false;
                return true;
            }
        }
        if(isFriend())
        {
//            Target.updateCache();
            //Add them to DM list
        }
        isLoadingProfile = false;
        return false;
    }
}
