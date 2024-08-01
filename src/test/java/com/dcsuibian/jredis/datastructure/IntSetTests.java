package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntSetTests {
    private IntSet intSet;

    @BeforeEach
    void setup() {
        intSet = new IntSet();
    }

    @Test
    void testSize() {
        intSet.add(1);
        intSet.add(2);
        intSet.add(3);
        assertEquals(3, intSet.size());
    }

    @Test
    void testAddAndContains() {
        intSet.add(1);
        intSet.add(2);
        intSet.add(3);
        intSet.add(Long.MAX_VALUE);
        assertTrue(intSet.add(4));
        assertFalse(intSet.add(1));
        assertTrue(intSet.contains(1));
        assertTrue(intSet.contains(2));
        assertTrue(intSet.contains(Long.MAX_VALUE));
        assertFalse(intSet.contains(5));
    }

    @Test
    public void testRemove() {
        intSet.add(1);
        intSet.add(2);
        intSet.add(3);
        intSet.remove(2);
        intSet.add(Long.MAX_VALUE);
        intSet.remove(1);
        assertEquals(2, intSet.size());
        assertFalse(intSet.contains(2));
        assertTrue(intSet.contains(3));
        assertTrue(intSet.contains(Long.MAX_VALUE));
    }
}
