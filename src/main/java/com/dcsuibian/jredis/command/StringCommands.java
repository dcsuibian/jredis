package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.SharedObjects;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyReadOrReply;
import static com.dcsuibian.jredis.util.NetworkUtil.addBulkStringReply;
import static com.dcsuibian.jredis.util.ObjectUtil.isWrongType;

public class StringCommands {
    private static boolean getGenericCommand(RedisClient c) {
        RedisObject o;
        if (null == (o = lookupKeyReadOrReply(c, c.getArgs()[1], SharedObjects.NULL[c.getRespVersion()]))) {
            return true;
        }
        if (isWrongType(c, o, RedisObject.Type.STRING)) {
            return false;
        }
        if (RedisObject.Encoding.SDS == o.getEncoding()) {
            addBulkStringReply(c, (Sds) o.getValue());
        } else if (RedisObject.Encoding.INTEGER == o.getEncoding()) {
            addBulkStringReply(c, ((Long) o.getValue()).toString().getBytes(StandardCharsets.UTF_8));
        } else {
            throw new RuntimeException("Unknown string encoding");
        }
        return true;
    }

    public static void getCommand(RedisClient c) {
        getGenericCommand(c);
    }

    public static void setCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void setnxCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void setexCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void msetCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void mgetCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void incrCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void decrCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
