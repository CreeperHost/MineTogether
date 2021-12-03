package net.creeperhost.minetogethergui.gif;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageRenderer
{
    private final int glTexture;
    private final Image image;

    public ImageRenderer(Image image)
    {
        this.image = image;
        glTexture = TextureUtil.generateTextureId();
        TextureUtil.prepareImage(glTexture, 0, image.width, image.height);
        Image.toNativeImage().upload(0, 0, 0, 0, 0, image.width, image.height, false, true);
    }

    public void render(PoseStack matrixStack, int x, int y, int width, int height, float partialTicks)
    {
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.bindTexture(glTexture);
        GuiComponent.blit(matrixStack, x, y, 0.0F, 0.0F, width, height, width, height);
    }

    public static Image fromURL(URL url) throws IOException
    {
//        if(ImageUtils.isImageUrl(url))
//        {
            byte[] bytes = IOUtils.toByteArray(url);
            return Image.fromMemory(bytes);
//        }
//        return null;
    }

    public static class Image
    {
        static int width;
        static int height;
        static int[] pixels;

        public Image(int width, int height, int[] pixels)
        {
            this.width = width;
            this.height = height;
            this.pixels = pixels;
        }

        public static Image fromMemory(byte[] fileData)
        {
            final ByteBuffer gif = MemoryUtil.memAlloc(fileData.length);
            try
            {
                gif.put(fileData);
                gif.position(0);
                try (final MemoryStack stack = MemoryStack.stackPush())
                {
                    final IntBuffer x = stack.mallocInt(1);
                    final IntBuffer y = stack.mallocInt(1);
                    final IntBuffer channels = stack.mallocInt(1);

                    ByteBuffer image = STBImage.stbi_load_from_memory(gif, x, y, channels, 4);
                    try
                    {
                        if(image == null) return null;

                        width = x.get();
                        height = y.get();
                        IntBuffer pixelData = image.asIntBuffer();
                        int[] pixels = new int[width * height];
                        pixelData.get(pixels);

                        return new Image(width, height, pixels);
                    }
                    finally
                    {
                        if (image != null) STBImage.stbi_image_free(image);
                    }
                }
            }
            finally
            {
                MemoryUtil.memFree(gif);
            }
        }

        public static NativeImage toNativeImage()
        {
            NativeImage img = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    img.setPixelRGBA(x, y, pixels[y * width + x]);
                }
            }
            return img;
        }
    }
}
