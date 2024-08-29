package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

@Getter
public class RespInteger extends Resp2Object {
    private final long value;

    public RespInteger(long value) {
        this.value = value;
    }
}
