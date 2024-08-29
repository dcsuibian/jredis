package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.server.RedisClient;

@FunctionalInterface
public interface CommandProcessor {
    void process(RedisClient client);
}
