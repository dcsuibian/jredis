package com.dcsuibian.jredis.datastructure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SdsTests {
    @Test
    void testToString() {
        Sds sds = new Sds("Hello JRedis!!!", StandardCharsets.UTF_8);
        assertEquals("Hello JRedis!!!", sds.toString(StandardCharsets.UTF_8));
    }
}
