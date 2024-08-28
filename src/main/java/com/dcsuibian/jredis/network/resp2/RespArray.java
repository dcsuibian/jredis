package com.dcsuibian.jredis.network.resp2;

import com.dcsuibian.jredis.network.RespObject;
import lombok.Getter;

@Getter
public class RespArray extends Resp2Object {
    private final RespObject[] value;

    public RespArray(RespObject[] value) {
        this.value = value;
    }
}
