package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListPackTests {
    private ListPack listPack;

    @BeforeEach
    void setup() {
        listPack = new ListPack(0);
    }

    @Test
    public void testAppend() {
        listPack.append("hello".getBytes());
        listPack.append("world".getBytes());
        listPack.append(12345L);
        assertEquals(3, listPack.getLength());
    }

    @Test
    public void testGetStringValueAndLong() {
        listPack.append("hello".getBytes());
        listPack.append(12345L);
        assertEquals("hello", new String(listPack.getStringValue(0)));
        assertEquals(12345L, listPack.getLong(1));
    }

    @Test
    public void testPrepend() {
        listPack.append("world".getBytes());
        listPack.prepend("hello".getBytes());
        assertEquals("hello", listPack.getString(0));
        assertEquals("world", listPack.getString(1));
    }

    @Test
    public void testTraverse() {
        listPack.append("hello".getBytes());
        listPack.append("world".getBytes());
        listPack.append(12345L);

        for (int i = 0; i < listPack.getLength(); i++) {
            if (i == 0) {
                assertEquals("hello", new String(listPack.getStringValue(i)));
            } else if (i == 1) {
                assertEquals("world", new String(listPack.getStringValue(i)));
            } else if (i == 2) {
                assertEquals(12345L, listPack.getLong(i));
            }
        }
    }

}
