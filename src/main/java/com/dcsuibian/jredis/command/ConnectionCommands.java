package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.IntContainer;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisServer;
import com.dcsuibian.jredis.server.SharedObjects;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

import static com.dcsuibian.jredis.util.GenericUtil.equalsIgnoreCase;
import static com.dcsuibian.jredis.util.NetworkUtil.addErrorReply;
import static com.dcsuibian.jredis.util.NetworkUtil.addReply;
import static com.dcsuibian.jredis.util.ObjectUtil.getIntFromBytesOrReply;

public class ConnectionCommands {
    public static void pingCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        RespSimpleString reply = new RespSimpleString("PONG".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(reply);
    }

    public static void quitCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
        ctx.close();
    }

    /**
     * When the user is omitted it means that we are trying to authenticate against the default user.
     */
    public static void authCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        c.setAuthenticated(true);
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void clientCommand(RedisClient c) {
        if (equalsIgnoreCase(c.getArgs()[1], "setname")) {
            c.setName(new Sds(c.getArgs()[2]));
        }
        addReply(c, SharedObjects.OK);
    }

    private static boolean selectDb(RedisClient c, int id) {
        RedisServer server = RedisServer.get();
        if (id < 0 || id > server.getDatabases().length) {
            return false;
        }
        c.setDatabase(server.getDatabases()[id]);
        return true;
    }

    public static void selectCommand(RedisClient c) {
        IntContainer id = new IntContainer();
        if (!getIntFromBytesOrReply(c, c.getArgs()[1], id, null)) {
            return;
        }
        if (RedisServer.get().isClusterEnabled() && 0 != id.getValue()) {
            addErrorReply(c, "SELECT is not allowed in cluster mode");
            return;
        }
        if (selectDb(c, id.getValue())) {
            addReply(c, SharedObjects.OK);
        } else {
            addErrorReply(c, "DB index is out of range");
        }
    }
}
