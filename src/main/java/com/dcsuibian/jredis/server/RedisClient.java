package com.dcsuibian.jredis.server;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RedisClient {
    private long id;
    private long flags;
    private RedisDatabase database;
    private RedisObject name;
    private byte[][] args;
    private User user;
    private Instant createTime;
    private long duration;
    private boolean authenticated;
    private RedisServer server;
    private ChannelHandlerContext channelHandlerContext;
}
