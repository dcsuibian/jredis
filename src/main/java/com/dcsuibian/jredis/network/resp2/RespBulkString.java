package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

@Getter
public class RespBulkString extends Resp2Object {
    public static final RespBulkString EMPTY = new RespBulkString(new byte[0]);
    private final byte[] value;

    public RespBulkString(byte[] value) {
        this.value = value;
    }
}
