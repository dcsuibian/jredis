package com.dcsuibian.jredis.datastructure;

public class ListPack {
    //region
    private static final int HEADER_SIZE = 6;
    private static final short HEADER_NUM_ELEMENTS_UNKNOWN = (short) 0xFFFF;
    private static final int MAX_INT_ENCODING_LENGTH = 9;
    private static final int MAX_BACK_LENGTH_SIZE = 5;
    private static final int ENCODING_INT = 0;
    private static final int ENCODING_STRING = 1;
    private static final int ENCODING_7BIT_UINT = 0;
    private static final int ENCODING_7BIT_UINT_MASK = 0x80;

    private static boolean is7BitUint(byte b) {
        return (b & ENCODING_7BIT_UINT_MASK) == ENCODING_7BIT_UINT;
    }

    private static final int ENCODING_7BIT_UINT_ENTRY_SIZE = 2;

    private static final int ENCODING_6BIT_STRING = 0x80;
    private static final int ENCODING_6BIT_STRING_MASK = 0xC0;

    private static boolean is6BitString(byte b) {
        return (b & ENCODING_6BIT_STRING_MASK) == ENCODING_6BIT_STRING;
    }

    private static final int ENCODING_13BIT_INT = 0xC0;
    private static final int ENCODING_13BIT_INT_MASK = 0xE0;

    private static boolean is13BitInt(byte b) {
        return (b & ENCODING_13BIT_INT_MASK) == ENCODING_13BIT_INT;
    }

    private static final int ENCODING_13BIT_INT_ENTRY_SIZE = 3;

    private static final int ENCODING_12BIT_STRING = 0xE0;
    private static final int ENCODING_12BIT_STRING_MASK = 0xF0;

    private static boolean is12BitString(byte b) {
        return (b & ENCODING_12BIT_STRING_MASK) == ENCODING_12BIT_STRING;
    }

    private static final int ENCODING_16BIT_INT = 0xF1;
    private static final int ENCODING_16BIT_INT_MASK = 0xFF;

    private static boolean is16BitInt(byte b) {
        return (b & ENCODING_16BIT_INT_MASK) == ENCODING_16BIT_INT;
    }

    private static final int ENCODING_16BIT_INT_ENTRY_SIZE = 4;

    private static final int ENCODING_24BIT_INT = 0xF2;
    private static final int ENCODING_24BIT_INT_MASK = 0xFF;

    private static boolean is24BitInt(byte b) {
        return (b & ENCODING_24BIT_INT_MASK) == ENCODING_24BIT_INT;
    }

    private static final int ENCODING_24BIT_INT_ENTRY_SIZE = 5;

    private static final int ENCODING_32BIT_INT = 0xF3;
    private static final int ENCODING_32BIT_INT_MASK = 0xFF;

    private static boolean is32BitInt(byte b) {
        return (b & ENCODING_32BIT_INT_MASK) == ENCODING_32BIT_INT;
    }

    private static final int ENCODING_32BIT_INT_ENTRY_SIZE = 6;

    private static final int ENCODING_64BIT_INT = 0xF4;
    private static final int ENCODING_64BIT_INT_MASK = 0xFF;

    private static boolean is64BitInt(byte b) {
        return (b & ENCODING_64BIT_INT_MASK) == ENCODING_64BIT_INT;
    }

    private static final int ENCODING_64BIT_INT_ENTRY_SIZE = 10;

    private static final int ENCODING_32BIT_STRING = 0xF0;
    private static final int ENCODING_32BIT_STRING_MASK = 0xFF;

    private static boolean is32BitString(byte b) {
        return (b & ENCODING_32BIT_STRING_MASK) == ENCODING_32BIT_STRING;
    }

    private static final int ENCODING_32BIT_STRING_ENTRY_SIZE = 5;
    private static final byte EOF = (byte) 0xFF;

    private static int ENCODING_6BIT_STRING_LENGTH(BytePointer p) {
        return p.get(0) & 0x3F;
    }

    private static int ENCODING_12BIT_STRING_LENGTH(BytePointer p) {
        return ((p.get(0) & 0xF) << 8) | p.get(1);
    }

