package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

@Getter
public class RespInteger extends Resp2Object {
    public static final RespInteger ZERO = new RespInteger(0);
    public static final RespInteger ONE = new RespInteger(1);
    private final long value;

    public RespInteger(long value) {
        this.value = value;
    }
}
