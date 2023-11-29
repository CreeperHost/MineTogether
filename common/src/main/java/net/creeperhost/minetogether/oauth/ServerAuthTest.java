package net.creeperhost.minetogether.oauth;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO, this needs a lot of work for the networking changes.
public class ServerAuthTest {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);

    private static boolean cancel = false;

    private static Connection networkManager = null;
    private static BiFunction<Boolean, String, Void> callback = null;

    public static void auth(BiFunction<Boolean, String, Void> callbackIn) {
        callback = callbackIn;
        Minecraft mc = Minecraft.getInstance();
        final String address = "mc.auth.minetogether.io";
        final int port = 25565;
        LOGGER.info("Connecting to {}, {}", address, port);
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {

            public void run() {
                InetSocketAddress inetaddress = null;

                try {
                    if (ServerAuthTest.cancel) {
                        return;
                    }

                    inetaddress = new InetSocketAddress(InetAddress.getByName(address), port);
                    networkManager = Connection.connectToServer(inetaddress, true, null);
                    networkManager.initiateServerboundPlayConnection(address, port, new NetHandlerLoginClientOurs(networkManager, mc));
                    networkManager.send(new ServerboundHelloPacket(mc.getUser().getName(), mc.getUser().getProfileId()));

                } catch (UnknownHostException unknownhostexception) {
                    if (ServerAuthTest.cancel) {
                        return;
                    }

                    LOGGER.error("Couldn't connect to server", unknownhostexception);
                    fireCallback(false, "Unknown Host");
                } catch (Exception exception) {
                    if (ServerAuthTest.cancel) {
                        return;
                    }

                    LOGGER.error("Couldn't connect to server", exception);
                    fireCallback(false, exception.getMessage());
                }
            }
        }).start();
    }

    public static void processPackets() {
        if (networkManager != null) {
            if (networkManager.isConnecting()) {
                networkManager.tick();
            } else {
                networkManager.handleDisconnection();
            }
        }
    }

    static final String regex = "code: (\\w{5})";
    static final Pattern pattern = Pattern.compile(regex);

    public static void disconnected(String reason) {
        final Matcher matcher = pattern.matcher(reason);
        if (matcher.find()) {
            String code = matcher.group(1);
            fireCallback(true, code);
        } else {
            fireCallback(false, reason);
        }
        networkManager = null;
    }

    public static void fireCallback(boolean status, String message) {
        if (callback == null) return;
        callback.apply(status, message);
        callback = null;
    }
}
