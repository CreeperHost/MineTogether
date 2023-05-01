package net.creeperhost.minetogether.connect;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by brandon3055 on 21/04/2023
 */
public final class RemoteServer {
    private final String friend;
    private final String serverToken;

    public Component status;
    public Component motd;
    public long ping;
    public boolean pinged;
    public List<Component> playerList = Collections.emptyList();
    public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
    public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
    @Nullable
    private String iconB64;

    public RemoteServer(String friend, String serverToken) {
        this.friend = friend;
        this.serverToken = serverToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteServer that = (RemoteServer) o;
        return Objects.equals(friend, that.friend) && Objects.equals(serverToken, that.serverToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(friend, serverToken);
    }

    public String friend() { return friend; }

    public String serverToken() { return serverToken; }

    @Override
    public String toString() {
        return "RemoteServer[" +
                "friend=" + friend + ", " +
                "serverToken=" + serverToken + ']';
    }

    @Nullable
    public String getIconB64() {
        return this.iconB64;
    }

    public void setIconB64(@Nullable String string) {
        this.iconB64 = string;
    }
}
