package net.creeperhost.minetogether.oauth;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerAuthTest {

    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);

    private static boolean cancel = false;

    public static String auth() {
        //TODO: Callback based?
        connect("5.181.12.90", 25565);
        return "";
    }

    static NetworkManager networkManager = null;

    private static void connect(final String ip, final int port)
    {
        Minecraft mc = Minecraft.getMinecraft();
        CreeperHost.logger.info("Connecting to {}, {}", ip, port);
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

                    inetaddress = InetAddress.getByName(ip);
                    networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
                    networkManager.setNetHandler(new NetHandlerLoginClientOurs(networkManager, mc));
                    networkManager.sendPacket(new C00Handshake(ip, port, EnumConnectionState.LOGIN, true));
                    networkManager.sendPacket(new CPacketLoginStart(mc.getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception)
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    CreeperHost.logger.error("Couldn't connect to server", unknownhostexception);
                }
                catch (Exception exception)
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    CreeperHost.logger.error("Couldn't connect to server", exception);
                    String s = exception.toString();

                    if (inetaddress != null)
                    {
                        String s1 = inetaddress + ":" + port;
                        s = s.replaceAll(s1, "");
                    }
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
                networkManager.processReceivedPackets();
            }
            else
            {
                networkManager.checkDisconnected();
            }
        }
    }

    public static void disconnected(String reason) {
        CreeperHost.logger.info(reason);
        networkManager = null;
    }
}
