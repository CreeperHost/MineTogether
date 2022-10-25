package net.creeperhost.minetogether.orderform.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ButtonMap extends Button {

    String buttonText;
    List<Component> tooltiplist = new ArrayList<>();
    int imageWidth;
    int imageHeight;
    float x;
    float y;

    public ButtonMap(float x, float y, int width, int height, int imageWidth, int imageHeight, String title, boolean active, OnPress pressedAction) {
        super((int) x, (int) y, width, height, new TranslatableComponent(title), pressedAction);
        this.buttonText = title;
        this.x = x;
        this.y = y;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        tooltiplist.add(new TranslatableComponent(buttonText));
        this.active = active;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);

        ResourceLocation map = new ResourceLocation(MineTogether.MOD_ID, "textures/map/" + buttonText + ".png");
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, map);

        if (isHoveredOrFocused()) {
            RenderSystem.setShaderColor(0F, 1F, 0F, alpha);
        }
        if (isFocused()) {
            RenderSystem.setShaderColor(0F, 0.6F, 0F, alpha);
        }
        if (!active) {
            RenderSystem.setShaderColor(0.4F, 0.4F, 0.4F, alpha);
        }
        ScreenHelper.drawModalRectWithCustomSizedTextureFloat(poseStack.last().pose(), x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
    }
}
