package com.dcsuibian.jredis.asynchronous;

import java.io.File;

@FunctionalInterface
public interface FileProcessor {
    void process(EventLoop eventLoop, File file, Object clientData, int mask);
}
