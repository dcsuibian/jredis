package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SdsTests {
    @Test
    void testToString() {
        Sds sds = new Sds("Hello JRedis!!!");
        assertEquals("Hello JRedis!!!", sds.toString());
    }
}
