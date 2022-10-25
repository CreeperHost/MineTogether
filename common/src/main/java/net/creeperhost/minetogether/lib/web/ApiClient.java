package net.creeperhost.minetogether.lib.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Capable of performing api requests.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public final class ApiClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final HeaderList EMPTY = new HeaderList();

    private final WebEngine engine;
    @Nullable
    private final WebAuth auth;
    private final String userAgent;

    private ApiClient(WebEngine engine, @Nullable WebAuth auth, String userAgent) {
        this.engine = engine;
        this.auth = auth;
        this.userAgent = userAgent;
    }

    /**
     * @return a new {@link ApiClientBuilder} ready for use.
     */
    public static ApiClientBuilder builder() {
        return new ApiClientBuilder();
    }

    /**
     * Execute the given {@link ApiRequest}.
     *
     * @param apiRequest The {@link ApiRequest} to execute.
     * @return The {@link ApiClientResponse}.
     * @throws IOException If an IO error occurs whilst executing the given {@link ApiRequest}.
     */
    public <R extends ApiResponse> ApiClientResponse<R> execute(ApiRequest<R> apiRequest) throws IOException {
        EngineRequest request = engine.newRequest();
        apiRequest.fillRequest(request);

        HeaderList authHeaders = auth != null ? auth.getAuthHeaders() : EMPTY;
        for (String authHeader : apiRequest.requiredAuthHeaders) {
            String value = authHeaders.get(authHeader);
            if (value == null) {
                // Could be dev env, or something is borked.
                LOGGER.warn("Missing required auth header '{}' for request '{}'", authHeader, apiRequest.url);
            } else {
                request.header(authHeader, value);
            }
        }
        if (request.getHeaders().get("User-Agent") == null) {
            request.header("User-Agent", userAgent);
        }
        try (EngineResponse response = engine.execute(request)) {
            WebBody body = response.body();
            if (body == null) {
                return new ApiClientResponse<>(response, null);
            }
            // TODO, split on semicolon and parse both the content type and encoding.
            if (!StringUtils.startsWith(body.contentType(), WebConstants.JSON)) {
                throw new UnsupportedOperationException("Response for request '" + request.getUrl() + "' returned content type '" + body.contentType() + "'. I don't know how to handle this yet.");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.open(), StandardCharsets.UTF_8))) {
                return new ApiClientResponse<>(response, apiRequest.getGson().fromJson(reader, apiRequest.responseClass));
            }
        }
    }

    public static class ApiClientBuilder {

        @Nullable
        private WebEngine engine;
        @Nullable
        private WebAuth auth;

        private final List<String> userAgentSegments = new LinkedList<>();

        private ApiClientBuilder() {
        }

        public ApiClientBuilder webEngine(WebEngine engine) {
            this.engine = engine;
            return this;
        }

        public ApiClientBuilder webAuth(WebAuth auth) {
            this.auth = auth;
            return this;
        }

        public ApiClientBuilder addUserAgentSegment(String segment) {
            userAgentSegments.add(segment);

            return this;
        }

        public ApiClient build() {
            return new ApiClient(
                    Objects.requireNonNull(engine, "WebEngine is required."),
                    auth,
                    String.join(" ", userAgentSegments)
            );
        }
    }
}
