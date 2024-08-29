package com.dcsuibian.jredis.sentinel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentinelAddress {
    private String hostname;
    private String ip;
    private int port;
}
