package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkedListTests {
    private LinkedList<Integer> linkedList;

    @BeforeEach
    void setup() {
        linkedList = new LinkedList<>();
    }

    @Test
    void testAddHead() {
        linkedList.addHead(1);
        linkedList.addHead(2);
        linkedList.addHead(3);
        assertEquals(3, linkedList.getLength());
        assertEquals(3, linkedList.getHead().getValue());
    }
}
