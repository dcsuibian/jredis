package com.dcsuibian.jredis.exception;

public class NeverException extends RuntimeException {
    public NeverException() {
        super("This exception should never be thrown.");
    }
}
