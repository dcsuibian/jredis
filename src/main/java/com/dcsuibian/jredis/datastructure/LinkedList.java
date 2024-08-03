package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedList<T> {
    @Getter
    @Setter
    public static class Node<T> {
        private Node<T> prev;
        private Node<T> next;
        private T value;
    }

    private Node<T> head;
    private Node<T> tail;
    private long length;
}
