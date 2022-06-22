package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

/**
 * A simple collection of name-value pair entries.
 * <p>
 * A combination of name-value must be unique.
 * <p>
 * All name characters must be within {@code u0021 .. u007e} (inclusive)<br>
 * All value characters must either be {@code \t} or within {@code u0020 .. u007e} (inclusive)
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class HeaderList implements Iterable<HeaderList.Entry> {

    // Interleaved list of name-value pairs.
    private final List<String> headers = new ArrayList<>();

    /**
     * Checks if the collection is empty.
     *
     * @return If the collection is empty.
     */
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    /**
     * Gets the number of entries in the collection.
     *
     * @return The number of entries.
     */
    public int size() {
        return headers.size() / 2;
    }

    /**
     * Adds name-value pair.
     *
     * @param name  The name.
     * @param value The value.
     */
    public void add(String name, String value) {
        if (name.isEmpty()) throw new IllegalStateException("Key name must not be empty!");
        checkNameChars(name);
        checkValueChars(name, value);
        if (contains(name, value)) {
            throw new IllegalArgumentException("Key value pair already exist. K:'" + name + "', V:'" + value + "'");
        }
        headers.add(name);
        headers.add(value.trim());
    }

    /**
     * Add all the entries from the specified {@link Map} to
     * this collection.
     *
     * @param entries The entries to add.
     */
    public void addAll(Map<String, String> entries) {
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add all the entries form the specified {@link HeaderList} to
     * this collection.
     *
     * @param other The entries to add.
     */
    public void addAll(HeaderList other) {
        for (int i = 0; i < other.headers.size(); i += 2) {
            String name = other.headers.get(i);
            String value = other.headers.get(i + 1);
            if (contains(name, value)) {
                throw new IllegalArgumentException("Key value pair already exist. K:'" + name + "', V:'" + value + "'");
            }
            headers.add(name);
            headers.add(value);
        }
    }

    /**
     * Remove all existing values of a specified name, then insert a new value.
     *
     * @param name  The name.
     * @param value The value.
     */
    public void set(String name, String value) {
        checkNameChars(name);
        checkValueChars(name, value);
        removeAll(name);
        add(name, value);
    }

    /**
     * Checks if this collection contains the given name-value pair.
     *
     * @param name  The name.
     * @param value The value.
     * @return If the name-value pair exists.
     */
    public boolean contains(String name, String value) {
        for (int i = 0; i < headers.size(); i += 2) {
            String v = headers.get(i);
            if (v.equalsIgnoreCase(name) && headers.get(i + 1).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all entries with the specified name.
     *
     * @param name The name to remove.
     */
    public void removeAll(String name) {
        for (int i = 0; i < headers.size(); i += 2) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                headers.remove(i);
                headers.remove(i);
                i -= 2;
            }
        }
    }

    /**
     * Get the first value with the specified name.
     *
     * @param name The name to get the first value for.
     * @return The first value. {@code null} if it does not exist.
     */
    @Nullable
    public String get(String name) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                return headers.get(i + 1);
            }
        }
        return null;
    }

    /**
     * Get all values with the specified name.
     *
     * @param name The name to get values for.
     * @return All values for the specified name.
     */
    public List<String> getValues(String name) {
        List<String> values = new LinkedList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                values.add(headers.get(i + 1));
            }
        }
        return values;
    }

    @Override
    public Iterator<Entry> iterator() {
        Iterator<String> backing = headers.iterator();
        return new Iterator<Entry>() {
            @Override
            public boolean hasNext() {
                return backing.hasNext();
            }

            @Override
            public Entry next() {
                return new Entry(backing.next(), backing.next());
            }
        };
    }

    @VisibleForTesting
    static void checkNameChars(String name) {
        char[] charArray = name.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if ('\u0021' > c || c > '\u007e') {
                throw new IllegalArgumentException(String.format("Name '%s' Contains invalid character \\u%04X at %d", name, (int) c, i));
            }
        }
    }

    @VisibleForTesting
    static void checkValueChars(String name, String value) {
        char[] charArray = value.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c != '\t' && ('\u0020' > c || c > '\u007e')) {
                throw new IllegalArgumentException(String.format("Value for name '%s' Contains invalid character \\u%04X at %d", name, (int) c, i));
            }
        }
    }

    public static class Entry implements Map.Entry<String, String> {

        public final String name;
        public final String value;

        public Entry(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getKey() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }
}
