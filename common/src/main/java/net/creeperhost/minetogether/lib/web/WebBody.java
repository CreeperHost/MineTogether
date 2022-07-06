package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a request or response body.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public interface WebBody {

    /**
     * Open a stream to read the body data.
     *
     * @return The stream.
     * @throws IOException If an IO error occurs.
     */
    InputStream open() throws IOException;

    /**
     * The length of the data.
     *
     * @return The length in bytes. {@code -1} for unknown.
     */
    long length();

    /**
     * The mime content type for this data.
     *
     * @return The content type.
     */
    @Nullable
    String contentType();

    /**
     * Create a {@link WebBody} from a {@link String}.
     * This method assumes {@code null} {@code contentType}
     * This method assumes {@link StandardCharsets#UTF_8} {@code charset}.
     *
     * @param str The string.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str) {
        return string(str, (String) null);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     * This method assumes {@link StandardCharsets#UTF_8} {@code charset}.
     *
     * @param str         The string.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str, @Nullable String contentType) {
        return string(str, StandardCharsets.UTF_8, contentType);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     * This method assumes {@code null} {@code Content-Type}
     *
     * @param str     The string.
     * @param charset The {@link Charset} for the body.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str, Charset charset) {
        return string(str, charset, null);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     *
     * @param str         The string.
     * @param charset     The {@link Charset} for the body.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str, Charset charset, @Nullable String contentType) {
        return bytes(str.getBytes(charset), contentType);
    }

    /**
     * Create a {@link WebBody} form a {@code byte[]}.
     * This method assumes {@code null} {@code Content-Type}
     *
     * @param bytes The bytes.
     * @return The {@link WebBody}
     */
    static WebBody bytes(byte[] bytes) {
        return new BytesBody(bytes);
    }

    /**
     * Create a {@link WebBody} form a {@code byte[]}.
     *
     * @param bytes       The bytes.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}
     */
    static WebBody bytes(byte[] bytes, @Nullable String contentType) {
        return new BytesBody(bytes, contentType);
    }

    /**
     * Simple {@link WebBody} implementation reading from
     * an in-memory, {@code byte[]}.
     */
    class BytesBody implements WebBody {

        private final byte[] bytes;

        @Nullable
        private final String contentType;

        public BytesBody(byte[] bytes) {
            this(bytes, null);
        }

        public BytesBody(byte[] bytes, @Nullable String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }

        // @formatter:off
        @Override public InputStream open() { return new ByteArrayInputStream(bytes); }
        @Override public long length() { return bytes.length; }
        @Nullable @Override public String contentType() { return contentType; }
        // @formatter:on
    }
}