    private static int ENCODING_32BIT_STRING_LENGTH(BytePointer p) {
        return ((p.get(1)) | (p.get(2) << 8) | (p.get(3) << 16) | (p.get(4) << 24));
    }

    private static final int BEFORE = 0;
    private static final int AFTER = 1;
    private static final int REPLACE = 2;
    //endregion
    private byte[] content;

    public ListPack(int capacity) {
        this.content = new byte[Math.max(capacity, HEADER_SIZE + 1)];
        setTotalBytes(HEADER_SIZE + 1);
        setNumElements(0);
        content[HEADER_SIZE] = EOF;
    }

    /**
     * Shrink the memory to fit.
     */
    private void shrinkToFit() {
        int size = getTotalBytes();
        if (size < content.length) {
            byte[] newContent = new byte[size];
            System.arraycopy(content, 0, newContent, 0, size);
            content = newContent;
        }
    }

    private int encodeBackLength(byte[] buf, int l) {
        if (l <= 127) {
            if (buf != null) buf[0] = (byte) l;
            return 1;
        } else if (l < 16383) {
            if (buf != null) {
                buf[0] = (byte) (l >> 7);
                buf[1] = (byte) ((l & 127) | 128);
            }
            return 2;
        } else if (l < 2097151) {
            if (buf != null) {
                buf[0] = (byte) (l >> 14);
                buf[1] = (byte) (((l >> 7) & 127) | 128);
                buf[2] = (byte) ((l & 127) | 128);
            }
            return 3;
        } else if (l < 268435455) {
            if (buf != null) {
                buf[0] = (byte) (l >> 21);
                buf[1] = (byte) (((l >> 14) & 127) | 128);
                buf[2] = (byte) (((l >> 7) & 127) | 128);
                buf[3] = (byte) ((l & 127) | 128);
            }
            return 4;
        } else {
            if (buf != null) {
                buf[0] = (byte) (l >> 28);
                buf[1] = (byte) (((l >> 21) & 127) | 128);
                buf[2] = (byte) (((l >> 14) & 127) | 128);
                buf[3] = (byte) (((l >> 7) & 127) | 128);
                buf[4] = (byte) ((l & 127) | 128);
            }
            return 5;
        }
    }

    private int decodeBackLength(BytePointer p) {
        int val = 0;
        int shift = 0;
        do {
            val |= (p.get(0) & 127) << shift;
            if ((p.get(0) & 128) == 0) break;
            shift += 7;
            p = new BytePointer(p.getData(), p.getOffset() - 1);
            if (shift > 28) return Integer.MAX_VALUE;
        } while (true);
        return val;
    }

    private int currentEncodedSizeUnsafe(BytePointer p) {
        byte b = p.get(0);
        if (is7BitUint(b)) return 1;
        if (is6BitString(b)) return 1 + ENCODING_6BIT_STRING_LENGTH(p);
        if (is13BitInt(b)) return 2;
        if (is16BitInt(b)) return 3;
        if (is24BitInt(b)) return 4;
        if (is32BitInt(b)) return 5;
        if (is64BitInt(b)) return 9;
        if (is12BitString(b)) return 2 + ENCODING_12BIT_STRING_LENGTH(p);
        if (is32BitString(b)) return 5 + ENCODING_32BIT_STRING_LENGTH(p);
        if (b == EOF) return 1;
        return 0;
    }

    private BytePointer skip(BytePointer p) {
        int entryLength = currentEncodedSizeUnsafe(p);
        entryLength += encodeBackLength(null, entryLength);
        return new BytePointer(p.getData(), p.getOffset() + entryLength);
    }

    private BytePointer next(BytePointer p) {
        p = skip(p);
        if (p.get(0) == EOF) {
            return null;
        }
        return p;
    }

    private BytePointer prev(BytePointer p) {
        if (HEADER_SIZE == p.getOffset()) {
            return null;
        }
        p = new BytePointer(p.getData(), p.getOffset() - 1);
        int prevLength = decodeBackLength(p);
        prevLength += encodeBackLength(null, prevLength);
        return new BytePointer(p.getData(), p.getOffset() - prevLength + 1);
    }

    private BytePointer first() {
        BytePointer p = new BytePointer(content, HEADER_SIZE);
        if (p.get(0) == EOF) {
            return null;
        }
        return p;
    }

