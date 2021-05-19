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
    public EnumToastType toastType;
    public int x;
    public int y;

    public void displayToast(Component text, int x, int y, int duration, EnumToastType toastType, Runnable method)
    {
        this.toastText = text;
        this.x = x;
        this.y = y;
        this.endTime = System.currentTimeMillis() + duration;
        this.fadeTime = endTime + 500;
        this.toastType = toastType;
        this.toastMethod = method;
    }

    public void clearToast(boolean fade)
    {
        this.toastText = null;
        this.endTime = System.currentTimeMillis();
        this.toastMethod = null;
        this.fadeTime = endTime + (fade ? 500 : 0);
    }

    public boolean isActiveToast()
    {
        return fadeTime >= System.currentTimeMillis();
    }

    private ResourceLocation getToastResourceLocation()
    {
        return TEXTURE_TOASTS;
    }

    public EnumToastType getToastType()
    {
        return toastType;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }
}
