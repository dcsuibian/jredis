package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

@Getter
public class RespSimpleString extends Resp2Object {
    public static final RespSimpleString OK = new RespSimpleString("OK".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleString PONG = new RespSimpleString("PONG".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleString QUEUED = new RespSimpleString("QUEUED".getBytes(StandardCharsets.UTF_8));
    private final byte[] value;

    public RespSimpleString(byte[] value) {
        this.value = value;
    }
}
