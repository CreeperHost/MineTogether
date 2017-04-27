package net.creeperhost.ingamesale.client;

import net.creeperhost.ingamesale.IngameSale;
import net.creeperhost.ingamesale.client.gui.GUICHMultiplayer;
import net.creeperhost.ingamesale.common.IGSCommonProxy;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.lang.reflect.Field;

/**
 * Created by Aaron on 26/04/2017.
 */
public class IGSClientProxy extends IGSCommonProxy
{
    static Field guiField;
    static Field parentScreenField;

    @SubscribeEvent
    public void openGUI(GuiOpenEvent event) {
        GuiScreen gui;
        if (guiField == null) {
            guiField = ReflectionHelper.findField(GuiOpenEvent.class, "gui");
            guiField.setAccessible(true);
        }

        if (parentScreenField == null) {
            parentScreenField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146798_g", "parentScreen");
            parentScreenField.setAccessible(true);
        }

        // Done using reflection so we can work on 1.8.9 before setters/getters
        try
        {
            gui = (GuiScreen) guiField.get(event);
            if (gui instanceof GuiMultiplayer) {
                GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
                GuiScreen parent = (GuiScreen) parentScreenField.get(mpGUI);
                IngameSale.logger.warn(parent);
                guiField.set(event, new GUICHMultiplayer(parent));
            }
        } catch (Throwable e)
        {
            IngameSale.logger.warn("Unable to replace GuiMultiplayer as couldn't get parentScreen. Have things changed?", e);
        }

    }

    public void doClientStuff() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
