package net.creeperhost.minetogether.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.sentry.Sentry;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.polylib.client.screen.widget.buttons.ButtonMultiple;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MineTogetherScreen extends Screen
{
    public MineTogetherScreen(Component component)
    {
        super(component);
    }

    @Override
    public void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int i, int j)
    {
        super.renderComponentHoverEffect(poseStack, style, i, j);
    }

    public boolean handleComponentClicked(@Nullable Style style, double mouseX, double mouseY)
    {
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderTooltips(poseStack, mouseX, mouseY, partialTicks);
    }

    public void renderTooltips(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        try
        {
            if (children() != null && !children().isEmpty())
            {
                for (GuiEventListener guiEventListener : children())
                {
                    if (!(guiEventListener instanceof Button)) continue;

                    Button abstractWidget = (Button) guiEventListener;

                    if (abstractWidget.isHoveredOrFocused() && abstractWidget instanceof ButtonMultiple)
                    {
                        ButtonMultiple buttonMultiple = (ButtonMultiple) abstractWidget;
                        if (buttonMultiple.getTooltip() != null && !buttonMultiple.getTooltip().getString().isEmpty())
                            renderTooltip(poseStack, buttonMultiple.getTooltip(), mouseX, mouseY);
                    }
                }
            }
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
    }

    public static void drawLogo(PoseStack matrixStack, Font fontRendererObj, int containerWidth, int containerHeight, int containerX, int containerY, float scale)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F); // reset alpha
        float adjust = (1 / scale);
        int width = (int) (containerWidth * adjust);
        int height = (int) (containerHeight * adjust);
        int x = (int) (containerX * adjust);
        int y = (int) (containerY * adjust);
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(Constants.MOD_ID, "textures/minetogether25.png");

        matrixStack.pushPose();
        matrixStack.scale(scale, scale, scale);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;
        int totalWidth = mtWidth + creeperWidth;

        totalHeight *= adjust;
        totalWidth *= adjust;

        RenderSystem.setShaderTexture(0, resourceLocationMineTogetherLogo);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = fontRendererObj.width(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        fontRendererObj.drawShadow(matrixStack, created, x + (width / 2 - (creeperTotalWidth / 2)), y + (height / 2 - (totalHeight / 2) + mtHeight + 7), 0x40FFFFFF);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F); // reset alpha as font renderer isn't nice like that

        RenderSystem.setShaderTexture(0, resourceLocationCreeperLogo);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);

        RenderSystem.disableBlend();
        matrixStack.popPose();
    }
}
