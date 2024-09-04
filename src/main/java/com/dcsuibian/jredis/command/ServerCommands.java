package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerCommands {
    public static void infoCommand(RedisClient c) {
        List<String> lines = new ArrayList<>();
        lines.add("# Server");
        lines.add("redis_version:jredis");
        lines.add("os:" + System.getProperty("os.name"));
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\r\n");
        }
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(new RespBulkString(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static void configCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
