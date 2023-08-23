package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SimpleToast extends PolyToast {

    private final Component title;
    private final Component description;
    private ItemStack displayIconStack = ItemStack.EMPTY;
    private ResourceLocation iconResourceLocation;

    public SimpleToast(Component title, Component description) {
        this.title = title;
        this.description = description;
    }

    public SimpleToast(Component title, Component description, ItemStack itemStack) {
        this.title = title;
        this.description = description;
        this.displayIconStack = itemStack;
    }

    public SimpleToast(Component title, Component description, ResourceLocation resourceLocation) {
        this.title = title;
        this.description = description;
        this.iconResourceLocation = resourceLocation;
    }

    @Override
    public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        toastComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());
        if (iconResourceLocation != null) {
            renderImage(poseStack, toastComponent, iconResourceLocation);
        }

        if (title != null) {
            List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(description, 125);
            int n = 0xFF88FF;
            if (list.size() == 1) {
                toastComponent.getMinecraft().font.draw(poseStack, title, 30.0f, 7.0f, n | 0xFF000000);
                toastComponent.getMinecraft().font.draw(poseStack, list.get(0), 30.0f, 18.0f, -1);
            } else {
                if (l < 1500L) {
                    int k = Mth.floor(Mth.clamp((float) (1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                    toastComponent.getMinecraft().font.draw(poseStack, title, 30.0f, 11.0f, n | k);
                } else {
                    int k = Mth.floor(Mth.clamp((float) (l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                    int m = this.height() / 2 - list.size() * toastComponent.getMinecraft().font.lineHeight / 2;
                    for (FormattedCharSequence formattedCharSequence : list) {
                        toastComponent.getMinecraft().font.draw(poseStack, formattedCharSequence, 30.0f, (float) m, 0xFFFFFF | k);
                        m += toastComponent.getMinecraft().font.lineHeight;
                    }
                }
            }
            if (!displayIconStack.isEmpty()) {
                toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(displayIconStack, 8, 8);
            }
            return l >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
        return Visibility.HIDE;
    }
}
