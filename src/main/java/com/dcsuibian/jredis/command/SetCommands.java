package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.IntSet;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyWrite;
import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyWriteOrReply;
import static com.dcsuibian.jredis.util.ObjectUtil.isWrongType;

public class SetCommands {
    public static void sscanCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void sremCommand(RedisClient c) {
        RedisObject set;
        if (null == (set = lookupKeyWriteOrReply(c, c.getArgs()[1], RespInteger.ZERO)) || isWrongType(c, set, RedisObject.Type.SET)) {
            return;
        }
        int deleted = 0;
        boolean keyRemoved = false;
        for (int i = 2; i < c.getArgs().length; i++) {
            if (setTypeRemove(set, c.getArgs()[i])) {
                deleted++;
                if (0 == setTypeSize(set)) {
                    Sds key = new Sds(c.getArgs()[1]);
                    c.getDatabase().getDictionary().remove(key);
                    c.getDatabase().getExpires().remove(key);
                    keyRemoved = true;
                    break;
                }
            }
        }
        c.getChannelHandlerContext().writeAndFlush(new RespInteger(deleted));
    }

    private static boolean setTypeRemove(RedisObject setObj, byte[] value) {
        if (RedisObject.Encoding.DICTIONARY == setObj.getEncoding()) {
            Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) setObj.getValue();
            Sds key = new Sds(value);
            if (dict.containsKey(key)) {
                dict.remove(key);
                if (dict.needsResize()) {
                    dict.resize();
                }
                return true;
            } else {
                return false;
            }
        } else if (RedisObject.Encoding.INT_SET == setObj.getEncoding()) {
            IntSet intSet = (IntSet) setObj.getValue();
            try {
                long longValue = Long.parseLong(new String(value, StandardCharsets.UTF_8));
                return intSet.remove(longValue);
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }


    private static int setTypeSize(RedisObject set) {
        if (RedisObject.Encoding.DICTIONARY == set.getEncoding()) {
            return ((Dictionary<Sds, Object>) set.getValue()).size();
        } else if (RedisObject.Encoding.INT_SET == set.getEncoding()) {
            return ((IntSet) set.getValue()).size();
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }

    public static void saddCommand(RedisClient c) {
        RedisObject set = lookupKeyWrite(c.getDatabase(), c.getArgs()[1]);
        if (isWrongType(c, set, RedisObject.Type.SET)) {
            return;
        }
        if (null == set) {
            set = setTypeCreate(c.getArgs()[2]);
            c.getDatabase().getDictionary().put(new Sds(c.getArgs()[1]), set);
        }
        int added = 0;
        for (int i = 2; i < c.getArgs().length; i++) {
            if (setTypeAdd(set, c.getArgs()[i])) {
                added++;
            }
        }
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(new RespInteger(added));
    }

    private static boolean setTypeAdd(RedisObject subject, byte[] value) {
        if (RedisObject.Encoding.DICTIONARY == subject.getEncoding()) {
            Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) subject.getValue();
            if (dict.containsKey(new Sds(value))) {
                return false;
            }
            dict.put(new Sds(value), null);
            return true;
        } else if (RedisObject.Encoding.INT_SET == subject.getEncoding()) {
            IntSet intSet = (IntSet) subject.getValue();
            try {
                long longValue = Long.parseLong(new String(value, StandardCharsets.UTF_8));
                boolean success = intSet.add(longValue);
                if (success) {
                    int maxEntries = Math.min(512, 1 << 30);
                    if (intSet.getLength() > maxEntries) {
                        setTypeConvert(subject, RedisObject.Encoding.DICTIONARY);
                    }
                }
                return success;
            } catch (NumberFormatException e) {
                setTypeConvert(subject, RedisObject.Encoding.DICTIONARY);
                Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) subject.getValue();
                if (dict.containsKey(new Sds(value))) {
                    return false;
                }
                dict.put(new Sds(value), null);
                return true;
            }
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }

    private static void setTypeConvert(RedisObject setObj, RedisObject.Encoding encoding) {
        if (RedisObject.Encoding.DICTIONARY == encoding) {
            IntSet intSet = (IntSet) setObj.getValue();
            Dictionary<Sds, Object> d = new Dictionary<>();
            d.expand(intSet.getLength());
            for (long value : intSet) {
                Sds sds = new Sds(String.valueOf(value), StandardCharsets.UTF_8);
                d.put(sds, null);
            }
        } else {
            throw new RuntimeException("Unsupported set conversion");
        }
    }

    private static RedisObject setTypeCreate(byte[] value) {
        RedisObject result = new RedisObject();
        result.setType(RedisObject.Type.SET);
        try {
            Long.parseLong(new String(value, StandardCharsets.UTF_8));
            result.setEncoding(RedisObject.Encoding.INT_SET);
            result.setValue(new IntSet());
        } catch (NumberFormatException e) {
            result.setEncoding(RedisObject.Encoding.DICTIONARY);
            result.setValue(new Dictionary<Sds, Object>());
        }
        return result;
    }
}
