package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.connect.LanServerInfoConnect;
import net.creeperhost.minetogether.module.connect.OurServerListEntryLanDetected;
import net.creeperhost.minetogether.module.multiplayer.data.CreeperHostServerEntry;
import net.creeperhost.minetogether.module.multiplayer.screen.JoinMultiplayerScreenPublic;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.class)
public class MixinServerSelectionList
{
    @Shadow
    @Final
    private JoinMultiplayerScreen screen;

    @Inject(at = @At("RETURN"), method = "refreshEntries()V")
    private void afterRefreshEntries(CallbackInfo info)
    {
        if (Config.getInstance().isMpMenuEnabled() && !(screen instanceof JoinMultiplayerScreenPublic))
        if (!(screen instanceof JoinMultiplayerScreenPublic))
        {
            ServerSelectionList thisFake = (ServerSelectionList) (Object) this;
            int size = thisFake.children().size();
            for (int i = 0; i < size; i++)
            {
                if (thisFake.children().get(i) instanceof ServerSelectionList.NetworkServerEntry)
                {
                    ServerSelectionList.NetworkServerEntry realEntry = (ServerSelectionList.NetworkServerEntry) thisFake.children().get(i);
                    if (realEntry.getServerData() instanceof LanServerInfoConnect)
                    {
                        thisFake.children().set(i, new OurServerListEntryLanDetected(screen, (LanServerInfoConnect) realEntry.getServerData(), thisFake));
                    }
                }
            }
            if (Config.getInstance().isMpMenuEnabled()) {
                try {
                    thisFake.children().add(size, new CreeperHostServerEntry(thisFake));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
