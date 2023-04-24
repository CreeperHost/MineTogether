package net.creeperhost.minetogether.connect.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import static net.creeperhost.minetogether.connect.netty.DataUtils.readVarInt;
import static net.creeperhost.minetogether.connect.netty.DataUtils.writeVarInt;

/**
 * Coded to append frame information, and extract it the other side.
 * <p>
 * Created by covers1624 on 10/4/23.
 */
public class FrameCodec extends ByteToMessageCodec<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int readableBytes = msg.readableBytes();
        writeVarInt(out, readableBytes);
        out.writeBytes(msg, msg.readerIndex(), readableBytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Netty will only remove bytes from the input buffer that we have actually read.
        // It makes this choice based on the reader index once this method returns.
        // We mark it here, so we can reset and pretend we did nothing.
        in.markReaderIndex();

        // Frames start with a varint header, attempt to read at least that may bytes.

        // Attempt to manually read a maximum of 5 bytes, or until the varint carry bit is not set.
        for (int i = 0; i < 5 && (in.readByte() & 0x80) == 0x80; i++) {
            if (!in.isReadable()) {
                // nothing we can do.
                in.resetReaderIndex();
                return;
            }
        }
        // Reset the reader index, so we can read the varint.
        in.resetReaderIndex();

        int frameLen = readVarInt(in);
        if (in.readableBytes() < frameLen) {
            // We do not have a full frame yet. Reset reader index and bail.
            in.resetReaderIndex();
            return;
        }
        out.add(in.readBytes(frameLen));
    }
}
