package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

public class GenericCommands {
    public static void scanCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void ttlCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void typeCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void keysCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void expireCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void existsCommand(RedisClient client) {
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        boolean exists = client.getDatabase().getDictionary().get(new Sds(client.getArgs()[1])) != null;
        if (exists) {
            ctx.writeAndFlush(new RespInteger(1));
        } else {
            ctx.writeAndFlush(new RespInteger(0));
        }
    }

    public static void delCommand(RedisClient client) {
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        client.getDatabase().getDictionary().remove(new Sds(client.getArgs()[1]));
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
