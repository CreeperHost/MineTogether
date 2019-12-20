package net.creeperhost.minetogether.events;

import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.gui.GuiGetServer;
import net.creeperhost.minetogether.client.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.lib.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScreenEvents
{
    @SubscribeEvent
    public void openScreen(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(event.getGui() instanceof MainMenuScreen)
        {
            event.addWidget(new GuiButtonCreeper(50, 160, p ->
            {
                Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }));
        }
    }
}
