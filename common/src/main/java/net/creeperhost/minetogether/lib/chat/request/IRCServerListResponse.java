package net.creeperhost.minetogether.lib.chat.request;

import net.creeperhost.minetogether.lib.web.ApiResponse;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by covers1624 on 21/6/22.
 */
public class IRCServerListResponse extends ApiResponse {

    @Nullable
    private String channel;
    @Nullable
    private Server server;

    public String getChannel() {
        return Objects.requireNonNull(channel);
    }

    public Server getServer() {
        return Objects.requireNonNull(server);
    }

    public static class Server {

        @Nullable
        private String address;
        private int port;
        private boolean ssl;

        public String getAddress() {
            return Objects.requireNonNull(address);
        }

        public int getPort() {
            return port;
        }

        public boolean isSsl() {
            return ssl;
        }
    }
}
