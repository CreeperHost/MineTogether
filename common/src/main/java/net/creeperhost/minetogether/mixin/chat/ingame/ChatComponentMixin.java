package net.creeperhost.minetogether.mixin.chat.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author covers1624
 */
@Mixin (ChatComponent.class)
abstract class ChatComponentMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    public abstract int getHeight();

    @Shadow
    public abstract int getWidth();

    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    public abstract List<String> getRecentChat();

    @Shadow
    public abstract int getLinesPerPage();

    @Inject (
            method = "render",
            at = @At ("HEAD")
    )
    private void onRender(PoseStack poseStack, int i, CallbackInfo ci) {
        // Don't render our additional background blackout if chat is not enabled.
        if (!Config.instance().chatEnabled) return;

        if (isChatFocused()) {
            // Render new 'filled' background under all chat lines.
            int y = getHeight() - 175 - (minecraft.font.lineHeight * Math.max(Math.min(getRecentChat().size(), getLinesPerPage()), 20));
            GuiComponent.fill(poseStack, 0, y, getWidth() + 6, getHeight() + 10 + y, minecraft.options.getBackgroundColor(Integer.MIN_VALUE));

            // If we are on a MineTogether tab, draw our logo.
            if (MineTogetherChat.getTarget() != ChatTarget.VANILLA) {
                int w = Mth.ceil((float) getWidth() / (float) minecraft.options.chatScale);
                int h = Mth.ceil((float) getHeight() / (float) minecraft.options.chatScale);
                drawLogo(poseStack, minecraft.font, w + 6, h + 6, -2, getHeight() - 340, 0.75F);
            }
        }
    }

    @Redirect (
            method = "render",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V",
                    ordinal = 0
            )
    )
    // When chat is focussed, this disables vanilla rendering the 'filled' background bellow a chat line.
    // We force-enable this fill if chat is disabled to revert to vanilla behaviour.
    private void onFill(PoseStack poseStack, int i, int j, int k, int l, int m) {
        if (!isChatFocused() || !Config.instance().chatEnabled) {
            GuiComponent.fill(poseStack, i, j, k, l, m);
        }
    }

    @Inject (
            method = "rescaleChat",
            at = @At ("HEAD")
    )
    private void onRescaleChat(CallbackInfo ci) {
        if (MineTogetherChat.getTarget() == ChatTarget.VANILLA) {
            MineTogetherChat.publicChat.rescaleChat();
        }
    }

    private static void drawLogo(PoseStack pStack, Font font, int containerWidth, int containerHeight, int containerX, int containerY, float scale) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        float invScale = 1 / scale;
        int width = (int) (containerWidth * invScale);
        int height = (int) (containerHeight * invScale);
        int x = (int) (containerX * invScale);
        int y = (int) (containerY * invScale);

        pStack.pushPose();
        pStack.scale(scale, scale, scale);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;

        totalHeight *= invScale;

        RenderSystem.setShaderTexture(0, Constants.MINETOGETHER_LOGO_25);
        RenderSystem.enableBlend();
        GuiComponent.blit(pStack, x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = font.width(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        font.drawShadow(pStack, created, x + (width / 2F - (creeperTotalWidth / 2F)), y + (height / 2F - (totalHeight / 2F) + mtHeight + 7), 0x40FFFFFF);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, Constants.CREEPERHOST_LOGO_25);
        RenderSystem.enableBlend();
        GuiComponent.blit(pStack, x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);

        RenderSystem.disableBlend();
        pStack.popPose();
    }
}
