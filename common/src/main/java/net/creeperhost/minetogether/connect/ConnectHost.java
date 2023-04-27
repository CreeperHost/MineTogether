package net.creeperhost.minetogether.connect;

/**
 * Created by covers1624 on 27/4/23.
 */
public record ConnectHost(
        String httpScheme,
        String host,
        int httpPort,
        int proxyPort
) {

    public static final ConnectHost LOCALHOST = new ConnectHost("http", "localhost", 32436, 32437);

    public String httpUrl() {
        return httpScheme + "://" + host + ":" + httpPort;
    }

}
