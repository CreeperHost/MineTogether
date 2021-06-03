package net.creeperhost.minetogetherlib.serverlists;

import com.google.gson.*;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogetherlib.util.Util;
import net.creeperhost.minetogetherlib.util.WebUtils;

import java.util.*;

public class ServerListCallbacks
{
//    private static Map<IServerHost, Map<String, String>> locationCache = new HashMap<IServerHost, Map<String, String>>();
    private static String userCountry;
    private static Util.CachedValue<ArrayList<Friend>> friendsList = null;
    private static Util.CachedValue<List<Server>> serverListCache;

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

    public static Invite getInvite(UUID uuid)
    {
        String hash = ChatCallbacks.getPlayerHash(uuid);
        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", hash);
        }
        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/friendinvites", sendStr, true, false);
        if(!resp.equals("error"))
        {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(resp);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.get("status").getAsString().equals("success")) {
                    JsonArray invites = obj.getAsJsonArray("invites");

                    for (JsonElement inviteEl : invites) {
                        JsonObject invite = inviteEl.getAsJsonObject();
                        JsonObject server = invite.getAsJsonObject("server");
                        String host = server.get("ip").getAsString();
                        int project = server.get("project").getAsInt();
                        String by = invite.get("by").getAsString();
                        String name = server.get("name").getAsString();
                        String port = server.get("port").getAsString();
                        String country = "UNK";
                        String subdivision = "Unknown";
                        if (server.has("location")) {
                            JsonObject el = server.getAsJsonObject("location");
                            country = el.get("country_code").getAsString();
                            subdivision = el.get("subdivision").getAsString();
                        }
                        country = country.toUpperCase();
                        EnumFlag flag = null;
                        if (!country.isEmpty()) {
                            try {
                                flag = EnumFlag.valueOf(country);
                            } catch (IllegalArgumentException ignored) {
                                flag = EnumFlag.UNKNOWN;
                            }
                        }

                        int uptime = server.get("uptime").getAsInt();
                        int players = server.get("expected_players").getAsInt();

                        String applicationURL = server.has("applicationUrl") ? server.get("applictionUrl").getAsString() : null;

                        Server serverEl = new Server(name, host + ":" + port, uptime, players, flag, subdivision, applicationURL);
                        return new Invite(serverEl, project, by);
                    }
                }
            }
        }
        return null;
    }

    public static List<Server> getServerList(Enum listType, UUID uuid, String base64, String curseID)
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
                    List<Server> list = new ArrayList<Server>();

                    Config defaultConfig = new Config();
                    if (defaultConfig.curseProjectID.equals(Config.getInstance().curseProjectID))
                    {
                        list.add(new Server("No project ID! Please fix the MineTogether config or ensure a version.json exists.", "127.0.0.1:25565", 0, 0, null, "Unknown", null));
                        return list;
                    }

                    Map<String, String> jsonPass = new HashMap<String, String>();
                    jsonPass.put("projectid", base64 == null ? curseID : base64);
                    if (enumOrdinal == 1)
                    {
                        if (playerHash == null)
                        {
                            playerHash = ChatCallbacks.getPlayerHash(uuid);
                        }

                        jsonPass.put("hash", playerHash);
                    }

                    jsonPass.put("listType", listType.name().toLowerCase());

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(jsonPass);

                    String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/list", jsonString, true, false);
                    int retries = 0;
                    while(resp.equals("error") && retries < 5)
                    {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}
                        retries++;
                        resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/list", jsonString, true, false);
                    }
                    if(!resp.equals("error")) {
                        JsonElement jElement = new JsonParser().parse(resp);
                        if (jElement.isJsonObject()) {
                            JsonObject object = jElement.getAsJsonObject();
                            JsonArray array = object.getAsJsonArray("servers");
                            if (array != null) {
                                for (JsonElement serverEl : array) {
                                    JsonObject server = (JsonObject) serverEl;
                                    String name = ChatCallbacks.getSafe(server, "name", "unknown");//server.get("name").getAsString();
                                    String host = ChatCallbacks.getSafe(server, "ip", "unknown");//server.get("ip").getAsString();
                                    String port = ChatCallbacks.getSafe(server, "port", "unknown");//server.get("port").getAsString();
                                    String country = "UNK";
                                    String subdivision = "Unknown";
                                    if (server.has("location")) {
                                        JsonObject el = server.getAsJsonObject("location");
                                        country = ChatCallbacks.getSafe(el, "country_code", "UNK");//el.get("country_code").getAsString();
                                        subdivision = ChatCallbacks.getSafe(el, "subdivision", "Unknown");//el.get("subdivision").getAsString();
                                    }
                                    country = country.toUpperCase();
                                    EnumFlag flag = null;
                                    if (!country.isEmpty()) {
                                        try {
                                            flag = EnumFlag.valueOf(country);
                                        } catch (IllegalArgumentException ignored) {
                                            flag = EnumFlag.UNKNOWN;
                                        }
                                    }

                                    int uptime = ChatCallbacks.getSafe(server, "uptime", 0);//server.get("uptime").getAsInt();
                                    int players = ChatCallbacks.getSafe(server, "expected_players", 0);//server.get("expected_players").getAsInt();

                                    String applicationURL = server.has("applicationUrl") ? server.get("applictionUrl").getAsString() : null;

                                    list.add(new Server(name, host + ":" + port, uptime, players, flag, subdivision, applicationURL));
                                }
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
}
