package net.creeperhost.minetogether.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ToastHandler
{
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

    public void displayToast(Component text, int duration, EnumToastType toastType, Runnable method)
    {
        this.toastText = text;
        this.x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 160;
        this.y = 0;
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

    public void render(PoseStack poseStack)
    {
        if (MineTogetherClient.toastHandler != null && MineTogetherClient.toastHandler.toastText != null)
        {
            long curTime = System.currentTimeMillis();
            if (MineTogetherClient.toastHandler.fadeTime > curTime)
            {
                long fadeDiff = MineTogetherClient.toastHandler.fadeTime - MineTogetherClient.toastHandler.endTime;
                long curFade = Math.min(MineTogetherClient.toastHandler.fadeTime - curTime, fadeDiff);
                float alpha = (float) curFade / (float) fadeDiff;

                RenderSystem.disableLighting();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                Minecraft.getInstance().getTextureManager().bind(Constants.TEXTURE_TOASTS);
                List<FormattedCharSequence> s = ComponentRenderUtils.wrapComponents(MineTogetherClient.toastHandler.toastText, 140, Minecraft.getInstance().font);

                int toastHeight = 32 + (s.size() * 8);
                Screen.blit(poseStack, MineTogetherClient.toastHandler.getX(), MineTogetherClient.toastHandler.getY(), MineTogetherClient.toastHandler.getToastType().getX(), MineTogetherClient.toastHandler.getToastType().getY(), 160, 32, 256, 256);

                RenderSystem.enableBlend();
                int textColour = (0xFFFFFF << 32) | ((int) (alpha * 255) << 24);

                int start = (MineTogetherClient.toastHandler.getY());
                for(FormattedCharSequence properties : s)
                {
                    int x = getX() + 6;
                    if(toastType == EnumToastType.WARNING) x = getX() + 16;

                    Minecraft.getInstance().font.drawShadow(poseStack, properties, x, start +=9, textColour);
                }

            } else
            {
                MineTogetherClient.toastHandler.clearToast(true);
            }
        }
    }

    public enum EnumToastType
    {
        DEFAULT(0, 0),
        WHITE(0, 32),
        WARNING(0, 64);

        int x;
        int y;

        EnumToastType(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
