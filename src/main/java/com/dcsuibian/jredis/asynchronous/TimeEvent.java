package com.dcsuibian.jredis.asynchronous;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TimeEvent {
    private long id;
    private Instant when;
    private TimeProcessor timeProcessor;
    private EventFinalizerProcessor finalizerProcessor;
    private Object clientData;
    private TimeEvent prev;
    private TimeEvent next;
    private int referenceCount;
}
