package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedList<T> {
    private LinkedListNode<T> head;
    private LinkedListNode<T> tail;
    private long length;
}
