package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisServer;
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

    public static void authCommand(RedisClient client) {
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        // TODO verify password
        client.setAuthenticated(true);
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void clientCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void selectCommand(RedisClient client) {
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        int index = Integer.parseInt(new String(client.getArgs()[1], StandardCharsets.UTF_8));
        RedisServer server = client.getServer();
        client.setDatabase(server.getDatabases()[index]);
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
