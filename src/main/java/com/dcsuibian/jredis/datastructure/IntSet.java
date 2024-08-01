package com.dcsuibian.jredis.datastructure;

import com.dcsuibian.jredis.exception.NeverException;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntSet {
    @Getter
    private Encoding encoding;
    @Getter
    private int length;
    private byte[] content;

    public enum Encoding {
        INT16(2), INT32(4), INT64(8);
        private final int size;

        Encoding(int size) {
            this.size = size;
        }
    }

    private static class SearchResult {
        boolean exists;
        int position;
    }

    /**
     * Return the required encoding for the provided value.
     */
    public static Encoding valueEncoding(long v) {
        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
            return Encoding.INT64;
        } else if (v < Short.MIN_VALUE || v > Short.MAX_VALUE) {
            return Encoding.INT32;
        } else {
            return Encoding.INT16;
        }
    }

    public IntSet() {
        encoding = Encoding.INT16;
        length = 0;
        content = new byte[0];
    }

    public boolean add(long value) {
        Encoding valueEncoding = valueEncoding(value);
        if (valueEncoding.size > encoding.size) {
            return upgradeAndAdd(value);
        }
        SearchResult searchResult = search(value);
        if (searchResult.exists) {
            return false;
        }
        resize(length + 1);
        if (searchResult.position < length) {
            moveTail(searchResult.position, searchResult.position + 1);
        }
        set(searchResult.position, value);
        length++;
        return true;
    }

    public boolean remove(long value) {
        boolean success = false;
        Encoding valueEncoding = valueEncoding(value);
        SearchResult searchResult = search(value);
        if (valueEncoding.size <= encoding.size && searchResult.exists) {
            int length = this.length;
            success = true;
            if (searchResult.position < length - 1) {
                moveTail(searchResult.position + 1, searchResult.position);
            }
            resize(length - 1);
            this.length = length - 1;
        }
        return success;
    }

    public int size() {
        return length;
    }

    /**
     * Determine whether a value belongs to this set.
     */
    public boolean contains(long value) {
        Encoding valueEncoding = valueEncoding(value);
        return valueEncoding.size <= encoding.size && search(value).exists;
    }

    /**
     * Return the value at position, given an encoding.
     */
    private long getEncoded(int position, Encoding encoding) {
        ByteBuffer buffer;
        switch (encoding) {
            case INT64:
                buffer = ByteBuffer.wrap(content, position * encoding.size, encoding.size);
                buffer.order(ByteOrder.BIG_ENDIAN);
                return buffer.getLong();
            case INT32:
                buffer = ByteBuffer.wrap(content, position * encoding.size, encoding.size);
                buffer.order(ByteOrder.BIG_ENDIAN);
                return buffer.getInt();
            case INT16:
                buffer = ByteBuffer.wrap(content, position * encoding.size, encoding.size);
                buffer.order(ByteOrder.BIG_ENDIAN);
                return buffer.getShort();
            default:
                throw new NeverException();
        }
    }

    /**
     * Return the value at position, using the configured encoding.
     */
    private long get(int position) {
        return getEncoded(position, encoding);
    }

    /**
     * Set the value at pos, using the configured encoding.
     */
    private void set(int position, long value) {
        ByteBuffer buffer;
        switch (encoding) {
            case INT64:
                buffer = ByteBuffer.wrap(content, position * encoding.size, encoding.size);
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.putLong(value);
                break;
            case INT32:
                buffer = ByteBuffer.wrap(content, position * encoding.size, encoding.size);
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.putInt((int) value);
                break;
            case INT16:
                buffer = ByteBuffer.wrap(content, position * encoding.size, encoding.size);
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.putShort((short) value);
                break;
            default:
                throw new NeverException();
        }
    }

    /**
     * Resize the contents array to the new length.
     */
    private void resize(int newLength) {
        byte[] newContents = new byte[newLength * encoding.size];
        System.arraycopy(content, 0, newContents, 0, Math.min(content.length, newContents.length));
        content = newContents;
    }

    private void moveTail(int from, int to) {
        int bytesToMove = (length - from) * encoding.size;
        System.arraycopy(content, from * encoding.size, content, to * encoding.size, bytesToMove);
    }


    private boolean upgradeAndAdd(long value) {
        Encoding currentEncoding = this.encoding;
        Encoding newEncoding = valueEncoding(value);
        int length = this.length;
        int prepend = value < 0 ? 1 : 0;
        this.encoding = newEncoding;
        resize(this.length + 1);
        while (length-- > 0) {
            set(length + prepend, getEncoded(length, currentEncoding));
        }
        if (prepend > 0) {
            set(0, value);
        } else {
            set(this.length, value);
        }
        this.length++;
        return true;
    }

    private SearchResult search(long value) {

        SearchResult result = new SearchResult();
        if (0 == this.length) {
            result.exists = false;
            result.position = 0;
            return result;
        } else {
            if (value > get(this.length - 1)) {
                result.exists = false;
                result.position = this.length;
                return result;
            } else if (value < get(0)) {
                result.exists = false;
                result.position = 0;
                return result;
            }
        }
        int min = 0, max = this.length - 1;
        while (max >= min) {
            int mid = (min + max) / 2;
            long current = get(mid);
            if (current == value) {
                result.exists = true;
                result.position = mid;
                return result;
            } else if (current < value) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }
        result.exists = false;
        result.position = min;
        return result;
    }
}