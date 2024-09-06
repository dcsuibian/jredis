package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisServer;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.dcsuibian.jredis.util.GenericUtil.equalsIgnoreCase;
import static com.dcsuibian.jredis.util.NetworkUtil.addArrayReply;

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
        RedisServer server = RedisServer.get();
        if (equalsIgnoreCase(c.getArgs()[1], "get") && equalsIgnoreCase(c.getArgs()[2], "databases")) {
            List<RespBulkString> bulkStrings = new ArrayList<>();
            bulkStrings.add(new RespBulkString("databases".getBytes(StandardCharsets.UTF_8)));
            bulkStrings.add(new RespBulkString(String.valueOf(server.getDatabases().length).getBytes(StandardCharsets.UTF_8)));
            RespBulkString[] array = bulkStrings.toArray(new RespBulkString[bulkStrings.size()]);
            addArrayReply(c, new RespArray(array));
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
