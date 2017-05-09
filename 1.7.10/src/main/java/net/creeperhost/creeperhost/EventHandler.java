package net.creeperhost.creeperhost;

import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.element.ButtonCreeper;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.gui.mpreplacement.GuiCHMultiplayer;
import net.creeperhost.creeperhost.api.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class EventHandler{

    private static final int BUTTON_ID = 30051988;

    private static Field parentScreenField;

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event){
        if (!Config.getInstance().isMainMenuEnabled())
            return;
        GuiScreen gui = Util.getGuiFromEvent(event);
        if(gui instanceof GuiMainMenu){
            CreeperHost.instance.setRandomImplementation();
            if (CreeperHost.instance.getImplementation() == null)
                return;
            List<GuiButton> buttonList = Util.getButtonList(event);
            if (buttonList != null) {
                buttonList.add(new ButtonCreeper(BUTTON_ID, gui.width/2+104, gui.height/4+48+72+12));
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event){
        if (!Config.getInstance().isMainMenuEnabled() || CreeperHost.instance.getImplementation() == null)
            return;
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
        if (!Config.getInstance().isMpMenuEnabled() || CreeperHost.instance.getImplementation() == null)
            return;
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
            CreeperHost.logger.warn("Unable to replace GuiMultiplayer as couldn't get parentScreen. Have things changed?", e);
        }

    }
}
