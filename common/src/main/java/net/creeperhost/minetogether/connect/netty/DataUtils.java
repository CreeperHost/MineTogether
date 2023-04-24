package net.creeperhost.minetogether.connect.netty;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by covers1624 on 10/4/23.
 */
public class DataUtils {

    // region Reading
    // region Var-Primitives

    /**
     * Reads a Variable length int from the stream.
     *
     * @return The int.
     */
    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;
        byte b0;

        do {
            b0 = buf.readByte();
            i |= (b0 & 0x7f) << j++ * 7;

            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((b0 & 0x80) == 0x80);

        return i;
    }

    /**
     * Reads a Variable length long from the stream.
     *
     * @return The long.
     */
    public static long readVarLong(ByteBuf buf) {
        long i = 0L;
        int j = 0;
        byte b0;

        do {
            b0 = buf.readByte();
            i |= (long) (b0 & 0x7f) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }

        }
        while ((b0 & 0x80) == 0x80);

        return i;
    }

    /**
     * Reads a Variable length signed int.
     *
     * @return The int.
     * @see #writeSignedVarInt
     */
    public static int readSignedVarInt(ByteBuf buf) {
        int i = readVarInt(buf);
        return (i & 1) == 0 ? i >>> 1 : -(i >>> 1) - 1;
    }

    /**
     * Reads a Variable length signed long.
     *
     * @return The long.
     * @see #writeSignedVarLong
     */
    public static long readSignedVarLong(ByteBuf buf) {
        long i = readVarLong(buf);
        return (i & 1) == 0 ? i >>> 1 : -(i >>> 1) - 1;
    }
    // endregion

    // region Arrays

    /**
     * Reads a block of bytes written with
     * {@link DataUtils#writeBytes}
     * <p>
     * This method uses a prepended length varint.
     *
     * @return The bytes.
     */
    public static byte[] readBytes(ByteBuf buf) {
        int len = readVarInt(buf);
        byte[] bytes = new byte[len];
        buf.readBytes(bytes, 0, len);
        return bytes;
    }
    // endregion

    // region Java Objects

    /**
     * Reads a UTF-8 encoded {@link String} from the stream.
     *
     * @return The {@link String}.
     */
    public static String readString(ByteBuf buf) {
        return new String(readBytes(buf), StandardCharsets.UTF_8);
    }

    /**
     * Reads a {@link UUID} from the stream.
     *
     * @return The {@link UUID}.
     */
    public static UUID readUUID(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    /**
     * Reads an {@link Enum} value from the stream.
     *
     * @param clazz The Class of the enum.
     * @return The {@link Enum} value.
     */
    public static <T extends Enum<T>> T readEnum(ByteBuf buf, Class<T> clazz) {
        return clazz.getEnumConstants()[readVarInt(buf)];
    }
    // endregion
    // endregion

    // region Writing

    //region Var-Primitives

    /**
     * Writes a Variable length int.
     * Doesn't handle Signed ints well, they end up as 5 bytes,
     * instead of 4, Use {@link #writeSignedVarInt} if you require numbers <= -1
     *
     * @param i The int.
     */
    public static void writeVarInt(ByteBuf buf, int i) {
        while ((i & 0xffffff80) != 0) {
            buf.writeByte(i & 0x7f | 0x80);
            i >>>= 7;
        }

        buf.writeByte(i);
    }

    /**
     * Writes a Variable length long.
     * Doesn't handle Signed longs well, they end up as 10 bytes,
     * instead of 8, Use {@link #writeSignedVarLong} if you require numbers <= -1
     *
     * @param l The long.
     */
    public static void writeVarLong(ByteBuf buf, long l) {
        while ((l & 0xffffffffffffff80L) != 0L) {
            buf.writeByte((int) (l & 0x7fL) | 0x80);
            l >>>= 7;
        }
        buf.writeByte((int) l);
    }

    /**
     * Writes a Signed Variable length int.
     * Favourable for numbers <= -1
     *
     * @param i The int.
     */
    public static void writeSignedVarInt(ByteBuf buf, int i) {
        writeVarInt(buf, i >= 0 ? 2 * i : -2 * (i + 1) + 1);
    }

    /**
     * Writes a Signed Variable length long.
     * Favourable for numbers <= -1
     *
     * @param i The long.
     */
    public static void writeSignedVarLong(ByteBuf buf, long i) {
        writeVarLong(buf, i >= 0 ? 2 * i : -2 * (i + 1) + 1);
    }

    // endregion

    // region Arrays

    /**
     * Writes an array to the stream, including its length.
     * First writes the arrays length as a varInt, followed
     * by the array data.
     *
     * @param b The array.
     */
    public static void writeBytes(ByteBuf buf, byte[] b) {
        writeBytes(buf, b, 0, b.length);
    }

    /**
     * Writes an array to the stream, including its length.
     * First writes the arrays length as a varInt, followed
     * by the array data.
     *
     * @param b   The array.
     * @param off An offset into the array to start reading from.
     * @param len How many elements to read.
     */
    public static void writeBytes(ByteBuf buf, byte[] b, int off, int len) {
        Objects.requireNonNull(b);
        checkLen(b.length, off, len);
        writeVarInt(buf, len);
        buf.writeBytes(b, off, len);
    }

    // endregion

    // region Java Objects

    //endregion

    /**
     * Writes a UTF-8 Encoded {@link String} to the stream.
     *
     * @param s The {@link String}.
     */
    public static void writeString(ByteBuf buf, String s) {
        writeBytes(buf, s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes a {@link UUID} to the stream.
     *
     * @param uuid The {@link UUID}.
     */
    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Writes an {@link Enum} value to the stream.
     *
     * @param value The {@link Enum} value to write.
     */
    public static void writeEnum(ByteBuf buf, Enum<?> value) {
        writeVarInt(buf, value.ordinal());
    }

    // endregion

    private static void checkLen(int arrLen, int off, int len) {
        if ((off < 0) || (off > arrLen) || (len < 0) || ((off + len) > arrLen) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
    }
}
