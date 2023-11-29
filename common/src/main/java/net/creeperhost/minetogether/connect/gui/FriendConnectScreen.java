package net.creeperhost.minetogether.connect.gui;

import com.mojang.logging.LogUtils;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.ConnectHost;
import net.creeperhost.minetogether.connect.RemoteServer;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


public class FriendConnectScreen extends ConnectScreen {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile boolean aborted;
    private final Screen parent;
    private Component status = Component.translatable("connect.connecting");
    private long lastNarration = -1L;

    private FriendConnectScreen(Screen screen) {
        super(screen, CommonComponents.CONNECT_FAILED);
        parent = screen;
    }

    public static void startConnecting(Screen screen, Minecraft minecraft, RemoteServer server, LanServer serverData) {
        FriendConnectScreen connectScreen = new FriendConnectScreen(screen);
        minecraft.disconnect();
        minecraft.prepareForMultiplayer();
        minecraft.updateReportEnvironment(ReportEnvironment.thirdParty(serverData.getAddress()));
        minecraft.quickPlayLog().setWorldData(QuickPlayLog.Type.MULTIPLAYER, serverData.getAddress(), "MT Friend Server"); //< TODO Ideally we want the world or the friend name here
        minecraft.setScreen(connectScreen);
        connectScreen.connect(minecraft, server);
    }

    private void connect(Minecraft minecraft, RemoteServer server) {
        LOGGER.info("Connecting to friend server, {}", server.friend);
        Thread thread = new Thread("MT Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            public void run() {
                try {
                    if (aborted) return;

                    synchronized (FriendConnectScreen.this) {
                        ConnectHost endpoint = ConnectHandler.getSpecificEndpoint(server.node);
                        JWebToken token = MineTogetherSession.getDefault().getTokenAsync().get();
                        connection = NettyClient.connect(endpoint, token, server.serverToken, minecraft.getDebugOverlay().getBandwidthLogger());
                        connection.initiateServerboundPlayConnection(
                                endpoint.address(),
                                endpoint.proxyPort(),                                        //TODO This v may break....
                                new ClientHandshakePacketListenerImpl(connection, minecraft, new ServerData("", "", ServerData.Type.OTHER), parent, false, (Duration) null, FriendConnectScreen.this::updateStatus)
                        );
                        connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getUser().getProfileId()));
                    }
                } catch (Exception ex) {
                    if (aborted) {
                        return;
                    }
                    FriendConnectScreen.LOGGER.error("Couldn't connect to server", ex);

                    Exception loggedException = ex.getCause() instanceof Exception e ? e : ex;


                    String string = loggedException.getMessage();
                    minecraft.execute(() -> {
                        minecraft.setScreen(new DisconnectedScreen(parent, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", string)));
                    });
                }

            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
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
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
                            aborted = true;
                            if (connection != null) {
                                connection.disconnect(Component.translatable("connect.aborted"));
                            }

                            minecraft.setScreen(parent);
                        })
                        .bounds(width / 2 - 100, height / 4 + 120 + 12, 200, 20)
                        .build()
        );
    }

    public void render(GuiGraphics graphics, int i, int j, float f) {
        renderBackground(graphics, i, j, f);
        long l = Util.getMillis();
        if (l - lastNarration > 2000L) {
            lastNarration = l;
            minecraft.getNarrator().sayNow(Component.translatable("narrator.joining"));
        }

        graphics.drawCenteredString(font, status, width / 2, height / 2 - 50, 16777215);

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, i, j, f);
        }
    }
}
