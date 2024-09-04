package com.dcsuibian.jredis.datastructure;

public class QuickList {
    private static final class Node {
        private Node prev;
        private Node next;
        private ListPack entry;
        private int count;
    }

    private Node head;
    private Node tail;
    private long count;
    private long length; /* number of nodes */

    public QuickList() {
        this.head = this.tail = null;
        this.length = 0;
        this.count = 0;
    }

    public int size() {
        return (int) this.count;
    }
}
