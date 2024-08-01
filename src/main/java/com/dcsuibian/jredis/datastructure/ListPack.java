package com.dcsuibian.jredis.datastructure;

public class ListPack {
    private static final int HEADER_SIZE = 6;
    private static final byte EOF = (byte) 0xFF;
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
    public void shrinkToFit() {
        int size = getTotalBytes();
        if (size < content.length) {
            byte[] newContent = new byte[size];
            System.arraycopy(content, 0, newContent, 0, size);
            content = newContent;
        }
    }

    public int getTotalBytes() {
        return ((content)[0]) | ((content)[1] << 8) | ((content)[2] << 16) | ((content)[3] << 24);
    }

    public void setTotalBytes(int totalBytes) {
        content[0] = (byte) (totalBytes & 0xFF);
        content[1] = (byte) ((totalBytes >> 8) & 0xFF);
        content[2] = (byte) ((totalBytes >> 16) & 0xFF);
        content[3] = (byte) ((totalBytes >> 24) & 0xFF);
    }

    public void setNumElements(int numElements) {
        content[4] = (byte) (numElements & 0xFF);
        content[5] = (byte) ((numElements >> 8) & 0xFF);
    }
}
