package com.dcsuibian.jredis.util;

import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.network.resp2.RespSimpleError;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.SharedObjects;

import java.nio.charset.StandardCharsets;

public class NetworkUtil {
    public static void addReply(RedisClient c, RespObject o) {
        c.getChannelHandlerContext().writeAndFlush(o);
    }

    public static void addErrorReply(RedisClient c, String s) {
        addReply(c, new RespSimpleError(s.getBytes(StandardCharsets.UTF_8)));
    }

    public static void addErrorReply(RedisClient c, RespSimpleError error) {
        addReply(c, error);
    }

    public static void addErrorArityReply(RedisClient c) {
        String s = "wrong number of arguments for '" + c.getCommand().getFullName().toString(StandardCharsets.UTF_8) + "' command";
        addReply(c, new RespSimpleError(s.getBytes(StandardCharsets.UTF_8)));
    }

    public static void addBulkStringReply(RedisClient c, Sds s) {
        addReply(c, new RespBulkString(s.getData()));
    }

    public static void addBulkStringReply(RedisClient c, byte[] s) {
        addReply(c, new RespBulkString(s));
    }

    public static void addLongReply(RedisClient c, long l) {
        if (0 == l) {
            addReply(c, SharedObjects.ZERO);
        } else if (1 == l) {
            addReply(c, SharedObjects.ONE);
        } else {
            addReply(c, new RespInteger(l));
        }
    }

    public static void addErrorExpireTimeReply(RedisClient c) {
        addErrorReply(c, "invalid expire time in '" + c.getCommand().getFullName().toString(StandardCharsets.UTF_8) + "' command");
    }

    public static void addArrayReply(RedisClient c, RespArray array) {
        addReply(c, array);
    }
}
