package net.creeperhost.minetogether.server;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherServer;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.server.web.ServerListUpdateRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ServerListThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String serverIp;
    private final String displayName;
    private final String projectId;
    private final int port;
    private final Discoverability discoverability;

    private String secret = MineTogether.FINGERPRINT;

    public ServerListThread(String serverIp, String displayName, String projectId, int port, Discoverability discoverability) {
        this.serverIp = serverIp;
        this.displayName = displayName;
        this.projectId = projectId;
        this.port = port;
        this.discoverability = discoverability;
        setDaemon(true);
        setName("MineTogether Server List Thread");
    }

    @Override
    public void run() {
        LOGGER.info("Enabling server list. Servers found breaking the Minecraft EULA may be removed if complaints are received.");
        int tries = 0;
        while (true) {
            long sleepTime = TimeUnit.SECONDS.toMillis(90);
            try {
                ApiClientResponse<ServerListUpdateRequest.Response> resp = MineTogether.API.execute(new ServerListUpdateRequest(
                        serverIp,
                        secret,
                        displayName,
                        projectId,
                        String.valueOf(port),
                        discoverability == Discoverability.INVITE
                ));
                ServerListUpdateRequest.Response response = resp.apiResponse();
                if (!response.getStatus().equals("error")) {
                    tries = 0;
                    MineTogetherServer.inviteId = response.id;
                    if (response.secret != null) {
                        secret = response.secret;
                    }
                } else {
                    LOGGER.error("Got error response for serverlist update: {}", response.getMessageOrNull());
                    tries++;
                }
            } catch (Throwable ex) {
                LOGGER.error("Failed to do update.", ex);
                tries++;
            }
            if (tries >= 4) {
                tries = 0;
                LOGGER.error("Too many server list errors. Re-trying in 45 minutes.");
                sleepTime = TimeUnit.MINUTES.toMillis(45);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
