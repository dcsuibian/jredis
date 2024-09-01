package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import io.netty.channel.ChannelHandlerContext;

import static com.dcsuibian.jredis.command.Util.isWrongType;
import static com.dcsuibian.jredis.command.Util.lookupKeyWrite;

public class ListCommands {
    public static final int LIST_HEAD = 0;
    public static final int LIST_TAIL = 1;

    private static void pushGenericCommand(RedisClient c, int where, boolean pushIfKeyExists) {
        RedisObject listObj = lookupKeyWrite(c.getDatabase(), c.getArgs()[1]);
        if (isWrongType(c, listObj, RedisObject.Type.LIST)) {
            return;
        }
        // TODO implement
    }

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
