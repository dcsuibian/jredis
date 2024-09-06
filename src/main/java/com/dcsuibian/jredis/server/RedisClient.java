package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.command.RedisCommand;
import com.dcsuibian.jredis.datastructure.Sds;
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
    private Sds name;
    private byte[][] args;
    private User user;
    private Instant createTime;
    private long duration;
    private boolean authenticated;
    private RedisServer server;
    private ChannelHandlerContext channelHandlerContext;
    private RedisCommand command;
    private int respVersion = 2;
}
