package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogetherconnect.ConnectMain;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/server/players/PlayerList;getMaxPlayers()I", cancellable = true)
    private void getMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        if (!this.server.isDedicatedServer() && this.server.isPublished() && this.server.getPort() == 42069) {
            cir.setReturnValue(ConnectMain.maxPlayerCount + 1);
        }
    }
}