    private BytePointer last() {
        BytePointer p = new BytePointer(content, getTotalBytes() - 1);
        return prev(p);
    }

    private int getTotalBytes() {
        return ((content)[0]) | ((content)[1] << 8) | ((content)[2] << 16) | ((content)[3] << 24);
    }

    private void setTotalBytes(int totalBytes) {
        content[0] = (byte) (totalBytes & 0xFF);
        content[1] = (byte) ((totalBytes >> 8) & 0xFF);
        content[2] = (byte) ((totalBytes >> 16) & 0xFF);
        content[3] = (byte) ((totalBytes >> 24) & 0xFF);
    }

    private int getNumElements() {
        return ((content)[4]) | ((content)[5] << 8);
    }

    private void setNumElements(int numElements) {
        content[4] = (byte) (numElements & 0xFF);
        content[5] = (byte) ((numElements >> 8) & 0xFF);
    }

    private BytePointer getWithSize(BytePointer p, LongContainer count, BytePointer intBuf, IntContainer entrySize) {
        long value;
        long unsignedValue, unsignedNegativeStart, unsignedNegativeEnd;
        byte b = p.get(0);
        if (is7BitUint(b)) {
            unsignedNegativeStart = 0xFFFF_FFFF_FFFF_FFFFL;
            unsignedNegativeEnd = 0;
            unsignedValue = b & 0x7F;
            if (entrySize != null) {
                entrySize.setValue(ENCODING_7BIT_UINT_ENTRY_SIZE);
            }
        } else if (is6BitString(b)) {
            count.setValue(ENCODING_6BIT_STRING_LENGTH(p));
            if (entrySize != null) {
                entrySize.setValue(1 + (int) count.getValue() + encodeBackLength(null, (int) count.getValue() + 1));
            }
            return new BytePointer(p.getData(), p.getOffset() + 1);
        } else if (is13BitInt(b)) {
            unsignedValue = ((b & 0x1F) << 8) | p.get(1);
            unsignedNegativeStart = 1 << 12;
            unsignedNegativeEnd = 8191;
            if (entrySize != null) {
                entrySize.setValue(ENCODING_13BIT_INT_ENTRY_SIZE);
            }
        } else if (is16BitInt(b)) {
            unsignedValue = p.get(1) | (p.get(2) << 8);
            unsignedNegativeStart = 1 << 15;
            unsignedNegativeEnd = 0xFFFF;
            if (entrySize != null) {
                entrySize.setValue(ENCODING_16BIT_INT_ENTRY_SIZE);
            }
        } else if (is24BitInt(b)) {
            unsignedValue = p.get(1) | (p.get(2) << 8) | (p.get(3) << 16);
            unsignedNegativeStart = 1 << 23;
            unsignedNegativeEnd = 0xFFFFFF;
            if (entrySize != null) {
                entrySize.setValue(ENCODING_24BIT_INT_ENTRY_SIZE);
            }
        } else if (is32BitInt(b)) {
            unsignedValue = p.get(1) | (p.get(2) << 8) | (p.get(3) << 16) | (p.get(4) << 24);
            unsignedNegativeStart = 1L << 31;
            unsignedNegativeEnd = 0xFFFF_FFFFL;
            if (entrySize != null) {
                entrySize.setValue(ENCODING_32BIT_INT_ENTRY_SIZE);
            }
        } else if (is64BitInt(b)) {
            unsignedValue = p.get(1) | (p.get(2) << 8) | (p.get(3) << 16) | (p.get(4) << 24) | ((long) p.get(5) << 32) | ((long) p.get(6) << 40) | ((long) p.get(7) << 48) | ((long) p.get(8) << 56);
            unsignedNegativeStart = 1L << 63;
            unsignedNegativeEnd = 0xFFFF_FFFF_FFFF_FFFFL;
            if (entrySize != null) {
                entrySize.setValue(ENCODING_64BIT_INT_ENTRY_SIZE);
            }
        } else if (is12BitString(b)) {
            count.setValue(ENCODING_12BIT_STRING_LENGTH(p));
            if (entrySize != null) {
                entrySize.setValue(2 + (int) count.getValue() + encodeBackLength(null, (int) count.getValue() + 2));
            }
            return new BytePointer(p.getData(), p.getOffset() + 2);
        } else if (is32BitString(b)) {
            count.setValue(ENCODING_32BIT_STRING_LENGTH(p));
            if (entrySize != null) {
                entrySize.setValue(5 + (int) count.getValue() + encodeBackLength(null, (int) count.getValue() + 5));
            }
            return new BytePointer(p.getData(), p.getOffset() + 5);
        } else {
            unsignedValue = 12345678900000000L + p.get(0);
            unsignedNegativeStart = 0xFFFF_FFFF_FFFF_FFFFL;
            unsignedNegativeEnd = 0;
        }
        if (Long.compareUnsigned(unsignedValue, unsignedNegativeStart) >= 0) {
            unsignedValue = unsignedNegativeEnd - unsignedValue;
            value = unsignedValue;
            value = -value - 1;
        } else {
            value = unsignedValue;
        }
        if (intBuf != null) {
            String s = String.valueOf(value);
            System.arraycopy(s.getBytes(), 0, intBuf.getData(), intBuf.getOffset(), s.length());
            count.setValue(s.length());
            return intBuf;
        } else {
            count.setValue(value);
            return null;
        }
    }

