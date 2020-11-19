package net.creeperhost.minetogether.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.common.WebUtils;
import net.creeperhost.minetogether.gui.chat.Target;
import net.creeperhost.minetogether.paul.Callbacks;
import org.kitteh.irc.client.library.command.WhoisCommand;
import org.kitteh.irc.client.library.element.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private boolean onlineShort = false;
    private boolean onlineMedium = false;
    private boolean isLoadingProfile = false;

    public Profile(String serverNick)
    {
        if(serverNick.length() == 30)
        {
            this.mediumHash = serverNick;
            this.shortHash = serverNick.substring(16);
            userDisplay = "User#" + mediumHash.substring(2,7);
        }
        else if(serverNick.length() < 30)
        {
            this.shortHash = serverNick;
            userDisplay = "User#" + shortHash.substring(2,7);
        } else {
            //Got a FULL hash... Uh oh (This should never actually happen)
            this.shortHash = "MT" + serverNick.substring(14);
            this.mediumHash = "MT" + serverNick.substring(28);
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

    private WhoisCommand whoisCommand = null;

    public boolean isOnline()
    {
        if(ChatHandler.client == null) return false;

        long currentTime = System.currentTimeMillis() / 1000;
        if(currentTime > (lastOnlineCheck + 10))
        {
            if (whoisCommand == null || whoisCommand.getClient() != ChatHandler.client)
                whoisCommand = new WhoisCommand(ChatHandler.client);

            this.lastOnlineCheck = System.currentTimeMillis() / 1000;
            whoisCommand.target(getShortHash()).execute();
            whoisCommand.target(getMediumHash()).execute();
        }
        boolean flag =  (onlineShort || onlineMedium);
        if(flag && !ChatHandler.friends.containsKey(mediumHash))
        {
            ChatHandler.friends.put(mediumHash, friendName);
            Target.updateCache();
        }
        else if(!flag && ChatHandler.friends.containsKey(mediumHash))
        {
            ChatHandler.friends.remove(mediumHash);
        }
        return flag;
    }

    public void setOnlineShort(boolean onlineShort)
    {
        this.onlineShort = onlineShort;
    }

    public void setOnlineMedium(boolean onlineMedium)
    {
        this.onlineMedium = onlineMedium;
    }

    public String getCurrentIRCNick() {
        Optional<User> userOpt = ChatHandler.client.getChannel(ChatHandler.CHANNEL).get().getUser(this.getShortHash());
        if(userOpt.isPresent()) return this.getShortHash();
        return this.getMediumHash();
    }

    public boolean isPremium() {
        return premium;
    }

    public String getUserDisplay() {
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
            ArrayList<Friend> friendsList = Callbacks.getFriendsList(false);
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
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("target", playerHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/profile", sendStr, true, true);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
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
        isLoadingProfile = false;
        return false;
    }
}
