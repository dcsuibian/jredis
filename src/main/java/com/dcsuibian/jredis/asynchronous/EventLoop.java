package com.dcsuibian.jredis.asynchronous;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class EventLoop implements Runnable {
    private long timeEventNextId;
    private FileEvent[] events;
    private FiredEvent[] fired;
    private TimeEvent timeEventHead;
    private volatile boolean stopped;
    private int size;
    private int flags;

    public EventLoop(int size) {
        events = new FileEvent[size];
        fired = new FiredEvent[size];
        this.size = size;
        timeEventHead = null;
        timeEventNextId = 0;
        stopped = false;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            processEvents();
        }
    }

    private void processEvents() {
        System.out.println("Processing events");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
