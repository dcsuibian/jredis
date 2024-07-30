package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickListNode<T> {
    private QuickListNode<T> prev;
    private QuickListNode<T> next;
}
