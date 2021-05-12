package net.creeperhost.minetogetherlib.chat;

import com.google.common.hash.Hashing;
import com.google.gson.*;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.serverlists.EnumFlag;
import net.creeperhost.minetogetherlib.serverlists.FriendStatusResponse;
import net.creeperhost.minetogetherlib.serverlists.ModPack;
import net.creeperhost.minetogetherlib.serverlists.Server;
import net.creeperhost.minetogetherlib.util.Util;
import net.creeperhost.minetogetherlib.util.WebUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ChatCallbacks
{
    private static Map<UUID, String> hashCache = new HashMap<UUID, String>();
    private static String friendCode;
    private static Util.CachedValue<ArrayList<Friend>> friendsList = null;

    public static boolean inviteFriend(Friend friend, UUID uuid, String ourServerID)
    {
        String hash = getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
            sendMap.put("target", friend.getCode());
            sendMap.put("server", String.valueOf(ourServerID));
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/invitefriend", sendStr, true, false);
        int retries = 0;
        while(resp.equals("error") && retries < 3) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/invitefriend", sendStr, true, false);
            retries++;
        }
        if(!resp.equals("error"))
        {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(resp);

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.get("status").getAsString().equals("success")) {
                    return true;
                }
            }
        }
        MineTogetherChat.logger.error("Unable to invite friend.");
        MineTogetherChat.logger.error(resp);
        return false;
    }
    
    public static String getPlayerHash(UUID uuid)
    {
        if (hashCache.containsKey(uuid)) return hashCache.get(uuid);
        
        String playerHash;
        playerHash = Hashing.sha256().hashBytes(uuid.toString().getBytes(StandardCharsets.UTF_8)).toString().toUpperCase();
        hashCache.put(uuid, playerHash);
        return playerHash;
    }

    public static Profile getProfile(UUID uuid)
    {
        String playerHash = getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("target", playerHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/profile", sendStr, true, false);
        if(resp.equalsIgnoreCase("error")) return null;
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (status.getAsString().equals("success"))
            {
                JsonObject profileData = obj.getAsJsonObject("profileData").getAsJsonObject(playerHash);
                String mediumHash = profileData.getAsJsonObject("chat").getAsJsonObject("hash").get("medium").getAsString();
                String shortHash = profileData.getAsJsonObject("chat").getAsJsonObject("hash").get("short").getAsString();
                String longHash = profileData.getAsJsonObject("hash").get("long").getAsString();

                String display = profileData.get("display").getAsString();
                boolean premium = profileData.get("premium").getAsBoolean();
                boolean isOnline = profileData.getAsJsonObject("chat").get("online").getAsBoolean();

                return new Profile(longHash, shortHash, mediumHash, isOnline, display, premium);
            } else
            {
                MineTogetherChat.logger.error(resp);
            }
        }
        return null;
    }

    private static String banMessage;

    public static boolean isBanned(UUID uuid)
    {
        String hash = getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/isbanned", sendStr, true, false);
        int retries = 0;
        while(resp.equals("error") && retries < 3)
        {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {}
            resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/isbanned", sendStr, true, false);
        }
        if(!resp.equals("error")) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(resp);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement status = obj.get("status");
                if (status.getAsString().equals("success")) {
                    JsonElement banned = obj.get("banned");
                    JsonElement ban = obj.get("ban");
                    JsonElement id = ban.getAsJsonObject().get("id");
                    JsonElement timestamp = ban.getAsJsonObject().get("timestamp");
                    JsonElement reason = ban.getAsJsonObject().get("reason");

                    banID = id.getAsString();
                    banMessage = reason.getAsString();
                    MineTogetherChat.profile.getAndUpdate(profile ->
                    {
                        profile.setBanned(banned.getAsBoolean());
                        return profile;
                    });
                    return banned.getAsBoolean();
                } else {
                    MineTogetherChat.logger.error(resp);
                }
            }
        }
        return false;
    }

    public static String banID = "";

    public static String getBanMessage(UUID uuid)
    {
        if(banMessage == null) isBanned(uuid);
        if(banMessage != null) return banMessage;
        return "";
    }
    
    public static String getFriendCode(UUID uuid)
    {
        if (friendCode != null)
            return friendCode;
        
        String hash = getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/friendCode", sendStr, true, false);
        int retries = 0;
        while(resp.equals("error") && retries < 3)
        {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {}
            retries++;
            resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/friendCode", sendStr, true, false);
        }
        if(!resp.equals("error")) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(resp);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement status = obj.get("status");
                if (status.getAsString().equals("success")) {
                    friendCode = obj.get("code").getAsString();
                } else {
                    MineTogetherChat.logger.error("Unable to get friendcode.");
                    MineTogetherChat.logger.error(resp);
                }
            }
            return friendCode;
        }
        return null;
    }
    
    public static FriendStatusResponse addFriend(String code, String display, UUID uuid)
    {
        String hash = getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
            sendMap.put("target", code);
            sendMap.put("display", display);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/requestfriend", sendStr, true, false);
        int retries = 0;
        while(resp.equals("error") && retries < 3)
        {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {}
            retries++;
            resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/requestfriend", sendStr, true, false);
        }
        if(!resp.equals("error")) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(resp);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement status = obj.get("status");
                JsonElement message = obj.get("message");

                FriendStatusResponse friendStatusResponse = new FriendStatusResponse(status.getAsString().equalsIgnoreCase("success"), message.getAsString(), "");
                if (!status.getAsString().equals("success")) {
                    if (!message.getAsString().equalsIgnoreCase("Friend request already pending.")) {
                        String friendHash = obj.get("hash").getAsString();
                        friendStatusResponse.setHash(friendHash);
                        MineTogetherChat.logger.error("Unable to add friend.");
                        MineTogetherChat.logger.error(resp);
                        return friendStatusResponse;
                    }
                    return null;
                }
            }
        }
        return null;
    }
    
    public static boolean removeFriend(String friendHash, UUID uuid)
    {
        String hash = getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<>();
        {
            sendMap.put("hash", hash);
            sendMap.put("target", friendHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/removefriend", sendStr, true, false);
        int retries = 0;
        while(resp.equals("error") && retries < 3)
        {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {}
            retries++;
            resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/removefriend", sendStr, true, false);
        }
        if(!resp.equals("error")) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(resp);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement status = obj.get("status");
                JsonElement message = obj.get("message");
                if (!status.getAsString().equals("success") && !message.getAsString().equalsIgnoreCase("Friend does not exist.")) {
                    MineTogetherChat.logger.error("Unable to remove friend: " + message);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    static boolean friendsGetting;
    public static CompletableFuture friendFuture;
    private static int friendFailures = 0;

    public static ArrayList<Friend> getFriendsList(boolean force, UUID uuid)
    {
        if (friendsList == null)
            friendsList = new Util.CachedValue<>(30000, new Util.CachedValue.ICacheCallback<ArrayList<Friend>>() {
                @Override
                public ArrayList<Friend> get(Object... args) {
                    if (friendsGetting) {
                        if (friendsList.getCachedValue(args) != null)
                            return friendsList.getCachedValue(args); // prevent NPE if it is called twice the first time somehow, would rather just make two calls
                    }
                    friendsGetting = true;
                    if(friendFuture != null && !friendFuture.isDone())
                    {
                        if(friendFailures > 3) {
                            friendFuture.cancel(true);
                            friendFailures = 0;
                        } else {
                            friendFailures++;
                        }
                    } else {
                        friendFuture = CompletableFuture.runAsync(() -> {
                            Map<String, String> sendMap = new HashMap<String, String>();
                            {
                                sendMap.put("hash", getPlayerHash(uuid));
                            }
                            String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/listfriend", new Gson().toJson(sendMap), true, true);

                            ArrayList<Friend> tempArr = new ArrayList<Friend>();

                            // no idea how this can return null, but apparently it can, so this will fix it.
                            if (resp.equals("error")) {
                                if (friendsList.getCachedValue(args) == null) {
                                    friendsList.set(tempArr);
                                }
                                return;
                            }

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
                                        Profile profile = ChatHandler.knownUsers.findByHash(code);
                                        if (profile == null) profile = ChatHandler.knownUsers.add(code);
                                        try
                                        {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) { e.printStackTrace(); }

                                        CompletableFuture.runAsync(() -> {
                                            Profile profile1 = ChatHandler.knownUsers.findByHash(code);
                                            profile1.loadProfile();
                                            ChatHandler.knownUsers.update(profile1);
                                        }, MineTogetherChat.profileExecutor);

                                        tempArr.add(new Friend(name, code, accepted));
                                    }
                                } else {
                                    tempArr = friendsList.getCachedValue(args);
                                }
                            }
                            friendsList.set(tempArr);
                            friendsGetting = false;
                        }, MineTogetherChat.profileExecutor);
                    }
                    return friendsList.getCachedValue(args);
                }

                @Override
                public boolean needsRefresh(Object... args) {
                    return args.length > 0 && args[0].equals(true);
                }
            });
        return friendsList.get(force);
    }

    public static Server getServer(int id) {
        Map<String, String> jsonPass = new HashMap<String, String>();
        jsonPass.put("serverid", String.valueOf(id));
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonPass);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/server", jsonString, true, false);
        int retries = 0;
        while(resp.equals("error") && retries < 3)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            retries++;
            resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/server", jsonString, true, false);
        }
        if(!resp.equals("error")) {
            JsonElement jElement = new JsonParser().parse(resp);
            if (jElement.isJsonObject()) {
                JsonObject object = jElement.getAsJsonObject();
                if (object.has("status") && object.get("status").getAsString().equals("success")) {
                    JsonObject server = object.get("server").getAsJsonObject();
                    String host = server.get("ip").getAsString();
                    String name = server.get("name").getAsString();
                    String port = server.get("port").getAsString();
                    int uptime = server.get("uptime").getAsInt();
                    int players = server.get("expected_players").getAsInt();
                    EnumFlag flag = EnumFlag.UNKNOWN;

                    return new Server(name, host + ":" + port, uptime, players, flag, "", "");
                }
            }
        }
        return null;
    }

    public static Map<String, String> regionMap = new HashMap<>();

    public static Map<String, String> getRegionMap(boolean force)
    {
        if(!regionMap.isEmpty() && !force)
        {
            return regionMap;
        }

        Map<String, String> rawMap = new HashMap<String, String>();
        Map<String, String> returnMap = new HashMap<String, String>();

        try
        {
            String jsonData = WebUtils.getWebResponse("https://www.creeperhost.net/json/locations");

            Type type = new com.google.common.reflect.TypeToken<Map<String, String>>() {}.getType();
            Gson g = new Gson();
            JsonElement el = new JsonParser().parse(jsonData);
            rawMap = g.fromJson(el.getAsJsonObject().get("regionMap"), type);
        } catch (Exception e)
        {
            MineTogetherChat.logger.error("Unable to fetch server locations" + e);
        }
        for (Map.Entry<String, String> entry : rawMap.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            returnMap.put(key, value);
        }
        regionMap = returnMap;
        return returnMap;
    }

    public static Map<String, String> dataCenterMap = new HashMap<>();

    public static Map<String, String> getDataCentres(boolean force)
    {
        String url = "https://www.creeperhost.net/json/datacentre/closest";
        Map<String, String> map = new HashMap<>();

        if(!dataCenterMap.isEmpty() && !force)
        {
            return dataCenterMap;
        }

        String resp = WebUtils.getWebResponse(url);

        JsonElement jElement = new JsonParser().parse(resp);

        if (jElement.isJsonObject())
        {
            JsonArray array = jElement.getAsJsonObject().getAsJsonArray("datacentres");

            if (array != null)
            {
                for (JsonElement serverEl : array)
                {
                    JsonObject object = (JsonObject) serverEl;
                    String name = object.get("name").getAsString();
                    String distance = object.get("distance").getAsString();
                    map.put(name, distance);
                }
                dataCenterMap = map;
                return dataCenterMap;
            }
        }
        return null;
    }
    
