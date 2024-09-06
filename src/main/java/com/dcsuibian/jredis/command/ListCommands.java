package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.SharedObjects;
import io.netty.channel.ChannelHandlerContext;

import java.util.LinkedList;

import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyReadOrReply;
import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyWrite;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.*;

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
            listObj = createQuickListObject();
            c.getDatabase().getDictionary().put(new Sds(c.getArgs()[1]), listObj);
        }
        for (int i = 2; i < c.getArgs().length; i++) {
            listTypePush(listObj, c.getArgs()[i], where);
        }

        LinkedList<Sds> list = (LinkedList<Sds>) listObj.getValue();
        addLongReply(c, list.size());
    }

    private static void listTypePush(RedisObject subject, byte[] value, int where) {
        if (RedisObject.Encoding.QUICK_LIST == subject.getEncoding()) {
            LinkedList<Sds> list = (LinkedList<Sds>) subject.getValue();
            if (LIST_HEAD == where) {
                list.addFirst(new Sds(value));
            } else if (LIST_TAIL == where) {
                list.addLast(new Sds(value));
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
        LongContainer begin = new LongContainer(), end = new LongContainer();
        if ((!getLongFromBytesOrReply(c, c.getArgs()[2], begin, null)) ||
                (!getLongFromBytesOrReply(c, c.getArgs()[3], end, null))) {
            return;
        }
        if (null == (o = lookupKeyReadOrReply(c, c.getArgs()[1], RespArray.EMPTY)) || isWrongType(c, o, RedisObject.Type.LIST)) {
            return;
        }
        addListRangeReply(c, o, begin.getValue(), end.getValue());
    }

    private static void addListRangeReply(RedisClient c, RedisObject o, long begin, long end) {
        long listLength = listTypeLength(o);
        if (begin < 0) {
            begin = listLength + begin;
        }
        if (end < 0) {
            end = listLength + end;
        }
        if (begin < 0) {
            begin = 0;
        }
        if (begin > end || begin >= listLength) {
            addReply(c, SharedObjects.EMPTY_ARRAY);
            return;
        }
        if (end >= listLength) {
            end = listLength - 1;
        }
        long rangeLength = (end - begin) + 1;
        LinkedList<Sds> list = (LinkedList<Sds>) o.getValue();
        RespObject[] array = new RespObject[(int) rangeLength];
        for (int i = 0; i < rangeLength; i++) {
            array[i] = new RespBulkString(list.get((int) (begin + i)).getData());
        }
        addArrayReply(c, new RespArray(array));
    }

    private static int listTypeLength(RedisObject subject) {
        if (RedisObject.Encoding.QUICK_LIST == subject.getEncoding()) {
            LinkedList<Sds> list = (LinkedList<Sds>) subject.getValue();
            return list.size();
        } else {
            throw new RuntimeException("Unknown list encoding");
        }
    }

    public static void lremCommand(RedisClient c) {
        LongContainer toRemove = new LongContainer();
        if (!getLongFromBytesOrReply(c, c.getArgs()[2], toRemove, null)) {
            return;
        }
        RedisObject subject = lookupKeyReadOrReply(c, c.getArgs()[1], SharedObjects.ZERO);
        if (null == subject || isWrongType(c, subject, RedisObject.Type.LIST)) {
            return;
        }
        LinkedList<Sds> list = (LinkedList<Sds>) subject.getValue();
        int removed = 0;
        if (toRemove.getValue() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(new Sds(c.getArgs()[3]))) {
                    list.remove(i);
                    removed++;
                    i--;
                    if (toRemove.getValue() == removed) {
                        break;
                    }
                }
            }
        } else {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i).equals(new Sds(c.getArgs()[3]))) {
                    list.remove(i);
                    removed++;
                    if (toRemove.getValue() == removed) {
                        break;
                    }
                }
            }
        }
        addLongReply(c, removed);
    }

    public static void llenCommand(RedisClient c) {
        RedisObject o = lookupKeyReadOrReply(c, c.getArgs()[1], RespInteger.ZERO);
        if (null == o || isWrongType(c, o, RedisObject.Type.LIST)) {
            return;
        }
        LinkedList<Sds> list = (LinkedList<Sds>) o.getValue();
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(new RespInteger(list.size()));
    }
}
