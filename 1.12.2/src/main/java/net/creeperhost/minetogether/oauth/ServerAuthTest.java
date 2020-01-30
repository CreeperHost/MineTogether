package net.creeperhost.minetogether.oauth;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerAuthTest {

    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);

    private static boolean cancel = false;

    private static NetworkManager networkManager = null;
    private static BiFunction<Boolean, String, Void> callback = null;

    public static void auth(BiFunction<Boolean, String, Void> callbackIn) {

        if (true) {
            try {
                Main.oauth();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return;
        }
        callback = callbackIn;
        //TODO: Callback based?
        Minecraft mc = Minecraft.getMinecraft();
        final String address = "mc.auth.minetogether.io";
        final int port = 25565;
        CreeperHost.logger.info("Connecting to {}, {}", address, port);
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
                    networkManager.sendPacket(new C00Handshake(address, port, EnumConnectionState.LOGIN, true));
                    networkManager.sendPacket(new CPacketLoginStart(mc.getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception)
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    CreeperHost.logger.error("Couldn't connect to server", unknownhostexception);
                    fireCallback(false, "Unknown Host");
                }
                catch (Exception exception)
                {
                    if (ServerAuthTest.cancel)
                    {
                        return;
                    }

                    CreeperHost.logger.error("Couldn't connect to server", exception);
                    fireCallback(false, exception.getMessage());
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
