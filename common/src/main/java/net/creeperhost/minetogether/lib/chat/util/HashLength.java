package net.creeperhost.minetogether.lib.chat.util;

import com.google.common.hash.HashCode;
import net.creeperhost.minetogether.lib.chat.annotation.HashLen;

import java.util.Locale;

/**
 * Represents the possible hash lengths.
 * <p>
 * Created by covers1624 on 21/6/22.
 */
public enum HashLength {
    FULL(64),
    MEDIUM(28);

    public final int len;

    HashLength(int len) {
        this.len = len;
    }

    /**
     * Checks if the provided hash matches this {@link HashLength}'s
     * expected string length.
     *
     * @param hash The hash to check.
     * @return If the length matches.
     */
    public boolean matches(String hash) {
        return hash.length() == len;
    }

    /**
     * Formats the full SHA256 hash to the correct length
     * for the current {@link HashLength}.
     *
     * @param hash The hash to format.
     * @return The formatted hash.
     */
    public String format(@HashLen (FULL) String hash) {
        assert FULL.matches(hash);

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

    public static boolean matchesAny(String hash) {
        for (HashLength value : values()) {
            if (value.matches(hash)) {
                return true;
            }
        }
        return false;
    }
}
