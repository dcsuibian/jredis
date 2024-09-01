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

        public Node(T value) {
            this.value = value;
            prev = next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private long length;

    public LinkedList() {
        head = tail = null;
        length = 0;
    }

    public void addHead(T value) {
        Node<T> node = new Node<>(value);
        if (0 == length) {
            head = tail = node;
            node.prev = node.next = null;
        } else {
            node.prev = null;
            node.next = head;
            head.prev = node;
            head = node;
        }
        length++;
    }

    public void addTail(T value) {
        Node<T> node = new Node<>(value);
        if (0 == length) {
            head = tail = node;
            node.prev = node.next = null;
        } else {
            node.prev = tail;
            node.next = null;
            tail.next = node;
            tail = node;
        }
        length++;
    }
}
