package net.creeperhost.minetogether.oauth;

import net.creeperhost.minetogether.MineTogether;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerAuthTest {

    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);

    private static boolean cancel = false;

    private static NetworkManager networkManager = null;
    private static BiFunction<Boolean, String, Void> callback = null;

    public static void auth(BiFunction<Boolean, String, Void> callbackIn) {
        callback = callbackIn;
        //TODO: Callback based?
        Minecraft mc = Minecraft.getInstance();
        final String address = "mc.auth.minetogether.io";
        final int port = 25565;
        MineTogether.logger.info("Connecting to {}, {}", address, port);
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet())
        {

            public void run()
            {
                InetAddress inetaddress = null;

                try
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    inetaddress = InetAddress.getByName(address);
                    networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
                    networkManager.setNetHandler(new NetHandlerLoginClientOurs(networkManager, mc));
                    networkManager.sendPacket(new CHandshakePacket(address, port, ProtocolType.LOGIN));
                    networkManager.sendPacket(new CLoginStartPacket(mc.getSession().getProfile()));
                }                catch (UnknownHostException unknownhostexception)
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    MineTogether.logger.error("Couldn't connect to server", unknownhostexception);
                    fireCallback(false, "Unknown Host");
                }
                catch (Exception exception)
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    MineTogether.logger.error("Couldn't connect to server", exception);
                    fireCallback(false, exception.getMessage());
                }
            }
        }).start();
    }

    public static void processPackets()
    {
        if (networkManager != null)
        {
            if (networkManager.isChannelOpen())
            {
                networkManager.tick();
            }
            else
            {
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
