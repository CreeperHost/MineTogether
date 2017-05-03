package de.ellpeck.chgui;

import de.ellpeck.chgui.gui.element.ButtonCreeper;
import de.ellpeck.chgui.gui.GuiGetServer;
import de.ellpeck.chgui.gui.mpreplacement.GuiCHMultiplayer;
import de.ellpeck.chgui.paul.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class EventHandler{

    private static final int BUTTON_ID = 30051988;

    private static Field parentScreenField;

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event){
        GuiScreen gui = Util.getGuiFromEvent(event);
        if(gui instanceof GuiMainMenu){
            List<GuiButton> buttonList = Util.getButtonList(event);
            if (buttonList != null) {
                buttonList.add(new ButtonCreeper(BUTTON_ID, gui.width/2+104, gui.height/4+48+72+12));
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event){
        GuiScreen gui = Util.getGuiFromEvent(event);
        if(gui instanceof GuiMainMenu){
            GuiButton button = Util.getButton(event);
            if(button != null && button.id == BUTTON_ID){
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }
    }

    @SubscribeEvent
    public void openGUI(GuiOpenEvent event) {
        if (parentScreenField == null) {
            parentScreenField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146798_g", "parentScreen");
            parentScreenField.setAccessible(true);
        }

        // Done using reflection so we can work on 1.8.9 before setters/getters
        try
        {
            GuiScreen gui = Util.getGuiFromEvent(event);
            if (gui instanceof GuiMultiplayer) {
                GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
                GuiScreen parent = (GuiScreen) parentScreenField.get(mpGUI);
                Util.setGuiInEvent(event, new GuiCHMultiplayer(parent));
            }
        } catch (Throwable e)
        {
            CreeperHostGui.logger.warn("Unable to replace GuiMultiplayer as couldn't get parentScreen. Have things changed?", e);
        }

    }
}
