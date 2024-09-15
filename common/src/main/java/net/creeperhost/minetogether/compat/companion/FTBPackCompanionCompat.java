package net.creeperhost.minetogether.compat.companion;

import dev.ftb.packcompanion.api.client.PackCompanionClientAPI;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;

/**
 * Created by brandon3055 on 14/07/2024
 */
public class FTBPackCompanionCompat {

    public static void init() {
        PackCompanionClientAPI.get().registerAdditionalPauseProvider(AdditionalPauseTarget.TOP_RIGHT, new PauseProvider());
    }

}
