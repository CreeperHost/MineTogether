package net.creeperhost.minetogether.events;

import net.creeperhost.minetogether.MineTogether;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MineTogether.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuiEvents
{
    @SubscribeEvent
    public void openScreen(GuiOpenEvent event)
    {
        System.out.println(event.getGui().getTitle().getFormattedText());
    }
}
