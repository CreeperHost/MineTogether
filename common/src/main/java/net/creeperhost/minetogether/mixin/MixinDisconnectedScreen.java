package net.creeperhost.minetogether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class MixinDisconnectedScreen extends Screen
{
    @Shadow @Final private Component reason;
    private boolean isPregenMessage;
    int ticks;

    protected MixinDisconnectedScreen(Component component) { super(component); }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci)
    {
        isPregenMessage = reason.getString().startsWith("MineTogether:");
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(PoseStack poseStack, int i, int j, float partialTicks, CallbackInfo ci)
    {
        if(isPregenMessage)
        {
            ticks++;
            ScreenHelpers.loadingSpin(partialTicks, ticks, width / 2, height / 2 - 80 , new ItemStack(Items.COOKED_BEEF));
        }
    }
}
