package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

public class SortedSetCommands {
    public static void zrevrangeCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void zremCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void zaddCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
