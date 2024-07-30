package com.dcsuibian.jredis.server;

public class RedisObject<T> {
    private int lru;
    private int referenceCount;
    private T value;
}
