package net.creeperhost.minetogether.client.screen.list;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.api.Minigame;
import net.creeperhost.minetogether.client.screen.minigames.MinigamesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class GuiListEntryMinigame extends GuiListEntry
{
    private MinigamesScreen minigamesScreenNew;
    Minigame minigame;
    private float transparency = 0.5F;
    private DynamicTexture texture;
    private ResourceLocation resourceLocation;
    private NativeImage image;
    private Future<?> future;

    public GuiListEntryMinigame(MinigamesScreen minigamesScreenNew, GuiList list, Minigame minigame)
    {
        super(list);
        this.minigamesScreenNew = minigamesScreenNew;
        this.minigame = minigame;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        if (isSelected)
        {
            if (transparency <= 1.0F)
                transparency += 0.04;
        } else
        {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }

        FontRenderer font = mc.fontRenderer;

        drawCenteredString(matrixStack, TextFormatting.BOLD + minigame.displayName + TextFormatting.RESET + " by " + minigame.author, x + 110, slotHeight + 26, 0xFFAAAAAA);

        String displayDescription = minigame.displayDescription;
        if (font.getStringWidth(displayDescription) > (listWidth - 96) * 2)
        {
            while (font.getStringWidth(displayDescription + "...") > (listWidth - 96) * 2)
            {
                displayDescription = displayDescription.substring(0, displayDescription.lastIndexOf(" "));
            }
            displayDescription += "...";
        }

        drawCenteredSplitString(matrixStack, displayDescription, x + listWidth / 2, y + 12, listWidth, 0xFFAAAAAA);

        if(resourceLocation == null)
        {
            createDynamicTexture(minigame);
        }
        renderImage(matrixStack, resourceLocation, x - 36, y);
    }

    private void drawCenteredSplitString(MatrixStack matrixStack, String drawText, int x, int y, int width, int drawColour)
    {
        List<IReorderingProcessor> iTextPropertiesList = RenderComponentsUtil.func_238505_a_(new StringTextComponent(drawText), width, mc.fontRenderer);
        for (IReorderingProcessor str : iTextPropertiesList)
        {
            drawCenteredString(matrixStack, str, x, y, drawColour);
            y += mc.fontRenderer.FONT_HEIGHT;
        }
    }

    public void drawCenteredString(MatrixStack matrixStack, String text, int x, int y, int color)
    {
        Minecraft.getInstance().fontRenderer.drawString(matrixStack, text, (float)(x - Minecraft.getInstance().fontRenderer.getStringWidth(text) / 2), (float)y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, IReorderingProcessor text, int x, int y, int color)
    {
        Minecraft.getInstance().fontRenderer.func_238407_a_(matrixStack, text, (float)(x - Minecraft.getInstance().fontRenderer.func_243245_a(text) / 2), (float)y, color);
    }

    public void createDynamicTexture(Minigame minigame)
    {
        if(future == null && minigame.displayIcon != null)
        {
            future = CompletableFuture.runAsync(() -> {
                try
                {
                    URL url = new URL(minigame.displayIcon);
                    try (InputStream is = url.openStream()) {
                        image = NativeImage.read(is);
                        texture = (new DynamicTexture(image));
                        resourceLocation = Minecraft.getInstance().getTextureManager().getDynamicTextureLocation("modpackicon/", texture);
                    }
                } catch (IOException ignored) {}
            });
        }
    }

    public void renderImage(MatrixStack matrixStack, ResourceLocation location, int x, int y)
    {
        ResourceLocation unknown = new ResourceLocation("minecraft", "textures/misc/unknown_server.png");

        if(location != null)
        {
            this.mc.getTextureManager().bindTexture(location);
        }
        else
        {
            this.mc.getTextureManager().bindTexture(unknown);
        }
        RenderSystem.enableBlend();
        AbstractGui.blit(matrixStack, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    public Minigame getMiniGame()
    {
        return minigame;
    }
}
