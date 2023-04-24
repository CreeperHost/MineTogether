package net.creeperhost.minetogether.connect.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.covers1624.quack.util.LazyValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by covers1624 on 24/4/23.
 */
public class NettyClient extends Thread {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final LazyValue<EpollEventLoopGroup> EPOLL_EVENT_LOOP_GROUP = new LazyValue<>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("MTConnect Netty Epoll IO #%d").setDaemon(true).build()));
    private static final LazyValue<NioEventLoopGroup> NIO_EVENT_LOOP_GROUP = new LazyValue<>(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("MTConnect Netty IO #%d").setDaemon(true).build()));

    public NettyClient() {
        setDaemon(true);
        setName("MineTogether Connect");
    }


}
