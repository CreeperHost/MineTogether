package net.creeperhost.minetogether.lib.web;

import net.creeperhost.minetogether.lib.web.apache.ApacheWebEngine;

import java.io.IOException;

/**
 * Represents an abstract interface for making web requests.
 * <p>
 * Created by covers1624 on 20/6/22.
 *
 * @see ApacheWebEngine
 */
public interface WebEngine {

    /**
     * Creates a new {@link EngineRequest} builder.
     *
     * @return The new {@link EngineRequest} builder.
     */
    EngineRequest newRequest();

    /**
     * Execute the given {@link EngineRequest}.
     * <p>
     * This method blocks until the underlying HTTP implementation
     * returns a response, or throws an exception.
     *
     * @param request The {@link EngineRequest} to execute.
     * @return The {@link EngineResponse} for the request.
     * @throws IOException Thrown by the underlying HTTP implementation in the event of an error.
     */
    EngineResponse execute(EngineRequest request) throws IOException;
}
