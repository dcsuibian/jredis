package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DictionaryTests {
    private Dictionary<String, Integer> dictionary;

    @BeforeEach
    void setup() {
        dictionary = new Dictionary<>();
    }

    @Test
    void testNextExp() {
        assertEquals(2, Dictionary.nextExp(1));
        assertEquals(4, Dictionary.nextExp(13));
        assertEquals(30, Dictionary.nextExp(0x4000_0000));
        assertEquals(-1, Dictionary.nextExp(0x4000_0001));
        assertEquals(-1, Dictionary.nextExp(Integer.MAX_VALUE));
    }

    @Test
    void testSize() {
        dictionary.put("a", 1);
        dictionary.put("b", 2);
        dictionary.put("c", 3);
        assertEquals(3, dictionary.size());
    }

    @Test
    void testPutAndGet() {
        dictionary.put("a", 1);
        dictionary.put("b", 2);
        dictionary.put("c", 3);
        assertEquals(1, dictionary.get("a"));
        assertEquals(2, dictionary.get("b"));
        assertEquals(3, dictionary.get("c"));
    }

    @Test
    void testPutWithSameKey() {
        dictionary.put("a", 1);
        dictionary.put("a", 2);
        assertEquals(2, dictionary.get("a"));
    }

    @Test
    void testRemove() {
        dictionary.put("a", 1);
        dictionary.put("b", 2);
        dictionary.put("c", 3);
        dictionary.remove("b");
        assertEquals(2, dictionary.size());
        assertEquals(1, dictionary.get("a"));
        assertEquals(3, dictionary.get("c"));
        assertNull(dictionary.get("b"));
    }
}
