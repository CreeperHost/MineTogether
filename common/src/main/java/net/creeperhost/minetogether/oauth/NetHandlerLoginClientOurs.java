package net.creeperhost.minetogether.oauth;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;

public class NetHandlerLoginClientOurs extends ClientHandshakePacketListenerImpl {

    private final Connection connection;

    public NetHandlerLoginClientOurs(Connection connection, Minecraft mcIn) {
        super(connection, mcIn, null, null, false, null, e -> { }, null);
        this.connection = connection;
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        ServerAuthTest.disconnected(details.reason().getString());
        // NO-OP
    }

    @Override
    public void handleGameProfile(ClientboundGameProfilePacket packetIn) {
        GameProfile gameProfile = packetIn.gameProfile();
        // TODO
//        connection.setProtocol(ConnectionProtocol.PLAY);
        ClientPlayNetHandlerOurs nhpc = new ClientPlayNetHandlerOurs(connection);
        //TODO, setListener method no longer exists.
//        connection.setListener(nhpc);
    }
}
