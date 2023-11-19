package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
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

    public SimpleToast(Component title) {
        this(title, Component.empty());
    }

    public SimpleToast(Component title, ResourceLocation resourceLocation) {
        this(title, Component.empty());
        this.iconResourceLocation = resourceLocation;
    }

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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        toastComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());
        if (iconResourceLocation != null) {
            renderImage(poseStack, toastComponent, iconResourceLocation);
        }

        if (title != null) {
            Font font = toastComponent.getMinecraft().font;
            List<FormattedCharSequence> titleList = font.split(title, 125);
            List<FormattedCharSequence> descList = font.split(description, 125);
            boolean titleOnly = description.getString().isEmpty();

            int n = 0xFF88FF;
            if (descList.size() == 1 && titleList.size() == 1) {
                font.draw(poseStack, title, 30, 7, n | 0xFF000000);
                font.draw(poseStack, descList.get(0), 30, 18, -1);
            } else {
                if (l < 1500L || titleOnly) {
                    int yPos = this.height() / 2 - titleList.size() * font.lineHeight / 2;
                    int alpha = titleOnly ? 0xFF000000 : Mth.floor(Mth.clamp((float) (1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                    for (FormattedCharSequence formattedCharSequence : titleList) {
                        font.draw(poseStack, formattedCharSequence, 30, yPos, n | alpha);
                        yPos += font.lineHeight;
                    }
                } else {
                    int yPos = this.height() / 2 - descList.size() * font.lineHeight / 2;
                    int alpha = Mth.floor(Mth.clamp((float) (l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                    for (FormattedCharSequence formattedCharSequence : descList) {
                        font.draw(poseStack, formattedCharSequence, 30, yPos, 0xFFFFFF | alpha);
                        yPos += font.lineHeight;
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
