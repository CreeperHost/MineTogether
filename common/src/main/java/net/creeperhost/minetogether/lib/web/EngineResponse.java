package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

/**
 * Represents an executed HTTP request.
 * <p>
 * Created by covers1624 on 21/6/22.
 */
public interface EngineResponse extends Closeable {

    /**
     * Gets the returned status code for the request.
     *
     * @return The status code.
     */
    int statusCode();

    /**
     * Gets the returned status message for the request.
     *
     * @return The status message.
     */
    String message();

    /**
     * Gets all the headers received for the request.
     *
     * @return The headers.
     */
    HeaderList headers();

    /**
     * Gets the response body for the request if present.
     *
     * @return The response body.
     */
    @Nullable
    WebBody body();
}
