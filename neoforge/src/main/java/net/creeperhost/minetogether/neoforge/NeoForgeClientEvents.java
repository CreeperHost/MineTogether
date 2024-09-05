package net.creeperhost.minetogether.neoforge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.gui.SettingGui;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Created by brandon3055 on 01/10/2023
 */
public class NeoForgeClientEvents {

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(NeoForgeClientEvents::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientEvents::registerClientCommands);
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(MTTextures.getAtlasHolder());
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("minetogether_settings")
                .executes(c -> {
                    Minecraft.getInstance().setScreen(new SettingGui.Screen(null));
                    return 0;
                })
        );
    }
}
