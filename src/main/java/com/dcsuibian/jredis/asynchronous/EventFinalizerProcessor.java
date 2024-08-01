package com.dcsuibian.jredis.asynchronous;

@FunctionalInterface
public interface EventFinalizerProcessor {
    void process(EventLoop eventLoop, Object clientData);
}
