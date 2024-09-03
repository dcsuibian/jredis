package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

public class SortedSetCommands {
    /* Input flags. */
    private static final int ZADD_IN_NONE = 0;
    private static final int ZADD_IN_INCREMENT = 1; /* Increment the score instead of setting it. */
    private static final int ZADD_IN_NX = (1 << 1); /* Don't touch elements not already existing. */
    private static final int ZADD_IN_XX = (1 << 2); /* Only touch elements already existing. */
    private static final int ZADD_IN_GT = (1 << 3); /* Only update existing when new scores are higher. */
    private static final int ZADD_IN_LT = (1 << 4); /* Only update existing when new scores are lower. */
    /* Output flags. */
    private static final int ZADD_OUT_NOP = 1; /* Operation not performed because of conditionals.*/
    private static final int ZADD_OUT_NAN = (1 << 1); /* Only touch elements already existing. */
    private static final int ZADD_OUT_ADDED = (1 << 2); /* The element was new and was added. */
    private static final int ZADD_OUT_UPDATED = (1 << 3); /* The element already existed, score updated. */

    public static void zrevrangeCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void zremCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    private static void zaddGenericCommand(RedisClient c, int flags) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void zaddCommand(RedisClient c) {
        zaddGenericCommand(c, ZADD_IN_NONE);
    }
}
