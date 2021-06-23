package net.creeperhost.minetogetherlib.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogetherlib.chat.irc.IRCServer;
import net.creeperhost.minetogetherlib.util.WebUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ChatUtil
{
    private static IRCServer cachedIrcServer;
    
    public static IRCServer getIRCServerDetails()
    {
        if(cachedIrcServer != null) return cachedIrcServer;

        String resp = WebUtils.getWebResponse("https://api.creeper.host/serverlist/chatserver");
        
        if (resp.equals("error"))
        {
            MineTogetherChat.logger.error("error while attempting to get ChatServer from url");
            return getFallbackIrcServer();
        }
        JsonParser parser = new JsonParser();
        JsonObject parse = parser.parse(resp).getAsJsonObject();
        if (parse.get("status").getAsString().equals("success"))
        {
            String channel = parse.get("channel").getAsString();
            JsonObject server = parse.getAsJsonObject("server");
            String address = server.get("address").getAsString();
            int port = server.get("port").getAsInt();
            boolean ssl = server.get("ssl").getAsBoolean();
            IRCServer ircServer = new IRCServer(address, port, ssl, channel);
            cachedIrcServer = ircServer;
            return ircServer;
        } else
        {
            return getFallbackIrcServer();
        }
    }

    public static IRCServer getFallbackIrcServer()
    {
        IRCServer ircServer = new IRCServer("irc.minetogether.io", 6667, false, "#public");
        cachedIrcServer = ircServer;
        return ircServer;
    }
}
