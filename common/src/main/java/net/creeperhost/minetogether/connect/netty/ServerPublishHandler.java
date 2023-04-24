package net.creeperhost.minetogether.connect.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.server.network.ServerConnectionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.creeperhost.minetogether.connect.netty.DataUtils.readString;
import static net.creeperhost.minetogether.connect.netty.PacketIds.*;

/**
 * Created by covers1624 on 24/4/23.
 */
public class ServerPublishHandler extends SimpleChannelInboundHandler<ByteBuf> {

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
                            pipe.addLast(new FrameCodec());
                            pipe.addLast(new ServerPublishHandler(session));

                        }
                    })
                    .connect(proxyHost, proxyPort)
                    .syncUninterruptibly();
            listener.channels.add(channelFuture);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean DEBUG_PACKETS = true;

    private final JWebToken session;

    public ServerPublishHandler(JWebToken session) {
        this.session = session;
    }

    @Nullable
    private Channel channel;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(S_HOST_REGISTER);
        DataUtils.writeString(buf, session.toString());
        sendPacket(buf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        debugInboundPacket(buf);
        int id = buf.readUnsignedByte();
        switch (id) {
            case C_ACCEPTED -> { }
            case C_DISCONNECTED -> {
                String reason = readString(buf);
                LOGGER.error("Disconnected from proxy: {}", reason);
            }
        }
    }

    private ChannelFuture sendPacket(ByteBuf buf) {
        assert channel != null : "Not connected.";

        debugOutboundPacket(buf);
        return channel.writeAndFlush(buf);
    }

    private static void debugOutboundPacket(ByteBuf buf) {
        if (!DEBUG_PACKETS) return;

        buf.markReaderIndex();
        int idx = buf.readUnsignedByte();
        switch (idx) {
            case S_HOST_REGISTER -> LOGGER.info("S_HOST_REGISTER <- \n\tsessionToken={}", readString(buf));
            case S_HOST_CONNECT -> LOGGER.info("S_HOST_CONNECT <- \n\tsessionToken={}\n\tlinkToken={}", readString(buf), readString(buf));
            case S_USER_CONNECT -> LOGGER.info("S_USER_CONNECT <- \n\tsessionToken={}\n\tserverToken={}", readString(buf), readString(buf));
            default -> LOGGER.error("Unknown packet {}", idx);
        }
        buf.resetReaderIndex();
    }

    private static void debugInboundPacket(ByteBuf buf) {
        if (!DEBUG_PACKETS) return;

        buf.markReaderIndex();
        int idx = buf.readUnsignedByte();
        switch (idx) {
            case C_DISCONNECTED -> LOGGER.info("C_DISCONNECTED ->\n\tmessage={}", readString(buf));
            case C_ACCEPTED -> LOGGER.info("C_ACCEPTED ->");
            default -> LOGGER.error("Unknown packet {}", idx);
        }
        buf.resetReaderIndex();
    }}
