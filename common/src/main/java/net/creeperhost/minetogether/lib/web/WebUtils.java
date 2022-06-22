package net.creeperhost.minetogether.lib.web;

import org.apache.commons.lang3.tuple.Pair;

import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by covers1624 on 20/6/22.
 */
public class WebUtils {

    /**
     * Checks if the provided method allows a request body.
     *
     * @param method The method to check.
     * @return If the method provided allows a request body.
     */
    public static boolean permitsRequestBody(String method) {
        return !method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("HEAD");
    }

    /**
     * Checks if the provided method requires a request body.
     *
     * @param method The method to check.
     * @return If the method provided requires a request body.
     */
    public static boolean requiresRequestBody(String method) {
        return method.equalsIgnoreCase("POST")
                || method.equalsIgnoreCase("PUT")
                || method.equalsIgnoreCase("PATCH")
                || method.equalsIgnoreCase("PROPPATCH")
                || method.equalsIgnoreCase("REPORT");
    }

    /**
     * Encode the specified {@link HeaderList} into query parameters attached to the
     * specified {@code url}.
     *
     * @param url        The url to attach parameters to.
     * @param parameters The parameters.
     * @return The url with encoded query parameters.
     */
    public static String encodeQueryParameters(String url, List<Pair<String, String>> parameters) {
        if (parameters.isEmpty()) return url;

        StringBuilder builder = new StringBuilder(url);
        builder.append("?");
        for (Pair<String, String> entry : parameters) {
            if (builder.charAt(builder.length() - 1) != '?') {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey(), UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), UTF_8));
        }
        return builder.toString();
    }
}
