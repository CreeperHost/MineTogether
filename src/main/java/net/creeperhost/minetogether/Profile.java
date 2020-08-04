package net.creeperhost.minetogether;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.common.WebUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Profile
{
    public String longHash = "";
    public String shortHash = "";
    public String mediumHash = "";
    public boolean online = false;
    public String display = "";
    public boolean premium = false;
    public String userDisplay = "";

    public Profile(String serverNick)
    {
        if(serverNick.length() == 30)
        {
            this.mediumHash = serverNick;
            userDisplay = "User" + mediumHash.substring(2,7);
        }
        else if(serverNick.length() < 30)
        {
            this.shortHash = serverNick;
            userDisplay = "User" + shortHash.substring(2,7);
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
        this.userDisplay = "User"+longHash.substring(0,5);
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

    public boolean isOnline() {
        return online;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getUserDisplay() {
        return userDisplay;
    }

    public boolean loadProfile()
    {
        String playerHash = (longHash.length() > 0) ? longHash : (mediumHash.length() > 0) ? mediumHash : shortHash;
        if(playerHash.length() == 0) return false;
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("target", playerHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/profile", sendStr, true, false);
        System.out.println(resp);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (status.getAsString().equals("success"))
            {
                JsonObject profileData = obj.getAsJsonObject("profileData").getAsJsonObject(playerHash);
                mediumHash = profileData.getAsJsonObject("chat").getAsJsonObject("hash").get("medium").getAsString();
                shortHash = profileData.getAsJsonObject("chat").getAsJsonObject("hash").get("short").getAsString();
                longHash = profileData.getAsJsonObject("hash").get("long").getAsString();

                display = profileData.get("display").getAsString();
                premium = profileData.get("premium").getAsBoolean();
                online = profileData.getAsJsonObject("chat").get("online").getAsBoolean();
                userDisplay = "User"+longHash.substring(0,5);
                if(premium && display.length() > 0)
                {
                    userDisplay = display;
                }
                return true;
            } else
            {
                //logger.error(resp);
            }
        }
        return false;
    }
}
