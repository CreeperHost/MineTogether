package net.creeperhost.minetogether.connect.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.creeperhost.minetogether.connect.netty.packet.*;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.network.ServerConnectionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Created by covers1624 on 24/4/23.
 */
public class NettyClient extends Thread {

    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private final IntegratedServer server;
    private final String proxyHost;
    private final int proxyPort;
    private final Supplier<Packet<ServerPacketHandler>> connectedPacketSupplier;

    public NettyClient(@Nullable IntegratedServer server, String proxyHost, int proxyPort, Supplier<Packet<ServerPacketHandler>> connectedPacketSupplier) {
        this.server = server;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.connectedPacketSupplier = connectedPacketSupplier;
        setDaemon(true);
        setName("MineTogether Connect");
    }

    @Override
    public void run() {
        EventLoopGroup eventGroup;
        Class<? extends Channel> channelClass;
        if (Epoll.isAvailable()) {
            eventGroup = ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP.get();
            channelClass = EpollSocketChannel.class;
        } else {
            eventGroup = ServerConnectionListener.SERVER_EVENT_GROUP.get();
            channelClass = NioSocketChannel.class;
        }
        ChannelFuture channelFuture = new Bootstrap()
                .group(eventGroup)
                .channel(channelClass)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(@NotNull Channel ch) throws Exception {
                        try {
                            ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException ignored) {
                        }

                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast("timeout", new ReadTimeoutHandler(120));
                        pipe.addLast("frame_codec", new FrameCodec());
                        pipe.addLast("packet_codec", new PacketCodec());
                        pipe.addLast("logging_codec", new LoggingPacketCodec(LOGGER));
                        pipe.addLast(new ChannelHandler());

                    }
                })
                .connect(proxyHost, proxyPort)
                .syncUninterruptibly();

        if (server != null) {
            ServerConnectionListener listener = server.getConnection();
            assert listener != null;

            synchronized (listener.channels) {
                listener.channels.add(channelFuture);
            }
        }
        channelFuture.channel().closeFuture().syncUninterruptibly();
    }

    private class ChannelHandler extends AbstractChannelHandler<ClientPacketHandler> implements ClientPacketHandler {

        private ConnectionType type;

        public ChannelHandler() {
            super(PacketType.Direction.CLIENT_BOUND);
        }

        @Override
        public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);

            Packet<ServerPacketHandler> packet = connectedPacketSupplier.get();
            if (packet instanceof SHostRegister) {
                type = ConnectionType.HOST_CONTROL;
            } else if (packet instanceof SHostConnect) {
                type = ConnectionType.HOST_USER;
            } else if (packet instanceof SUserConnect) {
                type = ConnectionType.CLIENT_USER;
            }

            sendPacket(packet);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet<ClientPacketHandler> packet) throws Exception {
            packet.handle(ctx, this);
        }

        @Override
        public void handleDisconnect(ChannelHandlerContext ctx, CDisconnect packet) {
            LOGGER.error("Disconnected from proxy: {}", packet.message);
        }

        @Override
        public void handleAccepted(ChannelHandlerContext ctx, CAccepted cAccepted) {
        }
    }
}
