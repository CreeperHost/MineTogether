package net.creeperhost.minetogether.oauth;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.util.text.ITextComponent;

public class NetHandlerLoginClientOurs extends ClientLoginNetHandler {

    public NetHandlerLoginClientOurs(NetworkManager networkManagerIn, Minecraft mcIn) {
        super(networkManagerIn, mcIn, null, e -> {});
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
        ServerAuthTest.disconnected(reason.getString());
        // NO-OP
    }

    @Override
    public void handleLoginSuccess(SLoginSuccessPacket packetIn) {
        GameProfile gameProfile = packetIn.getProfile();
        getNetworkManager().setConnectionState(ProtocolType.PLAY);
        ClientPlayNetHandlerOurs nhpc = new ClientPlayNetHandlerOurs(this.getNetworkManager());
        getNetworkManager().setNetHandler(nhpc);
    }
}
