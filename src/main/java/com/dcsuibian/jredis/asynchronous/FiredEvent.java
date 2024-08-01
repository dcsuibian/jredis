package com.dcsuibian.jredis.asynchronous;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class FiredEvent {
    private File file;
    private int mask;
}
