package com.dcsuibian.jredis.exception;

import io.netty.handler.codec.CodecException;

public class RespCodecException extends CodecException {
    public RespCodecException(String message) {
        super(message);
    }

    public RespCodecException(Throwable cause) {
        super(cause);
    }
}
