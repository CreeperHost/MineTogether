package net.creeperhost.minetogether.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ToastHandler
{
    public ResourceLocation TEXTURE_TOASTS = new ResourceLocation("textures/gui/toasts.png");
    
    public Runnable toastMethod;
    public ITextComponent toastText;
    public long endTime;
    public long fadeTime;
    Minecraft mc = Minecraft.getInstance();

    int u = 0;
    int v = 0;
    
    public void displayToast(ITextComponent text, int duration, Runnable method)
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
