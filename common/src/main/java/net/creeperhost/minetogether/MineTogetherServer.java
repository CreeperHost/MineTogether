package net.creeperhost.minetogether;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Initialize on a dedicated server.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherServer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        LOGGER.info("Initializing MineTogetherClient!");
    }
}
