package net.creeperhost.minetogether.connect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.FriendServerData;
import net.creeperhost.minetogether.connect.RemoteServer;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;


public class FriendConnectScreen extends Screen {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long NARRATION_DELAY_MS = 2000L;
    public static final Component UNKNOWN_HOST_MESSAGE = Component.translatable("disconnect.genericReason", new Object[]{Component.translatable("disconnect.unknownHost")});
    @Nullable
    volatile Connection connection;
    volatile boolean aborted;
    final Screen parent;
    private Component status = Component.translatable("connect.connecting");
    private long lastNarration = -1L;

    private FriendConnectScreen(Screen screen) {
        super(GameNarrator.NO_TITLE);
        this.parent = screen;
    }

    public static void startConnecting(Screen screen, Minecraft minecraft, RemoteServer server) {
        FriendConnectScreen connectScreen = new FriendConnectScreen(screen);
        minecraft.clearLevel();
        minecraft.prepareForMultiplayer();
        minecraft.setCurrentServer(new FriendServerData(server));
        minecraft.setScreen(connectScreen);
        connectScreen.connect(minecraft, server);
    }

    private void connect(Minecraft minecraft, RemoteServer server) {
        CompletableFuture<Optional<ProfilePublicKey.Data>> completableFuture = minecraft.getProfileKeyPairManager().preparePublicKey();
        Profile profile = ConnectHandler.getServerProfile(server);
        LOGGER.info("Connecting to MF Friend Server: {}", profile.isFriend() ? profile.getFriendName() : profile.getDisplayName());

        Thread thread = new Thread("MT Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            public void run() {
//                InetSocketAddress inetSocketAddress = null;

                try {
                    if (FriendConnectScreen.this.aborted) {
                        return;
                    }

//                    Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
//                    if (FriendConnectScreen.this.aborted) {
//                        return;
//                    }
//
//                    if (!optional.isPresent()) {
//                        minecraft.execute(() -> {
//                            minecraft.setScreen(new DisconnectedScreen(FriendConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, FriendConnectScreen.UNKNOWN_HOST_MESSAGE));
//                        });
//                        return;
//                    }

                    //Anything todo with the connection field is important.
//                    inetSocketAddress = (InetSocketAddress)optional.get();

                    JWebToken token = MineTogetherClient.getSession().get().orThrow();
                    FriendConnectScreen.this.connection = NettyClient.connect("http://localhost", 32437, token, server.serverToken());


//                    FriendConnectScreen.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
//                    FriendConnectScreen.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
                    FriendConnectScreen.this.connection.setListener(new ClientHandshakePacketListenerImpl(FriendConnectScreen.this.connection, minecraft, FriendConnectScreen.this.parent, FriendConnectScreen.this::updateStatus)); //<
                    FriendConnectScreen.this.connection.send(new ClientIntentionPacket("http://localhost", 32437, ConnectionProtocol.LOGIN)); //<
                    FriendConnectScreen.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), completableFuture.join(), Optional.ofNullable(minecraft.getUser().getProfileId()))); //<
                } catch (Exception var6) {
                    if (FriendConnectScreen.this.aborted) {
                        return;
                    }

//                    Throwable var5 = var6.getCause();
//                    Exception exception3;
//                    if (var5 instanceof Exception exception2) {
//                        exception3 = exception2;
//                    } else {
//                        exception3 = var6;
//                    }

                    FriendConnectScreen.LOGGER.error("Couldn't connect to server", var6);

                    String string = "";//TODO inetSocketAddress == null ? exception3.getMessage() : exception3.getMessage().replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "").replaceAll(inetSocketAddress.toString(), "");
                    minecraft.execute(() -> {
                        minecraft.setScreen(new DisconnectedScreen(FriendConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", new Object[]{string})));
                    });
                }

            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    private void updateStatus(Component component) {
        this.status = component;
    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick(); //<
            } else {
                this.connection.handleDisconnection();
            }
        }

    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, (button) -> {
            this.aborted = true;
            if (this.connection != null) {
                this.connection.disconnect(Component.translatable("connect.aborted")); //<
            }

            this.minecraft.setScreen(this.parent);
        }));
    }

    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        long l = Util.getMillis();
        if (l - this.lastNarration > 2000L) {
            this.lastNarration = l;
            this.minecraft.getNarrator().sayNow(Component.translatable("narrator.joining"));
        }

        drawCenteredString(poseStack, this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(poseStack, i, j, f);
    }
}
