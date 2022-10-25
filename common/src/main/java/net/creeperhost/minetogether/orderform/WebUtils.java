package net.creeperhost.minetogether.orderform;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.ModPackInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// TODO, This needs to be replaced with the new API request system.
@Deprecated
public class WebUtils {

    private static final Logger LOGGER = LogManager.getLogger();
    public static String userAgent = "";
    private static List<String> cookies;
    private static boolean logHide;

    public static String getWebResponse(String urlString) throws IOException, URISyntaxException {
        return getWebResponse(urlString, 0, false);
    }

    public static String getWebResponse(String urlString, int timeout, boolean print) throws IOException, URISyntaxException {
        //If the request fails with domain ressolution and the domain is api.creeper.host try again using the IP address 84.54.54.84
        if (timeout == 0) timeout = 120000;
        URL url = new URL(urlString);
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        url = uri.toURL();
        // lul
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setReadTimeout(timeout);

        conn.setRequestMethod("GET");

        if (cookies != null) {
            for (String cookie : cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
            }
        }
        if (userAgent == null) userAgent = "";
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setRequestProperty("Fingerprint", MineTogether.FINGERPRINT);
        conn.setRequestProperty("Identifier", URLEncoder.encode(ModPackInfo.realName, "UTF-8"));
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder respData = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            respData.append(line);
            respData.append("\n");
        }

        List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

        if (setCookies != null) {
            cookies = setCookies;
        }

        rd.close();
        return respData.toString();
    }

    private static String mapToFormString(Map<String, String> map) {
        StringBuilder postDataStringBuilder = new StringBuilder();

        String postDataString;

        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                postDataStringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            }
        } catch (Exception ignored) {
        } finally {
            postDataString = postDataStringBuilder.toString();
        }
        return postDataString;
    }

    public static String postWebResponse(String urlString, Map<String, String> postDataMap) throws IOException {
        return postWebResponse(urlString, mapToFormString(postDataMap));
    }

    public static String methodWebResponse(String urlString, String postDataString, String method, boolean isJson, boolean silent) throws IOException {
        return methodWebResponse(urlString, postDataString, method, isJson ? "application/json" : "application/x-www-form-urlencoded", silent, 20000);
    }

    public static String methodWebResponse(String urlString, String postDataString, String method, String contentType, boolean silent, int timeout) throws IOException {
        byte[] postData = postDataString.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setRequestProperty("Fingerprint", MineTogether.FINGERPRINT);
        conn.setRequestProperty("Identifier", URLEncoder.encode(ModPackInfo.realName, "UTF-8"));
        conn.setRequestMethod(method);
        if (cookies != null) {
            for (String cookie : cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
            }
        }
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(postData);

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder respData = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            respData.append(line);
        }

        List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

        if (setCookies != null) {
            cookies = setCookies;
        }

        rd.close();
        logHide = false;
        return respData.toString();
    }

    public static String postWebResponse(String urlString, String postDataString) throws IOException {
        return methodWebResponse(urlString, postDataString, "POST", false, false);
    }

}
