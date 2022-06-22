package net.creeperhost.minetogether.lib.chat.util;

import com.google.common.hash.HashCode;

import java.util.Locale;

/**
 * Represents the possible hash lengths.
 * <p>
 * Created by covers1624 on 21/6/22.
 */
public enum HashLength {
    FULL(64),
    MEDIUM(28),
    SHORT(15);

    public final int len;

    HashLength(int len) {
        this.len = len;
    }

    /**
     * Formats the full SHA256 hash to the correct length
     * for the current {@link HashLength}.
     *
     * @param hash The hash to format.
     * @return The formatted hash.
     */
    public String format(String hash) {
        if (this == FULL) {
            return hash;
        }
        return hash.substring(0, len);
    }

    /**
     * Formats the full SHA256 hash to the correct length
     * for the current {@link HashLength}.
     *
     * @param hash The hash to format.
     * @return The formatted hash.
     */
    public String format(HashCode hash) {
        return format(hash.toString().toUpperCase(Locale.ROOT));
    }
}
