package com.dcsuibian.jredis.datastructure;

import lombok.Getter;

@Getter
public class BytePointer {
    private final byte[] data;
    private final int offset;

    public BytePointer(byte[] data) {
        this(data, 0);
    }

    public BytePointer(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public byte get(int index) {
        return data[offset + index];
    }

    public void set(int index, byte value) {
        data[offset + index] = value;
    }
}