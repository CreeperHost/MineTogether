package net.creeperhost.minetogether.mixin.connect;

import net.creeperhost.minetogether.connectv2.ConnectHandlerV2;
import net.creeperhost.minetogether.connectv2.gui.FriendServerEntry;
import net.creeperhost.minetogether.connectv2.gui.ServerListAppender;
import net.creeperhost.minetogether.orderform.CreeperHostServerEntry;
import net.creeperhost.minetogether.serverlist.gui.JoinMultiplayerScreenPublic;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.LanServerDetection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {

    @Shadow
    protected ServerSelectionList serverSelectionList;

    @Shadow
    protected abstract void join(ServerData serverData);

    private JoinMultiplayerScreen getThis() {
        return (JoinMultiplayerScreen) (Object) this;
    }

    @Inject (at = @At ("TAIL"), method = "init()V")
    public void init(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        if (ConnectHandlerV2.isEnabled()){
            ServerListAppender.INSTANCE.init(serverSelectionList, getThis());
        }
    }

    @Inject (at = @At ("TAIL"), method = "removed()V") // closed
    public void removed(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        ServerListAppender.INSTANCE.remove();
    }

    @Inject (at = @At ("HEAD"), method = "joinSelectedServer", cancellable = true)
    public void joinSelectedServer(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        if (entry instanceof CreeperHostServerEntry) {
            ci.cancel();
        } else if (entry instanceof FriendServerEntry friendServer) {
            ConnectHandlerV2.connect(friendServer.remoteServer);
            ci.cancel();
        }
    }

    @Inject (at = @At ("TAIL"), method = "tick()V")
    public void tick(CallbackInfo ci) {
        if (getThis() instanceof JoinMultiplayerScreenPublic) return;

        if (ConnectHandlerV2.isEnabled()){
            ServerListAppender.INSTANCE.tick();
        }
    }
}
