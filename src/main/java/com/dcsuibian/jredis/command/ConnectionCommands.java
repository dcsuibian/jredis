package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisServer;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class ConnectionCommands {
    public static void pingCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        RespSimpleString reply = new RespSimpleString("PONG".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(reply);
    }

    public static void quitCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
        ctx.close();
    }

    public static void authCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        // TODO verify password
        c.setAuthenticated(true);
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void clientCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void selectCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        int index = Integer.parseInt(new String(c.getArgs()[1], StandardCharsets.UTF_8));
        RedisServer server = c.getServer();
        c.setDatabase(server.getDatabases()[index]);
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