    private BytePointer get(BytePointer p, LongContainer count, BytePointer intBuf) {
        return getWithSize(p, count, intBuf, null);
    }

    private BytePointer getValue(BytePointer p, IntContainer stringLength, LongContainer longValue) {
        LongContainer elementLength = new LongContainer();
        BytePointer result = get(p, elementLength, null);
        if (null != result) {
            stringLength.setValue((int) elementLength.getValue());
        } else {
            longValue.setValue(elementLength.getValue());
        }
        return result;
    }

    private void encodeIntegerGetType(long v, BytePointer intEncoding, LongContainer encodingLength) {
        if (v >= 0 && v <= 127) {
            intEncoding.set(0, (byte) v);
            encodingLength.setValue(1);
        } else if (v >= -4096 && v <= 4095) {
            if (v < 0) v = ((1 << 13) + v);
            intEncoding.set(0, (byte) ((v >> 8) | ENCODING_13BIT_INT));
            intEncoding.set(1, (byte) (v & 0xff));
            encodingLength.setValue(2);
        } else if (v >= -32768 && v <= 32767) {
            if (v < 0) v = ((1 << 16) + v);
            intEncoding.set(0, (byte) ENCODING_16BIT_INT);
            intEncoding.set(1, (byte) (v & 0xff));
            intEncoding.set(2, (byte) (v >> 8));
            encodingLength.setValue(3);
        } else if (v >= -8388608 && v <= 8388607) {
            if (v < 0) v = ((1 << 24) + v);
            intEncoding.set(0, (byte) ENCODING_24BIT_INT);
            intEncoding.set(1, (byte) (v & 0xff));
            intEncoding.set(2, (byte) ((v >> 8) & 0xff));
            intEncoding.set(3, (byte) (v >> 16));
            encodingLength.setValue(4);
        } else if (v >= -2147483648 && v <= 2147483647) {
            if (v < 0) v = ((1L << 32) + v);
            intEncoding.set(0, (byte) ENCODING_32BIT_INT);
            intEncoding.set(1, (byte) (v & 0xff));
            intEncoding.set(2, (byte) ((v >> 8) & 0xff));
            intEncoding.set(3, (byte) ((v >> 16) & 0xff));
            intEncoding.set(4, (byte) (v >> 24));
            encodingLength.setValue(5);
        } else {
            intEncoding.set(0, (byte) ENCODING_64BIT_INT);
            intEncoding.set(1, (byte) (v & 0xff));
            intEncoding.set(2, (byte) ((v >> 8) & 0xff));
            intEncoding.set(3, (byte) ((v >> 16) & 0xff));
            intEncoding.set(4, (byte) ((v >> 24) & 0xff));
            intEncoding.set(5, (byte) ((v >> 32) & 0xff));
            intEncoding.set(6, (byte) ((v >> 40) & 0xff));
            intEncoding.set(7, (byte) ((v >> 48) & 0xff));
            intEncoding.set(8, (byte) (v >> 56));
            encodingLength.setValue(9);
        }
    }

