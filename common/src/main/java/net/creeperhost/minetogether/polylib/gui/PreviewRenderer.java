package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.gui.PreviewElement;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 19/10/22.
 * <p>
 * 1.18 is missing some required bits to use the new PreviewElement in ingame chat, so I am just wrapping its logic with the old PreviewRenderer.
 */
public abstract class PreviewRenderer implements Widget {

    private final int imageSize;

    protected PreviewRenderer(int imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        PreviewElement.URLInfo url = getUrlUnderMouse(mouseX, mouseY);
        if (url == null) return;
        PreviewElement.ImageLoader image = PreviewElement.getImage(url, true);
        if (image == null) return;

        Minecraft mc = Minecraft.getInstance();
        GuiRender render = new GuiRender(mc, pStack, mc.renderBuffers().bufferSource());

        pStack.pushPose();
        pStack.translate(0, 0, 150);
        if (image.isLoaded()) {
            double aspect = image.width() / (double) image.height();
            double width = aspect > 1 ? imageSize : imageSize * aspect;
            double height = aspect > 1 ? imageSize / aspect : imageSize;
            double border = 3;

            double x = Math.min(mouseX, mc.getWindow().getGuiScaledWidth() - (width + (border * 2)));
            double y = Math.min(mouseY, mc.getWindow().getGuiScaledHeight() - (height + (border * 2)));

            render.toolTipBackground(x, y, width + (border * 2), height + (border * 2));
            image.render(render, x + border, y + border, width, height);
        } else {
            render.renderTooltip(new TranslatableComponent("minetogether:gui.chat.loading_preview"), mouseX, mouseY);
        }
        pStack.popPose();
    }

    @Nullable
    protected abstract PreviewElement.URLInfo getUrlUnderMouse(int mouseX, int mouseY);
}
