package net.creeperhost.minetogether.serverlist.data;

import com.google.gson.annotations.SerializedName;
import net.creeperhost.minetogether.util.EnumFlag;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 25/10/22.
 */
public class Server {

    public int id;
    public String name = "Unknown";
    public boolean invite = false;
    public String project = "Unknown";
    public String ip = "Unknown";
    @SerializedName ("api_version")
    public int apiVersion = 0;
    @SerializedName ("expected_players")
    public int expectedPlayers = 0;
    public Location location = new Location();
    public boolean featured = false;
    public long uptime = 0;
    public int port = 25565;
    @Nullable
    public String applicationUrl;

    public EnumFlag getFlag() {
        try {
            return EnumFlag.valueOf(location.countryCode);
        } catch (Throwable ex) {
            return EnumFlag.UNKNOWN;
        }
    }

    public static class Location {

        public String country = "Unknown";
        @SerializedName ("country_code")
        public String countryCode = "UNK";
        public String city = "Unknown";
        public String continent = "Unknown";
        public String subdivision = "Unknown";
        @SerializedName ("location_provider")
        public String locationProvider = "Unknown";
    }
}
