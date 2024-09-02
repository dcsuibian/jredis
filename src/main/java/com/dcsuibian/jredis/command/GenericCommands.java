package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GenericCommands {
    public static void scanGenericCommand(RedisClient c, RedisObject o, long cursor) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void scanCommand(RedisClient c) {
        // TODO Return the queried keys, not all keys
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        List<RespObject> list = new ArrayList<>();
        list.add(new RespBulkString("0".getBytes(StandardCharsets.UTF_8)));
        List<RespObject> keyList = new ArrayList<>();
        for (Sds key : c.getDatabase().getDictionary().keySet()) {
            keyList.add(new RespBulkString(key.getData()));
        }
        list.add(new RespArray(keyList.toArray(new RespObject[0])));
        RespArray respArray = new RespArray(list.toArray(new RespObject[0]));
        ctx.writeAndFlush(respArray);
    }

    public static void ttlCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void typeCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        String type;
        RedisObject o = c.getDatabase().getDictionary().get(new Sds(c.getArgs()[1]));
        if (null == o) {
            type = "none";
        } else {
            switch (o.getType()) {
                case STRING:
                    type = "string";
                    break;
                case LIST:
                    type = "list";
                    break;
                case SET:
                    type = "set";
                    break;
                case Z_SET:
                    type = "zset";
                    break;
                case HASH:
                    type = "hash";
                    break;
                case STREAM:
                    type = "stream";
                    break;
                default:
                    type = "unknown";
            }
        }
        ctx.writeAndFlush(new RespBulkString(type.getBytes(StandardCharsets.UTF_8)));
    }

    public static void keysCommand(RedisClient c) {
        // TODO Return the queried keys, not all keys
        Set<Sds> keySet = c.getDatabase().getDictionary().keySet();
        List<RespObject> list = new ArrayList<>();
        for (Sds key : keySet) {
            list.add(new RespBulkString(key.getData()));
        }
        RespArray respArray = new RespArray(list.toArray(new RespObject[0]));
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(respArray);
    }

    public static void expireCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void existsCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        boolean exists = c.getDatabase().getDictionary().get(new Sds(c.getArgs()[1])) != null;
        if (exists) {
            ctx.writeAndFlush(RespInteger.ONE);
        } else {
            ctx.writeAndFlush(RespInteger.ZERO);
        }
    }

    public static void delCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        c.getDatabase().getDictionary().remove(new Sds(c.getArgs()[1]));
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
