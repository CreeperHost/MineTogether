package net.creeperhost.minetogether.lib.chat;

import java.util.UUID;

/**
 * Created by covers1624 on 29/6/22.
 */
public interface ChatAuth {

    String getSignature();

    UUID getUUID();

    String getHash();

    boolean isOnline();

    String beginMojangAuth();
}
