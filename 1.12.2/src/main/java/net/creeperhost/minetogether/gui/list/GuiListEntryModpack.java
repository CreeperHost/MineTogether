package net.creeperhost.minetogether.gui.list;

import net.creeperhost.minetogether.gui.GuiModPackList;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.FutureTask;

public class GuiListEntryModpack extends GuiListEntry
{
    private GuiModPackList modpackList;
    Callbacks.Modpack modpack;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private DynamicTexture texture;
    private ResourceLocation resourceLocation;
    private BufferedImage image;

    public GuiListEntryModpack(GuiModPackList modPackList, GuiList list, Callbacks.Modpack modpack)
    {
        super(list);
        this.modpackList = modPackList;
        this.modpack = modpack;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRendererObj.getStringWidth(cross);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
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

        drawCenteredString(this.mc.fontRendererObj, modpack.getName(), x + (listWidth / 2), y + 5, 16777215);
        drawCenteredString(this.mc.fontRendererObj, TextFormatting.GRAY + modpack.getDisplayVersion(), x + (listWidth / 2), y + 20, 16777215);

        if(resourceLocation == null)
        {
            createDynamicTexture(modpack);
        }
        renderImage(resourceLocation, x - 45, y);
    }

    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawStringWithShadow(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
    }

    String test = "https://files.gigabit101.net/screenshots/test.gif";

    public void createDynamicTexture(Callbacks.Modpack modpack)
    {
        try
        {
            if (texture == null && resourceLocation == null)
            {
                if(image == null)
                {
                    Runnable runnable = () ->
                    {
                        try
                        {
                            URL url = new URL(modpack.getDisplayIcon());
                            image = ImageIO.read(url);
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
                texture = (new DynamicTexture(image));
                resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("modpackicon/", texture);
            }
        } catch (Exception ignored) {}
    }

    public void renderImage(ResourceLocation location, int x, int y)
    {
        if(location != null)
        {
            this.mc.getTextureManager().bindTexture(location);
            GlStateManager.enableBlend();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
            GlStateManager.disableBlend();
        }
    }

    public Callbacks.Modpack getModpack()
    {
        return modpack;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        return super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, x, y);
    }
}
