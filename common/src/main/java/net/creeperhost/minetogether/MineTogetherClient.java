package net.creeperhost.minetogether;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Initialize on a client.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherClient {

    public static final ResourceLocation WIDGETS_SHEET = new ResourceLocation(MineTogether.MOD_ID, "textures/widgets.png");

    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        LOGGER.info("Initializing MineTogetherClient!");

        MineTogetherChat.init();
    }
}
