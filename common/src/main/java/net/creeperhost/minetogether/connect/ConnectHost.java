package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.connect.lib.util.RSAUtils;
import net.creeperhost.minetogether.connect.lib.web.GetConnectServersRequest;

import java.security.PublicKey;

/**
 * Created by covers1624 on 27/4/23.
 */
public record ConnectHost(String address, int proxyPort, PublicKey publicKey) {

    public ConnectHost(GetConnectServersRequest.ConnectServer node) {
        this(node.address, node.port, RSAUtils.loadRSAPublicKey(RSAUtils.loadPem(node.publicKey)));
    }
}
