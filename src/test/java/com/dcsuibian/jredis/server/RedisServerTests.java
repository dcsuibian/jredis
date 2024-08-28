package com.dcsuibian.jredis.server;

import org.junit.jupiter.api.Test;

public class RedisServerTests {
    @Test
    void testStartAndStop() {
        RedisServer server = new RedisServer();
        server.start();
        server.stop();
    }

    public static void main(String[] args) {
        RedisServer server = new RedisServer(16379);
        server.start();
    }
}
