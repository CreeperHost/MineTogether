package net.creeperhost.minetogether.connect.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.covers1624.quack.util.SneakyUtils;
import net.creeperhost.minetogether.MineTogetherPlatform;
import net.creeperhost.minetogether.connect.netty.packet.*;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 24/4/23.
 */
public class NettyClient {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void publishServer(IntegratedServer server, String proxyHost, int proxyPort, JWebToken session) {
        Throwable[] error = new Throwable[1];
        ProxyConnection connection = new ProxyConnection() {
            @Override
            public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);

                sendPacket(new SHostRegister(session.toString()));
            }

            @Override
            public void handleDisconnect(ChannelHandlerContext ctx, CDisconnect packet) {
                super.handleDisconnect(ctx, packet);

                error[0] = new IOException("Failed to host server: " + packet.message);
                synchronized (error) {
                    error.notifyAll();
                }
            }

            @Override
            public void handleAccepted(ChannelHandlerContext ctx, CAccepted cAccepted) {
                super.handleAccepted(ctx, cAccepted);
                synchronized (error) {
                    error.notifyAll();
                }
            }

            @Override
            public void handleServerLink(ChannelHandlerContext ctx, CServerLink packet) {
                link(server, proxyHost, proxyPort, session, packet.linkToken);
            }
        };
        ChannelFuture channelFuture = openConnection(
                proxyHost,
                proxyPort,
                connection,
                ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP::get,
                ServerConnectionListener.SERVER_EVENT_GROUP::get
        );

        synchronized (error) {
            try {
                error.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted whilst waiting.", ex);
            }
        }
        if (error[0] != null) {
            SneakyUtils.throwUnchecked(error[0]);
        }

        ServerConnectionListener listener = server.getConnection();
        assert listener != null;

        synchronized (listener.channels) {
            listener.channels.add(channelFuture);
        }
    }

    public static Connection connect(String proxyHost, int proxyPort, JWebToken session, String serverToken) {
        Throwable[] error = new Throwable[1];
        Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        ProxyConnection proxyConnection = new ProxyConnection() {

            @Override
            public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                sendPacket(new SUserConnect(session.toString(), serverToken));
                // Required for Forge to add channel attributes.
                MineTogetherPlatform.prepareClientConnection(connection);
            }

            @Override
            public void handleDisconnect(ChannelHandlerContext ctx, CDisconnect packet) {
                super.handleDisconnect(ctx, packet);
                error[0] = new IOException("Failed to connect to server: " + packet.message);
                synchronized (error) {
                    error.notifyAll();
                }
            }

            @Override
            public void handleBeginRaw(ChannelHandlerContext ctx, CBeginRaw packet) {
                synchronized (error) {
                    error.notifyAll();
                }
            }
        };
        ChannelFuture channelFuture = openConnection(
                proxyHost,
                proxyPort,
                proxyConnection,
                Connection.NETWORK_EPOLL_WORKER_GROUP::get,
                Connection.NETWORK_WORKER_GROUP::get
        );
        ChannelPipeline pipeline = channelFuture.channel().pipeline();
        pipeline.addLast("mc:splitter", new Varint21FrameDecoder());
        pipeline.addLast("mc:decoder", new PacketDecoder(PacketFlow.CLIENTBOUND));
        pipeline.addLast("mc:prepender", new Varint21LengthFieldPrepender());
        pipeline.addLast("mc:encoder", new PacketEncoder(PacketFlow.SERVERBOUND));
        pipeline.addLast("mc:packet_handler", connection);

        synchronized (error) {
            try {
                error.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted whilst waiting.", ex);
            }
        }
        if (error[0] != null) {
            SneakyUtils.throwUnchecked(error[0]);
        }

        return connection;
    }

    private static void link(IntegratedServer server, String proxyHost, int proxyPort, JWebToken session, String linkToken) {
        Throwable[] error = new Throwable[1];
        ProxyConnection proxyConnection = new ProxyConnection() {
            @Override
            public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);

                sendPacket(new SHostConnect(session.toString(), linkToken));
            }

            @Override
            public void handleDisconnect(ChannelHandlerContext ctx, CDisconnect packet) {
                super.handleDisconnect(ctx, packet);

                error[0] = new IOException("Failed to host server: " + packet.message);
                synchronized (error) {
                    error.notifyAll();
                }
            }

            @Override
            public void handleAccepted(ChannelHandlerContext ctx, CAccepted cAccepted) {
                super.handleAccepted(ctx, cAccepted);
                synchronized (error) {
                    error.notifyAll();
                }
            }
        };
        ChannelFuture channelFuture = openConnection(
                proxyHost,
                proxyPort,
                proxyConnection,
                ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP::get,
                ServerConnectionListener.SERVER_EVENT_GROUP::get
        );

        ServerConnectionListener listener = server.getConnection();
        assert listener != null;

        synchronized (listener.channels) {
            listener.channels.add(channelFuture);
        }

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        ChannelPipeline pipeline = channelFuture.channel().pipeline();
        pipeline.addLast("mc:legacy_query", new LegacyQueryHandler(listener));
        pipeline.addLast("mc:splitter", new Varint21FrameDecoder());
        pipeline.addLast("mc:decoder", new PacketDecoder(PacketFlow.SERVERBOUND));
        pipeline.addLast("mc:prepender", new Varint21LengthFieldPrepender());
        pipeline.addLast("mc:encoder", new PacketEncoder(PacketFlow.CLIENTBOUND));
        pipeline.addLast("mc:packet_handler", connection);

        synchronized (listener.connections) {
            listener.connections.add(connection);
        }

        synchronized (error) {
            try {
                error.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted whilst waiting.", ex);
            }
        }
        if (error[0] != null) {
            SneakyUtils.throwUnchecked(error[0]);
        }
    }

    private static ChannelFuture openConnection(String proxyHost, int proxyPort, ProxyConnection connection, Supplier<EventLoopGroup> epollGroup, Supplier<EventLoopGroup> nioGroup) {
        EventLoopGroup eventGroup;
        Class<? extends Channel> channelClass;
        if (Epoll.isAvailable()) {
            eventGroup = epollGroup.get();
            channelClass = EpollSocketChannel.class;
        } else {
            eventGroup = nioGroup.get();
            channelClass = NioSocketChannel.class;
        }

        return new Bootstrap()
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
                        pipe.addLast(connection);

                    }
                })
                .connect(proxyHost, proxyPort)
                .syncUninterruptibly();
    }

    public static class ProxyConnection extends AbstractChannelHandler<ClientPacketHandler> implements ClientPacketHandler {

        public ProxyConnection() {
            super(PacketType.Direction.CLIENT_BOUND);
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

        @Override
        public void handleServerLink(ChannelHandlerContext ctx, CServerLink packet) {
            throw new NotImplementedException();
        }

        @Override
        public void handleBeginRaw(ChannelHandlerContext ctx, CBeginRaw packet) {
            throw new NotImplementedException();
        }

        @Override
        public void handleRaw(ChannelHandlerContext ctx, CRaw packet) {
            ctx.fireChannelRead(packet);
        }
    }
}
