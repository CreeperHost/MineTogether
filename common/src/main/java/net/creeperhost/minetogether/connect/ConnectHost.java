package net.creeperhost.minetogether.connect;

import java.security.PublicKey;

/**
 * Created by covers1624 on 27/4/23.
 */
public record ConnectHost(
        String httpScheme,
        String address,
        int httpPort,
        int proxyPort,
        PublicKey publicKey
) {

    public String httpUrl() {
        return httpScheme + "://" + address + ":" + httpPort;
    }
}
