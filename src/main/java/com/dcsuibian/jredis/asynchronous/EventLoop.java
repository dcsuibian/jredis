package com.dcsuibian.jredis.asynchronous;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

@Slf4j
@Getter
@Setter
public class EventLoop implements Runnable {
    //region
    private static final int SOCKET_CHANNEL_EVENTS = 1;
    private static final int TIME_EVENTS = 1 << 1;
    private static final int ALL_EVENTS = SOCKET_CHANNEL_EVENTS | TIME_EVENTS;
    private static final int DONT_WAIT = 1 << 2;
    private static final int CALL_BEFORE_SLEEP = 1 << 3;
    private static final int CALL_AFTER_SLEEP = 1 << 4;
    //endregion

    private final Selector selector;
    private long timeEventNextId;
    private TimeEvent timeEventHead;
    private volatile boolean stopped;
    private int flags;

    public EventLoop() {
        try {
            selector = Selector.open();
            timeEventHead = null;
            timeEventNextId = 0;
            stopped = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            processEvents(ALL_EVENTS | CALL_BEFORE_SLEEP | CALL_AFTER_SLEEP);
        }
    }

    public void registerChannel(SelectableChannel channel, int ops, ChannelProcessor processor) {
        try {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(selector, ops);
            key.attach(processor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processEvents(int flags) {
        /* Nothing to do? return ASAP */
        if (0 == (flags & TIME_EVENTS) && 0 == (flags & SOCKET_CHANNEL_EVENTS)) {
            return;
        }
        try {
            int readyChannels = selector.selectNow();
            if (0 != readyChannels) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (SelectionKey key : selectedKeys) {
                    if (!key.isValid()) {
                        continue;
                    }
                    ChannelProcessor processor = (ChannelProcessor) key.attachment();
                    processor.process(this, key);
                }
            }
        } catch (IOException e) {
            log.debug(e.toString());
        }
    }
}
