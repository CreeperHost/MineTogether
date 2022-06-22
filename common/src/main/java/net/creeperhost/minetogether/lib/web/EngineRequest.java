package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Map;

/**
 * Builds a request executable by a specific {@link WebEngine}.
 * <p>
 * {@link #method(String, WebBody)} MUST be called before any other function.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public interface EngineRequest {

    /**
     * Sets the HTTP method and {@link WebBody} for this request.
     *
     * @param method The HTTP method.
     * @param body   The {@link WebBody}.
     * @return The same {@link EngineRequest}.
     */
    EngineRequest method(String method, @Nullable WebBody body);

    /**
     * Set the {@link URL} for this request.
     *
     * @param url The {@link URL}.
     * @return The same {@link EngineRequest}.
     */
    default EngineRequest url(URL url) {
        return url(url.toString());
    }

    /**
     * Set the url for this request.
     *
     * @param url The url.
     * @return The same {@link EngineRequest}.
     */
    EngineRequest url(String url);

    /**
     * Add a specific HTTP header to this request.
     *
     * @param key   The key.
     * @param value The value.
     * @return The same {@link EngineRequest}.
     */
    EngineRequest header(String key, String value);

    /**
     * Add all the specified HTTP headers to this request.
     *
     * @param headers The headers to add.
     * @return The same {@link EngineRequest}.
     */
    EngineRequest headers(Map<String, String> headers);

    /**
     * Add all the specified HTTP headers to this request.
     *
     * @param headers The headers to add.
     * @return The same {@link EngineRequest}.
     */
    EngineRequest headers(HeaderList headers);

    /**
     * Removes all the specified HTTP headers with the given key.
     *
     * @param key The key.
     * @return The same {@link EngineRequest}.
     */
    EngineRequest removeHeader(String key);

    String getUrl();

    /**
     * Get all the headers for this request.
     *
     * @return A {@link HeaderList} collection representing all the headers.
     */
    HeaderList getHeaders();
}
