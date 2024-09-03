package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.datastructure.QuickList;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import io.netty.channel.ChannelHandlerContext;

import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyReadOrReply;
import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyWrite;
import static com.dcsuibian.jredis.util.ObjectUtil.getLongFromBytesOrReply;
import static com.dcsuibian.jredis.util.ObjectUtil.isWrongType;

public class ListCommands {
    public static final int LIST_HEAD = 0;
    public static final int LIST_TAIL = 1;

    private static void pushGenericCommand(RedisClient c, int where, boolean pushIfKeyExists) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        RedisObject listObj = lookupKeyWrite(c.getDatabase(), c.getArgs()[1]);
        if (isWrongType(c, listObj, RedisObject.Type.LIST)) {
            return;
        }
        if (null == listObj) {
            if (pushIfKeyExists) {
                ctx.writeAndFlush(RespInteger.ZERO);
                return;
            }
            listObj = new RedisObject(new QuickList());
            c.getDatabase().getDictionary().put(new Sds(c.getArgs()[1]), listObj);
        }
        for (int i = 2; i < c.getArgs().length; i++) {
            listTypePush(listObj, c.getArgs()[i], where);
        }

        QuickList ql = (QuickList) listObj.getValue();
        ctx.writeAndFlush(new RespInteger(ql.size()));
    }

    private static void listTypePush(RedisObject subject, byte[] value, int where) {
        if (RedisObject.Encoding.QUICK_LIST == subject.getEncoding()) {
            QuickList ql = (QuickList) subject.getValue();
            if (LIST_HEAD == where) {
                ql.addHead(value);
            } else {
                ql.addTail(value);
            }
        } else {
            throw new RuntimeException("Unknown list encoding");
        }
    }

    public static void lpushCommand(RedisClient c) {
        pushGenericCommand(c, LIST_HEAD, false);
    }

    public static void rpushCommand(RedisClient c) {
        pushGenericCommand(c, LIST_TAIL, false);
    }

    public static void lpushxCommand(RedisClient c) {
        pushGenericCommand(c, LIST_HEAD, true);
    }

    public static void rpushxCommand(RedisClient c) {
        pushGenericCommand(c, LIST_TAIL, true);
    }

    public static void lrangeCommand(RedisClient c) {
        RedisObject o;
        LongContainer start = new LongContainer(), end = new LongContainer();
        if ((!getLongFromBytesOrReply(c, c.getArgs()[2], start, null)) ||
                (!getLongFromBytesOrReply(c, c.getArgs()[3], end, null))) {
            return;
        }
        if (null == (o = lookupKeyReadOrReply(c, c.getArgs()[1], RespArray.EMPTY)) || isWrongType(c, o, RedisObject.Type.LIST)) {
            return;
        }
        // TODO implement
    }

    public static void lremCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void llenCommand(RedisClient c) {
        RedisObject o = lookupKeyReadOrReply(c, c.getArgs()[1], RespInteger.ZERO);
        if (null == o || isWrongType(c, o, RedisObject.Type.LIST)) {
            return;
        }
        QuickList ql = (QuickList) o.getValue();
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(new RespInteger(ql.size()));
    }
}
