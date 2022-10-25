package net.creeperhost.minetogether.chat;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.lib.chat.request.StatisticsRequest;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ChatStatistics {

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(1,
            new ThreadFactoryBuilder()
                    .setNameFormat("Stats Thread %d")
                    .setDaemon(true)
                    .build()
    );
    private static final Logger LOGGER = LogManager.getLogger();

    public static String userCount = "over 2 million";
    public static String onlineCount = "thousands of";

    @Nullable
    private static CompletableFuture<Void> future;
    private static long lastUpdate = 0;

    public static void pollStats() {
        if (future != null && !future.isDone()) return;
        if (lastUpdate + TimeUnit.MINUTES.toMillis(30) > System.currentTimeMillis()) return;

        future = CompletableFuture.runAsync(() -> {
            try {
                ApiClientResponse<StatisticsRequest.Response> resp = MineTogetherChat.CHAT_STATE.api.execute(new StatisticsRequest());
                StatisticsRequest.Response stats = resp.apiResponse();
                userCount = stats.users;
                onlineCount = stats.online;
                lastUpdate = System.currentTimeMillis();
            } catch (Throwable ex) {
                LOGGER.error("Error polling statistics.", ex);
            }
        }, EXECUTOR);
    }
}


