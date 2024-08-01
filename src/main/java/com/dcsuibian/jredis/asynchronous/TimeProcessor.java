package com.dcsuibian.jredis.asynchronous;

@FunctionalInterface
public interface TimeProcessor {
    void process(EventLoop eventLoop, long id, Object clientData);
}
