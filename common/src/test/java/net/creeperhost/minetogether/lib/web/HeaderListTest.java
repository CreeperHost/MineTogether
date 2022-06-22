package net.creeperhost.minetogether.lib.web;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 20/6/22.
 */
public class HeaderListTest {

    @Test
    public void testEmptyAndSize() {
        HeaderList list = new HeaderList();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());

        list.add("Key", "Value1");
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());

        list.add("Key", "Value2");
        assertEquals(2, list.size());

        list.add("Key", "Value3");
        list.add("Key", "Value4");
        list.add("Key", "Value5");
        list.add("Key", "Value6");
        list.add("Key", "Value7");
        list.add("Key", "Value8");
        list.add("Key", "Value9");
        list.add("Key", "Value10");
        assertEquals(10, list.size());
    }

    @Test
    public void testAddGetSingle() {
        HeaderList list = new HeaderList();
        list.add("Key", "Value");

        assertEquals("Value", list.get("Key"));
        assertEquals("Value", list.get("key")); // case insensitive.
    }

    @Test
    public void testAddGetMultiple() {
        HeaderList list = new HeaderList();
        list.add("Key", "Value1");
        list.add("Key", "Value2");

        assertEquals(Arrays.asList("Value1", "Value2"), list.getValues("Key"));
        assertEquals(Arrays.asList("Value1", "Value2"), list.getValues("key"));
    }

    @Test
    public void testAddCollision() {
        HeaderList list = new HeaderList();

        list.add("Key", "Value");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> list.add("Key", "Value"));
        assertEquals("Key value pair already exist. K:'Key', V:'Value'", ex.getMessage());
    }

    @Test
    public void testAddAll() {
        HeaderList list = new HeaderList();

        list.addAll(Collections.singletonMap(
                "Key", "Value1"
        ));

        HeaderList list2 = new HeaderList();
        list2.add("Key", "Value2");
        list2.add("Key", "Value3");
        list.addAll(list2);

        assertEquals(Arrays.asList("Value1", "Value2", "Value3"), list.getValues("Key"));
    }

    @Test
    public void testSet() {
        HeaderList list = new HeaderList();
        list.add("Key", "Value1");
        list.add("Key", "Value2");

        list.set("Key", "DifferentValue");

        assertEquals(Collections.singletonList("DifferentValue"), list.getValues("Key"));
    }

    @Test
    public void testContains() {
        HeaderList list = new HeaderList();
        list.add("Key1", "Value1");
        list.add("Key1", "Value2");
        list.add("Key2", "Value1");
        list.add("Key2", "Value2");

        assertTrue(list.contains("Key1", "Value1"));
        assertTrue(list.contains("key1", "Value1"));
        assertTrue(list.contains("Key1", "Value2"));
        assertTrue(list.contains("key1", "Value2"));
        assertTrue(list.contains("Key2", "Value1"));
        assertTrue(list.contains("key2", "Value1"));
        assertTrue(list.contains("Key2", "Value2"));
        assertTrue(list.contains("key2", "Value2"));
        assertFalse(list.contains("Key3", "Value1"));
        assertFalse(list.contains("key3", "Value1"));
    }

    @Test
    public void testRemoveAll() {
        HeaderList list = new HeaderList();
        list.add("Key", "Value1");
        list.add("Key", "Value2");

        list.removeAll("key");

        assertNull(list.get("Key"));
        assertNull(list.get("key"));
    }

    @Test
    public void testIterator() {
        HeaderList list = new HeaderList();
        list.add("Key", "Value1");
        list.add("Key", "Value2");

        Iterator<HeaderList.Entry> itr = list.iterator();
        assertTrue(itr.hasNext());

        HeaderList.Entry entry = itr.next();
        assertEquals("Key", entry.getKey());
        assertEquals("Value1", entry.getValue());
        HeaderList.Entry finalEntry = entry;
        assertThrows(UnsupportedOperationException.class, () -> finalEntry.setValue("Value1ButDifferent"));
        assertTrue(itr.hasNext());

        entry = itr.next();
        assertEquals("Key", entry.getKey());
        assertEquals("Value2", entry.getValue());
        assertFalse(itr.hasNext());
    }

    @Test
    public void testCheckNameChars() {
        char firstAllowed = '\u0021'; // !
        char lastAllowed = '\u007e'; // ~
        int seen = 0;
        for (char c = 0; c < firstAllowed; c++) {
            char ch = c;
            seen++;
            assertThrows(IllegalArgumentException.class, () -> HeaderList.checkNameChars(String.valueOf(ch)));
        }
        for (char ch = firstAllowed; ch <= lastAllowed; ch++) {
            seen++;
            HeaderList.checkNameChars(String.valueOf(ch));
        }
        char fistAfterLastAllowed = '\u007f';
        for (char c = fistAfterLastAllowed; c < '\uffff'; c++) {
            char ch = c;
            seen++;
            assertThrows(IllegalArgumentException.class, () -> HeaderList.checkNameChars(String.valueOf(ch)));
        }
        assertEquals('\uffff', seen);
    }

    @Test
    public void testCheckValueChars() {
        char[] special = { '\t' }; // Special inclusion outside the bellow range.
        char firstAllowed = '\u0020'; // <space>
        char lastAllowed = '\u007e'; // ~

        int seen = 0;
        for (char c = 0; c < firstAllowed; c++) {
            char ch = c;
            if (contains(special, ch)) continue;
            seen++;
            assertThrows(IllegalArgumentException.class, () -> HeaderList.checkValueChars("", String.valueOf(ch)));
        }
        for (char ch : special) {
            seen++;
            HeaderList.checkValueChars("", String.valueOf(ch));
        }
        for (char ch = firstAllowed; ch <= lastAllowed; ch++) {
            seen++;
            HeaderList.checkValueChars("", String.valueOf(ch));
        }
        char fistAfterLastAllowed = '\u007f';
        for (char i = fistAfterLastAllowed; i < '\uffff'; i++) {
            char ch = i;
            seen++;
            assertThrows(IllegalArgumentException.class, () -> HeaderList.checkValueChars("", String.valueOf(ch)));
        }
        assertEquals('\uffff', seen);
    }

    private static boolean contains(char[] arr, char c) {
        for (char c1 : arr) {
            if (c1 == c) return true;
        }
        return false;
    }
}
