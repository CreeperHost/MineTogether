package net.creeperhost.minetogether.mixin.chat.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.LocalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    public abstract double getScale();

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
        // Don't render our additional background blackout if chat is not enabled, or chat is not focused.
        if (!LocalConfig.instance().chatEnabled || minecraft.options.hideGui || !isChatFocused()) return;

        float scale = (float)getScale();
        int width = Mth.ceil((float)this.getWidth() + (12 * scale));
        int height = Mth.ceil(getHeight() * scale);

        // Render new 'filled' background under all chat lines.
        GuiComponent.fill(poseStack, 0, 8, width, -height + 8, minecraft.options.getBackgroundColor(0x80000000));

        // If we are on a MineTogether tab, draw our logo.
        if (MineTogetherChat.getTarget() != ChatTarget.VANILLA){
            int logoSize = (int) (Math.min(width, height) * 0.9D);
            mineTogether$drawLogo(poseStack, minecraft.font, -4 + (width / 2) - (logoSize / 2), -((height / 2) + (logoSize / 2)) + 8, logoSize, logoSize);
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
        if (!isChatFocused() || !LocalConfig.instance().chatEnabled || Minecraft.getInstance().options.hideGui) {
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

    /**
     * Draws the Mine Together logo with the "Created By {CreeperHost Logo}" bellow it.
     * The entire thing will be scaled appropriately to fit within the specified bounds.
     */
    @Unique
    private static void mineTogether$drawLogo(PoseStack stack, Font font, int x, int y, int width, int height) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        String created = "Created by";
        int strWidth = font.width(created);
        int creeperHeight = 19; //Value chosen so that Creeper Host logo text is roughly the same size as the "Created By" text
        int creeperWidth = (int) (creeperHeight * (960D / 266D)); //Computes width based on the image's aspect ratio.
        int createdWidth = strWidth + 2 + creeperWidth;
        float createdScale = width / (float) createdWidth;
        int creeperOffset = (int) ((font.lineHeight / 2D) - (creeperHeight / 2D));
        int creeperSHeight = (int) (creeperHeight * createdScale);

        stack.pushPose();
        stack.translate(x, y + height - (creeperHeight * createdScale) - creeperOffset, 0);
        stack.scale(createdScale, createdScale, createdScale);

        RenderSystem.setShaderTexture(0, Constants.CREEPERHOST_LOGO_25);
        RenderSystem.enableBlend();
        GuiComponent.blit(stack, createdWidth - creeperWidth, creeperOffset, 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);
        font.drawShadow(stack, created, 0, 0, 0x40FFFFFF);

        stack.popPose();

        int mtHeight = height - creeperSHeight - 4;
        int mtWidth = (int) (mtHeight * (348D / 318D));

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, Constants.MINETOGETHER_LOGO_25);
        RenderSystem.enableBlend();
        GuiComponent.blit(stack, x + (int) ((width / 2D) - (mtWidth / 2D)), y, 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        RenderSystem.disableBlend();
    }
}
