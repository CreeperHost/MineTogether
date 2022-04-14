package net.creeperhost.minetogether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.creeperhost.polylib.client.screenbuilder.ScreenBuilder;
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
    private boolean isPregenMessage = false;
    String chunksDone = "";
    String totalChunks = "";
    int ticks = 0;
    ScreenBuilder screenBuilder = new ScreenBuilder(Constants.GUI_SHEET_LOCATION);

    protected MixinDisconnectedScreen(Component component) { super(component); }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci)
    {
        if(reason != null) isPregenMessage = reason.getString().startsWith("MineTogether");
        if(isPregenMessage)
        {
            try
            {
                String[] strings = reason.getString().split(":");
                totalChunks = strings[1];
                chunksDone = strings[2];
            } catch (Exception e)
            {
                isPregenMessage = false;
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(PoseStack poseStack, int i, int j, float partialTicks, CallbackInfo ci)
    {
        if(isPregenMessage)
        {
            try
            {
                int totalChunksInt = Integer.parseInt(totalChunks.trim());
                int chunksDoneInt = Integer.parseInt(chunksDone.trim());

                String string = percentage(totalChunksInt, chunksDoneInt) + "% done";
                //TODO bring this back
//                screenBuilder.drawBigBlueBar(poseStack, width / 2 - 128, height / 2 + 50, chunksDoneInt, totalChunksInt, i, j, string, 256, 256);

            } catch (Exception ignored) {}

            ticks++;
            //TODO replace with LoadingSpinner.class
            ScreenHelper.loadingSpin(poseStack, partialTicks, ticks, width / 2, height / 2 - 80 , new ItemStack(Items.COOKED_BEEF));
        }
    }

    public int percentage(int MaxValue, int CurrentValue)
    {
        if (CurrentValue == 0) return 0;
        return (int) ((CurrentValue * 100.0f) / MaxValue);
    }
}
