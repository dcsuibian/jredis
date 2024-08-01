package com.dcsuibian.jredis.asynchronous;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileEvent {
    private int mask;
    private FileProcessor readProcessor;
    private FileProcessor writeProcessor;
    private Object clientData;
}
