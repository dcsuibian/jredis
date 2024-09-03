package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;

/**
 * Simple Dynamic String
 */
public class Sds implements Comparable<Sds> {

    private static final int SDS_MAX_PREALLOCATE = 1024 * 1024; // 1MB
    private byte[] value;
    @Getter
    @Setter
    private int length;

    public Sds() {
        value = new byte[0];
        length = 0;
    }

    public Sds(byte[] value) {
        this.value = new byte[value.length];
        System.arraycopy(value, 0, this.value, 0, value.length);
        length = value.length;
    }

    public Sds(String value, Charset charset) {
        this.value = value.getBytes(charset);
        length = this.value.length;
    }

    public int getAvailable() {
        return value.length - length;
    }

    public byte[] getData() {
        byte[] data = new byte[length];
        System.arraycopy(value, 0, data, 0, length);
        return data;
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

    public String toString(Charset charset) {
        return new String(value, 0, length, charset);
    }

    @Override
    public int compareTo(Sds o) {
        int minLength = Math.min(length, o.length);
        for (int i = 0; i < minLength; i++) {
            if (value[i] != o.value[i]) {
                return Byte.compare(value[i], o.value[i]);
            }
        }
        return Integer.compare(length, o.length);
    }
}
