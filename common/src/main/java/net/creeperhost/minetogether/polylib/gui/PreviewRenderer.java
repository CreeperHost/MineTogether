package net.creeperhost.minetogether.polylib.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 19/10/22.
 */
public abstract class PreviewRenderer implements Widget {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService PREVIEW_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("preview-render-%d").setDaemon(true).build());
    private static final Set<URL> NO_PREVIEW = Collections.synchronizedSet(new HashSet<>());
    private static final Cache<URL, Preview> CACHE = CacheBuilder.newBuilder() // TODO, weak values? we dont have anything else holding onto them, which should mean they die only when required by memory pressure.
            .expireAfterAccess(5, TimeUnit.MINUTES) // TODO Tweak this.
            .removalListener(e -> {
                if (e.wasEvicted()) {
                    try {
                        ((Preview) e.getValue()).close();
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to close Preview: {}", e.getKey(), ex);
                    }
                }
            })
            .build();

    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;

    protected PreviewRenderer(int xOffset, int yOffset, int width, int height) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        URL url = getUrlUnderMouse(mouseX, mouseY);
        if (url == null) return;

        Preview preview = getPreview(url);
        if (preview == null) return;

        preview.render(pStack, mouseX + xOffset, mouseY + yOffset, width, height, partialTicks);
    }

    @Nullable
    protected abstract URL getUrlUnderMouse(int mouseX, int mouseY);

    @Nullable
    private static Preview getPreview(URL url) {
        if (NO_PREVIEW.contains(url)) return null;

        Preview preview = CACHE.getIfPresent(url);
        if (preview != null) return preview;
        synchronized (CACHE) {
            preview = CACHE.getIfPresent(url);
            if (preview != null) return preview;

            preview = compute(url);
            CACHE.put(url, preview);

            return preview;
        }
    }

    private static Preview compute(URL url) {
        LoadingPreview preview = new LoadingPreview();
        PREVIEW_EXECUTOR.execute(() -> load(url, preview));
        return preview;
    }

    private static void load(URL url, LoadingPreview preview) {
        try {
            byte[] bytes = IOUtils.toByteArray(url);
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
            preview.setWrapped(new ImagePreview(image));
        } catch (IOException ex) {
            LOGGER.error("Failed to load preview for: {}", url, ex);
        }
    }

    private static abstract class Preview implements AutoCloseable {

        public abstract void render(PoseStack pStack, int x, int y, int w, int h, float partialTicks);
    }

    private static class LoadingPreview extends Preview {

        private Preview wrapped;
        public boolean closed;

        public LoadingPreview() {
        }

        @Override
        public void render(PoseStack pStack, int x, int y, int w, int h, float partialTicks) {
            if (closed) return;
            if (wrapped != null) {
                wrapped.render(pStack, x, y, w, h, partialTicks);
            } else {
                // TODO render LOADING dirt or gif.
            }

        }

        public void setWrapped(Preview wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void close() throws Exception {
            closed = true;
            if (wrapped != null) {
                wrapped.close();
            }
        }
    }

    private static class ImagePreview extends Preview {

        private final NativeImage image;
        private int glTexture = -1;

        private ImagePreview(NativeImage image) {
            this.image = image;
        }

        @Override
        public void render(PoseStack pStack, int x, int y, int w, int h, float partialTicks) {
            if (glTexture == -1) {
                glTexture = TextureUtil.generateTextureId();
                TextureUtil.prepareImage(glTexture, 0, image.getWidth(), image.getHeight());
                image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, true);
            }

            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(0, glTexture);
            GuiComponent.blit(pStack, x, y, 0.0F, 0.0F, w, h, w, h);
        }

        @Override
        public void close() throws Exception {
            TextureUtil.releaseTextureId(glTexture);
            glTexture = -1;
            image.close();
        }
    }

}
