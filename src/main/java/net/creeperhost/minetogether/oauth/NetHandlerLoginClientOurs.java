package net.creeperhost.minetogether.oauth;

import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;


public class NetHandlerLoginClientOurs extends NetHandlerLoginClient {
    private final NetworkManager networkManager;
    public NetHandlerLoginClientOurs(NetworkManager networkManagerIn, Minecraft mcIn) {
        super(networkManagerIn, mcIn, null);
        networkManager = networkManagerIn;
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
        ServerAuthTest.disconnected(reason.getUnformattedText());
        // NO-OP
    }

    @Override
    public void handleLoginSuccess(SPacketLoginSuccess packetIn) {
        GameProfile gameProfile = packetIn.getProfile();
        networkManager.setConnectionState(EnumConnectionState.PLAY);
        NetHandlerPlayClientOurs nhpc = new NetHandlerPlayClientOurs(this.networkManager);
        networkManager.setNetHandler(nhpc);
    }
}
