package net.creeperhost.minetogether.network;

import net.creeperhost.minetogether.MineTogether;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketServerID
{
    private final int serverID;
    
    public PacketServerID(int serverID)
    {
        this.serverID = serverID;
    }
    
    public static void encode(PacketServerID msg, PacketBuffer buf)
    {
        buf.writeInt(msg.serverID);
    }
    
    public static PacketServerID decode(PacketBuffer buf)
    {
        return new PacketServerID(buf.readInt());
    }
    
    public static class Handler
    {
        public static void handle(final PacketServerID message, Supplier<NetworkEvent.Context> ctx)
        {
            MineTogether.instance.curServerId = message.serverID;
        }
    }
}
