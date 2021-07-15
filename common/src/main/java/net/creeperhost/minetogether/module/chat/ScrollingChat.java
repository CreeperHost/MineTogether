package net.creeperhost.minetogether.module.chat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.creeperhost.minetogether.screen.MineTogetherScreen;
import net.creeperhost.minetogether.util.ComponentUtils;
import net.creeperhost.minetogethergui.gif.AnimatedGif;
import net.creeperhost.minetogethergui.gif.ImageRenderer;
import net.creeperhost.minetogethergui.gif.ImageUtils;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static net.creeperhost.minetogetherlib.chat.ChatHandler.ircLock;

public class ScrollingChat extends ObjectSelectionList
{
    private ArrayList<FormattedCharSequence> lines;
    private final int height;
    private final int top;
    private final int bottom;
    private final int itemHeight;
    private final MineTogetherScreen screen;
    private final int chatOffset;
    private final boolean renderBackground;
    private AnimatedGif gifImage;
    private AnimatedGif.GifPlayer gifPlayer;
    private ImageRenderer.Image image;
    private ImageRenderer imageRenderer;

    public ScrollingChat(MineTogetherScreen screen, int width, int height, int chatOffset)
    {
        super(Minecraft.getInstance(), width - 20, height - 50, 30, height - 50, 10);
        this.height = height - 50;
        this.width = width - 20;
        this.top = 30;
        this.bottom = height - 50;
        this.itemHeight = 10;
        this.chatOffset = chatOffset;
        this.screen = screen;
        lines = new ArrayList<>();
        this.renderBackground = true;
    }

    public ScrollingChat(MineTogetherScreen screen, int widthIn, int heightIn, int topIn, int bottomIn, int chatOffset, boolean renderBackground)
    {
        super(Minecraft.getInstance(), widthIn, heightIn, topIn, bottomIn, 10);
        this.height = heightIn;
        this.width = widthIn;
        this.top = 30;
        this.bottom = height;
        this.itemHeight = 10;
        this.screen = screen;
        lines = new ArrayList<>();
        this.chatOffset = chatOffset;
        this.renderBackground = renderBackground;
    }

    public void renderEntry(PoseStack poseStack, int index, int mouseX, int mouseY, float partialTicks)
    {
        try
        {
            FormattedCharSequence component = lines.get(index);
            int totalWidth = chatOffset;

            int oldTotal = totalWidth;
            totalWidth += minecraft.font.width(component);

            boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(index) && mouseY < getRowTop(index) + itemHeight;

            Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);

            if (hovering)
            {
                RenderSystem.enableBlend();
                RenderSystem.color4f(1, 1, 1, 0.90F);
                minecraft.font.draw(poseStack, component, oldTotal, getRowTop(index), 0xBBFFFFFF);
                screen.renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
                RenderSystem.color4f(1, 1, 1, 1);

                if (style.getHoverEvent() != null && style.getHoverEvent().getAction() == ComponentUtils.RENDER_GIF)
                {
                    Component urlComponent = (Component) style.getHoverEvent().getValue(ComponentUtils.RENDER_GIF);
                    String url = urlComponent.getString();
                    if (ImageUtils.getContentType(new URL(url)).equals("image/gif"))
                    {
                        if (gifImage == null)
                        {
                            CompletableFuture.runAsync(() ->
                            {
                                try
                                {
                                    gifImage = AnimatedGif.fromURL(new URL(url));
                                } catch (IOException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }, AnimatedGif.GIF_EXECUTOR);
                        }
                        if (gifPlayer == null)
                        {
                            gifPlayer = gifImage.makeGifPlayer();
                            gifPlayer.setAutoplay(true);
                            gifPlayer.setLooping(true);
                        }
                    }
                    else
                    {
                        if (image == null)
                        {
                            CompletableFuture.runAsync(() ->
                            {
                                try
                                {
                                    image = ImageRenderer.fromURL(new URL(url));
                                } catch (IOException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }, AnimatedGif.GIF_EXECUTOR);
                        }
                        if (image != null && imageRenderer == null) imageRenderer = new ImageRenderer(image);
                    }
                }
                else
                {
                    imageRenderer = null;
                    image = null;
                    gifImage = null;
                    gifPlayer = null;
                }
            }
            else
            {
                minecraft.font.draw(poseStack, component, oldTotal, getRowTop(index), 0xFFFFFF);
            }
        } catch (Exception ignored)
        {
        }
    }

    @Override
    protected int getItemCount()
    {
        return lines.size();
    }

    public void updateLines(String key)
    {
        LimitedSizeQueue<Message> tempMessages;
        int oldMaxScroll = this.getMaxScroll();
        synchronized (ircLock)
        {
            if (ChatHandler.messages == null || ChatHandler.messages.size() == 0) return;
            tempMessages = ChatHandler.messages.get(key);
        }

        ArrayList<FormattedCharSequence> oldLines = lines;
        int listHeight = this.height - (this.bottom - this.top - 4);
        lines = new ArrayList<>();
        if (tempMessages == null) return;
        try
        {
            for (Message message : tempMessages)
            {
                Component display = ChatFormatter.formatLine(message);
                if (display == null) continue;
                lines.addAll(ComponentRenderUtils.wrapComponents(display, width - 10, Minecraft.getInstance().font));
            }
        } catch (Exception ignored)
        {
        }
        if (lines.size() > oldLines.size() && this.getScrollAmount() == oldMaxScroll) ;
        {
            this.setScrollAmount(this.getMaxScroll());
        }
    }

    private int getRowBottom(int p_getRowBottom_1_)
    {
        return this.getRowTop(p_getRowBottom_1_) + this.itemHeight;
    }

    @Override
    protected boolean isSelectedItem(int i)
    {
        return false;
    }

    @Override
    protected int getScrollbarPosition()
    {
        return 0;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        if (renderBackground) this.renderBackground(poseStack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        if (renderBackground)
        {
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double) this.x0, (double) this.y1, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) this.y1, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) this.y0, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.y0, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tessellator.end();
        }

