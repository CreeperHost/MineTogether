package net.creeperhost.minetogether;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
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

    // TODO
    public static Screen orderScreen() {
        return new ConfirmScreen(e -> {
            if (e) {
                Util.getPlatform().openUri("https://creeperhost.net");
            }
        },
                new TextComponent("Ordering not yet implemented!"),
                new TextComponent("Open the CreeperHost website instead?")
        );
    }
}
