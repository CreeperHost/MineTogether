package net.creeperhost.minetogether.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ToastHandler
{
    private final ResourceLocation newResouce = new ResourceLocation("textures/gui/toasts.png");

    public static Runnable toastMethod;
    public static String toastText;
    public static long endTime;
    public static long fadeTime;
    Minecraft mc = Minecraft.getInstance();

    String mcVersion;
    int u = 0;
    int v = 0;

    public static void displayToast(String text, int duration, Runnable method)
    {
        toastText = text;
        endTime = System.currentTimeMillis() + duration;
        fadeTime = endTime + 500;
        toastMethod = method;
    }

    public static void clearToast(boolean fade)
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

    @SubscribeEvent
    public void guiRendered(TickEvent.RenderTickEvent evt)
    {
        if (ToastHandler.toastText != null)
        {
            long curTime = System.currentTimeMillis();
            if (ToastHandler.fadeTime > curTime)
            {
                long fadeDiff = ToastHandler.fadeTime - ToastHandler.endTime;
                long curFade = Math.min(ToastHandler.fadeTime - curTime, fadeDiff);
                float alpha = (float) curFade / (float) fadeDiff;

                RenderHelper.disableStandardItemLighting();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
                mc.getTextureManager().bindTexture(getToastResourceLocation());
                AbstractGui.blit(160, 0, u, v, 160, 32, 10, 10);
//                GlStateManager.enableBlend();
                int textColour = (0xFFFFFF << 32) | ((int) (alpha * 255) << 24);
                mc.fontRenderer.drawSplitString(ToastHandler.toastText, 160 + 5, 3, 160, textColour);
            } else
            {
                ToastHandler.clearToast(false);
            }
        }
    }

    private ResourceLocation getToastResourceLocation()
    {
        return newResouce;
    }
}
