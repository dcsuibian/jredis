package com.dcsuibian.jredis.asynchronous;

import java.nio.channels.SelectionKey;

@FunctionalInterface
public interface ChannelProcessor {
    void process(EventLoop eventLoop, SelectionKey key);
}
