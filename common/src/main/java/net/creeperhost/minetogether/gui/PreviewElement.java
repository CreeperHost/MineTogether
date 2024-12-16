package net.creeperhost.minetogether.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.creeperhost.minetogether.chat.gui.MessageElement;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiList;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 13/07/2024
 */
public class PreviewElement extends GuiElement<PreviewElement> {

    private static final Pattern META_IMAGE_PATTERN = Pattern.compile("^<meta +property=\"og:image(?::url)?\" +content=\"([^\"]+)\" *\\/>$");

    private static final Set<String> SUPPORTED_IMAGES = ImmutableSet.of(
            "image/jpeg",
            "image/png",
            "image/bmp",
            "image/gif" //Supports still frame gif or will show first frame of an animated gif.
    );

    private static final Set<String> ALLOWED_DOMAINS = ImmutableSet.of(
            "blockshot.ch"
    );

    private static final boolean DEBUG = Boolean.getBoolean("PreviewRenderer.debug");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final ExecutorService PREVIEW_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("preview-render-%d").setDaemon(true).build());
    private static final Set<URL> INVALID_URLS = Collections.synchronizedSet(new HashSet<>());
    private static final Cache<URL, ImageLoader> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener(e -> {
                if (e.wasEvicted()) {
                    try {
                        ((ImageLoader) e.getValue()).close();
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to close Preview: {}", e.getKey(), ex);
                    }
                }
            })
            .build();

    private URLProvider urlProvider;

    private int imageSize = 80;
    private boolean enforceDomains = true;

    public PreviewElement(@NotNull GuiParent<?> parent) {
        super(parent);
    }

    public PreviewElement setUrlProvider(URLProvider urlProvider) {
        this.urlProvider = urlProvider;
        return this;
    }

    public PreviewElement setImageSize(int imageSize) {
        this.imageSize = imageSize;
        return this;
    }

    public PreviewElement setEnforceDomains(boolean enforceDomains) {
        this.enforceDomains = enforceDomains;
        return this;
    }

    @Override
    public boolean renderOverlay(GuiRender render, double mouseX, double mouseY, float partialTicks, boolean consumed) {
        if (super.renderOverlay(render, mouseX, mouseY, partialTicks, consumed) || urlProvider == null) return true;

        URLInfo url = urlProvider.getUrlUnderMouse((int) mouseX, (int) mouseY);
        if (url == null) return false;
        ImageLoader image = getImage(url);
        if (image == null) return false;

        if (image.isLoaded()) {
            double aspect = image.width() / (double) image.height();
            double width = aspect > 1 ? imageSize : imageSize * aspect;
            double height = aspect > 1 ? imageSize / aspect : imageSize;
            double border = 3;

            double x = Math.min(mouseX, scaledScreenWidth() - (width + (border * 2)));
            double y = Math.min(mouseY, scaledScreenHeight() - (height + (border * 2)));

            render.toolTipBackground(x, y, width + (border * 2), height + (border * 2));
            image.render(render, x + border, y + border, width, height);
        } else {
            render.renderTooltip(Component.translatable("minetogether:gui.chat.loading_preview"), mouseX, mouseY);
        }

        return true;
    }

    @Nullable
    private ImageLoader getImage(URLInfo info) {
        URL url = info.url();
        if (enforceDomains && !info.admin() && !ALLOWED_DOMAINS.contains(url.getHost())) return null;
        if (INVALID_URLS.contains(url)) return null;

        synchronized (CACHE) {
            ImageLoader cached = CACHE.getIfPresent(url);
            if (cached != null) {
                return cached;
            }

            ImageLoader preview = new ImageLoader();
            PREVIEW_EXECUTOR.execute(() -> preview.load(url, false));
            CACHE.put(url, preview);
            return preview;
        }
    }

    @Nullable
    public static URL urlFromStyle(@Nullable Style style) {
        if (style == null) return null;
        HoverEvent event = style.getHoverEvent();
        if (event == null || event.getAction() != MessageFormatter.SHOW_URL_PREVIEW) return null;
        Component value = event.getValue(MessageFormatter.SHOW_URL_PREVIEW);
        try {
            return URI.create(value.getString()).toURL();
        } catch (Throwable ex) {
            return null;
        }
    }

    public static PreviewElement.URLInfo getHoveredURL(GuiList<Message> chatList, double mouseX, double mouseY) {
        for (GuiElement<?> child : chatList.getChildren()) {
            if (!(child instanceof MessageElement msgEle) || !child.isMouseOver()) {
                continue;
            }
            Message message = msgEle.getMessage();
            Style style = msgEle.getStyleAtPos(mouseX, mouseY);
            URL url = PreviewElement.urlFromStyle(style);
            if (url == null) return null;
            return new PreviewElement.URLInfo(url, message != null && message.sender == null);
        }
        return null;
    }

    public interface URLProvider {
        URLInfo getUrlUnderMouse(double mouseX, double mouseY);
    }

    public record URLInfo(URL url, boolean admin){}

    private static class ImageLoader {
        @Nullable
        private NativeImage image = null;
        private int glTexture = -1;
        private volatile boolean loaded = false;

        private void load(URL url, boolean ogRedirect) {
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(new HttpGet(url.toURI()))) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    if (DEBUG) LOGGER.info("Ignoring {} for preview, Entity null", url);
                    INVALID_URLS.add(url);
                    return;
                }

                if (SUPPORTED_IMAGES.contains(entity.getContentType().getValue())) {
                    BufferedImage bufferedImage = ImageIO.read(entity.getContent());
                    image = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
                    for (int x = 0; x < bufferedImage.getWidth(); x++) {
                        for (int y = 0; y < bufferedImage.getHeight(); y++) {
                            image.setPixel(x, y, bufferedImage.getRGB(x, y));
                        }
                    }
                    loaded = true;
                    return;
                }

                if (ogRedirect) {
                    if (DEBUG) LOGGER.info("Ignoring {} for preview, Invalid og:image type {}", url, entity.getContentType());
                    INVALID_URLS.add(url);
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = META_IMAGE_PATTERN.matcher(line);
                        if (matcher.find()) {
                            String link = matcher.group(1);
                            if (link != null) {
                                load(URI.create(link).toURL(), true);
                            }
                            return;
                        }
                    }
                }
                INVALID_URLS.add(url);
            } catch (Throwable ex) {
                LOGGER.error("Failed to load preview for: {}", url, ex);
                INVALID_URLS.add(url);
            }
        }

        public boolean isLoaded() {
            return loaded;
        }

        private int width() {
            return image.getWidth();
        }

        private int height() {
            return image.getHeight();
        }

        public void render(GuiRender render, double x, double y, double width, double height) {
            if (glTexture == -1) {
                glTexture = TextureUtil.generateTextureId();
                TextureUtil.prepareImage(glTexture, 0, image.getWidth(), image.getHeight());
                image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, true);
            }

            double x2 = x + width;
            double y2 = y + height;
            RenderSystem.setShaderTexture(0, glTexture);
            RenderSystem.setShader(CoreShaders.POSITION_TEX);
            Matrix4f matrix4f = render.pose().last().pose();

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.addVertex(matrix4f, (float) x, (float) y, (float) 0).setUv(0, 0);
            bufferBuilder.addVertex(matrix4f, (float) x, (float) y2, (float) 0).setUv(0, 1);
            bufferBuilder.addVertex(matrix4f, (float) x2, (float) y2, (float) 0).setUv(1, 1);
            bufferBuilder.addVertex(matrix4f, (float) x2, (float) y, (float) 0).setUv(1, 0);
            BufferUploader.drawWithShader(bufferBuilder.build());
        }

        public void close() {
            if (glTexture != -1) {
                TextureUtil.releaseTextureId(glTexture);
                glTexture = -1;
            }
            if (image != null) image.close();
        }
    }
}
