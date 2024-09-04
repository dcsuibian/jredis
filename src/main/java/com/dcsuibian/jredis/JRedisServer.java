package com.dcsuibian.jredis;

import com.dcsuibian.jredis.server.RedisServer;

import java.io.InputStream;

public class JRedisServer {
    private final RedisServer redisServer;

    public JRedisServer() {
        this.redisServer = new RedisServer();
    }

    public JRedisServer(String configFileContent) {
        this.redisServer = new RedisServer(configFileContent);
    }

    public JRedisServer(InputStream inputStream) {
        this.redisServer = new RedisServer(inputStream);
    }

    public void start() {
        redisServer.start();
    }

    public void stop() {
        redisServer.stop();
    }
}
