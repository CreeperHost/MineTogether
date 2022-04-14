package net.creeperhost.minetogether.mixin;

import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.KnownUsers;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.connect.ConnectHelper;
import net.creeperhost.minetogether.module.connect.OurServerListEntryLanDetected;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.List;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private List<ServerPlayer> players;

    @Shadow public abstract int getMaxPlayers();

    @Inject(at = @At("RETURN"), method = "getMaxPlayers()I", cancellable = true)
    private void getMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        if (ConnectHelper.isShared(server)) {
            cir.setReturnValue(ConnectMain.maxPlayerCount);
        }
    }

    private static final Component NOT_FRIEND_COMPONENT = new TranslatableComponent("minetogether.connect.join.notfriend");

    @Inject(at = @At("RETURN"), method = "canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;", cancellable = true)
    private void canPlayerJoinHook(SocketAddress socket, GameProfile profile, CallbackInfoReturnable<Component> cir)
    {
        if (ConnectHelper.isShared(server))
        {
            String playerHash = ChatCallbacks.getPlayerHash(profile.getId());
            Profile byHash = KnownUsers.findByHash(playerHash);
            boolean isFriend = byHash == null || !byHash.isFriend();
            if (isFriend) {
                cir.setReturnValue(NOT_FRIEND_COMPONENT);
            } else if(this.players.size() >= getMaxPlayers()) {
                cir.setReturnValue(OurServerListEntryLanDetected.FULL_MESSAGE_COMPONENT);
                Component component = ChatFormatter.newChatWithLinksOurs(I18n.get("minetogether.connect.join.tried.name", byHash.getUserDisplay(), ConnectMain.maxPlayerCount));
                Minecraft.getInstance().gui.getChat().addMessage(component);
            }
        }
    }
}
