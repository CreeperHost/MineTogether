package net.creeperhost.minetogether.oauth;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;

public class NetHandlerLoginClientOurs extends ClientHandshakePacketListenerImpl {

    public NetHandlerLoginClientOurs(Connection connection, Minecraft mcIn) {
        super(connection, mcIn, null, e -> {});
    }

    @Override
    public void onDisconnect(Component reason) {
        ServerAuthTest.disconnected(reason.getString());
        // NO-OP
    }

    @Override
    public void handleGameProfile(ClientboundGameProfilePacket packetIn)
    {
        GameProfile gameProfile = packetIn.getGameProfile();
        getConnection().setProtocol(ConnectionProtocol.PLAY);
        ClientPlayNetHandlerOurs nhpc = new ClientPlayNetHandlerOurs(this.getConnection());
        getConnection().setListener(nhpc);
    }
}
