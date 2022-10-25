package net.creeperhost.minetogether;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Initialize on a client.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherClient {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        LOGGER.info("Initializing MineTogetherClient!");

        MineTogetherChat.init();
        MineTogetherServerList.init();
    }
}
