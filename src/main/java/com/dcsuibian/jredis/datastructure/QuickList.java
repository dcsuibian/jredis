package com.dcsuibian.jredis.datastructure;

import java.util.LinkedList;

public class QuickList {
    private final LinkedList<byte[]> content;

    public QuickList() {
        content = new LinkedList<>();
    }

    public void addHead(byte[] value) {
        content.addFirst(value);
    }

    public void addTail(byte[] value) {
        content.addLast(value);
    }

    public int size() {
        return content.size();
    }
}
