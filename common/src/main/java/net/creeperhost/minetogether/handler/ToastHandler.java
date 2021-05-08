package net.creeperhost.minetogether.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToastHandler
{
    public ResourceLocation TEXTURE_TOASTS = new ResourceLocation("textures/gui/toasts.png");
    public Runnable toastMethod;
    public Component toastText;
    public long endTime;
    public long fadeTime;

    public void displayToast(Component text, int duration, Runnable method)
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
