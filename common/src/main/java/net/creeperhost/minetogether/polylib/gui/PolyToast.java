package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

public class PolyToast implements Toast {

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        return null;
    }

    //TODO scale the image
    public void renderImage(PoseStack poseStack, ToastComponent toastComponent, ResourceLocation resourceLocation) {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, 8, 8, 0, 0, 16, 16, 16, 16);
        RenderSystem.enableBlend();
    }
}
