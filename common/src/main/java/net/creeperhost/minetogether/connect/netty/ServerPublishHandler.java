package net.creeperhost.minetogether.connect.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.creeperhost.minetogether.connect.netty.packet.*;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.server.network.ServerConnectionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by covers1624 on 24/4/23.
 */
public class ServerPublishHandler extends AbstractChannelHandler<ClientPacketHandler> implements ClientPacketHandler {

    public static void publishServer(ServerConnectionListener listener, JWebToken session, String proxyHost, int proxyPort) {
        synchronized (listener.channels) {
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
                            pipe.addLast(new ServerPublishHandler(session));

                        }
                    })
                    .connect(proxyHost, proxyPort)
                    .syncUninterruptibly();
            listener.channels.add(channelFuture);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private final JWebToken session;

    public ServerPublishHandler(JWebToken session) {
        super(PacketType.Direction.CLIENT_BOUND);
        this.session = session;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        sendPacket(new SHostRegister(session.toString()));
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