    private int encodeGetType(byte[] element, BytePointer intEncoding, LongContainer encodingLength) {
        try {
            long v = Long.parseLong(new String(element));
            encodeIntegerGetType(v, intEncoding, encodingLength);
            return ENCODING_INT;
        } catch (NumberFormatException e) {
            if (element.length < 64) {
                encodingLength.setValue(1 + element.length);
            } else if (element.length < 4096) {
                encodingLength.setValue(2 + element.length);
            } else {
                encodingLength.setValue(5 + element.length);
            }
            return ENCODING_STRING;
        }
    }

    private void encodeString(BytePointer p, byte[] s) {
        int len = s.length;
        if (len < 64) {
            p.set(0, (byte) (ENCODING_6BIT_STRING | len));
            System.arraycopy(s, 0, p.getData(), p.getOffset() + 1, len);
        } else if (len < 4096) {
            p.set(0, (byte) ((len >> 8) | ENCODING_12BIT_STRING));
            p.set(1, (byte) (len & 0xFF));
            System.arraycopy(s, 0, p.getData(), p.getOffset() + 2, len);
        } else {
            p.set(0, (byte) ENCODING_32BIT_STRING);
            p.set(1, (byte) (len & 0xFF));
            p.set(2, (byte) ((len >> 8) & 0xFF));
            p.set(3, (byte) ((len >> 16) & 0xFF));
            p.set(4, (byte) ((len >> 24) & 0xFF));
            System.arraycopy(s, 0, p.getData(), p.getOffset() + 5, len);
        }
    }


    private void insert(byte[] elementString, byte[] elementInt, BytePointer p, int where, BytePointerContainer newPointer) {
        byte[] intEncoding = new byte[MAX_INT_ENCODING_LENGTH];
        byte[] backLength = new byte[MAX_BACK_LENGTH_SIZE];
        LongContainer encodingLength = new LongContainer();
        boolean delete = (elementString == null && elementInt == null);
        if (delete) {
            where = REPLACE;
        }
        if (where == AFTER) {
            p = skip(p);
            where = BEFORE;
        }
        int pOffset = p.getOffset();
        int encodingType;
        if (null != elementString) {
            encodingType = encodeGetType(elementString, new BytePointer(intEncoding), encodingLength);
            if (encodingType == ENCODING_INT) {
                elementInt = intEncoding;
            }
        } else if (null != elementInt) {
            encodingType = ENCODING_INT;
            encodingLength.setValue(elementInt.length);
        } else {
            encodingType = -1;
            encodingLength.setValue(0);
        }
        int backLengthSize = (!delete) ? encodeBackLength(backLength, (int) encodingLength.getValue()) : 0;
        int oldListPackBytes = getTotalBytes();
        int replacedLength = 0;
        if (where == REPLACE) {
            replacedLength = currentEncodedSizeUnsafe(p);
            replacedLength += encodeBackLength(null, replacedLength);
        }
        int newListPackBytes = oldListPackBytes + (int) encodingLength.getValue() + backLengthSize - replacedLength;
        BytePointer dst = new BytePointer(content, pOffset);
        if (newListPackBytes > oldListPackBytes && newListPackBytes > content.length) {
            byte[] newContent = new byte[newListPackBytes];
            System.arraycopy(content, 0, newContent, 0, oldListPackBytes);
            content = newContent;
            dst = new BytePointer(content, pOffset);
        }
        if (where == BEFORE) {
            System.arraycopy(content, pOffset, content, pOffset + (int) encodingLength.getValue() + backLengthSize, oldListPackBytes - pOffset);
        } else {
            int lenDiff = (int) encodingLength.getValue() + backLengthSize - replacedLength;
            System.arraycopy(content, pOffset + replacedLength + lenDiff, content, pOffset + replacedLength, oldListPackBytes - pOffset - replacedLength);
        }
        if (newListPackBytes < oldListPackBytes) {
            byte[] newContent = new byte[newListPackBytes];
            System.arraycopy(content, 0, newContent, 0, newListPackBytes);
            content = newContent;
            dst = new BytePointer(content, pOffset);
        }
        if (newPointer != null) {
            newPointer.setValue(dst);
            if (delete && dst.get(0) == EOF) {
                newPointer.setValue(null);
            }
        }
        if (!delete) {
            if (encodingType == ENCODING_INT) {
                System.arraycopy(elementInt, 0, dst.getData(), dst.getOffset(), (int) encodingLength.getValue());
            } else {
                encodeString(dst, elementString);
            }
            dst = new BytePointer(content, dst.getOffset() + (int) encodingLength.getValue());
            System.arraycopy(backLength, 0, dst.getData(), dst.getOffset(), backLengthSize);
            dst = new BytePointer(content, dst.getOffset() + backLengthSize);
        }
        if (where != REPLACE || delete) {
            int numElements = getNumElements();
            if (numElements != HEADER_NUM_ELEMENTS_UNKNOWN) {
                if (!delete) {
                    setNumElements(numElements + 1);
                } else {
                    setNumElements(numElements - 1);
                }
            }
        }
        setTotalBytes(newListPackBytes);
    }

