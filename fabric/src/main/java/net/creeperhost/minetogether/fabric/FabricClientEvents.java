package net.creeperhost.minetogether.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.gui.SettingGui;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;

/**
 * Created by brandon3055 on 05/09/2024
 */
public class FabricClientEvents {
    private static boolean openSettings = false;

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(FabricClientEvents::registerClientCommands);
        ClientTickEvents.END_CLIENT_TICK.register(FabricClientEvents::onClientTick);
    }

    private static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("minetogether_settings")
                .executes(c -> {
                    openSettings = true; //Needs to happen next tick because command runs before the 'current screen' is closed.
                    return 0;
                })
        );
    }

    private static void onClientTick(Minecraft mc) {
        if (openSettings) {
            openSettings = false;
            Minecraft.getInstance().setScreen(new SettingGui.Screen(null));
        }
    }
}
