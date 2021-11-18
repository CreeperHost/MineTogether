package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.module.connect.FriendsServerList;
import net.creeperhost.minetogether.module.connect.OurServerListEntryLanDetected;
import net.creeperhost.minetogether.module.multiplayer.data.CreeperHostServerEntry;
import net.creeperhost.minetogether.module.multiplayer.screen.JoinMultiplayerScreenPublic;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MixinJoinMultiplayerScreen
{
    @Shadow
    protected ServerSelectionList serverSelectionList;
    @Shadow
    private Button selectButton;
    @Shadow
    private LanServerDetection.LanServerList lanServerList;

    @Shadow protected abstract void join(ServerData serverData);

    FriendsServerList friendsServerList = null;
    private boolean applicableCache = false;
    private boolean isApplicableCached = false;

    @Inject(at = @At("TAIL"), method = "init()V")
    public void init(CallbackInfo ci)
    {
        if (!isApplicable(this)) return;
        if (friendsServerList == null)
        {
            friendsServerList = new FriendsServerList((JoinMultiplayerScreen) (Object) this, lanServerList);
        }
    }

    @Inject(at = @At("TAIL"), method = "removed()V") // closed
    public void removed(CallbackInfo ci)
    {
        if (this.getClass().getSimpleName().equals(JoinMultiplayerScreenPublic.class.getSimpleName()))
        {
            return;
        }
        if (friendsServerList != null)
        {
            friendsServerList.removed();
        }
    }

    @Inject(at = @At("HEAD"), method = "joinSelectedServer", cancellable = true)
    public void joinSelectedServer(CallbackInfo ci)
    {
        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        if(entry instanceof CreeperHostServerEntry)
        {
            ci.cancel();
        } else if (entry instanceof OurServerListEntryLanDetected) {
            if (!((OurServerListEntryLanDetected)entry).canBeJoined()) {
                //ci.cancel();
            }
        }
    }


    @Inject(at = @At("TAIL"), method = "tick()V")
    public void tick(CallbackInfo ci)
    {
        if (friendsServerList.isDirty())
        {
            List<LanServer> list = friendsServerList.getServers();
            friendsServerList.markClean();
            this.serverSelectionList.updateNetworkServers(list);
        }
    }

    @Inject(at = @At("TAIL"), method = "onSelectedChange()V")
    public void selectedChangeHook(CallbackInfo ci)
    {
        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        if (entry instanceof OurServerListEntryLanDetected && !((OurServerListEntryLanDetected) entry).canBeJoined()) {
            //this.selectButton.active = false;
        }
    }


    private boolean isApplicable(Object screen)
    {
        if (isApplicableCached) return applicableCache;
        applicableCache = !screen.getClass().getSimpleName().equals(JoinMultiplayerScreenPublic.class.getSimpleName());
        isApplicableCached = true;
        return applicableCache;
    }
}
