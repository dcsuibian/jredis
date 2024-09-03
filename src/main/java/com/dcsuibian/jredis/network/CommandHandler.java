package com.dcsuibian.jredis.network;

import com.dcsuibian.jredis.command.Commands;
import com.dcsuibian.jredis.command.RedisCommand;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.exception.RespCodecException;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespSimpleError;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CommandHandler extends ChannelInboundHandlerAdapter {
    private final RedisServer redisServer;

    public CommandHandler(RedisServer redisServer) {
        this.redisServer = redisServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        RedisClient client = new RedisClient();
        client.setDatabase(redisServer.getDatabases()[0]);
        client.setServer(redisServer);
        RedisServer.THREAD_LOCAL.set(redisServer);
        ctx.channel().attr(AttributeKey.valueOf("client")).set(client);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RespObject respObject = (RespObject) msg;
        Sds command;
        byte[][] args;
        if (respObject instanceof RespArray) {
            RespArray respArray = (RespArray) respObject;
            if (null == respArray.getValue()) {
                throw new RespCodecException("Invalid RESP request");
            }
            int length = respArray.getValue().length;
            if (length == 0) {
                throw new RespCodecException("Invalid RESP request");
            }
            args = new byte[length][];
            for (int i = 0; i < length; i++) {
                RespObject childRespObject = respArray.getValue()[i];
                if (childRespObject instanceof RespSimpleString) {
                    byte[] value = ((RespSimpleString) childRespObject).getValue();
                    args[i] = value;
                } else if (childRespObject instanceof RespBulkString) {
                    byte[] value = ((RespBulkString) childRespObject).getValue();
                    args[i] = value;
                } else {
                    throw new RespCodecException("Invalid RESP request");
                }
            }
            command = new Sds(args[0]);
        } else if (respObject instanceof RespSimpleString) {
            byte[] commandValue = ((RespSimpleString) respObject).getValue();
            command = new Sds(commandValue);
            args = new byte[][]{commandValue};
        } else if (respObject instanceof RespBulkString) {
            byte[] commandValue = ((RespBulkString) respObject).getValue();
            command = new Sds(commandValue);
            args = new byte[][]{commandValue};
        } else {
            throw new RespCodecException("Invalid RESP request");
        }
        String s = command.toString(StandardCharsets.UTF_8);
        s = s.toLowerCase();
        command = new Sds(s, StandardCharsets.UTF_8);
        RedisClient client = (RedisClient) ctx.channel().attr(AttributeKey.valueOf("client")).get();
        client.setArgs(args);
        client.setChannelHandlerContext(ctx);
        for (RedisCommand redisCommand : Commands.REDIS_COMMANDS) {
            if (redisCommand.getDeclaredName().equals(command)) {
                redisCommand.getProcessor().process(client);
                return;
            }
        }
        log.error("unknown command: {}", command.toString(StandardCharsets.UTF_8));
        RespSimpleError error = new RespSimpleError("unknown command".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(error);
    }
}
