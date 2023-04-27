package net.creeperhost.minetogether.connect.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.creeperhost.minetogether.connect.netty.packet.CRaw;
import net.creeperhost.minetogether.connect.netty.packet.SRaw;
import net.minecraft.network.protocol.Packet;

import java.util.List;

/**
 * Created by covers1624 on 26/4/23.
 */
public class RawCodec extends MessageToMessageCodec<Packet<?>, ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(new SRaw(Unpooled.copiedBuffer(msg), null)); // TODO pooled.
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Packet<?> msg, List<Object> out) throws Exception {
        if (msg instanceof CRaw p) {
            out.add(p.data);
        }
    }
}
