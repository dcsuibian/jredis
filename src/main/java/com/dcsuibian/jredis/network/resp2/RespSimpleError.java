package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

@Getter
public class RespSimpleError extends Resp2Object {
    public static final RespSimpleError WRONG_TYPE = new RespSimpleError("WRONGTYPE Operation against a key holding the wrong kind of value".getBytes(StandardCharsets.UTF_8));
    private final byte[] value;

    public RespSimpleError(byte[] value) {
        this.value = value;
    }
}
