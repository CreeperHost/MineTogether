package net.creeperhost.minetogethergui.gif;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogetherlib.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnimatedGif
{
    private static final int GIF_TICKS_PER_SECOND = 1000;
    private static final int MC_TICKS_PER_SECOND = 20;
    private static final int MIN_GIF_TICKS = MathHelper.ceil(0.01f * GIF_TICKS_PER_SECOND); // Most browsers have approximately 0.1s minimum interval between frames. Let's respect that!
    private static final int MIN_MC_TICKS = MathHelper.ceil(MIN_GIF_TICKS * (float)MC_TICKS_PER_SECOND / GIF_TICKS_PER_SECOND);
    public static Executor GIF_EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("minetogether-friend-%d").build());

    private final int width;
    private final int height;
    private final int frames;
    private final int[] pixels;
    private final int[] delays;

    //For gifs
    public AnimatedGif(int width, int height, int frames, int[] pixels, int[] delays)
    {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.pixels = pixels;
        this.delays = delays;
    }

    public static AnimatedGif fromURL(URL url) throws IOException
    {
        if(ImageUtils.isImageUrl(url))
        {
            byte[] bytes = IOUtils.toByteArray(url);
            if(ImageUtils.getContentType(url).equals("image/gif")) return fromMemory(bytes);
        }
        return null;
    }

    private static AnimatedGif fromMemory(byte[] fileData)
    {
        final ByteBuffer gif = MemoryUtil.memAlloc(fileData.length);
        try
        {
            gif.put(fileData);
            gif.position(0);
            try (final MemoryStack stack = MemoryStack.stackPush())
            {
                final PointerBuffer delaysBuffer = stack.mallocPointer(1);
                final IntBuffer x = stack.mallocInt(1);
                final IntBuffer y = stack.mallocInt(1);
                final IntBuffer z = stack.mallocInt(1);
                final IntBuffer channels = stack.mallocInt(1);

                ByteBuffer image = STBImage.stbi_load_gif_from_memory(gif, delaysBuffer, x, y, z, channels, 0);
                try
                {
                    if (image == null)
                    {
//                        return new AnimatedGif(0, 0, 0, null, null, true, getBase64EncodedImage(fileData));
                        return null;
                    }

                    int nch = channels.get();
                    if (nch != 4)
                    {
                        throw new RuntimeException("Unexpected number of channels " + nch + ", expected 4");
                    }

                    int width = x.get();
                    int height = y.get();
                    int frames = z.get();

                    IntBuffer delaysIntBuffer = delaysBuffer.getIntBuffer(frames);
                    int[] delays = new int[frames];
                    delaysIntBuffer.get(delays);

                    IntBuffer pixelData = image.asIntBuffer();
                    int[] pixels = new int[width * height * frames];
                    pixelData.get(pixels);

                    return new AnimatedGif(width, height, frames, pixels, delays);
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

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getFrames()
    {
        return frames;
    }

    public NativeImage toNativeImage()
    {
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, width, height * frames, false);
        for (int y = 0; y < height * frames; y++)
        {
            for (int x = 0; x < width; x++)
            {
                img.setPixelRGBA(x, y, pixels[y * width + x]);
            }
        }
        return img;
    }

    public int convertToMcTick(int delay)
    {
        return MathHelper.ceil(delay * (float)MC_TICKS_PER_SECOND / GIF_TICKS_PER_SECOND); // gif delays at in 1/100s increments, mc ticks are 1/20
    }


    public GifPlayer makeGifPlayer()
    {
        return new GifPlayer();
    }

    public class GifPlayer implements AutoCloseable
    {
        private final int glTexture;
        private final int totalFrameTicks;
        private boolean playing;
        private int animationProgress;
        private int lastFrame;
        /*
         While playing, holds the partial offset from the start tick.
         While stopped, holds the partial tick progress at the point it was stopped.
         */
        private float partialStart;
        private boolean autoplay;
        private boolean looping = true;

        private GifPlayer()
        {
            totalFrameTicks = Arrays.stream(delays).map(d -> Math.max(MIN_GIF_TICKS, d)).sum();
            glTexture = TextureUtil.generateTextureId();
            TextureUtil.prepareImage(glTexture, 0, width, height * frames);
            toNativeImage().upload(0, 0, 0, 0, 0, width, height * frames, false, true);
        }

        public void reset()
        {
            animationProgress = 0;
            partialStart = 0;
            playing = false;
        }

        public void restart(float partialTicks)
        {
            reset();
            start(partialTicks);
        }

        public void start(float partialTicks)
        {
            playing = true;
            partialStart = partialTicks - partialStart;
        }

        public void stop(float partialTicks)
        {
            partialStart = partialTicks - partialStart;
            playing = false;
            autoplay = false;
        }

        /**
         * Call this on your gui tick. Necessary to keep proper time.
         */
        public void tick()
        {
            animationProgress++;
        }

        public void render(PoseStack matrixStack, int x, int y, int w, int h, float partialTicks)
        {
            if (totalFrameTicks == 0) return;

            if (!playing && autoplay) start(partialTicks);

            if (playing)
            {
                float frameTime = ((animationProgress + partialTicks - partialStart) * (float)GIF_TICKS_PER_SECOND) / MC_TICKS_PER_SECOND;
                int frameNumber = MathHelper.floor(frameTime) % totalFrameTicks;
                int frameIndex = -1;
                for (int i = 0; i < delays.length; i++)
                {
                    int d = Math.max(delays[i], MIN_GIF_TICKS);
                    if (d > frameNumber)
                    {
                        frameIndex = i;
                        break;
                    }
                    frameNumber -= d;
                }
                if (frameIndex < 0)
                {
                    if (looping)
                    {
                        frameIndex = 0;
                    }
                    else
                    {
                        playing = false;
                        return;
                    }
                }
                lastFrame = frameIndex;
            }

            RenderSystem.enableTexture();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.bindTexture(glTexture);
            Screen.blit(matrixStack, x, y, w, h, 0, lastFrame * height, width, height, width, height * frames);
        }

        public void close()
        {
            TextureUtil.releaseTextureId(glTexture);
        }

        public void setAutoplay(boolean autoplay)
        {
            this.autoplay = autoplay;
        }

        public boolean getAutoplay()
        {
            return autoplay;
        }

        public void setLooping(boolean looping)
        {
            this.looping = looping;
        }

        public boolean getLooping()
        {
            return looping;
        }
    }
}
