package com.dcsuibian.jredis.network;

import lombok.Getter;

@Getter
public enum RespObjectType {
    SIMPLE_STRING((byte) '+'),
    SIMPLE_ERROR((byte) '-'),
    INTEGER((byte) ':'),
    BULK_STRING((byte) '$'),
    ARRAY((byte) '*'),
    NULL((byte) '_'),
    BOOLEAN((byte) '#'),
    DOUBLE((byte) ','),
    BIG_NUMBER((byte) '('),
    BULK_ERROR((byte) '!'),
    VERBATIM_STRING((byte) '='),
    MAP((byte) '%'),
    SET((byte) '~'),
    PUSH((byte) '>');
    private final byte value;

    RespObjectType(byte value) {
        this.value = value;
    }

    public static RespObjectType valueOf(byte value) {
        for (RespObjectType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown type: " + value);
    }
}