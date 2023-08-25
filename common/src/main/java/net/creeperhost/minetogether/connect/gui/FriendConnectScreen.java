package net.creeperhost.minetogether.connect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.ConnectHost;
import net.creeperhost.minetogether.connect.FriendServerData;
import net.creeperhost.minetogether.connect.RemoteServer;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class FriendConnectScreen extends ConnectScreen {

    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private volatile boolean aborted;
    private final Screen parent;
    private Component status = new TranslatableComponent("connect.connecting");

    private FriendConnectScreen(Screen screen) {
        super(screen, Minecraft.getInstance(), null, 0);
        parent = screen;
    }

    public static void startConnecting(Screen screen, Minecraft minecraft, RemoteServer server) {
        FriendConnectScreen connectScreen = new FriendConnectScreen(screen);
//        minecraft.clearLevel(); // Handled by parent constructor in 1.16
        minecraft.setCurrentServer(new FriendServerData(server));
        minecraft.setScreen(connectScreen);
        connectScreen.connect(minecraft, server);
    }

    private void connect(Minecraft minecraft, RemoteServer server) {
        Profile profile = ConnectHandler.getServerProfile(server);
        if (profile != null) {
            LOGGER.info("Connecting to MF Friend Server: {}", profile.isFriend() ? profile.getFriendName() : profile.getDisplayName());
        }

        Thread thread = new Thread("MT Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            public void run() {
                try {
                    if (aborted) {
                        return;
                    }

                    ConnectHost endpoint = ConnectHandler.getSpecificEndpoint(server.node);
                    JWebToken token = MineTogetherSession.getDefault().getTokenAsync().get();
                    connection = NettyClient.connect(endpoint, token, server.serverToken);

                    connection.setListener(new ClientHandshakePacketListenerImpl(connection, minecraft, parent, FriendConnectScreen.this::updateStatus));
                    connection.send(new ClientIntentionPacket(endpoint.address(), endpoint.proxyPort(), ConnectionProtocol.LOGIN));
                    connection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
                } catch (Exception ex) {
                    if (aborted) {
                        return;
                    }
                    FriendConnectScreen.LOGGER.error("Couldn't connect to server", ex);

                    Exception loggedException = ex.getCause() instanceof Exception ? (Exception) ex.getCause() : ex;

                    String string = loggedException.getMessage();
                    minecraft.execute(() -> {
                        minecraft.setScreen(new DisconnectedScreen(parent, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", string)));
                    });
                }

            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    protected void connect(String string, int i) {
        // Do nothing, we have our own function.
    }

    private void updateStatus(Component component) {
        status = component;
    }

    public void tick() {
        if (connection != null) {
            if (connection.isConnected()) {
                connection.tick();
            } else {
                connection.handleDisconnection();
            }
        }

    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        addButton(new Button(width / 2 - 100, height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, (button) -> {
            aborted = true;
            if (connection != null) {
                connection.disconnect(new TranslatableComponent("connect.aborted"));
            }

            minecraft.setScreen(parent);
        }));
    }

    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack);
        drawCenteredString(poseStack, font, status, width / 2, height / 2 - 50, 16777215);
        super.render(poseStack, i, j, f);
    }
}

