package net.creeperhost.creeperhost.lib;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * @author Koen Beckers (K-4U)
 */
public class KeyBindings {
    
    private static KeyBinding sivGuiKeybind;
    
    public static KeyBinding getSivGuiKeybind() {
    
        return sivGuiKeybind;
    }
    
    
    public static void init() {
        
        sivGuiKeybind = new KeyBinding("ch.key.sivgui", Keyboard.KEY_TAB, "ch.keys");
        sivGuiKeybind.setKeyModifierAndCode(KeyModifier.CONTROL, Keyboard.KEY_TAB);
        ClientRegistry.registerKeyBinding(sivGuiKeybind);
    }
    
}
