package net.creeperhost.minetogether.fabric;

import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.creeperhost.polylib.fabric.client.ResourceReloadListenerWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

/**
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MineTogether.init();

        if (Platform.getEnv() == EnvType.CLIENT) {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ResourceReloadListenerWrapper(MTTextures::getUploader, new ResourceLocation(MineTogether.MOD_ID, "gui_atlas_reload")));
        }
    }
}
