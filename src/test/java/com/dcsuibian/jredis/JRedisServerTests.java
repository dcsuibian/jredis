package com.dcsuibian.jredis;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class JRedisServerTests {
    @Test
    public void testStartAndStop() {
        InputStream stream = getClass().getResourceAsStream("/redis.conf");
        JRedisServer server = new JRedisServer(stream);
        server.start();
        server.stop();
    }

    public static void main(String[] args) {
        InputStream stream = JRedisServerTests.class.getResourceAsStream("/redis.conf");
        JRedisServer server = new JRedisServer(stream);
        server.start();
    }
}
