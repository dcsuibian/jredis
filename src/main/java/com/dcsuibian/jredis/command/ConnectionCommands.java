package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class ConnectionCommands {
    public static void pingCommand(RedisClient client) {
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        RespSimpleString reply = new RespSimpleString("PONG".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(reply);
    }

    public static void quitCommand(RedisClient client) {
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
        ctx.close();
    }
}
