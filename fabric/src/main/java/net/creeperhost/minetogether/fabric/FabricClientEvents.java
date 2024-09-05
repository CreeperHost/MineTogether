package net.creeperhost.minetogether.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.creeperhost.minetogether.gui.SettingGui;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;

/**
 * Created by brandon3055 on 05/09/2024
 */
public class FabricClientEvents {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(FabricClientEvents::registerClientCommands);
    }

    private static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("minetogether_settings")
                .executes(c -> {
                    Minecraft.getInstance().setScreen(new SettingGui.Screen(null));
                    return 0;
                })
        );
    }
}
