package net.creeperhost.minetogether.mixin.chat.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
@Mixin(ChatComponent.class)
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

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void onRender(GuiGraphics graphics, int i, int mouseX, int mouseY, CallbackInfo ci) {
        // Don't render our additional background blackout if chat is not enabled, or chat is not focused.
        if (!Config.instance().chatEnabled || Minecraft.getInstance().options.hideGui || !isChatFocused()) return;

        //This is now replicating what vanilla does exactly as of 1.20
        //Except of one thing. Vanilla translates +50z which breaks the vanilla chat scroll bar.

        float scale = (float)getScale();
        int width = Mth.ceil((float)this.getWidth() / scale) + 8;
        int height = getHeight();
        int guiHeight = graphics.guiHeight();
        int maxYPos = Mth.floor((float)(guiHeight - 40) / scale);

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1);
        graphics.pose().translate(4.0F, 0.0F, 0.0F);

        // Render new 'filled' background under all chat lines.
        graphics.fill(-4, maxYPos - height, width, maxYPos, minecraft.options.getBackgroundColor(0x80000000));

        int logoSize = (int) (Math.min(width, height) * 0.9D);

        // If we are on a MineTogether tab, draw our logo.
        if (MineTogetherChat.getTarget() != ChatTarget.VANILLA){
            drawLogo(graphics, minecraft.font, -4 + (width / 2) - (logoSize / 2), maxYPos - (height / 2) - (logoSize / 2), logoSize, logoSize);
        }

        graphics.pose().popPose();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                    ordinal = 0
            )
    )
    // When chat is focussed, this disables vanilla rendering the 'filled' background bellow a chat line.
    // We force-enable this fill if chat is disabled to revert to vanilla behaviour.
    private void onFill(GuiGraphics graphics, int i, int j, int k, int l, int m) {
        if (!isChatFocused() || !Config.instance().chatEnabled || Minecraft.getInstance().options.hideGui) {
            graphics.fill(i, j, k, l, m);
        }
    }

    @Inject(
            method = "rescaleChat",
            at = @At("HEAD")
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
    private static void drawLogo(GuiGraphics g, Font font, int x, int y, int width, int height) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        String created = "Created by";
        int strWidth = font.width(created);
        int creeperHeight = 19; //Value chosen so that Creeper Host logo text is roughly the same size as the "Created By" text
        int creeperWidth = (int) (creeperHeight * (960D / 266D)); //Computes width based on the image's aspect ratio.
        int createdWidth = strWidth + 2 + creeperWidth;
        float createdScale = width / (float) createdWidth;
        int creeperOffset = (int) ((font.lineHeight / 2D) - (creeperHeight / 2D));
        int creeperSHeight = (int) (creeperHeight * createdScale);

        g.pose().pushPose();
        g.pose().translate(x, y + height - (creeperHeight * createdScale) - creeperOffset, 0);
        g.pose().scale(createdScale, createdScale, createdScale);

        RenderSystem.enableBlend();
        g.blit(Constants.CREEPERHOST_LOGO_25, createdWidth - creeperWidth, creeperOffset, 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);
        g.drawString(font, created, 0, 0, 0x40FFFFFF, true);

        g.pose().popPose();

        int mtHeight = height - creeperSHeight - 4;
        int mtWidth = (int) (mtHeight * (348D / 318D));

        RenderSystem.enableBlend();
        g.blit(Constants.MINETOGETHER_LOGO_25, x + (int) ((width / 2D) - (mtWidth / 2D)), y, 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        RenderSystem.disableBlend();
    }
}
