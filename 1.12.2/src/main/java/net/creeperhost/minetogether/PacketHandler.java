package net.creeperhost.minetogether;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import static net.creeperhost.minetogether.CreeperHost.MOD_ID;

public class PacketHandler implements IMessageHandler<PacketHandler.ServerIDMessage, IMessage>
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

    public static void packetRegister()
    {
        INSTANCE.registerMessage(PacketHandler.class, ServerIDMessage.class, 0, Side.CLIENT);
    }

    @Override
    public IMessage onMessage(ServerIDMessage message, MessageContext ctx)
    {
        CreeperHost.instance.curServerId = message.serverID;
        return null;
    }

    public static class ServerIDMessage implements IMessage
    {
        int serverID;

        public ServerIDMessage(int serverID)
        {
            this.serverID = serverID;
        }

        public ServerIDMessage()
        {
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            serverID = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(serverID);
        }
    }
}