    private void insertString(byte[] element, BytePointer p, int where, BytePointerContainer newPointer) {
        insert(element, null, p, where, newPointer);
    }

    private void insertInt(long element, BytePointer p, int where, BytePointerContainer newPointer) {
        byte[] intEncoding = new byte[MAX_INT_ENCODING_LENGTH];
        LongContainer encodingLength = new LongContainer();
        encodeIntegerGetType(element, new BytePointer(intEncoding), encodingLength);
        insert(null, intEncoding, p, where, newPointer);
    }

    public int getLength() {
        int numElements = getNumElements();
        if (numElements != HEADER_NUM_ELEMENTS_UNKNOWN) {
            return numElements;
        }
        int count = 0;
        BytePointer p = first();
        while (p != null) {
            count++;
            p = next(p);
        }
        if (count < HEADER_NUM_ELEMENTS_UNKNOWN) {
            setNumElements(count);
        }
        return count;
    }

    public void prepend(byte[] s) {
        BytePointer p = first();
        if (p == null) {
            append(s);
        } else {
            insert(s, null, p, BEFORE, null);
        }
    }

    public void prepend(long v) {
        BytePointer p = first();
        if (p == null) {
            append(v);
        } else {
            insertInt(v, p, BEFORE, null);
        }
    }

    public void append(byte[] s) {
        int totalBytes = getTotalBytes();
        BytePointer eof = new BytePointer(content, totalBytes - 1);
        insert(s, null, eof, BEFORE, null);
    }

    public void append(long v) {
        int totalBytes = getTotalBytes();
        BytePointer eof = new BytePointer(content, totalBytes - 1);
        insertInt(v, eof, BEFORE, null);
    }

    public void replace(BytePointerContainer pointerContainer, byte[] s) {
        insert(s, null, pointerContainer.getValue(), REPLACE, pointerContainer);
    }

    public void replace(BytePointerContainer pointerContainer, long v) {
        insertInt(v, pointerContainer.getValue(), REPLACE, pointerContainer);
    }

    public void delete(BytePointer p, BytePointerContainer newPointer) {
        insert(null, null, p, REPLACE, newPointer);
    }

    private BytePointer getPointer(int index) {
        BytePointer p = first();
        for (int i = 0; i < index; i++) {
            p = next(p);
            if (p == null) {
                return null;
            }
        }
        return p;
    }

    public Long getLong(int index) {
        LongContainer longContainer = new LongContainer();
        BytePointer p = getPointer(index);
        if (null != p) {
            getValue(p, null, longContainer);
            return longContainer.getValue();
        }
        return null;
    }

    public byte[] getStringValue(int index) {
        IntContainer intContainer = new IntContainer();
        BytePointer p = getValue(getPointer(index), intContainer, null);
        if (null != p) {
            byte[] result = new byte[intContainer.getValue()];
            System.arraycopy(p.getData(), p.getOffset(), result, 0, intContainer.getValue());
            return result;
        }
        return null;
    }

    public String getString(int index) {
        byte[] result = getStringValue(index);
        if (null != result) {
            return new String(result);
        }
        return null;
    }
}
