package net.creeperhost.minetogether.orderform;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.lib.web.EngineRequest;
import net.creeperhost.minetogether.lib.web.EngineResponse;
import net.creeperhost.minetogether.lib.web.WebBody;
import net.creeperhost.minetogether.util.ModPackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static net.creeperhost.minetogether.MineTogether.WEB_ENGINE;

// TODO, This needs to be replaced with the new API request system.
@Deprecated
public class WebUtils {

    private static final Logger LOGGER = LogManager.getLogger();
    public static String userAgent = "";

    public static String getWebResponse(String urlString) throws IOException {
        EngineRequest request = WEB_ENGINE.newRequest()
                .method("GET", null)
                .url(urlString)
                .header("User-Agent", userAgent)
                .header("Fingerprint", MineTogether.FINGERPRINT)
                .header("Identifier", ModPackInfo.getInfo().realName);

        try (EngineResponse response = WEB_ENGINE.execute(request)) {
            WebBody entity = response.body();
            if (entity == null) {
                return "";
            }
            return IOUtils.toString(entity.open(), StandardCharsets.UTF_8);
        }
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

    public static String postWebResponse(String urlString, String postDataString) throws IOException {
        return methodWebResponse("POST", urlString, postDataString, false, false);
    }

    public static String methodWebResponse(String method, String urlString, String postDataString, boolean isJson, boolean silent) throws IOException {
        return methodWebResponse(method, urlString, postDataString, isJson ? "application/json" : "application/x-www-form-urlencoded", silent, 20000);
    }

    public static String methodWebResponse(String method, String urlString, String postDataString, String contentType, boolean silent, int timeout) throws IOException {
        EngineRequest request = WEB_ENGINE.newRequest()
                .method(method, WebBody.string(postDataString, contentType))
                .url(urlString)
                .header("User-Agent", userAgent)
                .header("Fingerprint", MineTogether.FINGERPRINT)
                .header("Identifier", ModPackInfo.getInfo().realName)
                .header("charset", "utf-8");
        try (EngineResponse response = WEB_ENGINE.execute(request)) {
            WebBody entity = response.body();
            if (entity == null) {
                return "";
            }
            return IOUtils.toString(entity.open(), StandardCharsets.UTF_8);
        }
    }
}
