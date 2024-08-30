package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

public class SetCommands {
    public static void sscanCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void sremCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void saddCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
