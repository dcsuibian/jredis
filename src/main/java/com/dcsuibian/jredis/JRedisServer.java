package com.dcsuibian.jredis;

import com.dcsuibian.jredis.server.RedisServer;

public class JRedisServer {
    private final RedisServer redisServer;

    public JRedisServer() {
        this.redisServer = new RedisServer();
    }

    public JRedisServer(int port) {
        this.redisServer = new RedisServer(port);
    }

    public void start() {
        redisServer.start();
    }

    public void stop() {
        redisServer.stop();
    }
}
