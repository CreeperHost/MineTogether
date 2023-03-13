package net.creeperhost.minetogetherconnect;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.ModPackInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LibraryHacks {

    private static final Logger LOGGER = LogManager.getLogger();
    public static String userAgent;
    private static List<String> cookies;
    private static boolean logHide;

    public static class WebUtils {

        public static String methodWebResponse(String urlString, String postDataString, String method, String contentType, boolean silent) {
            try {
                byte[] postData = postDataString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", userAgent);
                conn.setRequestProperty("Fingerprint", MineTogether.FINGERPRINT);
                conn.setRequestProperty("Identifier", URLEncoder.encode(ModPackInfo.getInfo().realName, "UTF-8"));
                conn.setRequestMethod(method);
                if (cookies != null) {
                    for (String cookie : cookies) {
                        conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                    }
                }
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setConnectTimeout(5000);
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                try {
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(postData);
                } catch (Throwable t) {
                    if (!silent) {
                        LOGGER.error(t);
                    }
                }

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
            } catch (Throwable t) {
                if (silent || logHide) {
                    return "error";
                }
                logHide = true;
            }

            return "error";
        }

        public static String putWebResponse(String urlString, String body, boolean isJson, boolean isSilent) {
            return methodWebResponse(urlString, body, "PUT", isJson, isSilent);
        }

        public static String methodWebResponse(String urlString, String postDataString, String method, boolean isJson, boolean silent) {
            return methodWebResponse(urlString, postDataString, method, isJson ? "application/json" : "application/x-www-form-urlencoded", silent);
        }
    }
}
