package net.creeperhost.minetogether.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.common.WebUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUtil
{
    private static List<String> cookies;
    private static boolean logHide;

    public static IRCServer getIRCServerDetails()
    {
        String resp = WebUtils.getWebResponse("https://api.creeper.host/serverlist/chatserver");

        if (resp.equals("error"))
        {
            return new IRCServer("irc.minetogether.io", 6667, false, "#public");
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
            return new IRCServer(address, port, ssl, channel);
        } else {
            return new IRCServer("irc.minetogether.io", 6667, false, "#public");
        }
    }

    private static String mapToFormString(Map<String, String> map)
    {
        StringBuilder postDataStringBuilder = new StringBuilder();

        String postDataString;

        try
        {
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                postDataStringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            postDataString = postDataStringBuilder.toString();
        }
        return postDataString;
    }

    public static String postWebResponse(String urlString, Map<String, String> postDataMap)
    {
        return postWebResponse(urlString, mapToFormString(postDataMap));
    }

    public static String methodWebResponse(String urlString, String postDataString, String method, boolean isJson, boolean silent)
    {
        try
        {
            postDataString.substring(0, postDataString.length() - 1);

            byte[] postData = postDataString.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.138 Safari/537.36 Vivaldi/1.8.770.56");
            conn.setRequestMethod(method);
            if (cookies != null)
            {
                for (String cookie : cookies)
                {
                    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            conn.setRequestProperty("Content-Type", isJson ? "application/json" : "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setConnectTimeout(5000);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            try
            {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(postData);
            }
            catch (Throwable t)
            {
                if (!silent)
                {
                    t.printStackTrace();
                }
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder respData = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                respData.append(line);
            }

            List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

            if (setCookies != null)
            {
                cookies = setCookies;
            }

            rd.close();
            logHide = false;
            return respData.toString();
        }
        catch (Throwable t)
        {
            if (silent || logHide)
            {
                return "error";
            }
            logHide = true;
            System.out.println("An error occurred while fetching " + urlString + ". Will hide repeated errors. " + t.getMessage() + " " + t.getStackTrace());
        }

        return "error";
    }

    public static String postWebResponse(String urlString, String postDataString)
    {
        return methodWebResponse(urlString, postDataString, "POST", false, false);
    }

    public static String putWebResponse(String urlString, String body, boolean isJson, boolean isSilent)
    {
        return methodWebResponse(urlString, body, "PUT", isJson, isSilent);
    }

    public static class IRCServer
    {
        public final String address;
        public final int port;
        public final boolean ssl;
        public final String channel;

        public IRCServer(String address, int port, boolean ssl, String channel)
        {
            this.address = address;
            this.port = port;
            this.ssl = ssl;
            this.channel = channel;
        }
    }
}
