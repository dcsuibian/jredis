package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

public class QuickList<T> {
    @Getter
    @Setter
    public static class Node<T> {
        private Node<T> prev;
        private Node<T> next;
    }

    private Node<T> head;
    private Node<T> tail;
    private long count;
    private long length;
}
