package net.creeperhost.minetogether.trade.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTrade implements IMessage
{
    private int windowId;
    private int button;

    public PacketTrade() {}

    public PacketTrade(int windowIdIn, int buttonIn)
    {
        this.windowId = windowIdIn;
        this.button = buttonIn;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.windowId = buf.readByte();
        this.button = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.windowId);
        buf.writeByte(this.button);
    }

    public int getButton() {
        return button;
    }

    public int getWindowId() {
        return windowId;
    }

    public static class Handler implements IMessageHandler<PacketTrade, IMessage>
    {
        @Override
        public IMessage onMessage(final PacketTrade message, MessageContext ctx)
        {
            return null;
        }
    }
}
