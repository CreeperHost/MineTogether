package de.ellpeck.chgui;

import de.ellpeck.chgui.gui.element.ButtonCreeper;
import de.ellpeck.chgui.gui.GuiGetServer;
import de.ellpeck.chgui.paul.Callbacks;
import de.ellpeck.chgui.paul.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler{

    private static final int BUTTON_ID = 30051988;

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event){
        GuiScreen gui = event.getGui();
        if(gui instanceof GuiMainMenu){
            event.getButtonList().add(new ButtonCreeper(BUTTON_ID, gui.width/2+104, gui.height/4+48+72+12));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event){
        if(event.getGui() instanceof GuiMainMenu){
            if(event.getButton().id == BUTTON_ID){
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }
    }
}
