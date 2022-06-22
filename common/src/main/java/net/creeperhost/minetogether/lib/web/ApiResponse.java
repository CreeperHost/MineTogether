package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by covers1624 on 20/6/22.
 */
public class ApiResponse {

    @Nullable
    private String status;
    @Nullable
    private String message;

    public String getStatus() {
        return Objects.requireNonNull(status);
    }

    @Nullable
    public String getMessageOrNull() {
        return message;
    }

    public String getMessage() {
        return Objects.requireNonNull(message);
    }
}