        int k = this.getRowLeft();
        int l = this.y0 + 4 - (int) this.getScrollAmount();

        //            ScreenHelpers.drawLogo(matrixStack, font, width - 20, height + 18, 20, 30, 0.75F);
        this.renderList(poseStack, k, l, mouseX, mouseY, partialTicks);
        if (renderBackground)
        {
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float f1 = 32.0F;
            int i1 = -100;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double) this.x0, (double) this.y0, -100.0D).uv(0.0F, (float) this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), (double) this.y0, -100.0D).uv((float) this.width / 32.0F, (float) this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), 0.0D, -100.0D).uv((float) this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.height, -100.0D).uv(0.0F, (float) this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), (double) this.height, -100.0D).uv((float) this.width / 32.0F, (float) this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), (double) this.y1, -100.0D).uv((float) this.width / 32.0F, (float) this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.y1, -100.0D).uv(0.0F, (float) this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tessellator.end();
        }
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        int j1 = 4;
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex((double) this.x0, (double) (this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        bufferbuilder.vertex((double) this.x1, (double) (this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        bufferbuilder.vertex((double) this.x1, (double) this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double) this.x0, (double) this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double) this.x0, (double) this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double) this.x1, (double) this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double) this.x1, (double) (this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
        bufferbuilder.vertex((double) this.x0, (double) (this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
        tessellator.end();

        this.renderDecorations(poseStack, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();

        if (gifPlayer != null && gifImage != null)
            gifPlayer.render(poseStack, mouseX + 5, mouseY + 5, 80, 60, partialTicks);
        if (imageRenderer != null) imageRenderer.render(poseStack, mouseX + 5, mouseY + 5, 80, 60, partialTicks);
    }

    public void tick()
    {
        if (gifPlayer != null) gifPlayer.tick();
    }

    @Override
    public int getRowTop(int i)
    {
        return super.getRowTop(i);
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getTop()
    {
        return top;
    }

    @Override
    protected void renderList(PoseStack poseStack, int p_renderList_1_, int p_renderList_2_, int mouseX, int mouseY, float p_renderList_5_)
    {
        int i = lines.size();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        if (!lines.isEmpty())
        {
            for (int j = 0; j < i; ++j)
            {
                int k = this.getRowTop(j);
                int l = this.getRowBottom(j);
                if (l >= this.y0 && k <= this.y1)
                {
                    int i1 = p_renderList_2_ + j * this.itemHeight + this.headerHeight;
                    int j1 = this.itemHeight - 4;
                    int k1 = this.getRowWidth();
                    if (this.isSelectedItem(j))
                    {
                        int l1 = this.x0 + this.width / 2 - k1 / 2;
                        int i2 = this.x0 + this.width / 2 + k1 / 2;
                        RenderSystem.disableTexture();
                        float f = this.isFocused() ? 1.0F : 0.5F;
                        RenderSystem.color4f(f, f, f, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                        bufferbuilder.vertex((double) l1, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.vertex((double) i2, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.vertex((double) i2, (double) (i1 - 2), 0.0D).endVertex();
                        bufferbuilder.vertex((double) l1, (double) (i1 - 2), 0.0D).endVertex();
                        tessellator.end();
                        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                        bufferbuilder.vertex((double) (l1 + 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.vertex((double) (i2 - 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.vertex((double) (i2 - 1), (double) (i1 - 1), 0.0D).endVertex();
                        bufferbuilder.vertex((double) (l1 + 1), (double) (i1 - 1), 0.0D).endVertex();
                        tessellator.end();
                        RenderSystem.enableTexture();
                    }
                    renderEntry(poseStack, j, mouseX, mouseY, p_renderList_5_);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
    {
        for (int i = 0; i < lines.size(); i++)
        {
            FormattedCharSequence component = lines.get(i);
            int totalWidth = 5;
            int oldTotal = totalWidth;
            totalWidth += minecraft.font.width(component);
            boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(i) && mouseY < getRowTop(i) + itemHeight;

            if (hovering)
            {
                Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);
                screen.handleComponentClicked(style, mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
}
