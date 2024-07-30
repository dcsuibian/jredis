package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple Dynamic String
 */
public class Sds {
    private static final int SDS_MAX_PREALLOCATE = 1024 * 1024; // 1MB
    private byte[] value;
    @Getter
    @Setter
    private int length;

    public Sds() {
        value = new byte[0];
        length = 0;
    }

    public Sds(String value) {
        this.value = value.getBytes();
        length = this.value.length;
    }

    public Sds(byte[] value) {
        this.value = value;
        length = value.length;
    }

    public int getAvailable() {
        return value.length - length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Sds) {
            Sds sds = (Sds) obj;
            if (length != sds.length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (value[i] != sds.value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < length; i++) {
            hash = 31 * hash + value[i];
        }
        return hash;
    }


    @Override
    public String toString() {
        return new String(value, 0, length);
    }
}
