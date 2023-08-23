package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.connect.lib.util.RSAUtils;
import net.creeperhost.minetogether.connect.lib.web.GetConnectServersRequest;

import java.security.PublicKey;
import java.util.Objects;

/**
 * Created by covers1624 on 27/4/23.
 */
public final class ConnectHost {

    private final String address;
    private final int proxyPort;
    private final PublicKey publicKey;

    ConnectHost(String address, int proxyPort, PublicKey publicKey) {
        this.address = address;
        this.proxyPort = proxyPort;
        this.publicKey = publicKey;
    }

    public String address() { return address; }

    public int proxyPort() { return proxyPort; }

    public PublicKey publicKey() { return publicKey; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ConnectHost that = (ConnectHost) obj;
        return Objects.equals(this.address, that.address) &&
                this.proxyPort == that.proxyPort &&
                Objects.equals(this.publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, proxyPort, publicKey);
    }

    @Override
    public String toString() {
        return "ConnectHost[" +
                "address=" + address + ", " +
                "proxyPort=" + proxyPort + ", " +
                "publicKey=" + publicKey + ']';
    }

    public ConnectHost(GetConnectServersRequest.ConnectServer node) {
        this(node.address, node.port, RSAUtils.loadRSAPublicKey(RSAUtils.loadPem(node.publicKey)));
    }
}
