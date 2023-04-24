package net.creeperhost.minetogether.connect.netty;

/**
 * Created by covers1624 on 24/4/23.
 */
public class PacketIds {

    // Server handled.
    public static final int S_HOST_REGISTER = 1;
    public static final int S_HOST_CONNECT = 2;
    public static final int S_USER_CONNECT = 3;

    // Client handled.
    public static final int C_DISCONNECTED = 0;
    public static final int C_ACCEPTED = 1;
}
