package net.creeperhost.minetogether.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.gui.SettingGui;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by brandon3055 on 01/10/2023
 */
public class ForgeClientEvents {

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(ForgeClientEvents::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(ForgeClientEvents::registerClientCommands);
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
