package net.creeperhost.minetogether.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ToastHandler
{
    public ResourceLocation TEXTURE_TOASTS = new ResourceLocation("textures/gui/toasts.png");
    
    public Runnable toastMethod;
    public String toastText;
    public long endTime;
    public long fadeTime;
    Minecraft mc = Minecraft.getInstance();

    int u = 0;
    int v = 0;
    
    public void displayToast(String text, int duration, Runnable method)
    {
        toastText = text;
        endTime = System.currentTimeMillis() + duration;
        fadeTime = endTime + 500;
        toastMethod = method;
    }
    
    public void clearToast(boolean fade)
    {
        toastText = null;
        endTime = System.currentTimeMillis();
        toastMethod = null;
        fadeTime = endTime + (fade ? 500 : 0);
    }
    
    public boolean isActiveToast()
    {
        return fadeTime >= System.currentTimeMillis();
    }
    
    private ResourceLocation getToastResourceLocation()
    {
        return TEXTURE_TOASTS;
    }
}
