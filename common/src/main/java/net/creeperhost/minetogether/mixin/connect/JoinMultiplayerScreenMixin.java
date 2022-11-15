package net.creeperhost.minetogether.mixin.connect;

import net.creeperhost.minetogether.connect.FriendsServerList;
import net.creeperhost.minetogether.connect.OurServerListEntryLanDetected;
import net.creeperhost.minetogether.orderform.CreeperHostServerEntry;
import net.creeperhost.minetogether.serverlist.gui.JoinMultiplayerScreenPublic;
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

@Mixin (JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {

    @Shadow
    protected ServerSelectionList serverSelectionList;
    @Shadow
    private LanServerDetection.LanServerList lanServerList;

    @Shadow
    protected abstract void join(ServerData serverData);

    private FriendsServerList friendsServerList = null;

    private JoinMultiplayerScreen getThis() {
        return (JoinMultiplayerScreen) (Object) this;
    }

    @Inject (at = @At ("TAIL"), method = "init()V")
    public void init(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        if (friendsServerList == null) {
            friendsServerList = new FriendsServerList(getThis(), lanServerList);
        }
    }

    @Inject (at = @At ("TAIL"), method = "removed()V") // closed
    public void removed(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        if (friendsServerList != null) {
            friendsServerList.removed();
        }
    }

    @Inject (at = @At ("HEAD"), method = "joinSelectedServer", cancellable = true)
    public void joinSelectedServer(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        if (entry instanceof CreeperHostServerEntry) {
            ci.cancel();
        } else if (entry instanceof OurServerListEntryLanDetected) {
            if (!((OurServerListEntryLanDetected) entry).canBeJoined()) {
                //ci.cancel();
            }
        }
    }

    @Inject (at = @At ("TAIL"), method = "tick()V")
    public void tick(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        if (friendsServerList != null && friendsServerList.isDirty()) {
            List<LanServer> list = friendsServerList.getServers();
            friendsServerList.markClean();
            this.serverSelectionList.updateNetworkServers(list);
        }
    }

    @Inject (at = @At ("TAIL"), method = "onSelectedChange()V")
    public void selectedChangeHook(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        if (entry instanceof OurServerListEntryLanDetected && !((OurServerListEntryLanDetected) entry).canBeJoined()) {
            //this.selectButton.active = false;
        }
    }
}