//    public static Map<String, String> getAllServerLocations()
//    {
//        IServerHost implementation = MineTogether.instance.getImplementation();
//        if (locationCache.get(implementation) == null)
//            locationCache.put(implementation, implementation.getAllServerLocations());
//        return locationCache.get(implementation);
//    }
//
//    public static Map<String, String> getCountries()
//    {
//        return countries;
//    }
//
//    public static AvailableResult getNameAvailable(String name)
//    {
//        return MineTogether.instance.getImplementation().getNameAvailable(name);
//    }
//
//    public static String getUserCountry()
//    {
//        if (userCountry == null)
//            try
//            {
//                String freeGeoIP = WebUtils.getWebResponse("https://www.creeperhost.net/json/datacentre/closest");
//
//                JsonObject jObject = new JsonParser().parse(freeGeoIP).getAsJsonObject();
//
//                jObject = jObject.getAsJsonObject("customer");
//
//                userCountry = jObject.getAsJsonPrimitive("country").getAsString();
//            } catch (Throwable t)
//            {
//                MineTogetherChat.logger.error("Unable to get user's country automatically, assuming USA", t);
//                userCountry = "US"; // default
//            }
//        return userCountry;
//    }
//
//    public static String getRecommendedLocation()
//    {
//        return MineTogether.instance.getImplementation().getRecommendedLocation();
//    }
//
//    public static OrderSummary getSummary(Order order)
//    {
//        return MineTogether.instance.getImplementation().getSummary(order);
//    }
//
//    public static boolean doesEmailExist(final String email)
//    {
//        return MineTogether.instance.getImplementation().doesEmailExist(email);
//    }
//
//    public static String doLogin(final String email, final String password)
//    {
//        return MineTogether.instance.getImplementation().doLogin(email, password);
//    }
//
//    public static String createAccount(final Order order)
//    {
//        return MineTogether.instance.getImplementation().createAccount(order);
//    }
//
//    public static String createOrder(final Order order)
//    {
//        return MineTogether.instance.getImplementation().createOrder(order);
//    }
    
    public static String getVersionFromCurse(String curse)
    {
        if(isInteger(curse))
        {
            String resp = WebUtils.getWebResponse("https://www.creeperhost.net/json/modpacks/curseforge/" + curse, 20000);
            try
            {
                JsonElement jElement = new JsonParser().parse(resp);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("success"))
                {
                    return jObject.getAsJsonPrimitive("id").getAsString();
                } else
                {
                    return "0";
                }
            } catch (Throwable ignored) {}
        }
        return "0";
    }

    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
        }
        catch(NumberFormatException | NullPointerException e)
        {
            return false;
        }
        return true;
    }
    
    public static String getVersionFromApi(String packid)
    {
        try
        {
            String resp = WebUtils.getWebResponse("https://www.creeperhost.net/json/modpacks/modpacksch/" + packid, 20000);
            JsonElement jElement = new JsonParser().parse(resp);
            JsonObject jObject = jElement.getAsJsonObject();
            if (jObject.getAsJsonPrimitive("status").getAsString().equals("success"))
            {
                return jObject.getAsJsonPrimitive("id").getAsString();
            } else
            {
                return "";
            }
        } catch (Throwable ignored) {}
        return "";
    }

    public static List<ModPack> getModpackFromCurse(String modpack, int limit)
    {
        try {
            String url = "https://www.creeperhost.net/json/modpacks/mc/search/unique/" + modpack;

            //Return the recommended if nothing is searched
            if (modpack == null || modpack.isEmpty()) {
                url = "https://www.creeperhost.net/json/modpacks/weekly/" + limit;
            }

            String resp = WebUtils.getWebResponse(url);
            List<ModPack> modpackList = new ArrayList<>();

            JsonElement jElement = new JsonParser().parse(resp);

            if (jElement.isJsonObject()) {
                JsonObject object = jElement.getAsJsonObject().getAsJsonObject("modpacks");
                JsonArray array = object.getAsJsonArray("mc");

                if (array != null) {
                    for (JsonElement serverEl : array) {
                        if (modpack != null && modpack.isEmpty() || modpackList.size() <= limit) {
                            JsonObject server = (JsonObject) serverEl;
                            String id = server.get("id").getAsString();
                            String name = server.get("displayName").getAsString();
                            String displayVersion = server.get("displayVersion").getAsString();
                            String displayIcon = server.get("displayIcon").getAsString();

                            modpackList.add(new ModPack(id, name, displayVersion, displayIcon));
                        }
                    }
                    return modpackList;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static String getSafe(JsonObject jsonObject, String value, String defaultString)
    {
        if(jsonObject == null) return defaultString;

        if(!jsonObject.has(value)) return defaultString;

        try
        {
            return jsonObject.get(value).getAsString();
        } catch (Exception e)
        {
            return defaultString;
        }
    }

    public static int getSafe(JsonObject jsonObject, String value, int defaultInt)
    {
        if(jsonObject == null) return defaultInt;

        if(!jsonObject.has(value)) return defaultInt;

        try
        {
            return jsonObject.get(value).getAsInt();
        } catch (Exception e)
        {
            return defaultInt;
        }
    }
}