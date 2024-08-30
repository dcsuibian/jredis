package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

public class ListCommands {
    public static void lpushCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void lrangeCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void lremCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void rpushCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
