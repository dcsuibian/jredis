package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

@Getter
public class RespSimpleString extends Resp2Object {
    private final byte[] value;

    public RespSimpleString(byte[] value) {
        this.value = value;
    }
}
