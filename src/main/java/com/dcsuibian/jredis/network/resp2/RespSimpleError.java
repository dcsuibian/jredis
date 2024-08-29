package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

@Getter
public class RespSimpleError extends Resp2Object {
    private final byte[] value;

    public RespSimpleError(byte[] value) {
        this.value = value;
    }
}
