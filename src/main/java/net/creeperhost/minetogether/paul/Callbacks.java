package net.creeperhost.minetogether.paul;

import com.google.common.hash.Hashing;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.api.*;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.client.screen.serverlist.data.Invite;
import net.creeperhost.minetogether.client.screen.serverlist.data.Server;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.EnumFlag;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.data.FriendStatusResponse;
import net.creeperhost.minetogether.data.ModPack;
import net.creeperhost.minetogether.util.Util;
import net.creeperhost.minetogether.util.WebUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Callbacks
{
    private static Map<IServerHost, Map<String, String>> locationCache = new HashMap<IServerHost, Map<String, String>>();
    
    private static Util.CachedValue<List<Server>> serverListCache;
    private static Map<UUID, String> hashCache = new HashMap<UUID, String>();
    private static String friendCode;
    private static Util.CachedValue<ArrayList<Friend>> friendsList = null;
    private static String userCountry;
    private static Map<String, String> countries = new LinkedHashMap<String, String>()
    {{
        put("GB", "United Kingdom");
        put("US", "United States");
        put("NZ", "New Zealand");
        put("AU", "Australia");
        put("DK", "Denmark");
        put("CA", "Canada");
        put("SE", "Sweden");
        put("NO", "Norway");
        put("BR", "Brazil");
        put("ES", "Spain");
        put("AF", "Afghanistan");
        put("AX", "Aland Islands");
        put("AL", "Albania");
        put("DZ", "Algeria");
        put("AS", "American Samoa");
        put("AD", "Andorra");
        put("AO", "Angola");
        put("AI", "Anguilla");
        put("AQ", "Antarctica");
        put("AG", "Antigua and Barbuda");
        put("AR", "Argentina");
        put("AM", "Armenia");
        put("AW", "Aruba");
        put("AT", "Austria");
        put("AZ", "Azerbaijan");
        put("BS", "Bahamas");
        put("BH", "Bahrain");
        put("BD", "Bangladesh");
        put("BB", "Barbados");
        put("BY", "Belarus");
        put("BE", "Belgium");
        put("BZ", "Belize");
        put("BJ", "Benin");
        put("BM", "Bermuda");
        put("BT", "Bhutan");
        put("BO", "Bolivia, Plurinational State of");
        put("BQ", "Bonaire, Sint Eustatius and Saba");
        put("BA", "Bosnia and Herzegovina");
        put("BW", "Botswana");
        put("BV", "Bouvet Island");
        put("IO", "British Indian Ocean Territory");
        put("BN", "Brunei Darussalam");
        put("BG", "Bulgaria");
        put("BF", "Burkina Faso");
        put("BI", "Burundi");
        put("KH", "Cambodia");
        put("CM", "Cameroon");
        put("CV", "Cape Verde");
        put("KY", "Cayman Islands");
        put("CF", "Central African Republic");
        put("TD", "Chad");
        put("CL", "Chile");
        put("CN", "China");
        put("CX", "Christmas Island");
        put("CC", "Cocos (Keeling) Islands");
        put("CO", "Colombia");
        put("KM", "Comoros");
        put("CG", "Congo");
        put("CD", "Congo, the Democratic Republic of the");
        put("CK", "Cook Islands");
        put("CR", "Costa Rica");
        put("CI", "C�te d'Ivoire");
        put("HR", "Croatia");
        put("CU", "Cuba");
        put("CW", "Cura�ao");
        put("CY", "Cyprus");
        put("CZ", "Czech Republic");
        put("DJ", "Djibouti");
        put("DM", "Dominica");
        put("DO", "Dominican Republic");
        put("EC", "Ecuador");
        put("EG", "Egypt");
        put("SV", "El Salvador");
        put("GQ", "Equatorial Guinea");
        put("ER", "Eritrea");
        put("EE", "Estonia");
        put("ET", "Ethiopia");
        put("FK", "Falkland Islands (Malvinas)");
        put("FO", "Faroe Islands");
        put("FJ", "Fiji");
        put("FI", "Finland");
        put("FR", "France");
        put("GF", "French Guiana");
        put("PF", "French Polynesia");
        put("TF", "French Southern Territories");
        put("GA", "Gabon");
        put("GM", "Gambia");
        put("GE", "Georgia");
        put("DE", "Germany");
        put("GH", "Ghana");
        put("GI", "Gibraltar");
        put("GR", "Greece");
        put("GL", "Greenland");
        put("GD", "Grenada");
        put("GP", "Guadeloupe");
        put("GU", "Guam");
        put("GT", "Guatemala");
        put("GG", "Guernsey");
        put("GN", "Guinea");
        put("GW", "Guinea-Bissau");
        put("GY", "Guyana");
        put("HT", "Haiti");
        put("HM", "Heard Island and McDonald Islands");
        put("VA", "Holy Sea (Vatican City State)");
        put("HN", "Honduras");
        put("HK", "Hong Kong");
        put("HU", "Hungary");
        put("IS", "Iceland");
        put("IN", "India");
        put("ID", "Indonesia");
        put("IR", "Iran, Islamic Republic of");
        put("IQ", "Iraq");
        put("IE", "Ireland");
        put("IM", "Isle of Man");
        put("IL", "Israel");
        put("IT", "Italy");
        put("JM", "Jamaica");
        put("JP", "Japan");
        put("JE", "Jersey");
        put("JO", "Jordan");
        put("KZ", "Kazakhstan");
        put("KE", "Kenya");
        put("KI", "Kiribati");
        put("KP", "Korea, Democratic People's Republic of");
        put("KR", "Korea, Republic of");
        put("KW", "Kuwait");
        put("KG", "Kyrgyzstan");
        put("LA", "Lao People's Democratic Republic");
        put("LV", "Latvia");
        put("LB", "Lebanon");
        put("LS", "Lesotho");
        put("LR", "Liberia");
        put("LY", "Libya");
        put("LI", "Liechtenstein");
        put("LT", "Lithuania");
        put("LU", "Luxembourg");
        put("MO", "Macao");
        put("MK", "Macedonia, the former Yugoslav Republic of");
        put("MG", "Madagascar");
        put("MW", "Malawi");
        put("MY", "Malaysia");
        put("MV", "Maldives");
        put("ML", "Mali");
        put("MT", "Malta");
        put("MH", "Marshall Islands");
        put("MQ", "Martinique");
        put("MR", "Mauritania");
        put("MU", "Mauritius");
        put("YT", "Mayotte");
        put("MX", "Mexico");
        put("FM", "Micronesia, Federated States of");
        put("MD", "Moldova, Republic of");
        put("MC", "Monaco");
        put("MN", "Mongolia");
        put("ME", "Montenegro");
        put("MS", "Montserrat");
        put("MA", "Morocco");
        put("MZ", "Mozambique");
        put("MM", "Myanmar");
        put("NA", "Namibia");
        put("NR", "Nauru");
        put("NP", "Nepal");
        put("NL", "Netherlands");
        put("NC", "New Caledonia");
        put("NI", "Nicaragua");
        put("NE", "Niger");
        put("NG", "Nigeria");
        put("NU", "Niue");
        put("NF", "Norfolk Island");
        put("MP", "Northern Mariana Islands");
        put("OM", "Oman");
        put("PK", "Pakistan");
        put("PW", "Palau");
        put("PS", "Palestinian Territory, Occupied");
        put("PA", "Panama");
        put("PG", "Papua New Guinea");
        put("PY", "Paraguay");
        put("PE", "Peru");
        put("PH", "Philippines");
        put("PN", "Pitcairn");
        put("PL", "Poland");
        put("PT", "Portugal");
        put("PR", "Puerto Rico");
        put("QA", "Qatar");
        put("RE", "R�union");
        put("RO", "Romania");
        put("RU", "Russian Federation");
        put("RW", "Rwanda");
        put("BL", "Saint Barth�lemy");
        put("SH", "Saint Helena, Ascension and Tristan da Cunha");
        put("KN", "Saint Kitts and Nevis");
        put("LC", "Saint Lucia");
        put("MF", "Saint Martin (French part)");
        put("PM", "Saint Pierre and Miquelon");
        put("VC", "Saint Vincent and the Grenadines");
        put("WS", "Samoa");
        put("SM", "San Marino");
        put("ST", "Sao Tome and Principe");
        put("SA", "Saudi Arabia");
        put("SN", "Senegal");
        put("RS", "Serbia");
        put("SC", "Seychelles");
        put("SL", "Sierra Leone");
        put("SG", "Singapore");
        put("SX", "Sint Maarten (Dutch part)");
        put("SK", "Slovakia");
        put("SI", "Slovenia");
        put("SB", "Solomon Islands");
        put("SO", "Somalia");
        put("ZA", "South Africa");
        put("GS", "South Georgia and the South Sandwich Islands");
        put("SS", "South Sudan");
        put("LK", "Sri Lanka");
        put("SD", "Sudan");
        put("SR", "Suriname");
        put("SJ", "Svalbard and Jan Mayen");
        put("SZ", "Swaziland");
        put("CH", "Switzerland");
        put("SY", "Syrian Arab Republic");
        put("TW", "Taiwan, Province of China");
        put("TJ", "Tajikistan");
        put("TZ", "Tanzania, United Republic of");
        put("TH", "Thailand");
        put("TL", "Timor-Leste");
        put("TG", "Togo");
        put("TK", "Tokelau");
        put("TO", "Tonga");
        put("TT", "Trinidad and Tobago");
        put("TN", "Tunisia");
        put("TR", "Turkey");
        put("TM", "Turkmenistan");
        put("TC", "Turks and Caicos Islands");
        put("TV", "Tuvalu");
        put("UG", "Uganda");
        put("UA", "Ukraine");
        put("AE", "United Arab Emirates");
        put("UM", "United States Minor Outlying Islands");
        put("UY", "Uruguay");
        put("UZ", "Uzbekistan");
        put("VU", "Vanuatu");
        put("VE", "Venezuela, Bolivarian Republic of");
        put("VN", "Viet Nam");
        put("VG", "Virgin Islands, British");
        put("VI", "Virgin Islands, U.S.");
        put("WF", "Wallis and Futuna");
        put("EH", "Western Sahara");
        put("YE", "Yemen");
        put("ZM", "Zambia");
        put("ZW", "Zimbabwe");
        put("UNKNOWN", "Unknown");
    }};
    
    public static Invite getInvite()
    {
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/friendinvites", sendStr, true, true);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            if (obj.get("status").getAsString().equals("success"))
            {
                JsonArray invites = obj.getAsJsonArray("invites");
                
                for (JsonElement inviteEl : invites)
                {
                    JsonObject invite = inviteEl.getAsJsonObject();
                    JsonObject server = invite.getAsJsonObject("server");
                    String host = getSafe(server, "ip", "Unknown");//server.get("ip").getAsString();
                    int project = getSafe(server, "project", 0);//server.get("project").getAsInt();
                    String by = getSafe(server, "by", "Unknown");//invite.get("by").getAsString();
                    String name = getSafe(server, "name", "Unknown");//server.get("name").getAsString();
                    String port = getSafe(server, "port", "Unknown");//server.get("port").getAsString();
                    String country = "UNK";
                    String subdivision = "Unknown";
                    if (server.has("location"))
                    {
                        JsonObject el = server.getAsJsonObject("location");
                        country = getSafe(server, "country_code", "UNK");//el.get("country_code").getAsString();
                        subdivision = getSafe(server, "subdivision", "Unknown");//el.get("subdivision").getAsString();
                    }
                    country = country.toUpperCase();
                    EnumFlag flag = null;
                    if (!country.isEmpty())
                    {
                        try
                        {
                            flag = EnumFlag.valueOf(country);
                        } catch (IllegalArgumentException ignored)
                        {
                            flag = EnumFlag.UNKNOWN;
                        }
                    }
                    
                    int uptime = getSafe(server, "uptime", 0);//server.get("uptime").getAsInt();
                    int players = getSafe(server, "expected_players", 0);//server.get("expected_players").getAsInt();
                    
                    String applicationURL = server.has("applicationUrl") ? server.get("applictionUrl").getAsString() : null;
                    
                    Server serverEl = new Server(name, host + ":" + port, uptime, players, flag, subdivision, applicationURL);
                    return new Invite(serverEl, project, by);
                }
            }
        }
        return null;
    }
    
    public static boolean isBanned()
    {
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/isbanned", sendStr, true, true);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (status.getAsString().equals("success"))
            {
                try
                {
                    JsonElement banned = obj.get("banned");
                    return banned.getAsBoolean();
                } catch (Exception e)
                {
                    return false;
                }
            } else
            {
                MineTogether.logger.error(resp);
            }
        }
        return false;
    }
    
    public static String banID = "";
    
    public static String getBanMessage()
    {
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/isbanned", sendStr, true, false);
        
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (status.getAsString().equals("success"))
            {
                if(obj.has("ban"))
                {
                    JsonElement ban = obj.get("ban");
                    JsonElement id = ban.getAsJsonObject().get("id");
                    JsonElement timestamp = ban.getAsJsonObject().get("timestamp");
                    JsonElement reason = ban.getAsJsonObject().get("reason");

                    banID = id.getAsString();

                    return reason.getAsString() + " " + timestamp.getAsString();
                }
            } else
            {
                MineTogether.logger.error(resp);
            }
        }
        return "";
    }
    
    public static boolean inviteFriend(Friend friend)
    {
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
            sendMap.put("target", friend.getCode());
            sendMap.put("server", String.valueOf(MineTogether.instance.curServerId));
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/invitefriend", sendStr, true, false);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            
            if (obj.get("status").getAsString().equals("success"))
            {
                return true;
            }
        }
        MineTogether.logger.error("Unable to invite friend.");
        MineTogether.logger.error(resp);
        return false;
    }
    
    public static String getPlayerHash(UUID uuid)
    {
        if (hashCache.containsKey(uuid))
            return hashCache.get(uuid);
        
        String playerHash;
        //noinspection UnstableApiUsage
        playerHash = Hashing.sha256().hashBytes(uuid.toString().getBytes(StandardCharsets.UTF_8)).toString().toUpperCase();
        hashCache.put(uuid, playerHash);
        return playerHash;
    }


    public static Profile getProfile()
    {
        String playerHash = getPlayerHash(MineTogether.UUID.get());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("target", playerHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/profile", sendStr, true, false);
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
                MineTogether.logger.error(resp);
            }
        }
        return null;
    }
    
    public static String getFriendCode()
    {
        if (friendCode != null)
            return friendCode;
        
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/friendCode", sendStr, true, false);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (status.getAsString().equals("success"))
            {
                friendCode = obj.get("code").getAsString();
            } else
            {
                MineTogether.logger.error("Unable to get friendcode.");
                MineTogether.logger.error(resp);
            }
        }
        return friendCode;
    }
    
    public static FriendStatusResponse addFriend(String code, String display)
    {
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
            sendMap.put("target", code);
            sendMap.put("display", display);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/requestfriend", sendStr, true, false);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            JsonElement message = obj.get("message");

            FriendStatusResponse friendStatusResponse = new FriendStatusResponse(status.getAsString().equalsIgnoreCase("success"), message.getAsString(), "");
            if (!status.getAsString().equals("success"))
            {
                MineTogether.logger.error("Unable to add friend.");
                MineTogether.logger.error(resp);
                return friendStatusResponse;
            }
        }
        return null;
    }
    
    public static boolean removeFriend(String friendHash)
    {
        String hash = getPlayerHash(MineTogether.proxy.getUUID());
        Map<String, String> sendMap = new HashMap<>();
        {
            sendMap.put("hash", hash);
            sendMap.put("target", friendHash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/removefriend", sendStr, true, false);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            JsonElement status = obj.get("status");
            if (!status.getAsString().equals("success"))
            {
                MineTogether.logger.error("Unable to remove friend.");
                MineTogether.logger.error(resp);
                return false;
            }
        }
        return true;
    }
    
    public static ArrayList<Minigame> getMinigames(boolean isModded)
    {
        
        Map<String, String> sendMap = new HashMap<>();
        {
            sendMap.put("mc", Util.getMinecraftVersion());
            sendMap.put("project", isModded ? Config.getInstance().curseProjectID : String.valueOf(0));
        }
        
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/mgtemplates", new Gson().toJson(sendMap), true, false);
        
        JsonParser parser = new JsonParser();
        
        Gson gson = new Gson();
        
        JsonElement parse = parser.parse(resp);
        if (parse.isJsonObject())
        {
            JsonObject obj = parse.getAsJsonObject();
            if (obj.get("status").getAsString().equals("success"))
            {
                return gson.fromJson(obj.get("templates"), new TypeToken<List<Minigame>>()
                {
                }.getType());
            }
        }
        
        return null;
    }
    
    static boolean friendsGetting;
    
    public static ArrayList<Friend> getFriendsList(boolean force)
    {
        if (friendsList == null)
            friendsList = new Util.CachedValue<ArrayList<Friend>>(60000, new Util.CachedValue.ICacheCallback<ArrayList<Friend>>()
            {
                @Override
                public ArrayList<Friend> get(Object... args)
                {
                    if (friendsGetting)
                    {
                        if (friendsList.getCachedValue(args) != null)
                            return friendsList.getCachedValue(args); // prevent NPE if it is called twice the first time somehow, would rather just make two calls
                    }
                    friendsGetting = true;
                    Map<String, String> sendMap = new HashMap<String, String>();
                    {
                        sendMap.put("hash", getPlayerHash(MineTogether.proxy.getUUID()));
                    }
                    
                    String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/listfriend", new Gson().toJson(sendMap), true, true);
                    
                    ArrayList<Friend> tempArr = new ArrayList<Friend>();
                    
                    // no idea how this can return null, but apparently it can, so this will fix it.
                    if (resp.equals("error"))
                    {
                        return tempArr;
                    }
                    
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
                                Profile profile = ChatHandler.knownUsers.findByHash(code);
                                tempArr.add(new Friend(profile, name, code, accepted));
                            }
                        }
                    }
                    friendsGetting = false;
                    return tempArr;
                }
                
                @Override
                public boolean needsRefresh(Object... args)
                {
                    return args.length > 0 && args[0].equals(true);
                }
            });
        return friendsList.get(force);
    }
    
    public static List<Server> getServerList(Enum listType)
    {
        if (serverListCache == null)
        {
            serverListCache = new Util.CachedValue<>(30000, new Util.CachedValue.ICacheCallback<List<Server>>()
            {
                private Enum lastRequest;
                private String playerHash;
                
                @Override
                public List<Server> get(Object... args)
                {
                    Enum listType = (Enum) args[0];
                    int enumOrdinal = listType.ordinal();
                    lastRequest = listType;
                    MineTogether.logger.info("Loading " + (listType.name().toLowerCase()) + " server list.");
                    List<Server> list = new ArrayList<Server>();
                    
                    Config defaultConfig = new Config();
                    if (defaultConfig.curseProjectID.equals(Config.getInstance().curseProjectID))
                    {
                        list.add(new Server("No project ID! Please fix the MineTogether config or ensure a version.json exists.", "127.0.0.1:25565", 0, 0, null, "Unknown", null));
                        return list;
                    }
                    
                    Map<String, String> jsonPass = new HashMap<String, String>();
                    jsonPass.put("projectid", MineTogether.instance.base64 == null ? Config.getInstance().curseProjectID : MineTogether.instance.base64);
                    if (enumOrdinal == 1)
                    {
                        if (playerHash == null)
                        {
                            playerHash = getPlayerHash(MineTogether.proxy.getUUID());
                        }
                        
                        jsonPass.put("hash", playerHash);
                    }
                    
                    jsonPass.put("listType", listType.name().toLowerCase());
                    
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(jsonPass);
                    
                    String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/list", jsonString, true, false);
                    MineTogether.logger.info(jsonString);
                    MineTogether.logger.info(resp);
                    
                    JsonElement jElement = new JsonParser().parse(resp);
                    if (jElement.isJsonObject())
                    {
                        JsonObject object = jElement.getAsJsonObject();
                        JsonArray array = object.getAsJsonArray("servers");
                        if (array != null)
                        {
                            for (JsonElement serverEl : array)
                            {
                                JsonObject server = (JsonObject) serverEl;
                                String name = getSafe(server, "name", "unknown");//server.get("name").getAsString();
                                String host = getSafe(server, "ip", "unknown");//server.get("ip").getAsString();
                                String port = getSafe(server, "port", "unknown");//server.get("port").getAsString();
                                String country = "UNK";
                                String subdivision = "Unknown";
                                if (server.has("location"))
                                {
                                    JsonObject el = server.getAsJsonObject("location");
                                    country = getSafe(el, "country_code", "UNK");//el.get("country_code").getAsString();
                                    subdivision = getSafe(el, "subdivision", "Unknown");//el.get("subdivision").getAsString();
                                }
                                country = country.toUpperCase();
                                EnumFlag flag = null;
                                if (!country.isEmpty())
                                {
                                    try
                                    {
                                        flag = EnumFlag.valueOf(country);
                                    } catch (IllegalArgumentException ignored)
                                    {
                                        flag = EnumFlag.UNKNOWN;
                                    }
                                }
                                
                                int uptime = getSafe(server, "uptime", 0);//server.get("uptime").getAsInt();
                                int players = getSafe(server, "expected_players", 0);//server.get("expected_players").getAsInt();
                                
                                String applicationURL = server.has("applicationUrl") ? server.get("applictionUrl").getAsString() : null;
                                
                                list.add(new Server(name, host + ":" + port, uptime, players, flag, subdivision, applicationURL));
                            }
                        }
                    }
                    return list;
                }
                
                @Override
                public boolean needsRefresh(Object... args)
                {
                    Enum listType = (Enum) args[0];
                    return listType != lastRequest;
                }
            });
        }
        return serverListCache.get(listType);
    }


    public static Server getServer(int id) {
        Map<String, String> jsonPass = new HashMap<String, String>();
        jsonPass.put("serverid", String.valueOf(id));
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonPass);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/server", jsonString, true, false);
        JsonElement jElement = new JsonParser().parse(resp);
        if (jElement.isJsonObject())
        {
            JsonObject object = jElement.getAsJsonObject();
            if(object.has("status") && object.get("status").getAsString().equals("success"))
            {
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
        return null;
    }
    
    public static Map<String, String> getAllServerLocations()
    {
        IServerHost implementation = MineTogether.instance.getImplementation();
        if (locationCache.get(implementation) == null)
            locationCache.put(implementation, implementation.getAllServerLocations());
        return locationCache.get(implementation);
    }
    
    public static Map<String, String> getCountries()
    {
        return countries;
    }
    
    public static AvailableResult getNameAvailable(String name)
    {
        return MineTogether.instance.getImplementation().getNameAvailable(name);
    }
    
    public static String getUserCountry()
    {
        if (userCountry == null)
            try
            {
                String freeGeoIP = WebUtils.getWebResponse("https://www.creeperhost.net/json/datacentre/closest");

                JsonObject jObject = new JsonParser().parse(freeGeoIP).getAsJsonObject();

                jObject = jObject.getAsJsonObject("customer");

                userCountry = jObject.getAsJsonPrimitive("country").getAsString();
            } catch (Throwable t)
            {
                MineTogether.logger.error("Unable to get user's country automatically, assuming USA", t);
                userCountry = "US"; // default
            }
        return userCountry;
    }
    
    public static String getRecommendedLocation()
    {
        return MineTogether.instance.getImplementation().getRecommendedLocation();
    }
    
    public static OrderSummary getSummary(Order order)
    {
        return MineTogether.instance.getImplementation().getSummary(order);
    }
    
    public static boolean doesEmailExist(final String email)
    {
        return MineTogether.instance.getImplementation().doesEmailExist(email);
    }
    
    public static String doLogin(final String email, final String password)
    {
        return MineTogether.instance.getImplementation().doLogin(email, password);
    }
    
    public static String createAccount(final Order order)
    {
        return MineTogether.instance.getImplementation().createAccount(order);
    }
    
    public static String createOrder(final Order order)
    {
        return MineTogether.instance.getImplementation().createOrder(order);
    }

    public static String getVersionFromCurse(String curse)
    {
        if (isInteger(curse))
        {
            try
            {
                String resp = WebUtils.getWebResponse("https://www.creeperhost.net/json/modpacks/curseforge/" + curse);

                JsonElement jElement = new JsonParser().parse(resp);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("success"))
                {
                    return jObject.getAsJsonPrimitive("id").getAsString();
                } else
                {
                    return "0";
                }
            } catch (Throwable ignored)
            {
            }
        }
        return "0";
    }

    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e)
        {
            return false;
        }
        return true;
    }

    public static String getVersionFromApi(String packid)
    {
        try
        {
            String resp = WebUtils.getWebResponse("https://www.creeperhost.net/json/modpacks/modpacksch/" + packid);

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
        String url = "https://www.creeperhost.net/json/modpacks/mc/search/" + modpack;
        
        //Return the recommended if nothing is searched
        if (modpack == null || modpack.isEmpty())
        {
            url = "https://www.creeperhost.net/json/modpacks/weekly/" + limit;
        }
        
        String resp = WebUtils.getWebResponse(url);
        List<ModPack> modpackList = new ArrayList<>();
        
        JsonElement jElement = new JsonParser().parse(resp);
        
        if (jElement.isJsonObject())
        {
            try
            {
                JsonObject object = jElement.getAsJsonObject().getAsJsonObject("modpacks");
                JsonArray array = object.getAsJsonArray("mc");

                if (array != null)
                {
                    for (JsonElement serverEl : array)
                    {
                        if (modpackList.isEmpty() || modpackList.size() <= limit)
                        {
                            JsonObject server = (JsonObject) serverEl;
                            String id = getSafe(server, "id", "failed to get id");
                            String name = getSafe(server, "displayName", "failed to load displayName");
                            String displayVersion = getSafe(server, "displayVersion", "failed to load displayVersion");
                            String displayIcon = getSafe(server, "displayIcon", null);

                            modpackList.add(new ModPack(id, name, displayVersion, displayIcon));
                        }
                    }
                    return modpackList;
                }
            } catch (Exception e)
            {
                return null;
            }
        }
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
