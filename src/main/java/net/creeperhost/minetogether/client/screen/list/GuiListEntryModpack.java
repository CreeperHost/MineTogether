package net.creeperhost.minetogether.client.screen.list;

import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.client.screen.order.GuiModPackList;
import net.creeperhost.minetogether.data.ModPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class GuiListEntryModpack extends GuiListEntry
{
    private GuiModPackList modpackList;
    ModPack modpack;
    private float transparency = 0.5F;
    private DynamicTexture texture;
    private ResourceLocation resourceLocation;
    private NativeImage image;
    private Future<?> future;
    
    public GuiListEntryModpack(GuiModPackList modPackList, GuiList list, ModPack modpack)
    {
        super(list);
        this.modpackList = modPackList;
        this.modpack = modpack;
    }
    
    @Override
    public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
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

        int maxLength = 35;

        drawCenteredString(mc.fontRenderer, modpack.getName().substring(0, Math.min(modpack.getName().length(), maxLength)), x + (listWidth / 2), y + 5, 16777215);
        drawCenteredString(mc.fontRenderer, TextFormatting.GRAY + modpack.getDisplayVersion().substring(0, Math.min(modpack.getDisplayVersion().length(), maxLength)), x + (listWidth / 2), y + 20, 16777215);

        if(resourceLocation == null)
        {
            createDynamicTexture(modpack);
        }
        renderImage(resourceLocation, x - 36, y);
    }

    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawStringWithShadow(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
    }

    public void createDynamicTexture(ModPack modpack)
    {
        if(future == null && modpack.getDisplayIcon() != null)
        {
            future = CompletableFuture.runAsync(() -> {
                try
                {
                    URL url = new URL(modpack.getDisplayIcon());
                    try (InputStream is = url.openStream()) {
                        image = NativeImage.read(is);
                        texture = (new DynamicTexture(image));
                        resourceLocation = Minecraft.getInstance().getTextureManager().getDynamicTextureLocation("modpackicon/", texture);
                    }
                } catch (IOException ignored) {}
            });
        }
    }

    public void renderImage(ResourceLocation location, int x, int y)
    {
        if(location != null)
        {
            this.mc.getTextureManager().bindTexture(location);
            RenderSystem.enableBlend();
            AbstractGui.blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }
    }
    
    public ModPack getModpack()
    {
        return modpack;
    }
}
