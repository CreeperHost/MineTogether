package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long l) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());
        if (iconResourceLocation != null) {
            renderImage(graphics, toastComponent, iconResourceLocation);
        }

        if (title != null) {
            Font font = toastComponent.getMinecraft().font;
            List<FormattedCharSequence> list = font.split(description, 125);
            int n = 0xFF88FF;
            if (list.size() == 1) {
                graphics.drawString(font, title, 30, 7, n | 0xFF000000);
                graphics.drawString(font, list.get(0), 30, 18, -1);
            } else {
                if (l < 1500L) {
                    int k = Mth.floor(Mth.clamp((float) (1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                    graphics.drawString(font, title, 30, 11, n | k);
                } else {
                    int k = Mth.floor(Mth.clamp((float) (l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                    int m = this.height() / 2 - list.size() * font.lineHeight / 2;
                    for (FormattedCharSequence formattedCharSequence : list) {
                        graphics.drawString(font, formattedCharSequence, 30, m, 0xFFFFFF | k);
                        m += font.lineHeight;
                    }
                }
            }
            if (!displayIconStack.isEmpty()) {
                graphics.renderFakeItem(displayIconStack, 8, 8);
            }
            return l >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
        return Visibility.HIDE;
    }
}
