package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedListNode<T> {
    private LinkedListNode<T> prev;
    private LinkedListNode<T> next;
    private T value;
}
