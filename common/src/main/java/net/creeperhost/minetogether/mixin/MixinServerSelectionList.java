package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.multiplayer.data.CreeperHostServerEntry;
import net.creeperhost.minetogether.module.multiplayer.screen.JoinMultiplayerScreenPublic;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadPoolExecutor;

@Mixin(ServerSelectionList.class)
public class MixinServerSelectionList
{
    @Shadow @Final private JoinMultiplayerScreen screen;

    @Inject(at=@At("RETURN"), method= "refreshEntries()V")
    private void afterRefreshEntries(CallbackInfo info)
    {
        if(Config.getInstance().isMpMenuEnabled() && !(screen instanceof JoinMultiplayerScreenPublic))
        {
            ServerSelectionList thisFake = (ServerSelectionList)(Object) this;
            thisFake.children().add(0, new CreeperHostServerEntry(thisFake));
        }
    }
}
