package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by covers1624 on 21/6/22.
 */
public class ApiClientResponse<R extends ApiResponse> {

    private final EngineResponse response;
    @Nullable
    private final R apiResponse;

    public ApiClientResponse(EngineResponse response, @Nullable R apiResponse) {
        this.response = response;
        this.apiResponse = apiResponse;
    }

    public int statusCode() {
        return response.statusCode();
    }

    public String message() {
        return response.message();
    }

    public HeaderList headers() {
        return response.headers();
    }

    public R apiResponse() {
        return Objects.requireNonNull(apiResponse, "Response does not have a body.");
    }
}
