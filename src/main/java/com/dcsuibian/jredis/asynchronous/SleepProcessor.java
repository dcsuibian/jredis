package com.dcsuibian.jredis.asynchronous;

@FunctionalInterface
public interface SleepProcessor {
    void process(EventLoop eventLoop);
}
