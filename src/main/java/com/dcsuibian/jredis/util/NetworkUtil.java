package com.dcsuibian.jredis.util;

import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.server.RedisClient;

public class NetworkUtil {
    public static void addReply(RedisClient c, RespObject o) {
        c.getChannelHandlerContext().writeAndFlush(o);
    }
}
