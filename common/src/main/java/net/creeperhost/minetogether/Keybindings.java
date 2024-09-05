package net.creeperhost.minetogether;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.creeperhost.minetogether.chat.gui.FriendChatGui;
import net.creeperhost.minetogether.chat.gui.PublicChatGui;
import net.creeperhost.minetogether.gui.SettingGui;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 05/09/2024
 */
public class Keybindings {

    public static final KeyMapping OPEN_FRIEND_CHAT = new KeyMapping("minetogether:keybind.friend_chat", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "minetogether:keybind.category");
    public static final KeyMapping OPEN_GLOBAL_CHAT = new KeyMapping("minetogether:keybind.global_chat", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "minetogether:keybind.category");
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping("minetogether:keybind.settings", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "minetogether:keybind.category");

    public static void init() {
        KeyMappingRegistry.register(OPEN_FRIEND_CHAT);
        KeyMappingRegistry.register(OPEN_GLOBAL_CHAT);
        KeyMappingRegistry.register(OPEN_SETTINGS);
        ClientTickEvent.CLIENT_POST.register(Keybindings::clientTick);
    }

    private static void clientTick(Minecraft mc) {
        if (OPEN_FRIEND_CHAT.consumeClick()) {
            mc.setScreen(new FriendChatGui.Screen(null));
        } else if (OPEN_GLOBAL_CHAT.consumeClick()) {
            mc.setScreen(new PublicChatGui.Screen(null));
        } else if (OPEN_SETTINGS.consumeClick()) {
            mc.setScreen(new SettingGui.Screen(null));
        }
    }
}
