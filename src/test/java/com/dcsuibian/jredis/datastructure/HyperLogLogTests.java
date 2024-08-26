package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HyperLogLogTests {
    private HyperLogLog hyperLogLog;

    @BeforeEach
    void setup() {
        hyperLogLog = new HyperLogLog();
    }

    @Test
    void testAddAndCount() {
        byte[] element = "test".getBytes();
        assertTrue(hyperLogLog.add(element));
        assertFalse(hyperLogLog.add(element));
        assertEquals(1, hyperLogLog.count());

        hyperLogLog.add("test1".getBytes());
        hyperLogLog.add("test2".getBytes());
        hyperLogLog.add("test3".getBytes());
        assertEquals(4, hyperLogLog.count());
    }

    @Test
    void testMerge() {
        HyperLogLog another = new HyperLogLog();
        hyperLogLog.add("test1".getBytes());
        hyperLogLog.add("test2".getBytes());
        hyperLogLog.add("test3".getBytes());
        another.add("test4".getBytes());
        another.add("test5".getBytes());
        another.add("test6".getBytes());
        assertEquals(6, hyperLogLog.merge(another).count());
    }
}
